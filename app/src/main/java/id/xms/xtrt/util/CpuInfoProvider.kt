package id.xms.xtrt.util

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.content.Context
import android.os.Build
import android.os.Debug
import android.os.Process
import java.io.File
import java.io.RandomAccessFile

@SuppressLint("StaticFieldLeak")
object CpuInfoProvider {

    private var context: Context? = null

    fun init(appContext: Context) {
        context = appContext
    }

    fun getCpuCoreCount(): Int {
        return try {
            Runtime.getRuntime().availableProcessors()
        } catch (e: Exception) {
            1
        }
    }

    fun getCpuName(): String {
        return try {
            val cpuAbi = Build.SUPPORTED_ABIS?.firstOrNull() ?: Build.CPU_ABI
            val hardware = Build.HARDWARE

            if (hardware.isNotEmpty() && hardware != "unknown") {
                hardware
            } else {
                val cpuInfo = File("/proc/cpuinfo")
                if (cpuInfo.exists() && cpuInfo.canRead()) {
                    val content = cpuInfo.readText()
                    val lines = content.lines()
                    val hardwareLine = lines.firstOrNull { it.startsWith("Hardware") || it.startsWith("model name") }
                    hardwareLine?.substringAfter(":")?.trim() ?: cpuAbi
                } else {
                    cpuAbi
                }
            }
        } catch (e: Exception) {
            Build.HARDWARE.ifEmpty { "Unknown CPU" }
        }
    }

    fun getMaxCpuFrequency(): Long {
        return try {
            val coreCount = getCpuCoreCount()
            var maxFreq = 0L

            for (i in 0 until coreCount) {
                try {
                    val paths = listOf(
                        "/sys/devices/system/cpu/cpu$i/cpufreq/cpuinfo_max_freq",
                        "/sys/devices/system/cpu/cpu$i/cpufreq/scaling_max_freq"
                    )

                    for (path in paths) {
                        val freqFile = File(path)
                        if (freqFile.exists() && freqFile.canRead()) {
                            val freqText = freqFile.readText().trim()
                            val freq = freqText.toLongOrNull() ?: 0L
                            if (freq > maxFreq) maxFreq = freq
                            break
                        }
                    }
                } catch (e: Exception) {
                }
            }

            if (maxFreq > 0) maxFreq / 1000 else 0L
        } catch (e: Exception) {
            0L
        }
    }

    fun getCurrentCpuFrequencies(): List<Long> {
        return try {
            val coreCount = getCpuCoreCount()
            val frequencies = mutableListOf<Long>()

            for (i in 0 until coreCount) {
                try {
                    // Try different possible paths
                    val paths = listOf(
                        "/sys/devices/system/cpu/cpu$i/cpufreq/scaling_cur_freq",
                        "/sys/devices/system/cpu/cpu$i/cpufreq/cpuinfo_cur_freq"
                    )

                    var freq = 0L
                    for (path in paths) {
                        val freqFile = File(path)
                        if (freqFile.exists() && freqFile.canRead()) {
                            val freqText = freqFile.readText().trim()
                            freq = freqText.toLongOrNull() ?: 0L
                            break
                        }
                    }
                    frequencies.add(if (freq > 0) freq / 1000 else 0L) // Convert to MHz
                } catch (e: Exception) {
                    frequencies.add(0L)
                }
            }
            frequencies
        } catch (e: Exception) {
            // Return list of zeros for all cores
            List(getCpuCoreCount()) { 0L }
        }
    }

    fun getCpuUsage(): Float {
        return try {
            context?.let { ctx ->
                // Method 1: Use ActivityManager (works without root)
                val activityManager = ctx.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
                val memInfo = ActivityManager.MemoryInfo()
                activityManager.getMemoryInfo(memInfo)

                // Get our process CPU usage
                val processId = Process.myPid()
                val startCpuTime = Process.getElapsedCpuTime()
                val startTime = System.currentTimeMillis()

                // Small delay to measure CPU usage
                Thread.sleep(100)

                val endCpuTime = Process.getElapsedCpuTime()
                val endTime = System.currentTimeMillis()

                val cpuUsage = if (endTime > startTime) {
                    ((endCpuTime - startCpuTime).toFloat() / (endTime - startTime)) * 100f
                } else {
                    0f
                }

                cpuUsage.coerceIn(0f, 100f)
            } ?: run {
                // Fallback method using /proc/stat (may not work without root)
                tryReadProcStat()
            }
        } catch (e: Exception) {
            // Final fallback - estimate based on process load
            try {
                val debug = Debug.MemoryInfo()
                Debug.getMemoryInfo(debug)
                // Rough estimation based on memory pressure
                val memPressure = debug.getTotalPrivateDirty().toFloat() / (1024 * 1024) // MB
                (memPressure * 0.1f).coerceIn(0f, 100f)
            } catch (e2: Exception) {
                0f
            }
        }
    }

    private fun tryReadProcStat(): Float {
        var reader: RandomAccessFile? = null
        return try {
            val statFile = File("/proc/stat")
            if (!statFile.exists() || !statFile.canRead()) {
                return 0f
            }

            reader = RandomAccessFile("/proc/stat", "r")
            val load = reader.readLine() ?: return 0f

            val toks = load.split("\\s+".toRegex())
            if (toks.size < 8) return 0f

            // Parse CPU times correctly
            val user = toks[1].toLongOrNull() ?: 0L
            val nice = toks[2].toLongOrNull() ?: 0L
            val system = toks[3].toLongOrNull() ?: 0L
            val idle = toks[4].toLongOrNull() ?: 0L
            val iowait = toks[5].toLongOrNull() ?: 0L
            val irq = toks[6].toLongOrNull() ?: 0L
            val softirq = toks[7].toLongOrNull() ?: 0L

            val totalIdle = idle + iowait
            val totalNonIdle = user + nice + system + irq + softirq
            val total = totalIdle + totalNonIdle

            val usage = if (total > 0) {
                ((totalNonIdle.toFloat() / total.toFloat()) * 100f)
            } else {
                0f
            }

            usage.coerceIn(0f, 100f)
        } catch (e: Exception) {
            0f
        } finally {
            try {
                reader?.close()
            } catch (e: Exception) {
                // Ignore close errors
            }
        }
    }

    fun getDeviceInfo(): String {
        return try {
            "${Build.MANUFACTURER} ${Build.MODEL} (${Build.DEVICE})"
        } catch (e: Exception) {
            "Unknown Device"
        }
    }
}
