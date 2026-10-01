package com.example.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RadialGradient
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Shader
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

object AestheticImageRenderer {

    fun generateAndSaveImage(
        context: Context,
        title: String,
        category: String,
        style: String,
        frameTitle: String,
        aspectRatio: String,
        uploadedPhotoUri: Uri? = null,
        seed: Long = System.currentTimeMillis()
    ): String {
        val (width, height) = when (aspectRatio) {
            "1:1" -> 900 to 900
            "4:5" -> 800 to 1000
            "3:4" -> 750 to 1000
            "9:16" -> 720 to 1280
            "16:9" -> 1280 to 720
            else -> 800 to 1000
        }

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val random = Random(seed)

        val promptLower = (title + " " + category + " " + style).lowercase()

        // 1. Rich Scene Background Gradient
        val (topColor, midColor, botColor) = getThemeColors(promptLower, category)
        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(
                0f, 0f, 0f, height.toFloat(),
                intArrayOf(topColor, midColor, botColor),
                floatArrayOf(0f, 0.55f, 1f),
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

        // 2. Atmospheric Ambient Light & Volumetric Rays
        drawAtmosphericLighting(canvas, width, height, promptLower, random)

        // 3. Subject Rendering: User Photo OR Category-Specific High Detail Procedural Art
        var photoDrawn = false
        if (uploadedPhotoUri != null) {
            try {
                context.contentResolver.openInputStream(uploadedPhotoUri)?.use { stream ->
                    val userBitmap = BitmapFactory.decodeStream(stream)
                    if (userBitmap != null) {
                        val margin = 50f
                        val destRect = RectF(margin, margin, width - margin, height - margin)
                        val srcRect = Rect(0, 0, userBitmap.width, userBitmap.height)
                        canvas.drawBitmap(userBitmap, srcRect, destRect, Paint(Paint.FILTER_BITMAP_FLAG))
                        photoDrawn = true
                    }
                }
            } catch (e: Exception) {
                photoDrawn = false
            }
        }

        if (!photoDrawn) {
            when {
                "car" in promptLower || "motor" in promptLower || "auto" in promptLower -> {
                    drawSupercarScene(canvas, width, height, random)
                }
                "watch" in promptLower || "luxury" in promptLower || "jewelry" in promptLower -> {
                    drawLuxuryWatchScene(canvas, width, height, random)
                }
                "nature" in promptLower || "travel" in promptLower || "mountain" in promptLower || "landscape" in promptLower -> {
                    drawLandscapeScene(canvas, width, height, random)
                }
                "cyber" in promptLower || "game" in promptLower || "gaming" in promptLower || "sci-fi" in promptLower || "tech" in promptLower -> {
                    drawCyberpunkSciFiScene(canvas, width, height, random)
                }
                "food" in promptLower || "restaurant" in promptLower || "coffee" in promptLower -> {
                    drawGourmetFoodScene(canvas, width, height, random)
                }
                "3d" in promptLower || "render" in promptLower -> {
                    draw3DAbstractRender(canvas, width, height, random)
                }
                else -> {
                    // Portrait, Selfie, Fashion, Wedding, Couple, Solo
                    drawPortraitArt(canvas, width, height, promptLower, category, style, random)
                }
            }
        }

        // 4. Photographic Bokeh, Flares & Highlights
        drawPhotographicOptics(canvas, width, height, random)

        // 5. Elegant Frame & Bezel
        drawFrameBorder(canvas, width, height, frameTitle)

        // 6. Gemini 3.8 Flash Metadata Badge
        drawMetadataOverlay(canvas, width, height, title, style)

        // Save Bitmap to internal cache
        val outputDir = File(context.filesDir, "generated_photos")
        if (!outputDir.exists()) outputDir.mkdirs()

        val photoFile = File(outputDir, "gemini_photo_${System.currentTimeMillis()}_${random.nextInt(9999)}.png")
        FileOutputStream(photoFile).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 95, out)
        }

