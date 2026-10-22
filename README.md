<div align="center">

# ✨ SwipeClean | سوايپ كلين
### Modern, Intelligent Android Photo & Video Decluttering App
### تطبيق أندرويد عصري وذكي لتنظيف الصور والفيديوهات بأسلوب السحب التفاعلي

[![Android](https://img.shields.io/badge/Platform-Android%208.0%2B%20(API%2026--34)-3DDC84?style=for-the-badge&logo=android&logoColor=white)](https://android.com)
[![Kotlin](https://img.shields.io/badge/Kotlin-1.9.22-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)](https://kotlinlang.org)
[![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-Material%203-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose)
[![Apple HIG](https://img.shields.io/badge/Design-Apple%20HIG%20Audited-000000?style=for-the-badge&logo=apple&logoColor=white)](https://developer.apple.com/design/human-interface-guidelines)
[![License](https://img.shields.io/badge/License-MIT-blue?style=for-the-badge)](LICENSE)

<br/>

<p align="center">
  <b>عربي</b> | <a href="#english">English</a>
</p>

</div>

---

## 📱 نبذة عن التطبيق (Overview)

**SwipeClean** هو تطبيق أندرويد متطور مبني بأحدث تقنيات **Jetpack Compose** و **Material 3**، مصمم لمساعدتك على فحص وتنظيم معرض الصور والفيديوهات بسهولة وسرعة فائقة من خلال إيماءات السحب التفاعلية المستوحاة من معايير تصميم **Apple Human Interface Guidelines (HIG)**.

- 👈 **سحب لليسار:** نقل العنصر فوراً إلى سلة المحذوفات داخل التطبيق دون حذفه نهائياً مباشرة، مع إمكانية التراجع.
- 👉 **سحب لليمين:** الاحتفاظ بالصورة أو الفيديو وتخطيها للعنصر التالي.
- 🗑️ **سلة محذوفات ذكية (In-App Trash Bin):** مراجعة جميع الملفات التي اخترت حذفها، ترتيبها حسب الحجم أو التاريخ، واسترجاع أي عنصر بنقرة واحدة، أو تأكيد الحذف النهائي من ذاكرة الجهاز.

---

## 🌟 أبرز المميزات (Key Features)

- 🎴 **شريط تنقل سفلي عصري (Apple HIG Bottom Tab Bar):**
  تنقل سلس بيد واحدة بين شاشة التنظيف (Clean Deck)، المعرض الكامل (Gallery Grid)، وسلة المحذوفات (Trash Bin).
- 🎬 **مشغل فيديو احترافي (ExoPlayer & Media3):**
  معاينة الفيديوهات مع شريط تمرير زمني تفاعلي (Live Scrubber Slider) للتحكم الدقيق بالثواني، كتم/تشغيل الصوت، وإيقاف تلقائي في الخلفية عند الخروج لمنع تكرار الصوت أو استنزاف البطارية.
- 🔍 **فحص كامل للشاشة وتكبير باللمس (Zoom & Pan Modal):**
  معاينة الصور والفيديوهات بوضوح فائق (Ultra-HD)، مع إمكانية التكبير حتى 220% بالضغط المزدوج أو إيماءات اللمس، ودعم التصفح كمعرض صور قياسي أثناء التكبير.
- 💾 **التعرف على جميع وسائط التخزين المتصلة:**
  كشف وتصنيف ذكي للملفات بين: ذاكرة الهاتف الداخلية (Internal Storage)، بطاقات الذاكرة (SD Cards)، ووحدات التخزين الخارجية (USB OTG / Hard Drives).
- 🌐 **دعم كامل للغتين (Arabic & English):**
  واجهة مخصصة بالكامل باللغتين مع دعم مثالي للاتجاهين (RTL & LTR).
- 🎨 **سمات وألوان متعددة (Dark & Light & Dynamic Color):**
  دعم الثيم الداكن والفاتح ومطابقة ثيم النظام، مع تباين لوني معتمد وفق معايير الوصول العالمية (**WCAG AA ≥ 4.5:1**).
- 🚀 **نظام تحديثات تلقائي متصل بـ GitHub (In-App GitHub Updates):**
  فحص ذكي في الخلفية لأحدث الإصدارات وإشعار المستخدم بالتغييرات مع رابط تحميل مباشر وموثوق.
- 💬 **دعم وملاحظات سريعة عبر واتساب:**
  إمكانية إرسال المقترحات والتبليغ عن الأخطاء بنقرة واحدة مباشرة للمطور.

---

## 🛠️ البنية التقنية (Tech Stack & Architecture)

- **Language:** Kotlin 100%
- **UI Framework:** Jetpack Compose (BOM 2024.02.00)
- **Design System:** Material Design 3 + Apple HIG principles
- **Architecture:** MVVM (Model-View-ViewModel) + Unidirectional Data Flow
- **Reactive State:** Kotlin Coroutines & StateFlow / SharedFlow
- **Media Engine:** AndroidX Media3 ExoPlayer 1.2.1
- **Image Loading:** Coil 2.5.0 (with Video Frame Decoder)
- **Storage Layer:** Android MediaStore API (Android 8 to Android 14+ compatible)
- **Minimum SDK:** Android 8.0 (API 26)
- **Target SDK:** Android 14 (API 34)

---

<div id="english"></div>

## 🇬🇧 English Overview

**SwipeClean** is a high-performance Android gallery cleaner and manager built with modern **Jetpack Compose** and **Material 3**. It makes organizing your camera roll effortless using intuitive swipe gestures:

- 👈 **Swipe Left:** Mark for trash (with undo support).
- 👉 **Swipe Right:** Keep and proceed to the next item.
- 🗑️ **Trash Bin:** Review marked items, sort by size or date, restore with a tap, or permanently empty.
- 🚀 **GitHub In-App Updates:** Check and download updates directly from GitHub Releases.
- 🎨 **WCAG AA Accessible:** Designed with high contrast ratios, responsive layouts, and Apple HIG polish.

---

## 📥 التحميل والتثبيت (Download & Installation)

يمكنك تحميل أحدث نسخة جاهزة بصيغة APK مباشرة من قسم [Releases](https://github.com/ShawkyMH/SwipeClean/releases):

1. حمّل ملف `SwipeClean.apk`.
2. ثبّت التطبيق على هاتفك واسمح بالأذونات لعرض الصور ومقاطع الفيديو.

---

## 🏗️ بناء المشروع من المصدر (Build from Source)

```bash
# Clone the repository
git clone https://github.com/ShawkyMH/SwipeClean.git
cd SwipeClean

# Build Debug APK
cd SwipeClean
./gradlew assembleDebug

# Build Signed Release APK
./gradlew assembleRelease
```

---

## 👤 المطور (Author)

- **ShawkyMH**
- 📧 Email: [royred222@gamil.com](mailto:royred222@gamil.com)
- 🐙 GitHub: [@ShawkyMH](https://github.com/ShawkyMH)

---

## 📄 الترخيص (License)

هذا المشروع مرخص تحت رخصة [MIT License](LICENSE).
