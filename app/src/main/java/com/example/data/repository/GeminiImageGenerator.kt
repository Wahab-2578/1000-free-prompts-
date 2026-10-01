package com.example.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit

/**
 * Service specifically targeting the 'gemini-2.5-flash' model for AI image generation.
 */
object GeminiImageGenerator {

    private const val TAG = "GeminiImageGen"
    private const val TARGET_MODEL = "gemini-2.5-flash"
    private const val BASE_API_URL = "https://generativelanguage.googleapis.com/v1beta/models"

    private val client by lazy {
        OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build()
    }

    suspend fun generateImage(
        context: Context,
        prompt: String,
        category: String,
        style: String,
        frameTitle: String,
        aspectRatio: String,
        uploadedPhotoUri: Uri? = null
    ): Pair<String, Boolean> = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        var generatedFilePath: String? = null
        var isGeminiCloud = false

        // Directly call the Gemini 2.5 Flash endpoint if API key is present and configured
        if (apiKey.isNotBlank() && apiKey != "MY_GEMINI_API_KEY") {
            try {
                val url = "$BASE_API_URL/$TARGET_MODEL:generateContent?key=$apiKey"

                val promptWithStyle = buildHighQualityPrompt(
                    prompt = prompt,
                    category = category,
                    style = style,
                    frameTitle = frameTitle
                )

                val partsArray = JSONArray().apply {
                    put(JSONObject().put("text", promptWithStyle))
                }

                // If user uploaded a photo, attach base64 inlineData for multimodal guidance
                if (uploadedPhotoUri != null) {
                    try {
                        context.contentResolver.openInputStream(uploadedPhotoUri)?.use { stream ->
                            val bytes = stream.readBytes()
                            val base64 = Base64.encodeToString(bytes, Base64.NO_WRAP)
                            partsArray.put(
                                JSONObject().apply {
                                    put(
                                        "inlineData",
                                        JSONObject().apply {
                                            put("mimeType", "image/jpeg")
                                            put("data", base64)
                                        }
                                    )
                                }
                            )
                        }
                    } catch (e: Exception) {
                        Log.w(TAG, "Could not attach user uploaded photo: ${e.message}")
                    }
                }

                val imageRatio = when (aspectRatio) {
                    "1:1" -> "1:1"
                    "4:5", "3:4" -> "3:4"
                    "9:16" -> "9:16"
                    "16:9" -> "16:9"
                    else -> "1:1"
                }

                val requestJson = JSONObject().apply {
                    put("contents", JSONArray().put(JSONObject().put("parts", partsArray)))
                    put(
                        "generationConfig",
                        JSONObject().apply {
                            put("responseModalities", JSONArray().put("TEXT").put("IMAGE"))
                            put(
                                "imageConfig",
                                JSONObject().apply {
                                    put("aspectRatio", imageRatio)
                                    put("imageSize", "1K")
                                }
                            )
                        }
                    )
                }

                val mediaType = "application/json; charset=utf-8".toMediaType()
                val body = requestJson.toString().toRequestBody(mediaType)
                val request = Request.Builder()
                    .url(url)
                    .post(body)
                    .build()

                Log.d(TAG, "Requesting image generation from model: $TARGET_MODEL")
                val response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    val responseString = response.body?.string() ?: ""
                    val root = JSONObject(responseString)
                    val candidates = root.optJSONArray("candidates")
                    if (candidates != null && candidates.length() > 0) {
                        val content = candidates.getJSONObject(0).optJSONObject("content")
                        val parts = content?.optJSONArray("parts")
                        if (parts != null) {
                            for (p in 0 until parts.length()) {
                                val partObj = parts.getJSONObject(p)
                                val inlineData = partObj.optJSONObject("inlineData")
                                if (inlineData != null) {
                                    val b64 = inlineData.optString("data", "")
                                    if (b64.isNotEmpty()) {
                                        val imageBytes = Base64.decode(b64, Base64.DEFAULT)
                                        val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                                        if (bitmap != null) {
                                            val dir = File(context.filesDir, "generated_photos")
                                            if (!dir.exists()) dir.mkdirs()
                                            val file = File(dir, "gemini_25_flash_${System.currentTimeMillis()}.png")
                                            FileOutputStream(file).use { out ->
                                                bitmap.compress(Bitmap.CompressFormat.PNG, 95, out)
                                            }
                                            generatedFilePath = file.absolutePath
                                            isGeminiCloud = true
                                            Log.d(TAG, "Successfully generated image with $TARGET_MODEL: ${file.absolutePath}")
                                            break
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    Log.w(TAG, "Call to $TARGET_MODEL returned HTTP ${response.code}: ${response.message}")
                }
            } catch (e: Exception) {
                Log.w(TAG, "Gemini image generation error with $TARGET_MODEL: ${e.message}")
            }
        }

        // Seamless fallback to high-resolution procedural renderer if API key is unconfigured or call fails
        if (generatedFilePath == null) {
            generatedFilePath = AestheticImageRenderer.generateAndSaveImage(
                context = context,
                title = prompt.take(40),
                category = category,
                style = style,
                frameTitle = frameTitle,
                aspectRatio = aspectRatio,
                uploadedPhotoUri = uploadedPhotoUri
            )
            isGeminiCloud = false
        }

        Pair(generatedFilePath, isGeminiCloud)
    }

    private fun buildHighQualityPrompt(
        prompt: String,
        category: String,
        style: String,
        frameTitle: String
    ): String {
        return "Masterclass ultra high-definition $style $category photograph. $prompt. Frame composition: $frameTitle. Award-winning cinematographic lighting, hyper-realistic fine textures, authentic depth of field, sharp focus, 8k masterpiece."
    }
}