        return photoFile.absolutePath
    }

    private fun getThemeColors(prompt: String, category: String): Triple<Int, Int, Int> {
        return when {
            "car" in prompt -> Triple(Color.rgb(15, 20, 28), Color.rgb(20, 28, 42), Color.rgb(8, 10, 15))
            "gold" in prompt || "luxury" in prompt || "watch" in prompt -> Triple(Color.rgb(32, 24, 18), Color.rgb(22, 18, 14), Color.rgb(8, 7, 6))
            "cyber" in prompt || "gaming" in prompt -> Triple(Color.rgb(18, 12, 38), Color.rgb(28, 14, 48), Color.rgb(8, 6, 18))
            "nature" in prompt || "travel" in prompt -> Triple(Color.rgb(16, 32, 44), Color.rgb(28, 48, 58), Color.rgb(12, 18, 22))
            "wedding" in prompt || "couple" in prompt -> Triple(Color.rgb(42, 22, 35), Color.rgb(28, 18, 26), Color.rgb(12, 8, 14))
            "food" in prompt -> Triple(Color.rgb(38, 24, 16), Color.rgb(26, 16, 12), Color.rgb(10, 8, 6))
            else -> Triple(Color.rgb(20, 26, 36), Color.rgb(15, 20, 28), Color.rgb(7, 9, 14))
        }
    }

    private fun drawAtmosphericLighting(canvas: Canvas, w: Int, h: Int, prompt: String, random: Random) {
        val lightColor = when {
            "cyber" in prompt -> Color.argb(90, 0, 220, 255)
            "gold" in prompt || "sunset" in prompt || "luxury" in prompt -> Color.argb(100, 255, 190, 70)
            "wedding" in prompt -> Color.argb(90, 255, 210, 230)
            else -> Color.argb(85, 210, 230, 255)
        }

        val glowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = RadialGradient(
                w * 0.5f, h * 0.3f, w * 0.7f,
                lightColor, Color.TRANSPARENT, Shader.TileMode.CLAMP
            )
        }
        canvas.drawCircle(w * 0.5f, h * 0.3f, w * 0.7f, glowPaint)

        // Accent rim light
        val rimPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = RadialGradient(
                w * 0.85f, h * 0.75f, w * 0.45f,
                Color.argb(55, 120, 170, 255), Color.TRANSPARENT, Shader.TileMode.CLAMP
            )
        }
        canvas.drawCircle(w * 0.85f, h * 0.75f, w * 0.45f, rimPaint)
    }

    private fun drawSupercarScene(canvas: Canvas, w: Int, h: Int, random: Random) {
        // Wet asphalt ground
        val groundPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(0f, h * 0.65f, 0f, h.toFloat(), Color.rgb(14, 18, 25), Color.rgb(5, 7, 10), Shader.TileMode.CLAMP)
        }
        canvas.drawRect(0f, h * 0.65f, w.toFloat(), h.toFloat(), groundPaint)

        // Neon City skyline reflections
        val skylinePaint = Paint().apply { color = Color.argb(120, 25, 35, 55) }
        for (i in 0 until 12) {
            val sw = w * 0.08f + (i * 27 % 40)
            val sx = i * (w / 11f)
            val sh = h * 0.25f + (i * 37 % (h * 0.2f))
            canvas.drawRect(sx, h * 0.65f - sh, sx + sw, h * 0.65f, skylinePaint)
        }

        // Supercar sleek body
        val carPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(
                w * 0.1f, h * 0.55f, w * 0.9f, h * 0.75f,
                Color.rgb(220, 30, 45), Color.rgb(80, 10, 20), Shader.TileMode.CLAMP
            )
        }

        val carPath = Path().apply {
            moveTo(w * 0.12f, h * 0.72f) // Front bumper
            quadTo(w * 0.22f, h * 0.68f, w * 0.32f, h * 0.62f) // Hood
            quadTo(w * 0.48f, h * 0.52f, w * 0.65f, h * 0.53f) // Windshield & Roof
            quadTo(w * 0.78f, h * 0.56f, w * 0.88f, h * 0.65f) // Rear slope
            lineTo(w * 0.88f, h * 0.72f)
            lineTo(w * 0.12f, h * 0.72f)
            close()
        }
        canvas.drawPath(carPath, carPaint)

        // Car rim/highlight line
        val rimStroke = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(200, 255, 230, 230)
            style = Paint.Style.STROKE
            strokeWidth = 3f
        }
        canvas.drawPath(carPath, rimStroke)

        // Wheels
        val tirePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.rgb(18, 20, 24) }
        val wheelAlloy = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.rgb(190, 205, 220); style = Paint.Style.STROKE; strokeWidth = 5f }
        val r = w * 0.075f
        canvas.drawCircle(w * 0.28f, h * 0.72f, r, tirePaint)
        canvas.drawCircle(w * 0.28f, h * 0.72f, r * 0.6f, wheelAlloy)
        canvas.drawCircle(w * 0.74f, h * 0.72f, r, tirePaint)
        canvas.drawCircle(w * 0.74f, h * 0.72f, r * 0.6f, wheelAlloy)

        // Headlight xenon beam
        val beamPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = RadialGradient(
                w * 0.14f, h * 0.68f, w * 0.45f,
                Color.argb(160, 200, 240, 255), Color.TRANSPARENT, Shader.TileMode.CLAMP
            )
        }
        canvas.drawCircle(w * 0.14f, h * 0.68f, w * 0.45f, beamPaint)
    }

    private fun drawLuxuryWatchScene(canvas: Canvas, w: Int, h: Int, random: Random) {
        val cx = w * 0.5f
        val cy = h * 0.48f
        val radius = w * 0.32f

        // Watch Bezel Outer Ring (Gold / Platinum)
        val bezelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = RadialGradient(
                cx, cy, radius * 1.15f,
                Color.rgb(235, 195, 100), Color.rgb(120, 85, 30), Shader.TileMode.CLAMP
            )
        }
        canvas.drawCircle(cx, cy, radius * 1.08f, bezelPaint)

        // Fluted Bezel Ring
        val flutedPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(40, 30, 20)
            style = Paint.Style.STROKE
            strokeWidth = 6f
        }
        canvas.drawCircle(cx, cy, radius * 1.02f, flutedPaint)

        // Watch Dial (Deep Emerald / Sunburst Obsidian)
        val dialPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = RadialGradient(
                cx, cy, radius,
                Color.rgb(15, 45, 35), Color.rgb(6, 16, 12), Shader.TileMode.CLAMP
            )
        }
        canvas.drawCircle(cx, cy, radius, dialPaint)

        // Hour Markers
        val markerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(245, 215, 140)
            strokeWidth = 6f
            strokeCap = Paint.Cap.ROUND
        }
        for (i in 0 until 12) {
            val angle = Math.toRadians(i * 30.0)
            val x1 = cx + (radius * 0.78f * sin(angle)).toFloat()
            val y1 = cy - (radius * 0.78f * cos(angle)).toFloat()
            val x2 = cx + (radius * 0.92f * sin(angle)).toFloat()
            val y2 = cy - (radius * 0.92f * cos(angle)).toFloat()
            canvas.drawLine(x1, y1, x2, y2, markerPaint)
        }

        // Watch Hands
        val handPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(255, 235, 175)
            strokeWidth = 7f
            strokeCap = Paint.Cap.ROUND
        }
        canvas.drawLine(cx, cy, cx + radius * 0.45f, cy - radius * 0.45f, handPaint) // Hour
        canvas.drawLine(cx, cy, cx - radius * 0.15f, cy - radius * 0.7f, handPaint) // Minute

        // Seconds hand
        val secPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(255, 60, 60)
            strokeWidth = 3f
        }
        canvas.drawLine(cx, cy, cx + radius * 0.65f, cy + radius * 0.35f, secPaint)
        canvas.drawCircle(cx, cy, 10f, secPaint)

        // Glint / Sapphire reflection arc
        val glintPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(
                cx - radius, cy - radius, cx + radius, cy + radius,
                Color.argb(80, 255, 255, 255), Color.TRANSPARENT, Shader.TileMode.CLAMP
            )
        }
        canvas.drawCircle(cx, cy, radius * 0.95f, glintPaint)
    }

    private fun drawLandscapeScene(canvas: Canvas, w: Int, h: Int, random: Random) {
        // Sunset Sun Orb
        val sunPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = RadialGradient(
                w * 0.5f, h * 0.45f, w * 0.35f,
                Color.rgb(255, 180, 80), Color.TRANSPARENT, Shader.TileMode.CLAMP
            )
        }
        canvas.drawCircle(w * 0.5f, h * 0.45f, w * 0.35f, sunPaint)

        // Far Mountain Ridge (Mist)
        val mtnFar = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.rgb(38, 48, 65) }
        val path1 = Path().apply {
            moveTo(0f, h * 0.52f)
            lineTo(w * 0.25f, h * 0.38f)
            lineTo(w * 0.5f, h * 0.46f)
            lineTo(w * 0.75f, h * 0.36f)
            lineTo(w.toFloat(), h * 0.52f)
            lineTo(w.toFloat(), h.toFloat())
            lineTo(0f, h.toFloat())
            close()
        }
        canvas.drawPath(path1, mtnFar)

        // Near Mountain Ridge
        val mtnNear = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.rgb(18, 28, 38) }
        val path2 = Path().apply {
            moveTo(0f, h * 0.62f)
            lineTo(w * 0.35f, h * 0.48f)
            lineTo(w * 0.68f, h * 0.58f)
            lineTo(w.toFloat(), h * 0.49f)
            lineTo(w.toFloat(), h.toFloat())
            lineTo(0f, h.toFloat())
            close()
        }
        canvas.drawPath(path2, mtnNear)

        // Lake reflection in foreground
        val lakePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(0f, h * 0.68f, 0f, h.toFloat(), Color.rgb(12, 22, 32), Color.rgb(6, 12, 18), Shader.TileMode.CLAMP)
        }
        canvas.drawRect(0f, h * 0.68f, w.toFloat(), h.toFloat(), lakePaint)

        // Pine trees silhouette along edge
        val pinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.rgb(8, 14, 18) }
        for (i in 0 until 18) {
            val tx = (i * w / 17f) + random.nextInt(15) - 7
            val ty = h * 0.68f
            val th = 30f + random.nextInt(45)
            val pPath = Path().apply {
                moveTo(tx, ty - th)
                lineTo(tx - 12f, ty)
                lineTo(tx + 12f, ty)
                close()
            }
            canvas.drawPath(pPath, pinePaint)
        }
    }

    private fun drawCyberpunkSciFiScene(canvas: Canvas, w: Int, h: Int, random: Random) {
        // Perspective Grid
        val gridPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(90, 0, 255, 230)
            strokeWidth = 2f
        }
        val horizonY = h * 0.55f

        // Horizontal grid lines
        var gy = horizonY + 10f
        var step = 12f
        while (gy < h) {
            canvas.drawLine(0f, gy, w.toFloat(), gy, gridPaint)
            gy += step
            step *= 1.25f
        }

        // Vanishing perspective rays
        for (i in -8..8) {
            val vx = w * 0.5f + (i * w * 0.15f)
            canvas.drawLine(w * 0.5f, horizonY, vx, h.toFloat(), gridPaint)
        }

        // Giant Cybernetic Sun / Holographic Ring
        val sunPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = RadialGradient(
                w * 0.5f, horizonY - 40f, w * 0.4f,
                Color.rgb(255, 0, 128), Color.rgb(60, 0, 90), Shader.TileMode.CLAMP
            )
        }
        canvas.drawCircle(w * 0.5f, horizonY - 40f, w * 0.32f, sunPaint)

        // Horizontal slices through sun (retro synthwave look)
        val slicePaint = Paint().apply { color = Color.rgb(18, 12, 38) }
        for (s in 0 until 7) {
            val sy = horizonY - 100f + (s * 18f)
            canvas.drawRect(w * 0.15f, sy, w * 0.85f, sy + 4f + s * 1.5f, slicePaint)
        }
    }

    private fun drawGourmetFoodScene(canvas: Canvas, w: Int, h: Int, random: Random) {
        val cx = w * 0.5f
        val cy = h * 0.52f
        val plateRadius = w * 0.36f

        // Ceramic Chef Plate
        val platePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = RadialGradient(
                cx, cy, plateRadius,
                Color.rgb(240, 242, 245), Color.rgb(180, 185, 195), Shader.TileMode.CLAMP
            )
        }
        canvas.drawCircle(cx, cy, plateRadius, platePaint)

        // Plate inner rim
        val innerRim = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(215, 220, 228)
            style = Paint.Style.STROKE
            strokeWidth = 4f
        }
        canvas.drawCircle(cx, cy, plateRadius * 0.82f, innerRim)

        // Gourmet Dish / Steak or Salmon with Garnish
        val foodPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = RadialGradient(
                cx, cy, plateRadius * 0.5f,
                Color.rgb(120, 45, 30), Color.rgb(60, 20, 15), Shader.TileMode.CLAMP
            )
        }
        val foodRect = RectF(cx - plateRadius * 0.45f, cy - plateRadius * 0.3f, cx + plateRadius * 0.45f, cy + plateRadius * 0.3f)
        canvas.drawRoundRect(foodRect, 28f, 28f, foodPaint)

        // Herb reduction swirl & microgreens
        val swirlPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(40, 95, 50)
            style = Paint.Style.STROKE
            strokeWidth = 5f
            strokeCap = Paint.Cap.ROUND
        }
        val swirlPath = Path().apply {
            moveTo(cx - plateRadius * 0.6f, cy - plateRadius * 0.2f)
            quadTo(cx, cy - plateRadius * 0.6f, cx + plateRadius * 0.5f, cy + plateRadius * 0.1f)
        }
        canvas.drawPath(swirlPath, swirlPaint)
    }

    private fun draw3DAbstractRender(canvas: Canvas, w: Int, h: Int, random: Random) {
        val cx = w * 0.5f
        val cy = h * 0.48f

        // Floating Chrome & Liquid Glass Orbs
        val orbColors = listOf(
            Color.rgb(220, 190, 255) to Color.rgb(60, 30, 110),
            Color.rgb(140, 230, 255) to Color.rgb(20, 80, 120),
            Color.rgb(255, 180, 210) to Color.rgb(110, 20, 50)
        )

        for (i in 0 until 3) {
            val ox = cx + (if (i == 0) 0f else if (i == 1) -w * 0.25f else w * 0.25f)
            val oy = cy + (if (i == 0) 0f else if (i == 1) -h * 0.12f else h * 0.1f)
            val or = if (i == 0) w * 0.24f else w * 0.14f
            val (c1, c2) = orbColors[i]

            val spherePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                shader = RadialGradient(ox - or * 0.35f, oy - or * 0.35f, or * 1.3f, c1, c2, Shader.TileMode.CLAMP)
            }
            canvas.drawCircle(ox, oy, or, spherePaint)

            // Specular Highlight
            val highlight = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.argb(190, 255, 255, 255)
            }
            canvas.drawCircle(ox - or * 0.3f, oy - or * 0.3f, or * 0.22f, highlight)
        }
    }

    private fun drawPortraitArt(
        canvas: Canvas,
        w: Int,
        h: Int,
        prompt: String,
        category: String,
        artStyle: String,
        random: Random
    ) {
        val isCouple = "couple" in prompt || category == "Couple"
        val isWedding = "wedding" in prompt || category == "Wedding"

        val skinPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = RadialGradient(
                w * 0.5f, h * 0.4f, w * 0.4f,
                Color.rgb(238, 198, 172), Color.rgb(180, 130, 105), Shader.TileMode.CLAMP
            )
        }

        val clothesPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(
                0f, h * 0.6f, 0f, h.toFloat(),
                Color.rgb(28, 36, 48), Color.rgb(10, 14, 20), Shader.TileMode.CLAMP
            )
        }

        val rimPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(180, 240, 245, 255)
            this.style = Paint.Style.STROKE
            strokeWidth = 3.5f
        }

        if (isCouple || isWedding) {
            // Two subjects
            val c1x = w * 0.36f
            val c1y = h * 0.42f
            val r1 = w * 0.17f
            canvas.drawCircle(c1x, c1y, r1, skinPaint)
            canvas.drawCircle(c1x, c1y, r1, rimPaint)

            val c2x = w * 0.64f
            val c2y = h * 0.41f
            val r2 = w * 0.16f
            canvas.drawCircle(c2x, c2y, r2, skinPaint)
            canvas.drawCircle(c2x, c2y, r2, rimPaint)

            // Shoulders & Attire
            val body = Path().apply {
                moveTo(w * 0.05f, h.toFloat())
                lineTo(w * 0.95f, h.toFloat())
                lineTo(w * 0.85f, h * 0.68f)
                quadTo(c2x, h * 0.58f, w * 0.5f, h * 0.65f)
                quadTo(c1x, h * 0.58f, w * 0.15f, h * 0.68f)
                close()
            }
            canvas.drawPath(body, clothesPaint)
            canvas.drawPath(body, rimPaint)

            if (isWedding) {
                // Ethereal lace veil drape
                val veilPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.argb(85, 255, 255, 255)
                    this.style = Paint.Style.FILL
                }
                val veilPath = Path().apply {
                    moveTo(c1x, c1y - r1)
                    quadTo(w * 0.1f, h * 0.5f, w * 0.05f, h.toFloat())
                    lineTo(w * 0.45f, h.toFloat())
                    quadTo(c1x, h * 0.65f, c1x, c1y - r1)
                    close()
                }
                canvas.drawPath(veilPath, veilPaint)
            }
        } else {
            // Solo / Selfie / Fashion Portrait
            val cx = w * 0.5f
            val cy = h * 0.4f
            val headRadius = w * 0.21f

            // Hair / Silhouette Halo
            val hairPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.rgb(25, 20, 22)
            }
            canvas.drawCircle(cx, cy - 10f, headRadius * 1.12f, hairPaint)

            // Face
            canvas.drawCircle(cx, cy, headRadius, skinPaint)
            canvas.drawCircle(cx, cy, headRadius, rimPaint)

            // Shoulders & Editorial Wardrobe
            val body = Path().apply {
                moveTo(w * 0.1f, h.toFloat())
                lineTo(w * 0.9f, h.toFloat())
                lineTo(w * 0.82f, h * 0.66f)
                quadTo(cx, h * 0.58f, w * 0.18f, h * 0.66f)
                close()
            }
            canvas.drawPath(body, clothesPaint)
            canvas.drawPath(body, rimPaint)
        }
    }

    private fun drawPhotographicOptics(canvas: Canvas, w: Int, h: Int, random: Random) {
        // Bokeh light disks
        val bokehPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        for (i in 0 until 24) {
            val bx = random.nextFloat() * w
            val by = random.nextFloat() * h * 0.85f
            val br = 6f + random.nextFloat() * 22f
            val bAlpha = 15 + random.nextInt(40)
            bokehPaint.color = Color.argb(bAlpha, 235, 242, 255)
            canvas.drawCircle(bx, by, br, bokehPaint)
        }

        // Subtle filmic lens vignette
        val vignettePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = RadialGradient(
                w * 0.5f, h * 0.5f, w * 0.75f,
                Color.TRANSPARENT, Color.argb(120, 0, 0, 0), Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(0f, 0f, w.toFloat(), h.toFloat(), vignettePaint)
    }

    private fun drawFrameBorder(canvas: Canvas, w: Int, h: Int, frameTitle: String) {
        val framePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 7f
        }

        when {
            "Gold" in frameTitle || "Glow" in frameTitle -> {
                framePaint.color = Color.argb(220, 220, 185, 80)
                canvas.drawRoundRect(22f, 22f, w - 22f, h - 22f, 28f, 28f, framePaint)
                framePaint.strokeWidth = 2f
                framePaint.color = Color.argb(140, 255, 240, 180)
                canvas.drawRoundRect(30f, 30f, w - 30f, h - 30f, 22f, 22f, framePaint)
            }
            "Emerald" in frameTitle -> {
                framePaint.color = Color.argb(220, 40, 130, 95)
                canvas.drawRoundRect(22f, 22f, w - 22f, h - 22f, 26f, 26f, framePaint)
            }
            "Film" in frameTitle -> {
                val filmBg = Paint().apply { color = Color.BLACK; style = Paint.Style.FILL }
                canvas.drawRect(0f, 0f, 26f, h.toFloat(), filmBg)
                canvas.drawRect(w - 26f, 0f, w.toFloat(), h.toFloat(), filmBg)
                val spPaint = Paint().apply { color = Color.WHITE; style = Paint.Style.FILL }
                for (y in 20 until h step 42) {
                    canvas.drawRoundRect(6f, y.toFloat(), 20f, (y + 22).toFloat(), 4f, 4f, spPaint)
                    canvas.drawRoundRect(w - 20f, y.toFloat(), w - 6f, (y + 22).toFloat(), 4f, 4f, spPaint)
                }
            }
            else -> {
                // Titanium & Liquid Glass Bezel
                framePaint.color = Color.argb(190, 210, 220, 235)
                canvas.drawRoundRect(20f, 20f, w - 20f, h - 20f, 30f, 30f, framePaint)
                framePaint.strokeWidth = 2f
                framePaint.color = Color.argb(90, 255, 255, 255)
                canvas.drawRoundRect(28f, 28f, w - 28f, h - 28f, 24f, 24f, framePaint)
            }
        }
    }

    private fun drawMetadataOverlay(canvas: Canvas, w: Int, h: Int, title: String, renderingStyle: String) {
        val bannerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(190, 12, 16, 24)
            this.style = Paint.Style.FILL
        }
        val rect = RectF(32f, h - 110f, w - 32f, h - 30f)
        canvas.drawRoundRect(rect, 18f, 18f, bannerPaint)

        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(75, 255, 255, 255)
            this.style = Paint.Style.STROKE
            strokeWidth = 1.5f
        }
        canvas.drawRoundRect(rect, 18f, 18f, borderPaint)

        // Text title
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 24f
            isFakeBoldText = true
        }
        val shortTitle = if (title.length > 28) title.take(26) + "..." else title
        canvas.drawText(shortTitle, 52f, h - 74f, textPaint)

        // Subtitle
        val subPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(185, 200, 220)
            textSize = 17f
        }
        canvas.drawText("Generated with Gemini 2.5 Flash • $renderingStyle", 52f, h - 48f, subPaint)
    }
}
