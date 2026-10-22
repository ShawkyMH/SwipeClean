package com.swipeclean.app.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class AppLanguage(val code: String, val displayNameAr: String, val displayNameEn: String) {
    ARABIC("ar", "العربية (Arabic)", "Arabic"),
    ENGLISH("en", "الإنجليزية (English)", "English")
}

enum class AppTheme(val displayNameAr: String, val displayNameEn: String) {
    SYSTEM("تلقائي (حسب الهاتف)", "System Default"),
    LIGHT("الوضع الفاتح (Light)", "Light Mode"),
    DARK("الوضع الداكن (Dark)", "Dark Mode")
}

enum class FullscreenSwipeMode(val displayNameAr: String, val displayNameEn: String, val descriptionAr: String, val descriptionEn: String) {
    BROWSE_ONLY(
        displayNameAr = "تصفح كمعرض صور عادي (الافتراضي)",
        displayNameEn = "Browse as Gallery (Default)",
        descriptionAr = "عند فتح وتكبير أي صورة أو فيديو، السحب يميناً ويساراً يقلب بين الملفات فقط دون حذف أو احتفاظ.",
        descriptionEn = "When zoomed/fullscreen, swiping left or right browses through media without deleting or keeping."
    ),
    CLEAN_ACTIONS(
        displayNameAr = "تنظيف وسحب مباشر (حذف واحتفاظ)",
        displayNameEn = "Swipe to Clean & Keep",
        descriptionAr = "السحب يميناً يحتفظ بالصورة والسحب يساراً يرسلها للسلة حتى أثناء ملء الشاشة.",
        descriptionEn = "Swiping right keeps and swiping left deletes even in fullscreen mode."
    )
}

class SettingsManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("swipeclean_settings", Context.MODE_PRIVATE)

    private val _language = MutableStateFlow(
        AppLanguage.entries.find { it.code == prefs.getString("key_language", "ar") } ?: AppLanguage.ARABIC
    )
    val language: StateFlow<AppLanguage> = _language.asStateFlow()

    private val _theme = MutableStateFlow(
        try {
            AppTheme.valueOf(prefs.getString("key_theme", AppTheme.DARK.name) ?: AppTheme.DARK.name)
        } catch (e: Exception) {
            AppTheme.DARK
        }
    )
    val theme: StateFlow<AppTheme> = _theme.asStateFlow()

    private val _fullscreenSwipeMode = MutableStateFlow(
        try {
            FullscreenSwipeMode.valueOf(
                prefs.getString("key_fs_mode", FullscreenSwipeMode.BROWSE_ONLY.name) ?: FullscreenSwipeMode.BROWSE_ONLY.name
            )
        } catch (e: Exception) {
            FullscreenSwipeMode.BROWSE_ONLY
        }
    )
    val fullscreenSwipeMode: StateFlow<FullscreenSwipeMode> = _fullscreenSwipeMode.asStateFlow()

    fun setLanguage(lang: AppLanguage) {
        _language.value = lang
        prefs.edit().putString("key_language", lang.code).apply()
    }

    fun setTheme(theme: AppTheme) {
        _theme.value = theme
        prefs.edit().putString("key_theme", theme.name).apply()
    }

    fun setFullscreenSwipeMode(mode: FullscreenSwipeMode) {
        _fullscreenSwipeMode.value = mode
        prefs.edit().putString("key_fs_mode", mode.name).apply()
    }
}
