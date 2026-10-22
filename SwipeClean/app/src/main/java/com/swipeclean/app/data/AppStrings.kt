package com.swipeclean.app.data

object AppStrings {

    fun appName(lang: AppLanguage) = "SwipeClean"

    fun keep(lang: AppLanguage) = if (lang == AppLanguage.ARABIC) "احتفظ" else "Keep"
    fun trash(lang: AppLanguage) = if (lang == AppLanguage.ARABIC) "إلى السلة" else "To Trash"
    fun kept(lang: AppLanguage) = if (lang == AppLanguage.ARABIC) "محفوظ" else "Kept"
    fun inTrash(lang: AppLanguage) = if (lang == AppLanguage.ARABIC) "في السلة" else "In Trash"
    fun remaining(lang: AppLanguage) = if (lang == AppLanguage.ARABIC) "متبقي" else "Remaining"

    fun gallery(lang: AppLanguage) = if (lang == AppLanguage.ARABIC) "المعرض" else "Gallery"
    fun settings(lang: AppLanguage) = if (lang == AppLanguage.ARABIC) "الإعدادات" else "Settings"
    fun feedback(lang: AppLanguage) = if (lang == AppLanguage.ARABIC) "تواصل وملاحظات" else "Feedback & Support"

    fun all(lang: AppLanguage) = if (lang == AppLanguage.ARABIC) "الكل" else "All"
    fun photos(lang: AppLanguage) = if (lang == AppLanguage.ARABIC) "📷 صور" else "📷 Photos"
    fun videos(lang: AppLanguage) = if (lang == AppLanguage.ARABIC) "🎬 فيديو" else "🎬 Videos"

    fun allFolders(lang: AppLanguage) = if (lang == AppLanguage.ARABIC) "كل المجلدات" else "All Folders"
    fun sort(lang: AppLanguage) = if (lang == AppLanguage.ARABIC) "ترتيب" else "Sort"
    fun storage(lang: AppLanguage) = if (lang == AppLanguage.ARABIC) "وحدة التخزين" else "Storage Source"

    fun appearance(lang: AppLanguage) = if (lang == AppLanguage.ARABIC) "المظهر والثيم" else "Theme & Appearance"
    fun language(lang: AppLanguage) = if (lang == AppLanguage.ARABIC) "اللغة (Language)" else "Language"
    fun fullscreenBehavior(lang: AppLanguage) = if (lang == AppLanguage.ARABIC) "سلوك ملء الشاشة والمعاينة" else "Fullscreen & Zoom Behavior"
    fun storageDevices(lang: AppLanguage) = if (lang == AppLanguage.ARABIC) "وحدات التخزين المتصلة" else "Connected Storage Drives"
    fun about(lang: AppLanguage) = if (lang == AppLanguage.ARABIC) "حول التطبيق والدعم" else "About & Support"

    fun browseModeTitle(lang: AppLanguage) = if (lang == AppLanguage.ARABIC) "وضع التصفح كمعرض صور" else "Gallery Browse Mode"
    fun cleanModeTitle(lang: AppLanguage) = if (lang == AppLanguage.ARABIC) "وضع التنظيف والسحب" else "Clean & Keep Mode"

    fun emptyTrash(lang: AppLanguage) = if (lang == AppLanguage.ARABIC) "إفراغ السلة وحذفها نهائياً" else "Empty Trash Permanently"
    fun restore(lang: AppLanguage) = if (lang == AppLanguage.ARABIC) "استرجاع" else "Restore"
    fun trashEmptyMsg(lang: AppLanguage) = if (lang == AppLanguage.ARABIC) "سلة المحذوفات فارغة" else "Trash is empty"
    fun reviewDoneMsg(lang: AppLanguage) = if (lang == AppLanguage.ARABIC) "🎉 تم مراجعة كل الملفات!" else "🎉 All files reviewed!"
}
