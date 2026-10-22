package com.swipeclean.app.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.swipeclean.app.AppConfig
import com.swipeclean.app.data.AppLanguage
import com.swipeclean.app.data.AppStrings

private val WhatsAppGreen = Color(0xFF25D366)

enum class FeedbackCategory(val labelAr: String, val labelEn: String, val emoji: String) {
    FEATURE("اقتراح ميزة", "Feature Suggestion", "💡"),
    BUG("إبلاغ عن مشكلة", "Bug Report", "🐞"),
    GENERAL("رأي عام أو استفسار", "General Feedback", "💬");

    fun label(lang: AppLanguage): String = if (lang == AppLanguage.ARABIC) labelAr else labelEn
}

@Composable
fun FeedbackDialog(
    currentLanguage: AppLanguage = AppLanguage.ARABIC,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var selectedCategory by remember { mutableStateOf(FeedbackCategory.FEATURE) }
    var messageText by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = AppStrings.close(currentLanguage),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = AppStrings.feedbackTitle(currentLanguage),
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = CircleShape,
                            color = WhatsAppGreen.copy(alpha = 0.15f),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Chat,
                                    contentDescription = null,
                                    tint = WhatsAppGreen,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = AppStrings.feedbackSubtitle(currentLanguage),
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Category Selection
                Text(
                    text = AppStrings.feedbackType(currentLanguage),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FeedbackCategory.entries.forEach { category ->
                        FilterChip(
                            selected = selectedCategory == category,
                            onClick = { selectedCategory = category },
                            label = {
                                Text(
                                    "${category.emoji} ${category.label(currentLanguage)}",
                                    fontSize = 11.sp,
                                    maxLines = 1
                                )
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Message Text Field
                OutlinedTextField(
                    value = messageText,
                    onValueChange = { messageText = it },
                    placeholder = {
                        Text(
                            AppStrings.feedbackPlaceholder(currentLanguage),
                            fontSize = 13.sp
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    shape = RoundedCornerShape(14.dp),
                    maxLines = 5
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Send Button via WhatsApp
                Button(
                    onClick = {
                        sendToWhatsApp(
                            context = context,
                            phoneNumber = AppConfig.WHATSAPP_NUMBER,
                            category = selectedCategory,
                            customMessage = messageText,
                            lang = currentLanguage
                        )
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = WhatsAppGreen),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Chat,
                        contentDescription = null,
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = AppStrings.sendViaWhatsApp(currentLanguage),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = AppStrings.whatsAppAutoInfo(currentLanguage),
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

private fun sendToWhatsApp(
    context: Context,
    phoneNumber: String,
    category: FeedbackCategory,
    customMessage: String,
    lang: AppLanguage
) {
    val cleanPhone = phoneNumber.replace("+", "").replace(" ", "").trim()

    val isAr = (lang == AppLanguage.ARABIC)

    val deviceInfo = if (isAr) """
        📱 الجهاز: ${Build.MANUFACTURER.replaceFirstChar { it.uppercase() }} ${Build.MODEL}
        ⚙️ إصدار الأندرويد: Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})
        📦 إصدار التطبيق: ${AppConfig.APP_NAME} v${AppConfig.APP_VERSION}
    """.trimIndent() else """
        📱 Device: ${Build.MANUFACTURER.replaceFirstChar { it.uppercase() }} ${Build.MODEL}
        ⚙️ Android OS: Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})
        📦 App Version: ${AppConfig.APP_NAME} v${AppConfig.APP_VERSION}
    """.trimIndent()

    val formattedMessage = buildString {
        if (isAr) {
            appendLine("👋 مرحباً! لدي رسالة بخصوص تطبيق *${AppConfig.APP_NAME}*:")
            appendLine()
            appendLine("📋 *النوع:* ${category.emoji} ${category.label(lang)}")
            if (customMessage.isNotBlank()) {
                appendLine("📝 *التفاصيل:*")
                appendLine(customMessage.trim())
            }
        } else {
            appendLine("👋 Hello! I have feedback regarding *${AppConfig.APP_NAME}*:")
            appendLine()
            appendLine("📋 *Type:* ${category.emoji} ${category.label(lang)}")
            if (customMessage.isNotBlank()) {
                appendLine("📝 *Details:*")
                appendLine(customMessage.trim())
            }
        }
        appendLine()
        appendLine("-------------------------")
        appendLine(deviceInfo)
    }

    val uriString = "https://api.whatsapp.com/send?phone=$cleanPhone&text=${Uri.encode(formattedMessage)}"
    val uri = Uri.parse(uriString)

    val whatsappIntent = Intent(Intent.ACTION_VIEW, uri).apply {
        setPackage("com.whatsapp")
    }

    try {
        context.startActivity(whatsappIntent)
    } catch (e: Exception) {
        // Fallback if official WhatsApp is not found (WhatsApp Business or Browser)
        try {
            val fallbackIntent = Intent(Intent.ACTION_VIEW, uri)
            context.startActivity(fallbackIntent)
        } catch (e2: Exception) {
            Toast.makeText(context, AppStrings.whatsAppNotFound(lang), Toast.LENGTH_SHORT).show()
        }
    }
}
