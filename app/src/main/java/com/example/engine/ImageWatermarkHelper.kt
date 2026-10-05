package com.example.engine

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ImageWatermarkHelper {

    fun applyWatermarkAndSave(
        context: Context,
        originalFile: File,
        projectName: String,
        workCategory: String,
        locationName: String,
        photoNumber: Int = 1
    ): File {
        val bitmap = BitmapFactory.decodeFile(originalFile.absolutePath) ?: return originalFile

        // Mutable copy for drawing
        val mutableBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(mutableBitmap)

        val width = mutableBitmap.width.toFloat()
        val height = mutableBitmap.height.toFloat()

        // Formatting date & time
        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        val dateStr = dateFormat.format(Date())

        val watermarkText = "LAPOOR  |  $projectName  |  $locationName  |  $workCategory  |  $dateStr"

        // Scale text size proportional to image resolution (e.g. 1.8% of height, min 18px)
        val textSize = (height * 0.022f).coerceIn(24f, 48f)
        val padding = textSize * 0.7f

        val paintText = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            this.textSize = textSize
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            setShadowLayer(4f, 2f, 2f, Color.argb(180, 0, 0, 0))
        }

        val textWidth = paintText.measureText(watermarkText)
        val textHeight = textSize

        // Position: Bottom left with comfortable padding
        val margin = height * 0.03f
        val bgRect = RectF(
            margin,
            height - margin - textHeight - (padding * 1.6f),
            (margin + textWidth + (padding * 2.2f)).coerceAtMost(width - margin),
            height - margin
        )

        // Draw translucent dark backdrop pill
        val paintBg = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(160, 24, 38, 28) // Subtle sage-tinted dark pill
            style = Paint.Style.FILL
        }
        val cornerRadius = bgRect.height() * 0.3f
        canvas.drawRoundRect(bgRect, cornerRadius, cornerRadius, paintBg)

        // Draw small pastel green indicator dot
        val dotRadius = textHeight * 0.25f
        val paintDot = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(120, 185, 135)
            style = Paint.Style.FILL
        }
        val dotX = bgRect.left + padding + dotRadius
        val dotY = bgRect.top + (bgRect.height() / 2f)
        canvas.drawCircle(dotX, dotY, dotRadius, paintDot)

        // Draw text
        val textX = dotX + dotRadius + (padding * 0.8f)
        val textY = bgRect.top + (bgRect.height() / 2f) + (textHeight / 3f)
        canvas.drawText(watermarkText, textX, textY, paintText)

        // Save watermarked image to local app storage
        val outputDir = File(context.filesDir, "project_photos").apply { mkdirs() }
        val outputFile = File(outputDir, "LAPOOR_${System.currentTimeMillis()}.jpg")

        FileOutputStream(outputFile).use { out ->
            mutableBitmap.compress(Bitmap.CompressFormat.JPEG, 92, out)
        }

        // Clean up
        mutableBitmap.recycle()
        bitmap.recycle()

        return outputFile
    }
}
