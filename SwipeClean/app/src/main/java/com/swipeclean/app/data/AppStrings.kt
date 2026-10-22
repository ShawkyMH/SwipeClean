package com.swipeclean.app.data

object AppStrings {

    fun appName(lang: AppLanguage) = "SwipeClean"

    fun keep(lang: AppLanguage) = if (lang == AppLanguage.ARABIC) "احتفظ" else "Keep"
    fun trash(lang: AppLanguage) = if (lang == AppLanguage.ARABIC) "إلى السلة" else "To Trash"
    fun kept(lang: AppLanguage) = if (lang == AppLanguage.ARABIC) "محفوظ" else "Kept"
    fun inTrash(lang: AppLanguage) = if (lang == AppLanguage.ARABIC) "في السلة" else "In Trash"
    fun remaining(lang: AppLanguage) = if (lang == AppLanguage.ARABIC) "متبقي" else "Remaining"

    fun clean(lang: AppLanguage) = if (lang == AppLanguage.ARABIC) "تنظيف" else "Clean"
    fun trashTab(lang: AppLanguage) = if (lang == AppLanguage.ARABIC) "السلة" else "Trash"
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
    fun updates(lang: AppLanguage) = if (lang == AppLanguage.ARABIC) "تحديثات التطبيق" else "App Updates"
    fun checkForUpdates(lang: AppLanguage) = if (lang == AppLanguage.ARABIC) "البحث عن تحديثات" else "Check for Updates"
    fun checkingUpdates(lang: AppLanguage) = if (lang == AppLanguage.ARABIC) "جاري التحقق من GitHub..." else "Checking GitHub..."
    fun githubReleases(lang: AppLanguage) = if (lang == AppLanguage.ARABIC) "صفحة الإصدارات على GitHub" else "GitHub Releases Page"

    fun browseModeTitle(lang: AppLanguage) = if (lang == AppLanguage.ARABIC) "وضع التصفح كمعرض صور" else "Gallery Browse Mode"
    fun cleanModeTitle(lang: AppLanguage) = if (lang == AppLanguage.ARABIC) "وضع التنظيف والسحب" else "Clean & Keep Mode"

    fun emptyTrash(lang: AppLanguage) = if (lang == AppLanguage.ARABIC) "إفراغ السلة وحذفها نهائياً" else "Empty Trash Permanently"
    fun restore(lang: AppLanguage) = if (lang == AppLanguage.ARABIC) "استرجاع" else "Restore"
    fun trashEmptyMsg(lang: AppLanguage) = if (lang == AppLanguage.ARABIC) "سلة المحذوفات فارغة" else "Trash is empty"
    fun reviewDoneMsg(lang: AppLanguage) = if (lang == AppLanguage.ARABIC) "🎉 تم مراجعة كل الملفات!" else "🎉 All files reviewed!"

    // Gallery Screen Strings
    fun galleryTitle(count: Int, lang: AppLanguage) = if (lang == AppLanguage.ARABIC) "المعرض ($count)" else "Gallery ($count)"
    fun noMediaFound(lang: AppLanguage) = if (lang == AppLanguage.ARABIC) "لا توجد صور أو فيديوهات" else "No photos or videos found"
    fun back(lang: AppLanguage) = if (lang == AppLanguage.ARABIC) "رجوع" else "Back"

