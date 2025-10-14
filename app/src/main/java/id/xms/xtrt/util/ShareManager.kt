package id.xms.xtrt.util

import android.content.Context
import android.content.Intent
import android.graphics.*
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import id.xms.xtrt.viewmodel.TestState
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.max
import kotlin.math.min

object ShareManager {

    // ===== 1. SHARE AS IMAGE (PNG) with Graph =====
    fun shareAsImage(context: Context, testState: TestState) {
        try {
            val bitmap = generateResultBitmap(testState)
            val imageFile = saveBitmapToCache(context, bitmap, "throttling_result.png")

            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                imageFile
            )

            val shareIntent = Intent().apply {
                action = Intent.ACTION_SEND
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_TEXT, generateShareText(testState))
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            context.startActivity(Intent.createChooser(shareIntent, "Share Result"))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // ===== 2. SHARE AS PDF with Graph =====
    fun shareAsPdf(context: Context, testState: TestState) {
        try {
            val pdfDocument = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
            val page = pdfDocument.startPage(pageInfo)

            drawPdfContent(page.canvas, testState)
            pdfDocument.finishPage(page)

            val pdfFile = File(context.cacheDir, "throttling_report_${System.currentTimeMillis()}.pdf")
            FileOutputStream(pdfFile).use { output ->
                pdfDocument.writeTo(output)
            }
            pdfDocument.close()

            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                pdfFile
            )

            val shareIntent = Intent().apply {
                action = Intent.ACTION_SEND
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            context.startActivity(Intent.createChooser(shareIntent, "Share PDF Report"))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // ===== 3. SHARE AS JSON (unchanged) =====
    fun shareAsJson(context: Context, testState: TestState) {
        try {
            val json = JSONObject().apply {
                put("test_date", SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date()))
                put("device_info", testState.deviceInfo)
                put("cpu_name", testState.cpuName)
                put("cpu_cores", testState.cpuCoreCount)
                put("max_frequency_mhz", testState.maxCpuFreq)
                put("test_duration_seconds", testState.elapsedTime)
                put("max_gips", testState.maxGips)
                put("min_gips", if (testState.minGips == Double.MAX_VALUE) 0.0 else testState.minGips)
                put("avg_gips", testState.avgGips)
                put("current_gips", testState.currentGips)
                put("stability_percent", testState.stability)
                put("degradation_percent", testState.degradation)

                val historyArray = JSONArray()
                testState.gipsHistory.forEach { (time, gips) ->
                    historyArray.put(JSONObject().apply {
                        put("time_seconds", time)
                        put("gips", gips)
                    })
                }
                put("performance_history", historyArray)
            }

            val jsonFile = File(context.cacheDir, "throttling_data_${System.currentTimeMillis()}.json")
            jsonFile.writeText(json.toString(2))

            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                jsonFile
            )

            val shareIntent = Intent().apply {
                action = Intent.ACTION_SEND
                type = "application/json"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            context.startActivity(Intent.createChooser(shareIntent, "Share Raw Data"))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // ===== HELPER: Generate Bitmap with Graph =====
    private fun generateResultBitmap(testState: TestState): Bitmap {
        val width = 1080
        val height = 2400 // Increased for graph
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Background
        canvas.drawColor(Color.parseColor("#1B1B1B"))

        val paint = Paint().apply {
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
        }

        // Title
        paint.color = Color.WHITE
        paint.textSize = 72f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("X Throttling Test", width / 2f, 120f, paint)

        // Device info
        paint.textSize = 42f
        paint.typeface = Typeface.DEFAULT
        canvas.drawText(testState.deviceInfo, width / 2f, 200f, paint)
        paint.textSize = 36f
        paint.color = Color.parseColor("#AAAAAA")
        canvas.drawText(testState.cpuName, width / 2f, 250f, paint)

        // Stability section
        paint.textSize = 48f
        paint.color = Color.parseColor("#CCCCCC")
        canvas.drawText("Throttling Stability", width / 2f, 340f, paint)

        val stabilityColor = when {
            testState.stability >= 95 -> Color.parseColor("#4CAF50")
            testState.stability >= 85 -> Color.parseColor("#8BC34A")
            testState.stability >= 70 -> Color.parseColor("#FF9800")
            else -> Color.parseColor("#F44336")
        }
        paint.color = stabilityColor
        paint.textSize = 160f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("${String.format("%.1f", testState.stability)}%", width / 2f, 480f, paint)

        // Status
        paint.textSize = 44f
        paint.color = Color.WHITE
        paint.typeface = Typeface.DEFAULT
        val status = when {
            testState.stability >= 95 -> "Excellent - No throttling"
            testState.stability >= 85 -> "Good - Minor throttling"
            testState.stability >= 70 -> "Fair - Moderate throttling"
            else -> "Poor - Significant throttling"
        }
        canvas.drawText(status, width / 2f, 560f, paint)

        // ✅ DRAW PERFORMANCE GRAPH
        drawGraph(canvas, testState, 100f, 640f, width - 200f, 600f)

        // Performance metrics below graph
        val startY = 1300f
        val lineHeight = 80f
        paint.textSize = 40f
        paint.textAlign = Paint.Align.LEFT
        paint.color = Color.WHITE

        canvas.drawText("Max GIPS:", 120f, startY, paint)
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText("${String.format("%.2f", testState.maxGips)}", width - 120f, startY, paint)

        paint.textAlign = Paint.Align.LEFT
        canvas.drawText("Min GIPS:", 120f, startY + lineHeight, paint)
        paint.textAlign = Paint.Align.RIGHT
        val minGips = if (testState.minGips == Double.MAX_VALUE) 0.0 else testState.minGips
        canvas.drawText("${String.format("%.2f", minGips)}", width - 120f, startY + lineHeight, paint)

        paint.textAlign = Paint.Align.LEFT
        canvas.drawText("Avg GIPS:", 120f, startY + lineHeight * 2, paint)
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText("${String.format("%.2f", testState.avgGips)}", width - 120f, startY + lineHeight * 2, paint)

        paint.textAlign = Paint.Align.LEFT
        canvas.drawText("Duration:", 120f, startY + lineHeight * 3, paint)
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText(formatDuration(testState.elapsedTime), width - 120f, startY + lineHeight * 3, paint)

        // Footer
        paint.textAlign = Paint.Align.CENTER
        paint.textSize = 32f
        paint.color = Color.parseColor("#888888")
        canvas.drawText("Generated by XThrottling Test", width / 2f, height - 80f, paint)
        val dateStr = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())
        canvas.drawText(dateStr, width / 2f, height - 40f, paint)

        return bitmap
    }

    // ===== HELPER: Draw Graph on Canvas =====
    private fun drawGraph(canvas: Canvas, testState: TestState, x: Float, y: Float, width: Float, height: Float) {
        if (testState.gipsHistory.isEmpty()) return

        val paint = Paint().apply { isAntiAlias = true }

        // Background
        paint.color = Color.parseColor("#2A2A2A")
        canvas.drawRect(x, y, x + width, y + height, paint)

        // Grid lines
        paint.color = Color.parseColor("#404040")
        paint.strokeWidth = 2f
        for (i in 0..5) {
            val gridY = y + (height / 5f) * i
            canvas.drawLine(x, gridY, x + width, gridY, paint)
        }

        // Get data range
        val maxGips = testState.gipsHistory.maxOfOrNull { it.second } ?: 1.0
        val minGips = testState.gipsHistory.minOfOrNull { it.second } ?: 0.0
        val range = maxGips - minGips
        val displayMin = (minGips - range * 0.1).coerceAtLeast(0.0)
        val displayMax = maxGips + range * 0.1
        val displayRange = displayMax - displayMin

        // Draw performance line
        val path = Path()
        val points = testState.gipsHistory

        points.forEachIndexed { index, (_, gips) ->
            val px = x + (width / max(1, points.size - 1)) * index
            val py = y + height - ((gips - displayMin) / displayRange * height).toFloat()

            if (index == 0) path.moveTo(px, py)
            else path.lineTo(px, py)
        }

        // Line color based on stability
        val lineColor = when {
            testState.stability >= 95 -> Color.parseColor("#4CAF50")
            testState.stability >= 85 -> Color.parseColor("#8BC34A")
            testState.stability >= 70 -> Color.parseColor("#FF9800")
            else -> Color.parseColor("#F44336")
        }

        paint.style = Paint.Style.STROKE
        paint.color = lineColor
        paint.strokeWidth = 5f
        canvas.drawPath(path, paint)

        // Fill area under line
        val fillPath = Path(path)
        fillPath.lineTo(x + width, y + height)
        fillPath.lineTo(x, y + height)
        fillPath.close()

        paint.style = Paint.Style.FILL
        paint.shader = LinearGradient(
            0f, y, 0f, y + height,
            intArrayOf(lineColor, Color.TRANSPARENT),
            floatArrayOf(0f, 1f),
            Shader.TileMode.CLAMP
        )
        paint.alpha = 80
        canvas.drawPath(fillPath, paint)
        paint.shader = null

        // Y-axis labels
        paint.style = Paint.Style.FILL
        paint.color = Color.WHITE
        paint.textSize = 28f
        paint.textAlign = Paint.Align.RIGHT
        for (i in 0..5) {
            val value = displayMax - (displayRange / 5.0) * i
            val labelY = y + (height / 5f) * i + 10f
            canvas.drawText(String.format("%.0f", value), x - 15f, labelY, paint)
        }

        // Title
        paint.textAlign = Paint.Align.LEFT
        paint.textSize = 36f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("Performance Graph", x, y - 15f, paint)

        paint.textSize = 28f
        paint.typeface = Typeface.DEFAULT
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText("${testState.gipsHistory.size} samples", x + width, y - 15f, paint)
    }

    // ===== HELPER: Draw PDF Content with Graph =====
    private fun drawPdfContent(canvas: Canvas, testState: TestState) {
        val paint = Paint().apply {
            isAntiAlias = true
            color = Color.BLACK
        }

        // Title
        paint.textSize = 24f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("X Throttling Test Report", 40f, 60f, paint)

        // Device info
        paint.textSize = 14f
        paint.typeface = Typeface.DEFAULT
        canvas.drawText("Device: ${testState.deviceInfo}", 40f, 90f, paint)
        canvas.drawText("CPU: ${testState.cpuName}", 40f, 110f, paint)
        canvas.drawText("Cores: ${testState.cpuCoreCount} @ ${testState.maxCpuFreq} MHz", 40f, 130f, paint)

        // Stability
        paint.textSize = 16f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("Throttling Stability: ${String.format("%.1f%%", testState.stability)}", 40f, 165f, paint)

        // ✅ Draw mini graph
        drawGraph(canvas, testState, 40f, 190f, 515f, 280f)

        // Metrics
        var y = 500f
        paint.textSize = 14f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("Performance Metrics", 40f, y, paint)

        paint.typeface = Typeface.DEFAULT
        y += 25f
        canvas.drawText("Max GIPS: ${String.format("%.2f", testState.maxGips)}", 40f, y, paint)
        y += 20f
        val minGips = if (testState.minGips == Double.MAX_VALUE) 0.0 else testState.minGips
        canvas.drawText("Min GIPS: ${String.format("%.2f", minGips)}", 40f, y, paint)
        y += 20f
        canvas.drawText("Avg GIPS: ${String.format("%.2f", testState.avgGips)}", 40f, y, paint)
        y += 20f
        canvas.drawText("Degradation: ${String.format("%.1f%%", testState.degradation)}", 40f, y, paint)
        y += 20f
        canvas.drawText("Duration: ${formatDuration(testState.elapsedTime)}", 40f, y, paint)

        // Footer
        paint.textSize = 10f
        canvas.drawText("Generated: ${SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())}", 40f, 810f, paint)
    }

    private fun saveBitmapToCache(context: Context, bitmap: Bitmap, filename: String): File {
        val file = File(context.cacheDir, filename)
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
        return file
    }

    private fun generateShareText(testState: TestState): String {
        return """
            📊 CPU Throttling Test Results
            
            Device: ${testState.deviceInfo}
            Stability: ${String.format("%.1f%%", testState.stability)}
            Max GIPS: ${String.format("%.2f", testState.maxGips)}
            
            #ThrottlingTest #PerformanceTest
        """.trimIndent()
    }

    private fun formatDuration(seconds: Long): String {
        val h = seconds / 3600
        val m = (seconds % 3600) / 60
        val s = seconds % 60
        return if (h > 0) String.format("%dh %02dm %02ds", h, m, s)
        else String.format("%dm %02ds", m, s)
    }
}
