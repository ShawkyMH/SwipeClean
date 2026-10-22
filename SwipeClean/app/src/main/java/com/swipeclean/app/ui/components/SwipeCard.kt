package com.swipeclean.app.ui.components

import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.OptIn
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem as ExoMediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.swipeclean.app.data.MediaItem
import com.swipeclean.app.data.MediaType
import com.swipeclean.app.ui.theme.DeleteRed
import com.swipeclean.app.ui.theme.KeepGreen
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue
import kotlin.math.roundToInt

@OptIn(UnstableApi::class)
@Composable
fun SwipeCard(
    mediaItem: MediaItem,
    onSwipeRight: () -> Unit,
    onSwipeLeft: () -> Unit,
    onOpenFullscreen: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val density = LocalDensity.current

    var cardWidth by remember { mutableFloatStateOf(0f) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var isSwipingOut by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    // Video states
    var isMuted by remember(mediaItem.id) { mutableStateOf(false) }
    var isPlaying by remember(mediaItem.id) { mutableStateOf(true) }

    val exoPlayer = remember(mediaItem.id) {
        if (mediaItem.type == MediaType.VIDEO) {
            ExoPlayer.Builder(context).build().apply {
                val item = ExoMediaItem.fromUri(mediaItem.uri)
                setMediaItem(item)
                repeatMode = Player.REPEAT_MODE_ONE
                volume = 1f
                prepare()
                playWhenReady = true
            }
        } else null
    }

    DisposableEffect(mediaItem.id) {
        onDispose {
            exoPlayer?.release()
        }
    }

    LaunchedEffect(isMuted) {
        exoPlayer?.volume = if (isMuted) 0f else 1f
    }

    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            exoPlayer?.play()
        } else {
            exoPlayer?.pause()
        }
    }

    // Default threshold in pixels if cardWidth is not measured yet
    val fallbackThreshold = with(density) { 90.dp.toPx() }
    val swipeThreshold = if (cardWidth > 0f) cardWidth * 0.28f else fallbackThreshold
    val maxRotation = 14f

    val rotation = if (cardWidth > 0f) {
        (offsetX / cardWidth) * maxRotation
    } else 0f

    val swipeProgress = if (cardWidth > 0f) {
        (offsetX / cardWidth).coerceIn(-1f, 1f)
    } else 0f

    // Natural physical swipe:
    // offsetX > 0: user dragged physically to the RIGHT -> KEEP
    // offsetX < 0: user dragged physically to the LEFT  -> DELETE
    val isKeepBadge = offsetX > 25f
    val isDeleteBadge = offsetX < -25f
    val badgeProgress = (offsetX.absoluteValue / swipeThreshold).coerceIn(0f, 1f)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .onSizeChanged { cardWidth = it.width.toFloat() }
            .offset { IntOffset(offsetX.roundToInt(), 0) }
            .graphicsLayer {
                rotationZ = rotation
            }
            .pointerInput(mediaItem.id) {
                if (isSwipingOut) return@pointerInput
                detectDragGestures(
                    onDragEnd = {
                        val currentX = offsetX
                        val exitDistance = if (cardWidth > 0f) cardWidth * 1.5f else 1200f
                        when {
                            currentX > swipeThreshold -> {
                                isSwipingOut = true
                                coroutineScope.launch {
                                    androidx.compose.animation.core.Animatable(currentX).animateTo(
                                        targetValue = exitDistance,
                                        animationSpec = tween(220)
                                    ) {
                                        offsetX = this.value
                                    }
                                    onSwipeRight()
                                    offsetX = 0f
                                    isSwipingOut = false
                                }
                            }
                            currentX < -swipeThreshold -> {
                                isSwipingOut = true
                                coroutineScope.launch {
                                    androidx.compose.animation.core.Animatable(currentX).animateTo(
                                        targetValue = -exitDistance,
                                        animationSpec = tween(220)
                                    ) {
                                        offsetX = this.value
                                    }
                                    onSwipeLeft()
                                    offsetX = 0f
                                    isSwipingOut = false
                                }
                            }
                            else -> {
                                coroutineScope.launch {
                                    androidx.compose.animation.core.Animatable(currentX).animateTo(
                                        targetValue = 0f,
                                        animationSpec = tween(180)
                                    ) {
                                        offsetX = this.value
                                    }
                                }
                            }
                        }
                    },
                    onDragCancel = {
                        coroutineScope.launch {
                            val currentX = offsetX
                            androidx.compose.animation.core.Animatable(currentX).animateTo(
                                targetValue = 0f,
                                animationSpec = tween(180)
                            ) {
                                offsetX = this.value
                            }
                        }
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        offsetX += dragAmount.x
                    }
                )
            }
            .clip(RoundedCornerShape(28.dp))
    ) {
        Card(
            modifier = Modifier.fillMaxSize(),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF070B14)),
            elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                // Media Content (Image or Video)
                if (mediaItem.type == MediaType.VIDEO && exoPlayer != null) {
                    AndroidView(
                        factory = { ctx ->
                            PlayerView(ctx).apply {
                                player = exoPlayer
                                useController = false
                                layoutParams = FrameLayout.LayoutParams(
                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                    ViewGroup.LayoutParams.MATCH_PARENT
                                )
                                setOnTouchListener { _, _ -> false }
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(mediaItem.uri)
                            .crossfade(true)
                            .build(),
                        contentDescription = mediaItem.name,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                // Top Controls Overlay
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Black.copy(alpha = 0.65f), Color.Transparent)
                            )
                        )
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Gallery / Video Fullscreen Inspect Button
                    Surface(
                        shape = CircleShape,
                        color = Color.Black.copy(alpha = 0.6f),
                        contentColor = Color.White
                    ) {
                        IconButton(onClick = onOpenFullscreen) {
                            Icon(
                                imageVector = Icons.Default.Fullscreen,
                                contentDescription = if (mediaItem.type == MediaType.VIDEO) "ملء الشاشة وتكبير الفيديو" else "معاينة وتكبير الصورة",
                                tint = Color.White
                            )
                        }
                    }

                    // Video Controls
                    if (mediaItem.type == MediaType.VIDEO) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Surface(
                                shape = CircleShape,
                                color = Color.Black.copy(alpha = 0.55f)
                            ) {
                                IconButton(onClick = { isPlaying = !isPlaying }) {
                                    Icon(
                                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                        contentDescription = "تشغيل / إيقاف",
                                        tint = Color.White
                                    )
                                }
                            }

                            Surface(
                                shape = CircleShape,
                                color = if (isMuted) DeleteRed.copy(alpha = 0.85f) else Color.Black.copy(alpha = 0.55f)
                            ) {
                                IconButton(onClick = { isMuted = !isMuted }) {
                                    Icon(
                                        imageVector = if (isMuted) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                                        contentDescription = "كتم / تشغيل الصوت",
                                        tint = Color.White
                                    )
                                }
                            }
                        }
                    }
                }

                // Swipe feedback badges (Direct physical X-axis check)
                if (isKeepBadge) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(KeepGreen.copy(alpha = badgeProgress * 0.45f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = KeepGreen.copy(alpha = 0.95f),
                            shadowElevation = 8.dp
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp))
                                Text("احتفظ", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                            }
                        }
                    }
                } else if (isDeleteBadge) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(DeleteRed.copy(alpha = badgeProgress * 0.45f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = DeleteRed.copy(alpha = 0.95f),
                            shadowElevation = 8.dp
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp))
                                Text("إلى السلة", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                            }
                        }
                    }
                }

                // Bottom Metadata Info Bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.88f))
                            )
                        )
                        .padding(horizontal = 18.dp, vertical = 14.dp)
                ) {
                    Column {
                        Text(
                            text = mediaItem.name,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            maxLines = 1
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = if (mediaItem.type == MediaType.VIDEO) "🎬 فيديو" else "📷 صورة (اضغط ⛶ للمعاينة والتكبير)",
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 12.sp
                            )
                            Text(
                                text = "${if (mediaItem.bucketName.isNotBlank()) "📁 ${mediaItem.bucketName} • " else ""}${formatFileSize(mediaItem.size)}",
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

fun formatFileSize(size: Long): String {
    return when {
        size >= 1_073_741_824 -> String.format("%.1f GB", size / 1_073_741_824.0)
        size >= 1_048_576 -> String.format("%.1f MB", size / 1_048_576.0)
        size >= 1024 -> String.format("%.1f KB", size / 1024.0)
        else -> "$size B"
    }
}