    // Trash Screen Strings
    fun trashTitle(count: Int, lang: AppLanguage) = if (lang == AppLanguage.ARABIC) "سلة الحذف ($count)" else "Trash Bin ($count)"
    fun permanentDeleteTitle(lang: AppLanguage) = if (lang == AppLanguage.ARABIC) "حذف نهائي من الجهاز؟" else "Delete Permanently from Device?"
    fun permanentDeleteMsg(count: Int, size: String, lang: AppLanguage) = if (lang == AppLanguage.ARABIC)
        "سيتم حذف $count ملف بحجم إجمالي ($size) نهائياً من ذاكرة الهاتف. لا يمكن التراجع عن هذا الإجراء."
    else
        "This will permanently delete $count files ($size) from your device storage. This action cannot be undone."
    fun deleteNow(lang: AppLanguage) = if (lang == AppLanguage.ARABIC) "حذف الآن" else "Delete Now"
    fun cancel(lang: AppLanguage) = if (lang == AppLanguage.ARABIC) "إلغاء" else "Cancel"
    fun deletingFiles(lang: AppLanguage) = if (lang == AppLanguage.ARABIC) "جاري حذف الملفات نهائياً..." else "Permanently deleting files..."
    fun trashEmptyDescription(lang: AppLanguage) = if (lang == AppLanguage.ARABIC)
        "الملفات التي تقوم بحذفها من شاشة التنظيف ستظهر هنا لمراجعتها أو حذفها نهائياً."
    else
        "Files marked for deletion during review will appear here before permanent deletion."
    fun emptyTrashWithSize(size: String, lang: AppLanguage) = if (lang == AppLanguage.ARABIC) "إفراغ السلة ($size)" else "Empty Trash ($size)"
    fun itemsInTrash(count: Int, lang: AppLanguage) = if (lang == AppLanguage.ARABIC) "$count عنصر في السلة" else "$count items in trash"
    fun trashSize(size: String, lang: AppLanguage) = if (lang == AppLanguage.ARABIC) "حجم السلة: $size" else "Trash size: $size"
    fun sortTrash(lang: AppLanguage) = if (lang == AppLanguage.ARABIC) "ترتيب السلة" else "Sort Trash"

    // Swipe Card Strings
    fun photoTapToInspect(lang: AppLanguage) = if (lang == AppLanguage.ARABIC) "📷 صورة (اضغط ⛶ للمعاينة والتكبير)" else "📷 Photo (Tap ⛶ to inspect & zoom)"
    fun videoBadge(lang: AppLanguage) = if (lang == AppLanguage.ARABIC) "🎬 فيديو" else "🎬 Video"

    // Fullscreen Dialog Strings
    fun close(lang: AppLanguage) = if (lang == AppLanguage.ARABIC) "إغلاق" else "Close"
    fun resetZoom(lang: AppLanguage) = if (lang == AppLanguage.ARABIC) "إعادة ضبط" else "Reset Zoom"
    fun playPause(lang: AppLanguage) = if (lang == AppLanguage.ARABIC) "تشغيل/إيقاف" else "Play/Pause"
    fun sound(lang: AppLanguage) = if (lang == AppLanguage.ARABIC) "الصوت" else "Audio"
    fun browseModeBadge(lang: AppLanguage) = if (lang == AppLanguage.ARABIC) "معرض تصفح 🖼️" else "Browse Gallery 🖼️"
    fun cleanModeBadge(lang: AppLanguage) = if (lang == AppLanguage.ARABIC) "وضع تنظيف 🧹" else "Clean Mode 🧹"
    fun browseModeHint(lang: AppLanguage) = if (lang == AppLanguage.ARABIC) "⛶ اسحب لليسار واليمين للتصفح كمعرض صور" else "⛶ Swipe left or right to browse gallery"
    fun cleanModeHint(lang: AppLanguage) = if (lang == AppLanguage.ARABIC) "⛶ اسحب يميناً للاحتفاظ ويساراً للحذف" else "⛶ Swipe right to keep, left to trash"
    fun previous(lang: AppLanguage) = if (lang == AppLanguage.ARABIC) "السابق" else "Previous"
    fun next(lang: AppLanguage) = if (lang == AppLanguage.ARABIC) "التالي" else "Next"
    fun videoPrefix(lang: AppLanguage) = if (lang == AppLanguage.ARABIC) "🎬 فيديو • " else "🎬 Video • "
    fun photoPrefix(lang: AppLanguage) = if (lang == AppLanguage.ARABIC) "📷 صورة • " else "📷 Photo • "

