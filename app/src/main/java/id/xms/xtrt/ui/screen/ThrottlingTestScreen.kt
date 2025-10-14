package id.xms.xtrt.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import id.xms.xtrt.ui.components.*
import id.xms.xtrt.util.ShareManager
import id.xms.xtrt.viewmodel.ThrottlingTestViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThrottlingTestScreen(
    viewModel: ThrottlingTestViewModel = viewModel()
) {
    val testState by viewModel.testState.collectAsState()
    val context = LocalContext.current
    var showShareSheet by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "XThrottling Test",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background.copy(alpha = 0f),
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                // STATE: Initial/Idle/No results yet
                !testState.isRunning && testState.gipsHistory.isEmpty() -> {
                    EmptyStateView(
                        deviceInfo = testState.deviceInfo,
                        cpuName = testState.cpuName,
                        coreCount = testState.cpuCoreCount,
                        maxFreq = testState.maxCpuFreq,
                        onStartTest = { viewModel.startTest() }
                    )
                }
                // STATE: Test is running (active)
                testState.isRunning -> {
                    RunningTestView(
                        testState = testState,
                        onStopTest = { viewModel.stopTest() }
                    )
                }
                // STATE: Test completed
                !testState.isRunning && testState.gipsHistory.isNotEmpty() -> {
                    CompletedTestView(
                        testState = testState,
                        onRunAgain = { viewModel.startTest() },
                        onShare = { showShareSheet = true }
                    )
                    if (showShareSheet) {
                        ShareBottomSheet(
                            onShareImage = {
                                ShareManager.shareAsImage(context, testState)
                                showShareSheet = false
                            },
                            onSharePdf = {
                                ShareManager.shareAsPdf(context, testState)
                                showShareSheet = false
                            },
                            onShareJson = {
                                ShareManager.shareAsJson(context, testState)
                                showShareSheet = false
                            },
                            onDismiss = { showShareSheet = false }
                        )
                    }
                }
            }
        }
    }
}
