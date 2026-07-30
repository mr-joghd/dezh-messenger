<div dir="rtl">

<p align="center">
  <img src="assets/logo.png" alt="دژ مسنجر" width="120"/>
</p>

<h1 align="center">🛡️ دژ مسنجر</h1>
<p align="center"><b>لایه امنیتی برای پیام‌رسان‌های شما</b></p>

<p align="center">
  <img src="https://img.shields.io/badge/version-1.4-blue?style=flat-square" />
  <img src="https://img.shields.io/badge/platform-Android-green?style=flat-square" />
  <img src="https://img.shields.io/badge/license-MIT-orange?style=flat-square" />
  <img src="https://img.shields.io/badge/status-beta-yellow?style=flat-square" />
  <img src="https://img.shields.io/badge/made%20by-StarryNight%20Studio-purple?style=flat-square" />
</p>

---

## دژ چیست؟

دژ مسنجر یک پیام‌رسان معمولی نیست؛ بلکه یک **محیط ایزوله و امن** برای استفاده از پیام‌رسان‌های دیگر است.

پیام‌رسان‌هایی مثل **ایتا، روبیکا، بله و سروش‌پلاس** را به صورت WebView در فضای کاملاً جداگانه اجرا می‌کند:

- 🚫 هیچ‌کدام دسترسی مستقیم به سیستم ندارند
- 🔗 ارتباط با گوشی محدود و کنترل‌شده است
- 📦 همه چیز در **Sandbox** امن اجرا می‌شود

---

## ✨ قابلیت‌ها

### 🔒 لایه امنیتی
اجرای پیام‌رسان‌ها در محیط WebView ایزوله با کنترل دسترسی.

### 📁 فایل‌منیجر و گالری داخلی
- مدیریت فایل‌ها و رسانه‌ها
- مشاهده عکس‌ها و ویدیوها
- انتقال مطمئن به حافظه اصلی گوشی

### 🎨 تم‌ها
- 🌌 Galaxy Theme
- 💚 Emerald Theme
- تم‌های بیشتر در راهند...

---

## 📦 نصب

### از طریق GitHub Releases
آخرین نسخه را از [صفحه Releases](../../releases) دانلود کنید.

### هشدار گوگل پلی پروتکت

اگر هنگام نصب این پیام ظاهر شد:
> **"App blocked to protect your device"**

نگران نباشید. این هشدار استاندارد گوگل برای اپ‌هایی است که از خارج از Play Store نصب می‌شوند.

**مراحل نصب:**
1. روی **More details** بزنید
2. روی **Install anyway** بزنید
3. گزینه **OK** را تأیید کنید

> ⚠️ فقط از **کانال رسمی StarryNight Studio** اپ را دانلود کنید.

---

## 🏗️ ساخت از سورس

### پیش‌نیازها
- Android Studio Hedgehog یا بالاتر
- JDK 17
- Android SDK 34

```bash
git clone https://github.com/StarrynightStudio/dezh-messenger.git
cd dezh-messenger
./gradlew assembleDebug
```

### نکته امنیتی برای بیلد Release
فایل `key.properties` را خودتان بسازید و **هرگز** آن را کامیت نکنید:

```properties
storePassword=YOUR_STORE_PASSWORD
keyPassword=YOUR_KEY_PASSWORD
keyAlias=YOUR_KEY_ALIAS
storeFile=YOUR_KEYSTORE_PATH
```

---

## 🛡️ امنیت

برای گزارش آسیب‌پذیری، لطفاً [SECURITY.md](SECURITY.md) را بخوانید.

**هرگز** مشکلات امنیتی را به صورت عمومی در Issues مطرح نکنید.

---

## 🤝 مشارکت

از مشارکت شما استقبال می‌کنیم! لطفاً [CONTRIBUTING.md](CONTRIBUTING.md) را بخوانید.

1. Fork کنید
2. Branch بسازید (`git checkout -b feature/AmazingFeature`)
3. کامیت کنید (`git commit -m 'Add AmazingFeature'`)
4. Push کنید (`git push origin feature/AmazingFeature`)
5. Pull Request بفرستید

---

## ⚙️ وضعیت پروژه

> دژ در حال توسعه فعال است. ممکن است باگ یا محدودیت وجود داشته باشد.
> مشکلات را از طریق [Issues](../../issues) گزارش دهید.

---

## 📄 لایسنس

این پروژه تحت [لایسنس MIT](LICENSE) منتشر شده است.

---

## 🦉 از StarryNight Studio

ما باور داریم ابزارهای دیجیتال باید **قابل کنترل، شفاف و امن** باشند.
دژ اولین گام ما در این مسیر است.

> **دژ مسنجر — استفاده از پیام‌رسان‌ها بدون کنترل شدن.**

</div>