    // Feedback Dialog Strings
    fun feedbackTitle(lang: AppLanguage) = if (lang == AppLanguage.ARABIC) "تواصل واقتراحات" else "Feedback & Suggestions"
    fun feedbackSubtitle(lang: AppLanguage) = if (lang == AppLanguage.ARABIC)
        "يسعدنا سماع اقتراحاتك لتطوير التطبيق أو الإبلاغ عن أي خطأ مباشرة عبر واتساب."
    else
        "We would love to hear your suggestions or bug reports directly via WhatsApp."
    fun feedbackType(lang: AppLanguage) = if (lang == AppLanguage.ARABIC) "نوع الملاحظة:" else "Feedback Type:"
    fun feedbackCategoryFeature(lang: AppLanguage) = if (lang == AppLanguage.ARABIC) "اقتراح ميزة" else "Feature Suggestion"
    fun feedbackCategoryBug(lang: AppLanguage) = if (lang == AppLanguage.ARABIC) "إبلاغ عن مشكلة" else "Bug Report"
    fun feedbackCategoryGeneral(lang: AppLanguage) = if (lang == AppLanguage.ARABIC) "رأي عام أو استفسار" else "General Inquiry"
    fun feedbackDetails(lang: AppLanguage) = if (lang == AppLanguage.ARABIC) "تفاصيل رسالتك أو اقتراحك:" else "Details of your message:"
    fun feedbackPlaceholder(lang: AppLanguage) = if (lang == AppLanguage.ARABIC)
        "اكتب هنا ما يدور ببالك لمساعدتنا على تحسين التطبيق..."
    else
        "Write your thoughts or bug details here to help us improve the app..."
    fun sendViaWhatsApp(lang: AppLanguage) = if (lang == AppLanguage.ARABIC) "إرسال عبر واتساب" else "Send via WhatsApp"
    fun registeredPhone(lang: AppLanguage) = if (lang == AppLanguage.ARABIC) "الرقم المسجل: " else "Registered phone: "
    fun whatsAppAutoInfo(lang: AppLanguage) = if (lang == AppLanguage.ARABIC)
        "سيتم فتح واتساب تلقائياً مع إرفاق معلومات الجهاز لمساعدتنا في فحص الأخطاء."
    else
        "WhatsApp will open automatically with device info to help us diagnose issues."
    fun whatsAppNotFound(lang: AppLanguage) = if (lang == AppLanguage.ARABIC) "لم يتم العثور على تطبيق واتساب" else "WhatsApp app not found"

    // Update Dialog Strings
    fun downloadUpdate(lang: AppLanguage) = if (lang == AppLanguage.ARABIC) "تحميل التحديث" else "Download Update"
    fun downloadingUpdate(lang: AppLanguage) = if (lang == AppLanguage.ARABIC) "جاري تحميل التحديث..." else "Downloading update..."
    fun downloadComplete(lang: AppLanguage) = if (lang == AppLanguage.ARABIC) "اكتمل التحميل بنجاح!" else "Download Complete!"
    fun downloadReadyToInstall(lang: AppLanguage) = if (lang == AppLanguage.ARABIC)
        "الملف جاهز للتثبيت. اضغط على الزر أدناه لبدء التحديث."
    else
        "The update is downloaded and ready to install. Tap below to begin."
    fun installUpdate(lang: AppLanguage) = if (lang == AppLanguage.ARABIC) "تثبيت التحديث الآن 🚀" else "Install Update Now 🚀"
    fun downloadError(lang: AppLanguage) = if (lang == AppLanguage.ARABIC) "فشل التحميل. يرجى إعادة المحاولة." else "Download failed. Please try again."
    fun retry(lang: AppLanguage) = if (lang == AppLanguage.ARABIC) "إعادة المحاولة" else "Retry"
    fun downloadViaBrowser(lang: AppLanguage) = if (lang == AppLanguage.ARABIC) "تحميل عبر المتصفح" else "Download via Browser"
    fun allowInstallPermission(lang: AppLanguage) = if (lang == AppLanguage.ARABIC)
        "يرجى السماح بتثبيت التطبيقات من هذا المصدر في إعدادات الهاتف للمتابعة."
    else
        "Please allow installing apps from this source in Settings to proceed."
}
