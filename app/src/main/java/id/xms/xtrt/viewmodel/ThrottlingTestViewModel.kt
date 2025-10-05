package id.xms.xtrt.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.xms.xtrt.benchmark.BenchmarkEngine
import id.xms.xtrt.util.CpuInfoProvider
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers

data class TestState(
    val isRunning: Boolean = false,
    val currentGips: Double = 0.0,
    val maxGips: Double = 0.0,
    val minGips: Double = Double.MAX_VALUE,
    val avgGips: Double = 0.0,
    val elapsedTime: Long = 0,
    val gipsHistory: List<Pair<Long, Double>> = emptyList(),
    val cpuName: String = "",
    val cpuCoreCount: Int = 0,
    val maxCpuFreq: Long = 0,
    val currentCpuFreqs: List<Long> = emptyList(),
    val cpuUsage: Float = 0f,
    val deviceInfo: String = "",
    val stability: Double = 100.0,
    val degradation: Double = 0.0
)

class ThrottlingTestViewModel : ViewModel() {

    private val _testState = MutableStateFlow(TestState())
    val testState: StateFlow<TestState> = _testState.asStateFlow()

    private val benchmarkEngine = BenchmarkEngine()
    private var benchmarkJob: Job? = null
    private var monitorJob: Job? = null

    private val gipsSamples = mutableListOf<Double>()
    private val maxSamples = 3600 // 1 hour of samples

    init {
        loadCpuInfo()
    }

    private fun loadCpuInfo() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val cpuName = CpuInfoProvider.getCpuName()
                val cpuCoreCount = CpuInfoProvider.getCpuCoreCount()
                val maxCpuFreq = CpuInfoProvider.getMaxCpuFrequency()
                val deviceInfo = CpuInfoProvider.getDeviceInfo()

                _testState.update { state ->
                    state.copy(
                        cpuName = cpuName,
                        cpuCoreCount = cpuCoreCount,
                        maxCpuFreq = maxCpuFreq,
                        deviceInfo = deviceInfo
                    )
                }
            } catch (e: Exception) {
                _testState.update { state ->
                    state.copy(
                        cpuName = "Unknown CPU",
                        cpuCoreCount = Runtime.getRuntime().availableProcessors(),
                        maxCpuFreq = 0L,
                        deviceInfo = "Unknown Device"
                    )
                }
            }
        }
    }

    fun startTest() {
        if (_testState.value.isRunning) return

        // Reset state
        gipsSamples.clear()
        _testState.update { state ->
            state.copy(
                isRunning = true,
                currentGips = 0.0,
                maxGips = 0.0,
                minGips = Double.MAX_VALUE,
                avgGips = 0.0,
                elapsedTime = 0,
                gipsHistory = emptyList(),
                stability = 100.0,
                degradation = 0.0
            )
        }

        // Start benchmark
        benchmarkJob = viewModelScope.launch {
            try {
                benchmarkEngine.runBenchmark { result ->
                    updateTestResults(result)
                }
            } catch (e: Exception) {
                _testState.update { state ->
                    state.copy(
                        isRunning = false,
                        currentGips = 0.0
                    )
                }
            }
        }

        // Start CPU monitoring
        monitorJob = viewModelScope.launch(Dispatchers.IO) {
            try {
                while (_testState.value.isRunning) {
                    val monitorStart = System.currentTimeMillis()

                    val currentCpuFreqs = CpuInfoProvider.getCurrentCpuFrequencies()
                    val cpuUsage = CpuInfoProvider.getCpuUsage()

                    _testState.update { state ->
                        state.copy(
                            currentCpuFreqs = currentCpuFreqs,
                            cpuUsage = cpuUsage
                        )
                    }

                    val monitorDuration = System.currentTimeMillis() - monitorStart
                    val remainingTime = 500 - monitorDuration

                    if (remainingTime > 0) {
                        delay(remainingTime)
                    } else {
                        delay(50)
                    }
                }
            } catch (e: Exception) {
                // Continue silently
            }
        }
    }

    fun stopTest() {
        benchmarkJob?.cancel()
        monitorJob?.cancel()
        benchmarkJob = null
        monitorJob = null
        _testState.update { it.copy(isRunning = false) }
    }

    private fun updateTestResults(result: BenchmarkEngine.BenchmarkResult) {
        // Limit memory usage
        if (gipsSamples.size >= maxSamples) {
            gipsSamples.removeAt(0)
        }
        gipsSamples.add(result.gips)

        val currentState = _testState.value
        val newMax = maxOf(currentState.maxGips, result.gips)
        val newMin = if (currentState.minGips == Double.MAX_VALUE) {
            result.gips
        } else {
            minOf(currentState.minGips, result.gips)
        }

        val newAvg = if (gipsSamples.isNotEmpty()) gipsSamples.average() else 0.0

        // Current-based stability (real-time responsive)
        val currentGips = result.gips
        val stability = if (newMax > 0) {
            (currentGips / newMax) * 100.0
        } else {
            100.0
        }

        // Peak degradation (historical worst case)
        val degradation = if (newMax > 0 && newMin != Double.MAX_VALUE) {
            ((newMax - newMin) / newMax) * 100.0
        } else {
            0.0
        }

        // Keep graph history
        val maxGraphPoints = 7200
        val newHistory = (currentState.gipsHistory + (result.elapsedSeconds to result.gips))
            .takeLast(maxGraphPoints)

        _testState.update { state ->
            state.copy(
                currentGips = result.gips,
                maxGips = newMax,
                minGips = newMin,
                avgGips = newAvg,
                elapsedTime = result.elapsedSeconds,
                gipsHistory = newHistory,
                stability = stability,
                degradation = degradation
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopTest()
        gipsSamples.clear()
    }
}
