package com.example.ui.screens.splash

import android.content.Context
import android.speech.tts.TextToSpeech
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.SilverFrost
import kotlinx.coroutines.delay
import java.util.Locale

@Composable
fun SplashScreen(
    isVoiceEnabled: Boolean,
    onSplashFinished: () -> Unit
) {
    val context = LocalContext.current
    var tts: TextToSpeech? by remember { mutableStateOf(null) }

    val logoAlpha = remember { Animatable(0f) }
    val logoScale = remember { Animatable(0.88f) }
    val glowAlpha = remember { Animatable(0f) }
    val shimmerOffset = remember { Animatable(-150f) }
    val textAlpha = remember { Animatable(0f) }

    // TTS Setup
    DisposableEffect(Unit) {
        if (isVoiceEnabled) {
            tts = TextToSpeech(context) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    tts?.apply {
                        language = Locale.US
                        setPitch(0.85f) // Deep, slightly robotic tone
                        setSpeechRate(0.92f) // Clear, measured pace
                    }
                }
            }
        }
        onDispose {
            try {
                tts?.stop()
                tts?.shutdown()
            } catch (e: Exception) {
                // Ignore
            }
        }
    }

    LaunchedEffect(Unit) {
        // Step 1: Dark atmospheric background (0.0s - 0.5s)
        delay(300)

        // Step 2: Subtle light appears behind logo
        glowAlpha.animateTo(
            targetValue = 0.7f,
            animationSpec = tween(durationMillis = 700, easing = FastOutSlowInEasing)
        )

        // Robot voice line 1: "Main... PromptVault AI hoon."
        if (isVoiceEnabled && tts != null) {
            tts?.speak("Main PromptVault AI hoon.", TextToSpeech.QUEUE_FLUSH, null, "promptvault_intro")
        }

        // Step 3 & 4: Logo slowly fades in and scales 90% to 100%
        logoAlpha.animateTo(1f, animationSpec = tween(800, easing = FastOutSlowInEasing))
        logoScale.animateTo(1f, animationSpec = tween(800, easing = FastOutSlowInEasing))

        // Step 5: Glass/light reflection passing across the logo
        shimmerOffset.animateTo(150f, animationSpec = tween(700, easing = LinearEasing))

        // Robot voice line 2: "Made with AI."
        delay(200)
        if (isVoiceEnabled && tts != null) {
            tts?.speak("Made with AI.", TextToSpeech.QUEUE_ADD, null, "promptvault_tagline")
        }

        // Step 7: App name & subtitle smoothly appears
        textAlpha.animateTo(1f, animationSpec = tween(600, easing = FastOutSlowInEasing))

        // Wait to finish smoothly at ~3.4s total duration
        delay(1200)
        onSplashFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground),
        contentAlignment = Alignment.Center
    ) {
        // Atmospheric Ambient Background Glow behind logo
        Box(
            modifier = Modifier
                .size(240.dp)
                .alpha(glowAlpha.value)
                .blur(60.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0x55475569),
                            Color(0x221E293B),
                            Color.Transparent
                        )
                    )
                )
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(24.dp)
        ) {
            // Animated Logo Container
            Box(
                modifier = Modifier
                    .size(110.dp)
                    .scale(logoScale.value)
                    .alpha(logoAlpha.value),
                contentAlignment = Alignment.Center
            ) {
                // Outer glass ring
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(30.dp))
                        .background(Color(0x1F222B3A))
                        .border(
                            1.dp,
                            Brush.linearGradient(
                                listOf(Color(0x66CBD5E1), Color(0x1A475569))
                            ),
                            RoundedCornerShape(30.dp)
                        )
                )

                // App icon graphic
                Image(
                    painter = painterResource(id = R.drawable.ic_app_icon_fg),
                    contentDescription = "PromptVault AI Logo",
                    modifier = Modifier
                        .size(76.dp)
                        .clip(RoundedCornerShape(20.dp))
                )

                // Subtle light reflection beam passing across the logo
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(30.dp))
                        .offset(x = shimmerOffset.value.dp)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color(0x33FFFFFF),
                                    Color.Transparent
                                )
                            )
                        )
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Title & Subtitle fade in
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.alpha(textAlpha.value)
            ) {
                Text(
                    text = "PromptVault AI",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFF8FAFC),
                        letterSpacing = 1.2.sp
                    )
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "1000+ Curated AI Image Prompts",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = SilverFrost.copy(alpha = 0.8f),
                        letterSpacing = 0.5.sp
                    )
                )

                Spacer(modifier = Modifier.height(14.dp))

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0x1AFFFFFF))
                        .border(1.dp, Color(0x22FFFFFF), RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Made with AI",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = SilverFrost,
                            fontWeight = FontWeight.Medium,
                            letterSpacing = 0.8.sp
                        )
                    )
                }
            }
        }
    }
}
