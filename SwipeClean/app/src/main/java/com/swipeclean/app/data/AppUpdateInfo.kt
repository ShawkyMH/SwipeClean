package com.swipeclean.app.data

data class AppUpdateInfo(
    val versionCode: Int,
    val versionName: String,
    val isMandatory: Boolean,
    val titleAr: String,
    val titleEn: String,
    val changelogAr: String,
    val changelogEn: String,
    val downloadUrl: String
) {
    fun title(lang: AppLanguage): String = if (lang == AppLanguage.ARABIC) titleAr else titleEn
    fun changelog(lang: AppLanguage): String = if (lang == AppLanguage.ARABIC) changelogAr else changelogEn
}
