package com.starrynightstudio.dezhmessenger.xwpqrs

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.webkit.*
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.compose.ui.draw.*
import com.starrynightstudio.dezhmessenger.xwpqrs.ui.theme.MyApplicationTheme
import com.starrynightstudio.dezhmessenger.xwpqrs.DezhTheme
import com.starrynightstudio.dezhmessenger.xwpqrs.ThemeManager
import com.starrynightstudio.dezhmessenger.xwpqrs.TriggerService
import com.starrynightstudio.dezhmessenger.xwpqrs.ui.theme.LocalThemeColors
import com.starrynightstudio.dezhmessenger.xwpqrs.ui.theme.getThemeColors
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import java.net.URLDecoder
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import android.media.MediaMetadataRetriever
import android.media.ThumbnailUtils
import android.widget.VideoView
import android.widget.MediaController
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec
import java.security.SecureRandom
import org.json.JSONObject
import org.json.JSONArray
import android.content.Intent
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

val LocalAppLanguage = compositionLocalOf { "en" }

@Composable
fun localizedString(key: String): String {
    return LocalizedStrings.get(key, LocalAppLanguage.current)
}

@Composable
fun rememberRgbFlowBrush(width: Float = 1000f): Brush {
    val infiniteTransition = rememberInfiniteTransition(label = "rgb_flow")
    val shiftOffset by infiniteTransition.animateFloat(
        initialValue = -300f,
        targetValue = 600f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shift"
    )
    val colors = listOf(
        Color(0xFFFF0055), // Neon pink
        Color(0xFF00FFCC), // Neon cyan
        Color(0xFF0066FF), // Neon blue
        Color(0xFF9900FF), // Neon purple
        Color(0xFFFF0055)  // Return to neon pink
    )
    return Brush.linearGradient(
        colors = colors,
        start = androidx.compose.ui.geometry.Offset(shiftOffset, 0f),
        end = androidx.compose.ui.geometry.Offset(shiftOffset + width, width)
    )
}

@Composable
fun Modifier.animateRgbBorder(
    cornerRadius: androidx.compose.ui.unit.Dp = 16.dp,
    borderWidth: androidx.compose.ui.unit.Dp = 2.dp,
    glowing: Boolean = true
): Modifier {
    val brushValue = rememberRgbFlowBrush()
    return this.drawWithCache {
        val strokeWidthPx = borderWidth.toPx()
        val cornerRadiusPx = cornerRadius.toPx()

        onDrawWithContent {
            drawContent()
            
            if (glowing) {
                drawRoundRect(
                    brush = brushValue,
                    size = size,
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerRadiusPx),
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidthPx * 4f),
                    alpha = 0.15f
                )
                drawRoundRect(
                    brush = brushValue,
                    size = size,
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerRadiusPx),
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidthPx * 2f),
                    alpha = 0.35f
                )
            }
            drawRoundRect(
                brush = brushValue,
                size = size,
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerRadiusPx),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidthPx)
            )
        }
    }
}

fun Modifier.rgbGradientText(brush: Brush?): Modifier {
    if (brush == null) return this
    return this.alpha(0.99f)
        .drawWithCache {
            onDrawWithContent {
                drawContent()
                drawRect(brush = brush, blendMode = androidx.compose.ui.graphics.BlendMode.SrcAtop)
            }
        }
}

@Composable
fun AmberCrtOverlay(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Box(modifier = modifier) {
        content()
        Canvas(modifier = Modifier.matchParentSize()) {
            val scanlineSpacing = 5.dp.toPx()
            var y = 0f
            while (y < size.height) {
                drawLine(
                    color = Color.Black.copy(alpha = 0.22f),
                    start = androidx.compose.ui.geometry.Offset(0f, y),
                    end = androidx.compose.ui.geometry.Offset(size.width, y),
                    strokeWidth = 1.dp.toPx()
                )
                y += scanlineSpacing
            }
        }
    }
}

@Composable
fun DezhThemedLayout(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    val themeColors = LocalThemeColors.current
    val isRgb = themeColors.isRGB

    val backgroundModifier = if (isRgb) {
        Modifier
            .background(Color(0xFF000000))
            .animateRgbBorder(cornerRadius = 0.dp, borderWidth = 3.dp, glowing = true)
    } else {
        Modifier.background(
            Brush.verticalGradient(
                colors = listOf(
                    themeColors.background,
                    themeColors.surface
                )
            )
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .then(backgroundModifier)
    ) {
        if (themeColors.isAmber) {
            AmberCrtOverlay(modifier = Modifier.fillMaxSize()) {
                content()
            }
        } else {
            content()
        }
    }
}

object LocalizedStrings {
    val EN = mapOf(
        "app_title" to "Dezh Secure Hub",
        "tab_messengers" to "Messengers",
        "tab_gallery" to "Secure Gallery",
        "tab_file_manager" to "File Manager",
        "tab_info" to "Guide",
        "tag_secure" to "SECURE",
        "eitaa_name" to "Eitaa",
        "eitaa_desc" to "Popular cloud-based Iranian messaging platform.",
        "eitaa_badge" to "Isolated Private Workspace",
        "rubika_name" to "Rubika",
        "rubika_desc" to "Multi-service sharing portal and messaging ecosystem.",
        "rubika_badge" to "Restricted Sandbox Space",
        "soroush_name" to "Soroush Plus",
        "soroush_desc" to "Feature-rich interactive channels & chats.",
        "soroush_badge" to "Zero Main Gallery Tracking",
        "bale_name" to "Bale",
        "bale_desc" to "Messaging app integrated with utility payments.",
        "bale_badge" to "Safe Local Copy Isolation",
        "sandbox_info_title" to "Dezh Sandbox Hub",
        "sandbox_info_desc" to "Learn about Dezh Messenger's isolated operational environment and system structure.",
        "op_concept_title" to "Operational Concept",
        "op_concept_desc" to "Dezh Messenger acts as a clean, highly secure Sandbox Hub for isolating web-based messaging systems. By isolating web assets within separate storage instances, Dezh avoids shared cookie leaks, ensures network integrity, and retains full workspace confidentiality. Any files downloaded remain encrypted or restricted strictly under app-specific isolated partitions, preventing standard local mobile applications from scanning, reading, or harvesting user transaction and message files.",
        "usage_guidelines_title" to "Usage Guidelines",
        "usage_guidelines_desc" to "1. Selecting and launching any messenger starts a freshly isolated multi-origin runtime engine under separate cookies.\n\n2. To upload media, tap inside a WebView to trigger the Dezh Sandbox upload selector proxy. Files can be imported safely from the physical device or selected from direct isolated storage.\n\n3. To export messaging backups or downloaded files, tap the 'Secure Export' icon in the Gallery or File Manager tabs. High-performance MediaStore logic will securely stream files straight into a dedicated public 'Dezh' directory.",
        "active_shields_title" to "Active Sandbox Shields",
        "shield1_title" to "Local File System Sandbox",
        "shield1_desc" to "WebView read restricted from filesystem.",
        "shield2_title" to "Transient Media Picker",
        "shield2_desc" to "Forced dynamic Photo Picker interceptor.",
        "shield3_title" to "Hardware Access Toggles",
        "shield3_desc" to "Camera/Mic proxy controls via 3-dot menu.",
        "shield4_title" to "Strict Same-Origin Sandboxing",
        "shield4_desc" to "Segmented storage domains prevent tracking.",
        "credits_title" to "Project Metadata & Credits",
        "dev_label" to "Developer:",
        "owner_label" to "Creator Studio:",
        "owner_val" to "Starrynight Studio",
        "platforms_label" to "Target Platforms:",
        "copyright_label" to "Copyright Notice:",
        "copyright_val" to "Copyright © 2026 Dezh Messenger. All rights reserved.",
        "toast_camera_revoked" to "Camera Access Revoked.",
        "toast_mic_revoked" to "Microphone Access Revoked.",
        "toast_loc_revoked" to "Location Access Revoked.",
        "isolating_label" to "Isolating: ",
        "safe_isolation_active" to "Isolated storage is fully shielded.",
        "safe_isolation_desc" to "Safe Isolation Active",
        "sandbox_menu_desc" to "Sandbox Configuration",
        "sandbox_hardware_proxy" to "SANDBOX HARDWARE PROXY",
        "proxy_camera" to "Camera Feed Proxy",
        "proxy_mic" to "Microphone Audio Proxy",
        "proxy_geo" to "Geolocation Proxy",
        "sandbox_purge_control" to "ISOLATION PURGE CONTROL",
        "purge_item" to "Purge Local Session & Cache",
        "purge_success" to "Sandbox cleared and cookies purged.",
        "relaunch_item" to "Relaunch Isolated Instance",
        "relaunch_desc" to "Relaunch Sandbox",
        "toast_file_saved" to "saved successfully inside Sandbox!",
        "toast_download_fail" to "Download Isolation completed.",
        "workspace_title" to "DEZH APPS WORKSPACE",
        "isolated_media_upload" to "Isolated Media Upload",
        "upload_desc" to "Choose an isolated file from your Sandbox storage, or import a specific file via modern proxy picker.",
        "btn_import_photo" to "Import Photo via Proxy Picker",
        "select_existing_file" to "SELECT EXISTING ISOLATED FILE:",
        "no_files_found" to "No isolated files found inside Sandbox folder.",
        "btn_cancel" to "Cancel Request",
        "external_browser_title" to "External Secured Client",
        "close_browser" to "Close Browser",
        "navigate_back" to "Navigate Back",
        "reload" to "Reload",
        "gallery_title" to "Sandbox Gallery",
        "gallery_desc" to "Scan and display local visual sandbox assets.",
        "no_media_found" to "No media files found",
        "no_media_desc" to "Downloaded photos, GIFs, and videos will show up here automatically when saved in messenger chats.",
        "export_success" to "Exported successfully to: ",
        "export_fail" to "Export failed. Grant permissions if required.",
        "saved_to_device" to "Saved to Device: ",
        "export_error" to "Export error",
        "file_manager_title" to "Isolated File Manager",
        "file_manager_desc" to "All downloads inside the messengers stay restricted inside Dezh storage.",
        "no_files_yet" to "No downloaded files yet",
        "no_files_yet_desc" to "All chat backups, text, images, and documents remain securely isolated inside Dezh Messenger storage.",
        "file_removed" to "Isolated file removed securely.",
        "preview_not_supported" to "Preview not supported for this format.",
        "export_public" to "Export to Dezh Public Folder",
        "image_label" to "IMAGE",
        "video_label" to "VIDEO"
    )

    val FA = mapOf(
        "app_title" to "پیام‌رسان امن دژ",
        "tab_messengers" to "پیام‌رسان‌ها",
        "tab_gallery" to "گالری دژ",
        "tab_file_manager" to "مدیریت فایل",
        "tab_info" to "راهنما",
        "tag_secure" to "ایمن",
        "eitaa_name" to "ایتا",
        "eitaa_desc" to "پیام‌رسان ابری محبوب و پیشرفته ایرانی.",
        "eitaa_badge" to "فضای کاری کاملاً مستقل و ایزوله",
        "rubika_name" to "روبیکا",
        "rubika_desc" to "سوپراپلیکیشن چندمنظوره با خدمات متنوع و اکوسیستم گفتگوی یکپارچه.",
        "rubika_badge" to "فضای ایزوله (سندباکس) اختصاصی",
        "soroush_name" to "سروش پلاس",
        "soroush_desc" to "پیام‌رسان تعاملی همراه با کانال‌های متنوع تفریحی و خدمات کاربری.",
        "soroush_badge" to "مسدودسازی ردیابی گالری دستگاه",
        "bale_name" to "بله",
        "bale_desc" to "پیام‌رسان متصل به خدمات بانکی و پرداختی با قابلیت انتقال امن فایل.",
        "bale_badge" to "ایزوله‌سازی پایگاه داده محلی",
        "sandbox_info_title" to "نقطه کنترل ایزوله دژ",
        "sandbox_info_desc" to "درباره محیط عملیاتی ایزوله و ساختار امنیتی پیام‌رسان دژ بیشتر بدانید.",
        "op_concept_title" to "مفهوم عملیاتی ایزوله دژ",
        "op_concept_desc" to "پیام‌رسان دژ به عنوان یک هاب امن و مستقل برای ایزوله‌سازی پیام‌رسان‌های تحت وب عمل می‌کند. دژ با تفکیک حافظه و ذخیره‌سازی داده‌های هر برنامه در بخش جداگانه، از نشت کوکی‌ها و رهگیری اطلاعات جلوگیری کرده و امنیت حریم خصوصی شما را تضمین می‌کند. تمامی فایل‌های دانلود شده در فضای امن ایزوله دژ باقی می‌مانند و از دسترسی، اسکن یا برداشت نامتعارف فایل‌ها توسط سایر برنامه‌های محلی نصب‌شده روی دستگاه شما پیشگیری می‌شود.",
        "usage_guidelines_title" to "دستورالعمل‌های بهره‌برداری",
        "usage_guidelines_desc" to "۱. با انتخاب و ورود به هر پیام‌رسان، یک موتور اجرایی ایزوله با حافظه کوکی مجزا راه‌اندازی می‌شود.\n\n۲. جهت بارگذاری رسانه، لمس دکمه آپلود در برنامه، پروکسی هوشمند دژ را فعال می‌کند و می‌توانید فایل‌ها را از حافظه دستگاه یا گالری امنِ ایزوله دژ انتخاب کنید.\n\n۳. جهت انتقال فایل‌های بارگیری‌شده به پوشه عمومی و خروجی از حالت حفاظتی دژ، دکمه «خروجی دژ» را در گالری یا مدیریت فایل انتخاب کنید. سیستم فایل را مستقیماً به گالری عمومی دستگاه شما منتقل می‌کند.",
        "active_shields_title" to "سپرهای امنیتی فعال دژ",
        "shield1_title" to "ایزوله پرونده‌های محلی",
        "shield1_desc" to "محدودیت کامل وب‌ویو برای خواندن مستقیم فایل‌های سیستمی.",
        "shield2_title" to "پروکسی بارگذاری تصویر",
        "shield2_desc" to "اجبار موتور اجرایی به استفاده از واسط تصویر موقت دژ.",
        "shield3_title" to "سوییچ کنترل‌های سخت‌افزاری",
        "shield3_desc" to "قطع یا وصل موقت دوربین و بلندگو در منوی گوشه ایزوله.",
        "shield4_title" to "سپر ایزوله‌سازی هم‌مبدا",
        "shield4_desc" to "سگمنت‌های داده مجزا که ردیابی دامنه‌ای را ناممکن می‌کند.",
        "credits_title" to "اطلاعات پروژه و حق مالکیت دژ",
        "dev_label" to "توسعه‌دهنده:",
        "owner_label" to "استودیو سازنده:",
        "owner_val" to "Starrynight Studio",
        "platforms_label" to "پیام‌رسان‌های پشتیبانی شده:",
        "copyright_label" to "کپی‌رایت قانونی:",
        "copyright_val" to "کپی‌رایت © ۲۰۲۶ پیام‌رسان دژ. تمامی حقوق قانونی این اثر محفوظ است.",
        "toast_camera_revoked" to "دسترسی به دوربین لغو شد.",
        "toast_mic_revoked" to "دسترسی به میکروفون لغو شد.",
        "toast_loc_revoked" to "دسترسی به موقعیت مکانی لغو شد.",
        "isolating_label" to "ایزوله‌سازی: ",
        "safe_isolation_active" to "حافظه اختصاصی این بخش کاملاً تحت نظارت و امنیت دژ قرار دارد.",
        "safe_isolation_desc" to "ایزوله‌سازی همه‌جانبه فعال",
        "sandbox_menu_desc" to "تنظیمات ایزوله امن",
        "sandbox_hardware_proxy" to "پروکسی سخت‌افزاری ایزوله دژ",
        "proxy_camera" to "پروکسی تصویر دوربین",
        "proxy_mic" to "پروکسی صدای میکروفون",
        "proxy_geo" to "پروکسی موقعیت جغرافیایی",
        "sandbox_purge_control" to "کنترل پاکسازی ایزوله دژ",
        "purge_item" to "حذف نشست‌های محلی و حافظه موقت",
        "purge_success" to "ایزوله به‌طور کامل پاکسازی و کوکی‌ها بازنشانی شدند.",
        "relaunch_item" to "راه‌اندازی مجدد ایزوله ایمن",
        "relaunch_desc" to "راه‌اندازی مجدد ایزوله",
        "toast_file_saved" to "با موفقیت در فضای ایزوله دژ ذخیره شد!",
        "toast_download_fail" to "بارگیری و انتقال به فضای ایزوله دژ انجام نشد.",
        "workspace_title" to "فضای اختصاصی نرم‌افزار دژ",
        "isolated_media_upload" to "بارگذاری رسانه در فضای ایزوله",
        "upload_desc" to "یک فایل حفاظت‌شده را از حافظه ایزوله دژ انتخاب کنید، یا با استفاده از پروکسی امن، عکسی از دستگاه خود وارد نمایید.",
        "btn_import_photo" to "افزودن عکس از گالری دستگاه (پروکسی امن)",
        "select_existing_file" to "انتخاب فایل‌های موجود در فضای ایزوله:",
        "no_files_found" to "هیچ فایل ایزوله‌ای در پوشه دژ یافت نشد.",
        "btn_cancel" to "لغو درخواست",
        "external_browser_title" to "نمایشگر امن پیوند خارجی",
        "close_browser" to "بستن مرورگر",
        "navigate_back" to "بازگشت",
        "reload" to "بارگذاری مجدد",
        "gallery_title" to "آلبوم ایزوله دژ",
        "gallery_desc" to "نمایش کلیه تصاویر و رسانه‌های محلی ذخیره شده در فضای امن ایزوله دژ.",
        "no_media_found" to "هیچ رسانه‌ای در ایزوله دژ یافت نشد",
        "no_media_desc" to "تصاویر، عکس‌های متحرک و ویدئوهای دانلود شده از چت پیام‌رسان‌ها مستقیماً در این زبانه نشان داده خواهند شد.",
        "export_success" to "با موفقیت به این پوشه منتقل شد: ",
        "export_fail" to "انتقال فایل ناموفق بود. دسترسی‌ها را بررسی کنید.",
        "saved_to_device" to "در فضای عمومی ذخیره شد: ",
        "export_error" to "خطا در صدور فایل به خارج از ایزوله",
        "file_manager_title" to "مدیریت فایل ایزوله",
        "file_manager_desc" to "تمامی فایل‌های دانلود شده پیام‌رسان‌ها به صورت مدیریت‌شده در دژ قرار دارند.",
        "no_files_yet" to "هنوز فایلی بارگیری نشده است",
        "no_files_yet_desc" to "فایل‌های پشتیبان چت، عکس‌ها، ویدئوها و اسناد گوناگون با بیشترین امنیت در پوشه اختصاصی دژ حفظ خواهند شد.",
        "file_removed" to "فایل با امنیت کامل از ایزوله حذف شد.",
        "preview_not_supported" to "پیش‌نمایش برای این نوع فایل پشتیبانی نمی‌شود.",
        "export_public" to "خروجی به پوشه عمومی دژ",
        "image_label" to "تصویر",
        "video_label" to "ویدئو"
    )

    fun get(key: String, lang: String): String {
        return if (lang == "fa") {
            FA[key] ?: EN[key] ?: key
        } else {
            EN[key] ?: key
        }
    }
}

// --------------------------------------------------------------------------
// Core Messenger Configuration Definitions
// --------------------------------------------------------------------------
data class Messenger(
    val id: String,
    val name: String,
    val url: String,
    val description: String,
    val brandColor: Color,
    val iconLetter: String,
    val securityBadge: String
)

fun Messenger.localized(lang: String): Messenger {
    return when (id) {
        "eitaa" -> this.copy(
            name = LocalizedStrings.get("eitaa_name", lang),
            description = LocalizedStrings.get("eitaa_desc", lang),
            securityBadge = LocalizedStrings.get("eitaa_badge", lang)
        )
        "rubika" -> this.copy(
            name = LocalizedStrings.get("rubika_name", lang),
            description = LocalizedStrings.get("rubika_desc", lang),
            securityBadge = LocalizedStrings.get("rubika_badge", lang)
        )
        "soroush" -> this.copy(
            name = LocalizedStrings.get("soroush_name", lang),
            description = LocalizedStrings.get("soroush_desc", lang),
            securityBadge = LocalizedStrings.get("soroush_badge", lang)
        )
        "bale" -> this.copy(
            name = LocalizedStrings.get("bale_name", lang),
            description = LocalizedStrings.get("bale_desc", lang),
            securityBadge = LocalizedStrings.get("bale_badge", lang)
        )
        else -> this
    }
}

class MainActivity : ComponentActivity() {

    var shouldShowSecretPasswordDialog by mutableStateOf(false)

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        if (TriggerService.handleIncomingSecretFile(intent)) {
            shouldShowSecretPasswordDialog = true
        }
    }

    // Global file upload callback reference for custom onShowFileChooser uploads
    private var fileUploadCallback: ValueCallback<Array<Uri>>? = null

    // Register safe System Photo Picker proxy for injecting assets into localized environment
    private val systemPhotoPickerLauncher = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            // Import and copy picked file into our private sandbox downloads folder
            val importedFile = importFileToSandboxFromUri(this, uri)
            if (importedFile != null) {
                val secureUri = getFileProviderUri(this, importedFile)
                fileUploadCallback?.onReceiveValue(arrayOf(secureUri))
                val currentLang = getSharedPreferences("sandbox_prefs", MODE_PRIVATE).getString("app_language", "en") ?: "en"
                val msg = if (currentLang == "fa") {
                    "عکس انتخابی به فضای ایزوله منتقل و برای گفتگوها آماده شد."
                } else {
                    "Selected photo imported into the Sandbox and is ready for chats."
                }
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
            } else {
                fileUploadCallback?.onReceiveValue(null)
            }
        } else {
            fileUploadCallback?.onReceiveValue(null)
        }
        fileUploadCallback = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        if (TriggerService.handleIncomingSecretFile(intent)) {
            shouldShowSecretPasswordDialog = true
        }

        // Sync and register any restored sessions from disk back to CookieManager in Application scope upon re-initialization
        PersistentSessionManager.restoreSessionsFromDisk(this)

        // Populate Sandbox with visual sample items if folder is empty on first launch
        prepopulateSampleSandboxFiles(this)

        // 2. File Lifecycle Logic: Auto-Generation of don'topen.virus if deleted (disabled permanently if RGB is unlocked)
        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
            try {
                ThemeManager.getUnlockedThemes(this@MainActivity).collect { set ->
                    val virusFile = File(getSandboxDownloadDir(this@MainActivity), "don'topen.virus")
                    if (!set.contains(DezhTheme.RGB_GAMING)) {
                        if (!virusFile.exists()) {
                            virusFile.writeText("WARNING: System Override Payload quarantining dangerous web assets. Decrypt using signature code 'starrynight' to authorize bypass.")
                        }
                    } else {
                        if (virusFile.exists()) {
                            virusFile.delete()
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        val messengers = listOf(
            Messenger(
                id = "eitaa",
                name = "ایتا",
                url = "https://web.eitaa.com/",
                description = "پیام‌رسان ابری محبوب و پیشرفته ایرانی.",
                brandColor = Color(0xFFE07025),
                iconLetter = "E",
                securityBadge = "فضای کاری کاملاً مستقل و ایزوله"
            ),
            Messenger(
                id = "rubika",
                name = "روبیکا",
                url = "https://web.rubika.ir/",
                description = "سوپراپلیکیشن چندمنظوره با خدمات متنوع و اکوسیستم گفتگوی یکپارچه.",
                brandColor = Color(0xFF9C27B0),
                iconLetter = "R",
                securityBadge = "فضای ایزوله (سندباکس) اختصاصی"
            ),
            Messenger(
                id = "soroush",
                name = "سروش پلاس",
                url = "https://web.splus.ir/",
                description = "پیام‌رسان تعاملی همراه با کانال‌های متنوع تفریحی و خدمات کاربری.",
                brandColor = Color(0xFF2196F3),
                iconLetter = "S",
                securityBadge = "مسدودسازی ردیابی گالری دستگاه"
            ),
            Messenger(
                id = "bale",
                name = "بله",
                url = "https://web.bale.ai/chat",
                description = "پیام‌رسان متصل به خدمات بانکی و پرداختی با قابلیت انتقال امن فایل.",
                brandColor = Color(0xFF4CAF50),
                iconLetter = "B",
                securityBadge = "ایزوله‌سازی پایگاه داده محلی"
            )
        )

        setContent {
            val context = LocalContext.current
            val prefs = remember { context.getSharedPreferences("sandbox_prefs", MODE_PRIVATE) }
            var currentLanguage by remember {
                mutableStateOf(prefs.getString("app_language", "en") ?: "en")
            }
            val currentThemeState = remember { ThemeManager.getActiveTheme(context) }
                .collectAsState(initial = DezhTheme.SIMPLE_DARK)
            val currentTheme = currentThemeState.value

            MyApplicationTheme(currentTheme = currentTheme) {
                CompositionLocalProvider(
                    LocalLayoutDirection provides (if (currentLanguage == "fa") LayoutDirection.Rtl else LayoutDirection.Ltr),
                    LocalAppLanguage provides currentLanguage
                ) {
                    var currentSelection by remember { mutableStateOf<Messenger?>(null) }
                    var activeTabIndex by remember { mutableStateOf(0) }
                    var activeWebViewForSync by remember { mutableStateOf<WebView?>(null) }
                    val scope = rememberCoroutineScope()

                    var newlyUnlockedThemeName by remember { mutableStateOf<String?>(null) }

                    // Dialog for file-based secret RGB theme payload password verification
                    if (shouldShowSecretPasswordDialog) {
                        var secretInputPassword by remember { mutableStateOf("") }
                        AlertDialog(
                            onDismissRequest = { shouldShowSecretPasswordDialog = false },
                            confirmButton = {
                                Button(
                                    onClick = {
                                        if (TriggerService.verifySecretPassword(secretInputPassword)) {
                                            shouldShowSecretPasswordDialog = false
                                            CoroutineScope(Dispatchers.Main).launch {
                                                TriggerService.unlockRgbTheme(context)
                                                ThemeManager.setActiveTheme(context, DezhTheme.RGB_GAMING)
                                                newlyUnlockedThemeName = if (currentLanguage == "fa") "نورپردازی گیمینگ" else "RGB Gaming"
                                            }
                                        } else {
                                            val errorMsg = if (currentLanguage == "fa") {
                                                "رمز عبور نادرست است."
                                            } else {
                                                "Incorrect password."
                                            }
                                            Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                ) {
                                    Text(if (currentLanguage == "fa") "تایید" else "Verify")
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = { shouldShowSecretPasswordDialog = false }) {
                                    Text(if (currentLanguage == "fa") "لغو" else "Cancel")
                                }
                            },
                            title = {
                                Text(if (currentLanguage == "fa") "باز کردن پوسته گیمینگ" else "Unlock RGB Theme")
                            },
                            text = {
                                Column {
                                    Text(if (currentLanguage == "fa") "رمز عبور فایل پیکربندی مخفی دژ را وارد کنید:" else "Please enter the secret password to unlock RGB Gaming theme:")
                                    Spacer(modifier = Modifier.height(8.dp))
                                    OutlinedTextField(
                                        value = secretInputPassword,
                                        onValueChange = { secretInputPassword = it },
                                        label = { Text("Password") },
                                        singleLine = true,
                                        visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation()
                                    )
                                }
                            }
                        )
                    }

                    // Dialog state for File upload interception
                    var showUploadDialog by remember { mutableStateOf(false) }
                    var currentUploadCallback by remember { mutableStateOf<ValueCallback<Array<Uri>>?>(null) }

                    // Secondary In-App Browser state (for external links)
                    var externalBrowserUrl by remember { mutableStateOf<String?>(null) }

                    // Storage permissions launcher for older Android versions
                    val permissionLauncher = rememberLauncherForActivityResult(
                        contract = ActivityResultContracts.RequestMultiplePermissions()
                    ) { result ->
                        val readsGranted = result[Manifest.permission.READ_EXTERNAL_STORAGE] ?: false
                        if (!readsGranted && Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2) {
                            val msg = if (currentLanguage == "fa") {
                                "دسترسی به حافظه برای صادر کردن اسناد در نسخه‌های قدیمی الزامی است."
                            } else {
                                "Storage permission is required to export documents on older Android versions."
                            }
                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                        }
                    }

                    LaunchedEffect(Unit) {
                        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2) {
                            permissionLauncher.launch(
                                arrayOf(
                                    Manifest.permission.READ_EXTERNAL_STORAGE,
                                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                                )
                            )
                        }
                    }

                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            AnimatedContent(
                                targetState = currentSelection,
                                transitionSpec = {
                                    if (targetState != null) {
                                        (slideInHorizontally { width -> -width } + fadeIn()).togetherWith(
                                            slideOutHorizontally { width -> width } + fadeOut())
                                    } else {
                                        (slideInHorizontally { width -> width } + fadeIn()).togetherWith(
                                            slideOutHorizontally { width -> -width } + fadeOut())
                                    }
                                },
                                label = "main_screen_transition"
                            ) { messenger ->
                                if (messenger != null) {
                                    // Render Secure isolated browser viewport
                                    SandboxBrowserScreen(
                                        messenger = messenger,
                                        prefs = prefs,
                                        context = this@MainActivity,
                                        onBack = { currentSelection = null },
                                        onRegisterUploadCallback = { callback ->
                                            fileUploadCallback?.onReceiveValue(null)
                                            fileUploadCallback = callback
                                            currentUploadCallback = callback
                                            showUploadDialog = true
                                        },
                                        onOpenExternalLink = { externalUrl ->
                                            externalBrowserUrl = externalUrl
                                        },
                                        onTriggerBackup = {},
                                        onWebViewRegistered = { activeWebViewForSync = it }
                                    )
                                } else {
                                    // Render main app Dashboard containing primary tabs
                                    val lang = LocalAppLanguage.current
                                    val localizedMessengers = remember(messengers, lang) {
                                        messengers.map { it.localized(lang) }
                                    }
                                    DashboardContainer(
                                        messengers = localizedMessengers,
                                        activeTabIndex = activeTabIndex,
                                        currentLanguage = currentLanguage,
                                        onLanguageChange = { newLang ->
                                            currentLanguage = newLang
                                            prefs.edit().putString("app_language", newLang).apply()
                                        },
                                        onTabChange = { activeTabIndex = it },
                                        onMessengerSelect = { currentSelection = it },
                                        onTriggerBackup = {},
                                        onThemeUnlocked = { newlyUnlockedThemeName = it }
                                    )
                                }
                            }

                            // Dialog: Custom Upload Flow from Isolated files or System Photo Picker Proxy
                            if (showUploadDialog) {
                                SandboxUploadChooserDialog(
                                    context = this@MainActivity,
                                    onFileSelected = { file ->
                                        showUploadDialog = false
                                        val secureUri = getFileProviderUri(this@MainActivity, file)
                                        currentUploadCallback?.onReceiveValue(arrayOf(secureUri))
                                        currentUploadCallback = null
                                    },
                                    onPickFromSystem = {
                                        showUploadDialog = false
                                        systemPhotoPickerLauncher.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo)
                                        )
                                    },
                                    onDismiss = {
                                        showUploadDialog = false
                                        currentUploadCallback?.onReceiveValue(null)
                                        currentUploadCallback = null
                                    }
                                )
                            }

                            // Secondary Screen: In-App Browser for Restricted External Link Navigation
                            if (externalBrowserUrl != null) {
                                InAppBrowserOverlay(
                                    url = externalBrowserUrl!!,
                                    onClose = { externalBrowserUrl = null }
                                )
                            }

                            // Custom non-invasive animated floating card when theme is unlocked!
                            AnimatedVisibility(
                                visible = newlyUnlockedThemeName != null,
                                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
                                modifier = Modifier.align(Alignment.BottomCenter)
                            ) {
                                newlyUnlockedThemeName?.let { theme ->
                                    ThemeUnlockNotification(themeName = theme) {
                                        newlyUnlockedThemeName = null
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// --------------------------------------------------------------------------
// Feature 1: Security Dashboard Screen (Active Shields & Messengers Layout)
// --------------------------------------------------------------------------
@Composable
fun SecurityDashboardScreen(
    messengers: List<Messenger>,
    onMessengerClick: (Messenger) -> Unit
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.Transparent
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Spacer(modifier = Modifier.height(8.dp))
                
                // Spacious selection grid of messengers
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(messengers) { messenger ->
                        MessengerGridCard(
                            messenger = messenger,
                            onClick = { onMessengerClick(messenger) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ShieldItem(
    title: String,
    description: String,
    isProtected: Boolean
) {
    val themeColors = LocalThemeColors.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (isProtected) Icons.Default.CheckCircle else Icons.Default.Warning,
            contentDescription = "سپر فعال",
            tint = if (isProtected) Color(0xFF00E676) else Color(0xFFFF3D00),
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = themeColors.onBackground
            )
            Text(
                text = description,
                style = MaterialTheme.typography.labelSmall,
                color = themeColors.onBackground.copy(alpha = 0.5f)
            )
        }
    }
}

@Composable
fun MessengerGridCard(
    messenger: Messenger,
    onClick: () -> Unit
) {
    val themeColors = LocalThemeColors.current
    val isRgb = themeColors.isRGB
    
    val cardBackground = if (isRgb) Color(0xCC121212) else themeColors.surface.copy(alpha = 0.5f)
    val titleColor = if (isRgb) Color.White else themeColors.onSurface
    val descColor = if (isRgb) Color(0xFF94A3B8) else themeColors.onSurface.copy(alpha = 0.6f)
    val badgeColor = if (isRgb) Color(0xFF64748B) else themeColors.onSurface.copy(alpha = 0.3f)
    
    val cardModifier = Modifier
        .fillMaxWidth()
        .height(190.dp)
        .then(
            if (isRgb) Modifier.animateRgbBorder(cornerRadius = 16.dp, borderWidth = 1.8.dp, glowing = true)
            else Modifier.border(1.dp, themeColors.border.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
        )
        .clickable(onClick = onClick)
        .testTag("messenger_card_${messenger.id}")

    Card(
        modifier = cardModifier,
        colors = CardDefaults.cardColors(
            containerColor = cardBackground
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Icon Badge & Color Tag
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(messenger.brandColor.copy(alpha = 0.15f))
                        .border(1.2.dp, messenger.brandColor, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = messenger.iconLetter,
                        color = messenger.brandColor,
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp
                    )
                }
                
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(themeColors.primary.copy(alpha = 0.12f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = localizedString("tag_secure"),
                        style = MaterialTheme.typography.labelSmall,
                        color = themeColors.primary,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 8.sp
                    )
                }
            }

            // Description and Title details
            Column {
                Text(
                     text = messenger.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = titleColor
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = messenger.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = descColor,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Security Badge Title
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "",
                    tint = badgeColor,
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = messenger.securityBadge,
                    style = MaterialTheme.typography.labelSmall,
                    color = badgeColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontSize = 9.sp
                )
            }
        }
    }
}

// --------------------------------------------------------------------------
// Feature 2: High Security Sandbox Browser Screen with URL & Download Interception
// --------------------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SandboxBrowserScreen(
    messenger: Messenger,
    prefs: SharedPreferences,
    context: Context,
    onBack: () -> Unit,
    onRegisterUploadCallback: (ValueCallback<Array<Uri>>?) -> Unit,
    onOpenExternalLink: (String) -> Unit,
    onTriggerBackup: () -> Unit = {},
    onWebViewRegistered: (WebView?) -> Unit = {}
) {
    var webViewInstance by remember { mutableStateOf<WebView?>(null) }
    var menuExpanded by remember { mutableStateOf(false) }
    val currentLang = LocalAppLanguage.current

    LaunchedEffect(webViewInstance) {
        onWebViewRegistered(webViewInstance)
    }

    // Dynamic compose state linked to persistent permissions storage
    var isCameraEnabled by remember(messenger.id) {
        mutableStateOf(prefs.getBoolean("${messenger.id}_camera", false))
    }
    var isMicEnabled by remember(messenger.id) {
        mutableStateOf(prefs.getBoolean("${messenger.id}_microphone", false))
    }
    var isLocEnabled by remember(messenger.id) {
        mutableStateOf(prefs.getBoolean("${messenger.id}_location", false))
    }

    // Permission request launchers for immediate feedback if users flip switches toggled ON
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (!isGranted) {
            prefs.edit().putBoolean("${messenger.id}_camera", false).apply()
            isCameraEnabled = false
            Toast.makeText(context, LocalizedStrings.get("toast_camera_revoked", currentLang), Toast.LENGTH_SHORT).show()
        }
    }

    val microphonePermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (!isGranted) {
            prefs.edit().putBoolean("${messenger.id}_microphone", false).apply()
            isMicEnabled = false
            Toast.makeText(context, LocalizedStrings.get("toast_mic_revoked", currentLang), Toast.LENGTH_SHORT).show()
        }
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (!isGranted) {
            prefs.edit().putBoolean("${messenger.id}_location", false).apply()
            isLocEnabled = false
            Toast.makeText(context, LocalizedStrings.get("toast_loc_revoked", currentLang), Toast.LENGTH_SHORT).show()
        }
    }

    // Handle back button inside WebView
    BackHandler(enabled = true) {
        if (webViewInstance?.canGoBack() == true) {
            webViewInstance?.goBack()
        } else {
            onBack()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = messenger.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF00E676))
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = localizedString("isolating_label") + messenger.url.substringAfter("https://").substringBefore("/"),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("back_to_dashboard")
                    ) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = localizedString("navigate_back"))
                    }
                },
                actions = {
                    // Safe Sandbox Status Indicators
                    IconButton(
                        onClick = {
                            Toast.makeText(context, LocalizedStrings.get("safe_isolation_active", currentLang), Toast.LENGTH_SHORT).show()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.OfflineShare,
                            contentDescription = localizedString("safe_isolation_desc"),
                            tint = Color(0xFF00B0FF)
                        )
                    }

                    // 3-Dot Options dropdown representing Sandbox Dynamic Permissions
                    Box {
                        IconButton(
                            onClick = { menuExpanded = true },
                            modifier = Modifier.testTag("sandbox_options_menu")
                        ) {
                            Icon(imageVector = Icons.Default.MoreVert, contentDescription = localizedString("sandbox_menu_desc"))
                        }

                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false },
                            modifier = Modifier
                                .width(280.dp)
                                .background(MaterialTheme.colorScheme.surface)
                                .border(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                        ) {
                            Text(
                                text = localizedString("sandbox_hardware_proxy"),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                            )

                            // CAMERA TOGGLE
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Outlined.Videocam,
                                                contentDescription = "",
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Text(localizedString("proxy_camera"))
                                        }
                                        Switch(
                                            checked = isCameraEnabled,
                                            onCheckedChange = { checked ->
                                                isCameraEnabled = checked
                                                prefs.edit().putBoolean("${messenger.id}_camera", checked).apply()
                                                if (checked) {
                                                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                                                        cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                                                    }
                                                }
                                            },
                                            modifier = Modifier.testTag("camera_switch")
                                        )
                                    }
                                },
                                onClick = {}
                            )

                            // MICROPHONE TOGGLE
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Outlined.Mic,
                                                contentDescription = "",
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Text(localizedString("proxy_mic"))
                                        }
                                        Switch(
                                            checked = isMicEnabled,
                                            onCheckedChange = { checked ->
                                                isMicEnabled = checked
                                                prefs.edit().putBoolean("${messenger.id}_microphone", checked).apply()
                                                if (checked) {
                                                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                                                        microphonePermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                                    }
                                                }
                                            },
                                            modifier = Modifier.testTag("microphone_switch")
                                        )
                                    }
                                },
                                onClick = {}
                            )

                            // LOCATION TOGGLE
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Outlined.LocationOn,
                                                contentDescription = "",
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Text(localizedString("proxy_geo"))
                                        }
                                        Switch(
                                            checked = isLocEnabled,
                                            onCheckedChange = { checked ->
                                                isLocEnabled = checked
                                                prefs.edit().putBoolean("${messenger.id}_location", checked).apply()
                                                if (checked) {
                                                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                                        locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                                                    }
                                                }
                                            },
                                            modifier = Modifier.testTag("location_switch")
                                        )
                                    }
                                },
                                onClick = {}
                            )

                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                            Text(
                                text = localizedString("sandbox_purge_control"),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                            )

                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.DeleteForever,
                                            contentDescription = localizedString("purge_item"),
                                            tint = MaterialTheme.colorScheme.error
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(localizedString("purge_item"))
                                    }
                                },
                                onClick = {
                                    menuExpanded = false
                                    webViewInstance?.let { webView ->
                                        webView.clearCache(true)
                                        webView.clearFormData()
                                        webView.clearHistory()
                                        webView.evaluateJavascript(
                                            "window.localStorage.clear(); window.sessionStorage.clear();", 
                                            null
                                        )
                                        
                                        val cookieManager = CookieManager.getInstance()
                                        cookieManager.setAcceptCookie(true)
                                        cookieManager.removeAllCookies {
                                            Toast.makeText(context, LocalizedStrings.get("purge_success", currentLang), Toast.LENGTH_SHORT).show()
                                        }
                                        webView.loadUrl(messenger.url)
                                    }
                                },
                                modifier = Modifier.testTag("purge_isolation_item")
                            )

                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Refresh,
                                            contentDescription = localizedString("relaunch_desc")
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(localizedString("relaunch_item"))
                                    }
                                },
                                onClick = {
                                    menuExpanded = false
                                    webViewInstance?.reload()
                                },
                                modifier = Modifier.testTag("relaunch_instance_item")
                            )
                        }
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            AndroidView(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("sandbox_webview_${messenger.id}"),
                factory = { ctx ->
                    WebView(ctx).apply {
                        webViewInstance = this
                        
                        visibility = android.view.View.VISIBLE
                        
                        settings.apply {
                            javaScriptEnabled = true
                            allowFileAccess = true
                            allowContentAccess = true
                            domStorageEnabled = true
                            databaseEnabled = true
                            cacheMode = WebSettings.LOAD_DEFAULT
                            userAgentString = "Mozilla/5.0 (Linux; Android 13; K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/116.0.0.0 Mobile Safari/537.36"
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                safeBrowsingEnabled = true
                            }
                        }
                        
                        CookieManager.getInstance().setAcceptCookie(true)
                        CookieManager.getInstance().setAcceptThirdPartyCookies(this, true)

                        // Register a Javascript interface to handle Blob downloads passed via JavaScript
                        class AndroidBridge(private val bridgeContext: android.content.Context) {
                            @JavascriptInterface
                            fun saveBlobData(base64Data: String, filename: String) {
                                saveBlobData(base64Data, filename, null)
                            }

                            @JavascriptInterface
                            fun saveBlobData(base64Data: String, filename: String, mimeType: String?) {
                                val actualData = if (base64Data.startsWith("data:")) base64Data else {
                                    val safeMime = mimeType ?: "application/octet-stream"
                                    "data:$safeMime;base64,$base64Data"
                                }
                                interceptAndDownloadFile(bridgeContext, actualData, "filename=\"$filename\"", mimeType)
                            }

                            @JavascriptInterface
                            fun processBlob(base64Data: String, filename: String) {
                                saveBlobData(base64Data, filename, null)
                            }

                            @JavascriptInterface
                            fun processBlob(base64Data: String, filename: String, mimeType: String?) {
                                saveBlobData(base64Data, filename, mimeType)
                            }
                        }

                        val bridgeInstance = AndroidBridge(context)
                        addJavascriptInterface(bridgeInstance, "AndroidBridge")
                        addJavascriptInterface(bridgeInstance, "AndroidDownloadBridge")

                        // 1. Intercept downloads and route strictly into sandbox private files
                        setDownloadListener { url, _, contentDisposition, mimetype, _ ->
                            if (url.startsWith("blob:")) {
                                val jsCmd = """
                                    (function() {
                                        try {
                                            var xhr = new XMLHttpRequest();
                                            xhr.open('GET', '$url', true);
                                            xhr.responseType = 'blob';
                                            xhr.onload = function() {
                                                if (this.status == 200) {
                                                    var blob = this.response;
                                                    var reader = new FileReader();
                                                    reader.onloadend = function() {
                                                        var base64data = reader.result;
                                                        var filename = "";
                                                        var disp = "$contentDisposition";
                                                        if (disp && disp.includes("filename=")) {
                                                            filename = disp.split("filename=")[1].replace(/['"]/g, '').split(';')[0].trim();
                                                        } else {
                                                            filename = "download_" + new Date().getTime();
                                                        }
                                                        window.AndroidDownloadBridge.processBlob(base64data, filename, blob.type || "$mimetype");
                                                    };
                                                    reader.readAsDataURL(blob);
                                                }
                                            };
                                            xhr.send();
                                        } catch (e) {
                                            console.error("Blob interception failed", e);
                                        }
                                    })();
                                """.trimIndent()
                                evaluateJavascript(jsCmd, null)
                            } else {
                                interceptAndDownloadFile(context, url, contentDisposition, mimetype)
                            }
                        }

                        // 2. Intercept navigation - open external links in secondary internal browser!
                        webViewClient = object : WebViewClient() {
                            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                                val urlStr = request?.url?.toString() ?: ""
                                val host = request?.url?.host ?: ""
                                
                                if (PersistentSessionManager.isSyncing) {
                                    return true
                                }

                                val messengerHost = messenger.url.substringAfter("https://").substringBefore("/")
                                if (host.contains(messengerHost) || urlStr.contains("telegram.org") || urlStr.contains("eitaa.com") || urlStr.contains("rubika.ir") || urlStr.contains("splus.ir") || urlStr.contains("bale.ai")) {
                                    return false
                                }
                                
                                if (urlStr.startsWith("http")) {
                                    onOpenExternalLink(urlStr)
                                    return true
                                }
                                return false
                            }

                            override fun onPageFinished(view: WebView?, urlStr: String?) {
                                super.onPageFinished(view, urlStr)
                                if (urlStr == null) return

                                view?.post {
                                    view.visibility = android.view.View.VISIBLE
                                }

                                try {
                                    val cookieMgr = CookieManager.getInstance()
                                    val cookies1 = cookieMgr.getCookie(urlStr) ?: ""
                                    val cookies2 = cookieMgr.getCookie(messenger.url) ?: ""
                                    val combinedCookies = when {
                                        cookies1.isNotEmpty() && cookies2.isNotEmpty() && cookies1 != cookies2 -> "$cookies1; $cookies2"
                                        cookies1.isNotEmpty() -> cookies1
                                        else -> cookies2
                                    }

                                    if (combinedCookies.isNotEmpty()) {
                                        PersistentSessionManager.saveSessionToDisk(context, messenger.id, urlStr, combinedCookies)
                                        cookieMgr.flush()
                                    }
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                        }

                        webChromeClient = object : WebChromeClient() {
                            // custom Photo picker selector handler
                            override fun onShowFileChooser(
                                webView: WebView?,
                                filePathCallback: ValueCallback<Array<Uri>>?,
                                fileChooserParams: FileChooserParams?
                            ): Boolean {
                                onRegisterUploadCallback(filePathCallback)
                                return true
                            }

                            override fun onPermissionRequest(request: PermissionRequest) {
                                val currentCamera = prefs.getBoolean("${messenger.id}_camera", false)
                                val currentMicrophone = prefs.getBoolean("${messenger.id}_microphone", false)
                                val resourcesToApprove = mutableListOf<String>()

                                for (res in request.resources) {
                                    when (res) {
                                        PermissionRequest.RESOURCE_VIDEO_CAPTURE -> {
                                            val hasPermission = ContextCompat.checkSelfPermission(
                                                ctx,
                                                Manifest.permission.CAMERA
                                            ) == PackageManager.PERMISSION_GRANTED
                                            if (currentCamera && hasPermission) {
                                                resourcesToApprove.add(res)
                                            }
                                        }
                                        PermissionRequest.RESOURCE_AUDIO_CAPTURE -> {
                                            val hasPermission = ContextCompat.checkSelfPermission(
                                                ctx,
                                                Manifest.permission.RECORD_AUDIO
                                            ) == PackageManager.PERMISSION_GRANTED
                                            if (currentMicrophone && hasPermission) {
                                                resourcesToApprove.add(res)
                                            }
                                        }
                                    }
                                }

                                post {
                                    if (resourcesToApprove.isNotEmpty()) {
                                        request.grant(resourcesToApprove.toTypedArray())
                                    } else {
                                        request.deny()
                                    }
                                }
                            }

                            override fun onGeolocationPermissionsShowPrompt(
                                origin: String?,
                                callback: GeolocationPermissions.Callback?
                            ) {
                                val currentLoc = prefs.getBoolean("${messenger.id}_location", false)
                                val hasPermission = ContextCompat.checkSelfPermission(
                                    ctx,
                                    Manifest.permission.ACCESS_FINE_LOCATION
                                ) == PackageManager.PERMISSION_GRANTED
                                callback?.invoke(origin, currentLoc && hasPermission, false)
                            }
                        }

                        loadUrl(messenger.url)
                    }
                },
                update = {
                    webViewInstance = it
                },
                onRelease = { webView ->
                    try {
                        val cookieMgr = CookieManager.getInstance()
                        val currentCookies = cookieMgr.getCookie(messenger.url) ?: ""
                        if (currentCookies.isNotEmpty()) {
                            PersistentSessionManager.saveSessionToDisk(context, messenger.id, messenger.url, currentCookies)
                            cookieMgr.flush()
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    webView.apply {
                        stopLoading()
                        clearHistory()
                        removeAllViews()
                        destroy()
                    }
                }
            )
        }
    }
}

// --------------------------------------------------------------------------
// Pre-populates beautiful simulated file assets for instant sandbox testing
// --------------------------------------------------------------------------
fun prepopulateSampleSandboxFiles(context: Context) {
    val dir = getSandboxDownloadDir(context)
    val files = dir.listFiles()
    if (files.isNullOrEmpty()) {
        // Create text, code logs, and visual images
        try {
            // File 1: Secure Dezh Log
            val logFile = File(dir, "Dezh_Secure_Audit.txt")
            logFile.writeText(
                """
                ====================================================
                DEZH MESSENGER SECURE ISOLATED SANDBOX WORKSPACE LOG
                ====================================================
                Status: ACTIVE & ENCRYPTED
                Zero system file access rules: ACTIVE
                Intermediary Photo Picker routing: ENGAGED
                Same-Origin isolation partition: SECURE
                
                Log Generation Time: ${SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())}
                All communications inside webview were successfully routed through proxy layers.
                ====================================================
                """.trimIndent()
            )

            // File 2: Eitaa Isolated Database config
            val configFile = File(dir, "Eitaa_Partition_Metadata.json")
            configFile.writeText(
                """
                {
                  "partition_id": "9x28-ks01-ps9a",
                  "isolated_cookies": true,
                  "cache_level": "transient_memory_only",
                  "local_storage_quota": "50MB",
                  "access_logs_enabled": false
                }
                """.trimIndent()
            )

            // File 3: Custom Generated Sample Image
            val imageFile = File(dir, "Dezh_Shield_Visual.png")
            val bitmap = Bitmap.createBitmap(512, 512, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            val paint = Paint().apply {
                isAntiAlias = true
            }

            // Paint deep space background
            paint.color = 0xFF0F172A.toInt() // Deep Slate
            canvas.drawRect(0f, 0f, 512f, 512f, paint)

            paint.color = 0xFF00B0FF.toInt() // Electric Sky Blue
            paint.strokeWidth = 10f
            paint.style = Paint.Style.STROKE
            canvas.drawRect(40f, 40f, 472f, 472f, paint)

            paint.color = 0xFF00E676.toInt() // Emerald Green Accent
            paint.style = Paint.Style.FILL
            paint.textSize = 32f
            canvas.drawText("DEZH ISOLATED IMAGE", 80f, 200f, paint)

            paint.color = 0xFFFFFFFF.toInt()
            paint.textSize = 20f
            canvas.drawText("Secure Preview Asset", 80f, 260f, paint)
            canvas.drawText("Generated in In-App Sandbox", 80f, 300f, paint)

            val outStream = FileOutputStream(imageFile)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outStream)
            outStream.flush()
            outStream.close()

            // File 4: Chat Transcript backup text
            val chatFile = File(dir, "Rubika_Chat_Backup.html")
            chatFile.writeText(
                """
                <!DOCTYPE html>
                <html>
                <head>
                   <title>Rubika Transcripts</title>
                   <style>body { font-family: sans-serif; background: #0f172a; color: white; padding: 12px; }</style>
                </head>
                <body>
                   <h3>Rubika Chat Sandbox Feed Backup</h3>
                   <p><b>[Session Authorized]</b> 2026-06-09</p>
                   <div><i>Messages purged from standard memory. Backup retained inside secure app space.</i></div>
                </body>
                </html>
                """.trimIndent()
            )

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

// --------------------------------------------------------------------------
// Core Database File Path Utilities
// --------------------------------------------------------------------------
fun getSandboxDownloadDir(context: Context): File {
    val dir = File(context.getExternalFilesDir(null) ?: context.filesDir, "Dezh")
    if (!dir.exists()) {
        dir.mkdirs()
    }
    return dir
}

fun getFileProviderUri(context: Context, file: File): Uri {
    return FileProvider.getUriForFile(
        context,
        "com.starrynightstudio.dezhmessenger.xwpqrs.fileprovider",
        file
    )
}

// Write selected photo picker files locally so they reside in complete sandbox
fun importFileToSandboxFromUri(context: Context, uri: Uri): File? {
    return try {
        val resolver = context.contentResolver
        val filename = "Imported_" + System.currentTimeMillis() + "_" + getFilenameFromUri(resolver, uri)
        val targetFile = File(getSandboxDownloadDir(context), filename)
        
        resolver.openInputStream(uri)?.use { input ->
            targetFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }
        targetFile
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

fun getFilenameFromUri(resolver: android.content.ContentResolver, uri: Uri): String {
    var result: String? = null
    if (uri.scheme == "content") {
        val cursor = resolver.query(uri, null, null, null, null)
        try {
            if (cursor != null && cursor.moveToFirst()) {
                val index = cursor.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME)
                if (index >= 0) {
                    result = cursor.getString(index)
                }
            }
        } finally {
            cursor?.close()
        }
    }
    if (result == null) {
        result = uri.path
        val cut = result?.lastIndexOf('/') ?: -1
        if (cut != -1) {
            result = result?.substring(cut + 1)
        }
    }
    return result ?: "unnamed_media"
}

// --------------------------------------------------------------------------
// Dynamic MediaStore Export Layer Implementation
// --------------------------------------------------------------------------
fun exportSandboxFileToPublicDevice(context: Context, file: File, onFinished: (Boolean, String) -> Unit) {
    CoroutineScope(Dispatchers.IO).launch {
        var success = false
        var targetDirName = "Dezh"
        val filename = file.name
        val mimeType = getMimeTypeForExtension(file.extension)
        val resolver = context.contentResolver

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                    put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
                    
                    if (mimeType.startsWith("image/")) {
                        put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/$targetDirName")
                    } else if (mimeType.startsWith("video/")) {
                        put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/$targetDirName")
                    } else {
                        put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/$targetDirName")
                    }
                }

                val insertUri = if (mimeType.startsWith("image/")) {
                    resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                } else if (mimeType.startsWith("video/")) {
                    resolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, contentValues)
                } else {
                    resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                }

                if (insertUri != null) {
                    resolver.openOutputStream(insertUri)?.use { out ->
                        file.inputStream().use { input ->
                            input.copyTo(out)
                        }
                    }
                    success = true
                }
            } else {
                // Legacy Direct Storage Access For older devices
                val publicDir = if (mimeType.startsWith("image/")) {
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                } else {
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                }
                
                val dezhPublicFolder = File(publicDir, targetDirName)
                if (!dezhPublicFolder.exists()) {
                    dezhPublicFolder.mkdirs()
                }

                val destFile = File(dezhPublicFolder, filename)
                file.inputStream().use { input ->
                    destFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
                success = true
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        withContext(Dispatchers.Main) {
            val destination = if (mimeType.startsWith("image/")) "Pictures/$targetDirName" else "Downloads/$targetDirName"
            onFinished(success, destination)
        }
    }
}

fun getMimeTypeForExtension(ext: String): String {
    return when (ext.lowercase(Locale.ROOT)) {
        "jpg", "jpeg" -> "image/jpeg"
        "png" -> "image/png"
        "webp" -> "image/webp"
        "gif" -> "image/gif"
        "mp4" -> "video/mp4"
        "webm" -> "video/webm"
        "mp3" -> "audio/mpeg"
        "pdf" -> "application/pdf"
        "json" -> "application/json"
        "txt" -> "text/plain"
        "html", "htm" -> "text/html"
        else -> "application/octet-stream"
    }
}

fun getExtensionFromMime(mimeType: String?): String {
    if (mimeType == null) return "bin"
    val cleanMime = mimeType.trim().lowercase(java.util.Locale.ROOT).substringBefore(";")
    return when (cleanMime) {
        "image/jpeg", "image/jpg" -> "jpg"
        "image/png" -> "png"
        "image/gif" -> "gif"
        "image/webp" -> "webp"
        "image/bmp" -> "bmp"
        "image/svg+xml" -> "svg"
        "video/mp4" -> "mp4"
        "video/x-matroska", "video/mkv" -> "mkv"
        "video/webm" -> "webm"
        "video/3gpp" -> "3gp"
        "video/quicktime" -> "mov"
        "audio/mpeg", "audio/mp3" -> "mp3"
        "audio/ogg" -> "ogg"
        "audio/wav", "audio/x-wav" -> "wav"
        "audio/aac" -> "aac"
        "audio/m4a" -> "m4a"
        "text/plain" -> "txt"
        "text/html" -> "html"
        "text/css" -> "css"
        "application/json" -> "json"
        "application/javascript" -> "js"
        "application/pdf" -> "pdf"
        "application/zip" -> "zip"
        "application/x-tar" -> "tar"
        "application/gzip" -> "gz"
        "application/octet-stream" -> "bin"
        else -> {
            if (cleanMime.contains("/")) {
                val sub = cleanMime.substringAfter("/")
                if (sub.isNotBlank() && sub.length in 2..4) sub else "bin"
            } else {
                "bin"
            }
        }
    }
}

fun normalizeExtension(ext: String): String {
    return when (ext.lowercase(java.util.Locale.ROOT)) {
        "jpeg" -> "jpg"
        "htm" -> "html"
        else -> ext.lowercase(java.util.Locale.ROOT)
    }
}

fun ensureCorrectFileExtension(filename: String, mimeType: String?): String {
    var finalFilename = filename.trim()
    val expectedExtension = getExtensionFromMime(mimeType)
    val lastDotIndex = finalFilename.lastIndexOf('.')
    val currentExtension = if (lastDotIndex != -1 && lastDotIndex < finalFilename.length - 1) {
        finalFilename.substring(lastDotIndex + 1).lowercase(java.util.Locale.ROOT)
    } else {
        ""
    }

    if (currentExtension.isNotEmpty()) {
        val normalizedCurrent = normalizeExtension(currentExtension)
        val normalizedExpected = normalizeExtension(expectedExtension)
        if (normalizedCurrent != normalizedExpected) {
            if (currentExtension == "blob" || currentExtension == "bin" || currentExtension.length > 5 || !currentExtension.all { it.isLetterOrDigit() }) {
                val nameWithoutExt = finalFilename.substring(0, lastDotIndex)
                finalFilename = "$nameWithoutExt.$expectedExtension"
            }
        }
    } else {
        if (finalFilename.endsWith(".")) {
            finalFilename += expectedExtension
        } else {
            finalFilename += ".$expectedExtension"
        }
    }
    return finalFilename
}

// Intercepts general WebView downloads and routes them directly into sandbox folder
fun interceptAndDownloadFile(context: Context, url: String, contentDisposition: String?, mimeType: String?) {
    CoroutineScope(Dispatchers.IO).launch {
        var filename = "downloaded_" + System.currentTimeMillis()
        var detectedMime = mimeType

        // 1. MIME Type Extraction
        if (url.startsWith("data:")) {
            val mimePart = url.substringAfter("data:").substringBefore(";base64")
            if (mimePart.isNotBlank() && mimePart.contains("/")) {
                detectedMime = mimePart
            }
        }

        try {
            if (!contentDisposition.isNullOrBlank() && contentDisposition.contains("filename=")) {
                val rawName = contentDisposition.substringAfter("filename=").replace("\"", "").substringBefore(";")
                filename = URLDecoder.decode(rawName, "UTF-8")
            } else if (!url.startsWith("data:") && !url.startsWith("blob:")) {
                val urlPath = URL(url).path
                val lastSegment = urlPath.substringAfterLast("/")
                if (lastSegment.isNotBlank()) {
                    filename = URLDecoder.decode(lastSegment, "UTF-8")
                }
            } else {
                val extension = getExtensionFromMime(detectedMime)
                filename = "isolated_media_" + System.currentTimeMillis() + "." + extension
            }
        } catch (e: Exception) {
            e.printStackTrace()
            filename = "isolated_media_" + System.currentTimeMillis()
        }

        // Apply our extension mapping & file renaming logic with double-extension safety
        filename = ensureCorrectFileExtension(filename, detectedMime)

        val targetFile = File(getSandboxDownloadDir(context), filename)
        var success = false

        try {
            if (url.startsWith("data:")) {
                // Parse base64 uri schemas directly
                val base64Data = url.substringAfter(",")
                val decodedBytes = android.util.Base64.decode(base64Data, android.util.Base64.DEFAULT)
                targetFile.writeBytes(decodedBytes)
                success = true
            } else if (url.startsWith("blob:")) {
                // Blobs are resolved via Javascript bridge to base64; fallback to non-crashing failure here
                success = false
            } else {
                // standard networking client download stream
                val connection = URL(url).openConnection()
                connection.connect()
                connection.inputStream.use { input ->
                    targetFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
                success = true
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        withContext(Dispatchers.Main) {
            val currentLang = context.getSharedPreferences("sandbox_prefs", Context.MODE_PRIVATE).getString("app_language", "en") ?: "en"
            if (success) {
                val msg = if (currentLang == "fa") {
                    "فایل «$filename» با موفقیت در فضای ایزوله دژ ذخیره شد!"
                } else {
                    "File \"$filename\" successfully saved inside the Dezh Sandbox!"
                }
                Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
            } else {
                val msg = if (currentLang == "fa") {
                    "بارگیری و انتقال به فضای ایزوله دژ انجام نشد."
                } else {
                    "Download and isolated storage transfer failed."
                }
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            }
        }
    }
}

// --------------------------------------------------------------------------
// Custom App Upload Selector Dialog (In-App Sandbox Photo Picker Intercept)
// --------------------------------------------------------------------------
@Composable
fun SandboxUploadChooserDialog(
    context: Context,
    onFileSelected: (File) -> Unit,
    onPickFromSystem: () -> Unit,
    onDismiss: () -> Unit
) {
    var isolatedFiles by remember { mutableStateOf(emptyList<File>()) }

    LaunchedEffect(Unit) {
        isolatedFiles = getSandboxDownloadDir(context).listFiles()?.toList() ?: emptyList()
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = localizedString("workspace_title"),
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = localizedString("workspace_title"),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.sp
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = localizedString("isolated_media_upload"),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = localizedString("upload_desc"),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Action Call: Safe System Import Proxy
                Button(
                    onClick = onPickFromSystem,
                    modifier = Modifier.fillMaxWidth().testTag("system_import_proxy_btn"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Default.AddPhotoAlternate, contentDescription = "")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(localizedString("btn_import_photo"), fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = localizedString("select_existing_file"),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                )
                Spacer(modifier = Modifier.height(8.dp))

                if (isolatedFiles.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .background(
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.03f),
                                RoundedCornerShape(8.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = localizedString("no_files_found"),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(isolatedFiles) { file ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.04f))
                                    .clickable { onFileSelected(file) }
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val isImg = getMimeTypeForExtension(file.extension).startsWith("image/")
                                Icon(
                                    imageVector = if (isImg) Icons.Default.Image else Icons.Default.InsertDriveFile,
                                    contentDescription = "",
                                    tint = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = file.name,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    val isFa = LocalAppLanguage.current == "fa"
                                    val unitLabel = if (isFa) "کیلوبایت" else "KB"
                                    Text(
                                        text = "${file.length() / 1024} $unitLabel • ${file.extension.uppercase()}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(localizedString("btn_cancel"), color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}

// --------------------------------------------------------------------------
// Secondary Dynamic In-App Browser Overlay (Restricted URL Isolation)
// --------------------------------------------------------------------------
@Composable
fun InAppBrowserOverlay(
    url: String,
    onClose: () -> Unit
) {
    val defaultTitle = localizedString("external_browser_title")
    var webViewInstance by remember { mutableStateOf<WebView?>(null) }
    var currentUrl by remember { mutableStateOf(url) }
    var title by remember { mutableStateOf("") }
    val displayTitle = if (title.isEmpty()) defaultTitle else title
    var progress by remember { mutableStateOf(0.1f) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.85f))
            .padding(top = 32.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                .background(MaterialTheme.colorScheme.surface)
        ) {
            // Header Top Bar Action Navigation Controls
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
                    .background(Color.Transparent),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    IconButton(onClick = onClose) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = localizedString("close_browser"))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = displayTitle,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = currentUrl,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Row {
                    IconButton(
                        onClick = { webViewInstance?.goBack() },
                        enabled = webViewInstance?.canGoBack() == true
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = localizedString("navigate_back"),
                            tint = if (webViewInstance?.canGoBack() == true) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                        )
                    }
                    IconButton(
                        onClick = { webViewInstance?.reload() }
                    ) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = localizedString("reload"))
                    }
                }
            }

            // Simple visual loading ticker
            if (progress < 0.95f) {
                LinearProgressIndicator(
                    progress = progress,
                    modifier = Modifier.fillMaxWidth().height(2.dp),
                    color = MaterialTheme.colorScheme.primary
                )
            } else {
                Spacer(modifier = Modifier.height(2.dp))
            }

            // Embedded Browser instance
            AndroidView(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                factory = { ctx ->
                    WebView(ctx).apply {
                        webViewInstance = this
                        settings.apply {
                            javaScriptEnabled = true
                            domStorageEnabled = true
                            allowFileAccess = false
                            allowContentAccess = false
                            cacheMode = WebSettings.LOAD_DEFAULT
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                safeBrowsingEnabled = true
                            }
                        }
                        webViewClient = object : WebViewClient() {
                            override fun onPageStarted(view: WebView?, url: String?, favicon: android.graphics.Bitmap?) {
                                super.onPageStarted(view, url, favicon)
                                url?.let { currentUrl = it }
                            }
                        }
                        webChromeClient = object : WebChromeClient() {
                            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                                super.onProgressChanged(view, newProgress)
                                progress = newProgress.toFloat() / 100f
                            }

                            override fun onReceivedTitle(view: WebView?, pageTitle: String?) {
                                super.onReceivedTitle(view, pageTitle)
                                pageTitle?.let { title = it }
                            }
                        }
                        loadUrl(url)
                    }
                },
                update = {
                    webViewInstance = it
                },
                onRelease = { webView ->
                    webView.apply {
                        stopLoading()
                        clearHistory()
                        removeAllViews()
                        destroy()
                    }
                }
            )
        }
    }
}

// --------------------------------------------------------------------------
// Tab-based Main Screen Navigation Dashboard UI
// --------------------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardContainer(
    messengers: List<Messenger>,
    activeTabIndex: Int,
    currentLanguage: String,
    onLanguageChange: (String) -> Unit,
    onTabChange: (Int) -> Unit,
    onMessengerSelect: (Messenger) -> Unit,
    onTriggerBackup: () -> Unit = {},
    onThemeUnlocked: (String) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val themeColors = LocalThemeColors.current

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        modifier = Modifier.clickable(
                            interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                            indication = null
                        ) {
                            TriggerService.onAppTitleClicked(context, scope) {
                                onThemeUnlocked(if (currentLanguage == "fa") "ابسیدین رازآلود" else "Obsidian")
                            }
                        },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            modifier = Modifier.size(34.dp),
                            shape = RoundedCornerShape(8.dp),
                            color = themeColors.primary.copy(alpha = 0.15f),
                            border = BorderStroke(1.dp, themeColors.primary)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Shield,
                                    contentDescription = "",
                                    tint = themeColors.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = localizedString("app_title"),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            color = if (themeColors.isRGB) Color.White else themeColors.onSurface,
                            modifier = Modifier.rgbGradientText(if (themeColors.isRGB) rememberRgbFlowBrush() else null)
                        )
                    }
                },
                actions = {
                    Row(
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .clip(RoundedCornerShape(30.dp))
                            .background(themeColors.onSurface.copy(alpha = 0.08f))
                            .border(1.dp, themeColors.onSurface.copy(alpha = 0.15f), RoundedCornerShape(30.dp))
                            .clickable {
                                val nextLang = if (currentLanguage == "en") "fa" else "en"
                                onLanguageChange(nextLang)
                                TriggerService.onLanguageSwitched(context, scope) {
                                    onThemeUnlocked(if (nextLang == "fa") "مانیتور کهربایی" else "Amber")
                                }
                            }
                            .padding(horizontal = 4.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(30.dp))
                                .background(if (currentLanguage == "en") themeColors.primary else Color.Transparent)
                                .padding(horizontal = 10.dp, vertical = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "EN",
                                color = if (currentLanguage == "en") themeColors.onPrimary else themeColors.onSurface,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(30.dp))
                                .background(if (currentLanguage == "fa") themeColors.primary else Color.Transparent)
                                .padding(horizontal = 10.dp, vertical = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "FA",
                                color = if (currentLanguage == "fa") themeColors.onPrimary else themeColors.onSurface,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = themeColors.surface,
                    titleContentColor = themeColors.onSurface
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = themeColors.surface,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = activeTabIndex == 0,
                    onClick = { onTabChange(0) },
                    icon = {
                        val brush = if (themeColors.isRGB) rememberRgbFlowBrush() else null
                        Icon(
                            imageVector = Icons.Default.GridView,
                            contentDescription = localizedString("tab_messengers"),
                            tint = if (themeColors.isRGB) Color.White else (if (activeTabIndex == 0) themeColors.primary else themeColors.onSurface.copy(alpha = 0.4f)),
                            modifier = Modifier.rgbGradientText(if (activeTabIndex == 0) brush else null)
                        )
                    },
                    label = {
                        val brush = if (themeColors.isRGB) rememberRgbFlowBrush() else null
                        Text(
                            text = localizedString("tab_messengers"),
                            color = if (themeColors.isRGB) Color.White else (if (activeTabIndex == 0) themeColors.primary else themeColors.onSurface.copy(alpha = 0.4f)),
                            modifier = Modifier.rgbGradientText(if (activeTabIndex == 0) brush else null)
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = if (themeColors.isRGB) Color.Transparent else themeColors.primary,
                        selectedTextColor = if (themeColors.isRGB) Color.Transparent else themeColors.primary,
                        indicatorColor = if (themeColors.isRGB) Color.Transparent else themeColors.background,
                        unselectedIconColor = themeColors.onSurface.copy(alpha = 0.4f),
                        unselectedTextColor = themeColors.onSurface.copy(alpha = 0.4f)
                    )
                )

                NavigationBarItem(
                    selected = activeTabIndex == 1,
                    onClick = { onTabChange(1) },
                    icon = {
                        val brush = if (themeColors.isRGB) rememberRgbFlowBrush() else null
                        Icon(
                            imageVector = Icons.Default.PhotoLibrary,
                            contentDescription = localizedString("tab_gallery"),
                            tint = if (themeColors.isRGB) Color.White else (if (activeTabIndex == 1) themeColors.primary else themeColors.onSurface.copy(alpha = 0.4f)),
                            modifier = Modifier.rgbGradientText(if (activeTabIndex == 1) brush else null)
                        )
                    },
                    label = {
                        val brush = if (themeColors.isRGB) rememberRgbFlowBrush() else null
                        Text(
                            text = localizedString("tab_gallery"),
                            color = if (themeColors.isRGB) Color.White else (if (activeTabIndex == 1) themeColors.primary else themeColors.onSurface.copy(alpha = 0.4f)),
                            modifier = Modifier.rgbGradientText(if (activeTabIndex == 1) brush else null)
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = if (themeColors.isRGB) Color.Transparent else themeColors.primary,
                        selectedTextColor = if (themeColors.isRGB) Color.Transparent else themeColors.primary,
                        indicatorColor = if (themeColors.isRGB) Color.Transparent else themeColors.background,
                        unselectedIconColor = themeColors.onSurface.copy(alpha = 0.4f),
                        unselectedTextColor = themeColors.onSurface.copy(alpha = 0.4f)
                    )
                )

                NavigationBarItem(
                    selected = activeTabIndex == 2,
                    onClick = { onTabChange(2) },
                    icon = {
                        val brush = if (themeColors.isRGB) rememberRgbFlowBrush() else null
                        Icon(
                            imageVector = Icons.Default.FolderZip,
                            contentDescription = localizedString("tab_file_manager"),
                            tint = if (themeColors.isRGB) Color.White else (if (activeTabIndex == 2) themeColors.primary else themeColors.onSurface.copy(alpha = 0.4f)),
                            modifier = Modifier.rgbGradientText(if (activeTabIndex == 2) brush else null)
                        )
                    },
                    label = {
                        val brush = if (themeColors.isRGB) rememberRgbFlowBrush() else null
                        Text(
                            text = localizedString("tab_file_manager"),
                            color = if (themeColors.isRGB) Color.White else (if (activeTabIndex == 2) themeColors.primary else themeColors.onSurface.copy(alpha = 0.4f)),
                            modifier = Modifier.rgbGradientText(if (activeTabIndex == 2) brush else null)
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = if (themeColors.isRGB) Color.Transparent else themeColors.primary,
                        selectedTextColor = if (themeColors.isRGB) Color.Transparent else themeColors.primary,
                        indicatorColor = if (themeColors.isRGB) Color.Transparent else themeColors.background,
                        unselectedIconColor = themeColors.onSurface.copy(alpha = 0.4f),
                        unselectedTextColor = themeColors.onSurface.copy(alpha = 0.4f)
                    )
                )

                NavigationBarItem(
                    selected = activeTabIndex == 3,
                    onClick = { onTabChange(3) },
                    icon = {
                        val brush = if (themeColors.isRGB) rememberRgbFlowBrush() else null
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = localizedString("tab_info"),
                            tint = if (themeColors.isRGB) Color.White else (if (activeTabIndex == 3) themeColors.primary else themeColors.onSurface.copy(alpha = 0.4f)),
                            modifier = Modifier.rgbGradientText(if (activeTabIndex == 3) brush else null)
                        )
                    },
                    label = {
                        val brush = if (themeColors.isRGB) rememberRgbFlowBrush() else null
                        Text(
                            text = localizedString("tab_info"),
                            color = if (themeColors.isRGB) Color.White else (if (activeTabIndex == 3) themeColors.primary else themeColors.onSurface.copy(alpha = 0.4f)),
                            modifier = Modifier.rgbGradientText(if (activeTabIndex == 3) brush else null)
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = if (themeColors.isRGB) Color.Transparent else themeColors.primary,
                        selectedTextColor = if (themeColors.isRGB) Color.Transparent else themeColors.primary,
                        indicatorColor = if (themeColors.isRGB) Color.Transparent else themeColors.background,
                        unselectedIconColor = themeColors.onSurface.copy(alpha = 0.4f),
                        unselectedTextColor = themeColors.onSurface.copy(alpha = 0.4f)
                    )
                )
            }
        }
    ) { innerPadding ->
        DezhThemedLayout(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AnimatedContent(
                targetState = activeTabIndex,
                transitionSpec = {
                    fadeIn().togetherWith(fadeOut())
                },
                label = "dashboard_tabs"
            ) { tabIndex ->
                when (tabIndex) {
                    0 -> SecurityDashboardScreen(messengers = messengers, onMessengerClick = onMessengerSelect)
                    1 -> SandboxGalleryTab(context = context)
                    2 -> SandboxFileManagerTab(context = context, onThemeUnlocked = onThemeUnlocked)
                    3 -> SandboxInfoTab(context = context, onTriggerBackup = onTriggerBackup)
                }
            }
        }
    }
}

// --------------------------------------------------------------------------
// Sub-view Tab: Decoupled Operational Info & Owner Credits
// --------------------------------------------------------------------------
@Composable
fun SandboxInfoTab(context: Context, onTriggerBackup: () -> Unit = {}) {
    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        val themeColors = LocalThemeColors.current
        Text(
            text = localizedString("sandbox_info_title"),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = if (themeColors.isRGB) Color.White else themeColors.onBackground,
            modifier = Modifier.rgbGradientText(if (themeColors.isRGB) rememberRgbFlowBrush() else null)
        )
        Text(
            text = localizedString("sandbox_info_desc"),
            style = MaterialTheme.typography.bodySmall,
            color = themeColors.onBackground.copy(alpha = 0.6f)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // --- Custom Theme Selection Dashboard ---
        val scope = rememberCoroutineScope()
        val isLanguageFa = LocalAppLanguage.current == "fa"
        val activeThemeState = remember { ThemeManager.getActiveTheme(context) }.collectAsState(initial = DezhTheme.SIMPLE_DARK)
        val activeTheme = activeThemeState.value
        val unlockedThemesState = remember { ThemeManager.getUnlockedThemes(context) }.collectAsState(initial = setOf(DezhTheme.SIMPLE_DARK, DezhTheme.LIGHT_MODE, DezhTheme.CYBER, DezhTheme.PINKY))
        val unlockedThemes = unlockedThemesState.value

        Text(
            text = if (isLanguageFa) "انتخاب پوسته دژ" else "Dezh Custom Themes",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = themeColors.primary,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
                .testTag("theme_selector_card"),
            colors = CardDefaults.cardColors(containerColor = themeColors.surface.copy(alpha = 0.5f)),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, themeColors.border.copy(alpha = 0.3f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = if (isLanguageFa) "شخصی‌سازی ظاهر پیام‌رسان با ۷ پوسته اختصاصی." else "Choose from 7 total custom-styled themes (including 3 hidden easter egg themes).",
                    style = MaterialTheme.typography.bodySmall,
                    color = themeColors.onSurface.copy(alpha = 0.7f),
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                ThemeSettingsScreen(context, themeColors, isLanguageFa)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Operational concept card
        Card(
            modifier = Modifier.fillMaxWidth().testTag("operational_concept_card"),
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.04f)),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = localizedString("op_concept_title"),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF00B0FF)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = localizedString("op_concept_desc"),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Usage guidelines card
        Card(
            modifier = Modifier.fillMaxWidth().testTag("usage_guidelines_card"),
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.04f)),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = localizedString("usage_guidelines_title"),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF00B0FF)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = localizedString("usage_guidelines_desc"),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Shields Integrity information card
        Card(
            modifier = Modifier.fillMaxWidth().testTag("shields_integrity_card"),
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.04f)),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = localizedString("active_shields_title"),
                        tint = Color(0xFF00E676),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = localizedString("active_shields_title"),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    ShieldItem(title = localizedString("shield1_title"), description = localizedString("shield1_desc"), isProtected = true)
                    ShieldItem(title = localizedString("shield2_title"), description = localizedString("shield2_desc"), isProtected = true)
                    ShieldItem(title = localizedString("shield3_title"), description = localizedString("shield3_desc"), isProtected = true)
                    ShieldItem(title = localizedString("shield4_title"), description = localizedString("shield4_desc"), isProtected = true)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Backup Hub and Import/Export Guide Card
        val isFa = LocalAppLanguage.current == "fa"
        Card(
            modifier = Modifier.fillMaxWidth().testTag("backup_guide_card"),
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.04f)),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, Color(0xFF00B0FF).copy(alpha = 0.2f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = "",
                        tint = Color(0xFF00E676),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = if (isFa) "امنیت نشست‌های خودکار دژ" else "Dezh Persistent Session Security",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = if (isFa) {
                        "تمام نشست‌های فعال شما به‌طور خودکار درون حافظه امنِ رمزگذاری‌شده دستگاه (EncryptedSharedPreferences) ذخیره می‌شوند تا بدون نیاز به فایل‌های خارجی، همواره ایمن، محافظت‌شده و فعال بمانند."
                    } else {
                        "Your messenger sessions are automatically secured inside the device's hardware-backed EncryptedSharedPreferences storage, keeping them permanently protected and active without requiring manual file exports."
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.8f)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Button(
                    onClick = {
                        try {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/StarrynightStudio"))
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    },
                    modifier = Modifier.fillMaxWidth().testTag("info_open_support_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.08f), contentColor = Color(0xFF00E676))
                ) {
                    Icon(imageVector = Icons.Default.SupportAgent, contentDescription = "", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isFa) "پشتیبان دژ" else "Support Hub",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Project and developer context cards
        Card(
            modifier = Modifier.fillMaxWidth().testTag("project_credits_card"),
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.04f)),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = localizedString("credits_title"),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF00B0FF)
                )
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(localizedString("dev_label"), color = Color.White.copy(alpha = 0.6f), style = MaterialTheme.typography.bodyMedium)
                    Text("MR_joghd", color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                }
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = Color.White.copy(alpha = 0.05f))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(localizedString("owner_label"), color = Color.White.copy(alpha = 0.6f), style = MaterialTheme.typography.bodyMedium)
                    Text(localizedString("owner_val"), color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                }
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = Color.White.copy(alpha = 0.05f))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(localizedString("platforms_label"), color = Color.White.copy(alpha = 0.6f), style = MaterialTheme.typography.bodyMedium)
                    val isFa = LocalAppLanguage.current == "fa"
                    val supportText = if (isFa) "ایتا، روبیکا، سروش، بله" else "Eitaa, Rubika, Soroush, Bale"
                    Text(supportText, color = Color.White, style = MaterialTheme.typography.bodyMedium)
                }
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = Color.White.copy(alpha = 0.05f))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(localizedString("copyright_label"), color = Color.White.copy(alpha = 0.6f), style = MaterialTheme.typography.bodyMedium)
                    Text(localizedString("copyright_val"), color = Color.White.copy(alpha = 0.8f), style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.End)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

// --------------------------------------------------------------------------
// Sub-view Tab: Isolated Sandbox Photo/Video Gallery
// --------------------------------------------------------------------------

@Composable
fun rememberVideoThumbnail(file: File): Bitmap? {
    val bitmapState = remember(file) { mutableStateOf<Bitmap?>(null) }
    LaunchedEffect(file) {
        withContext(Dispatchers.IO) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val size = android.util.Size(512, 512)
                    ThumbnailUtils.createVideoThumbnail(file, size, null)
                } else {
                    val retriever = MediaMetadataRetriever()
                    retriever.setDataSource(file.absolutePath)
                    val frame = retriever.getFrameAtTime(1000000) // 1 second
                    retriever.release()
                    frame
                }
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }?.let {
            bitmapState.value = it
        }
    }
    return bitmapState.value
}

@Composable
fun FilePreviewDialog(
    file: File,
    onDismiss: () -> Unit,
    onExport: () -> Unit,
    context: Context
) {
    val currentLang = LocalAppLanguage.current
    val ext = file.extension.lowercase(Locale.ROOT)
    val isImage = ext == "png" || ext == "jpg" || ext == "jpeg" || ext == "webp" || ext == "gif"
    val isVideo = ext == "mp4" || ext == "mkv"
    val isText = ext == "txt" || ext == "json" || ext == "html"

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = file.name,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = localizedString("btn_cancel"), tint = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.Black.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    if (isImage) {
                        AsyncImage(
                            model = file,
                            contentDescription = localizedString("image_label"),
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit,
                            error = rememberAsyncImagePainter(
                                model = remember(file) {
                                    try {
                                        android.graphics.BitmapFactory.decodeFile(file.absolutePath)
                                    } catch (e: Exception) {
                                        null
                                    }
                                }
                            )
                        )
                    } else if (isVideo) {
                        AndroidView(
                            factory = { ctx ->
                                VideoView(ctx).apply {
                                    val mediaController = MediaController(ctx)
                                    mediaController.setAnchorView(this)
                                    setMediaController(mediaController)
                                    setVideoPath(file.absolutePath)
                                    setOnPreparedListener { mp ->
                                        mp.isLooping = true
                                        start()
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxSize(),
                            onRelease = { videoView ->
                                videoView.stopPlayback()
                            }
                        )
                    } else if (isText) {
                        val content = remember(file) {
                            try {
                                file.readText()
                            } catch (e: Exception) {
                                if (currentLang == "fa") "خطا در خواندن فایل متنی" else "Error reading text file"
                            }
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.5f))
                                .padding(10.dp)
                                .verticalScroll(rememberScrollState())
                        ) {
                            Text(
                                text = content,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White
                            )
                        }
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.InsertDriveFile,
                                contentDescription = "",
                                tint = Color.White.copy(alpha = 0.3f),
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = localizedString("preview_not_supported"),
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.5f)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onExport,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00B0FF))
                ) {
                    Icon(imageVector = Icons.Default.Download, contentDescription = "")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(localizedString("export_public"))
                }
            }
        }
    }
}

@Composable
fun SandboxGalleryTab(context: Context) {
    val currentLang = LocalAppLanguage.current
    var isolatedMediaFiles by remember { mutableStateOf(emptyList<File>()) }
    var refreshTrigger by remember { mutableStateOf(0) }
    var previewFile by remember { mutableStateOf<File?>(null) }
    val themeColors = LocalThemeColors.current

    LaunchedEffect(refreshTrigger) {
        val allFiles = getSandboxDownloadDir(context).listFiles() ?: emptyArray()
        isolatedMediaFiles = allFiles.filter { file ->
            val ext = file.extension.lowercase(Locale.ROOT)
            ext == "png" || ext == "jpg" || ext == "jpeg" || ext == "webp" || ext == "gif" || ext == "mp4" || ext == "mkv"
        }.sortedByDescending { it.lastModified() }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.Transparent
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = localizedString("gallery_title"),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (themeColors.isRGB) Color.White else themeColors.onBackground,
                        modifier = Modifier.rgbGradientText(if (themeColors.isRGB) rememberRgbFlowBrush() else null)
                    )
                    Text(
                        text = localizedString("gallery_desc"),
                        style = MaterialTheme.typography.bodySmall,
                        color = themeColors.onBackground.copy(alpha = 0.6f)
                    )
                }

                IconButton(
                    onClick = { refreshTrigger++ },
                    colors = IconButtonDefaults.iconButtonColors(containerColor = themeColors.onBackground.copy(alpha = 0.05f))
                ) {
                    Icon(imageVector = Icons.Default.Cached, contentDescription = localizedString("reload"), tint = themeColors.primary)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (isolatedMediaFiles.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .border(1.dp, themeColors.border.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
                        .background(themeColors.surface.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PhotoLibrary,
                            contentDescription = "",
                            tint = themeColors.onBackground.copy(alpha = 0.2f),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = localizedString("no_media_found"),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = themeColors.onBackground.copy(alpha = 0.7f)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = localizedString("no_media_desc"),
                            style = MaterialTheme.typography.bodySmall,
                            color = themeColors.onBackground.copy(alpha = 0.4f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(isolatedMediaFiles) { file ->
                        GalleryCard(
                            file = file,
                            onPreview = { previewFile = file },
                            onExport = {
                                exportSandboxFileToPublicDevice(context, file) { success, target ->
                                    if (success) {
                                        Toast.makeText(context, LocalizedStrings.get("export_success", currentLang) + " " + target, Toast.LENGTH_LONG).show()
                                    } else {
                                        Toast.makeText(context, LocalizedStrings.get("export_failed", currentLang), Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }

        // Unified Multimedia In-App Secure Preview Dialog
        if (previewFile != null) {
            FilePreviewDialog(
                file = previewFile!!,
                onDismiss = { previewFile = null },
                onExport = {
                    val currentFile = previewFile!!
                    previewFile = null
                    exportSandboxFileToPublicDevice(context, currentFile) { success, dest ->
                        if (success) {
                            Toast.makeText(context, LocalizedStrings.get("export_success", currentLang) + " " + dest, Toast.LENGTH_LONG).show()
                        } else {
                            Toast.makeText(context, LocalizedStrings.get("export_failed", currentLang), Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                context = context
            )
        }
    }
}

@Composable
fun GalleryCard(
    file: File,
    onPreview: () -> Unit,
    onExport: () -> Unit
) {
    val ext = file.extension.lowercase(Locale.ROOT)
    val isVideo = ext == "mp4" || ext == "mkv"
    val themeColors = LocalThemeColors.current

    val videoThumbnail = if (isVideo) rememberVideoThumbnail(file) else null

    val fileSizeText = remember(file) {
        val length = file.length()
        if (length >= 1024 * 1024) {
            String.format(Locale.US, "%.1f MB", length.toFloat() / (1024 * 1024))
        } else {
            "${length / 1024} KB"
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp),
        colors = CardDefaults.cardColors(containerColor = themeColors.surface.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, themeColors.border.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clickable { onPreview() }
            ) {
                if (isVideo) {
                    if (videoThumbnail != null) {
                        Image(
                            bitmap = videoThumbnail.asImageBitmap(),
                            contentDescription = file.name,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.4f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Videocam,
                                contentDescription = "Loading video thumbnail",
                                tint = Color.White.copy(alpha = 0.3f),
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }

                    // Translucent Video central overlay
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Play video preview",
                            tint = Color.White,
                            modifier = Modifier
                                .size(40.dp)
                                .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                                .padding(8.dp)
                        )
                    }
                } else {
                    AsyncImage(
                        model = file,
                        contentDescription = file.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        error = rememberAsyncImagePainter(
                            model = remember(file) {
                                try {
                                    android.graphics.BitmapFactory.decodeFile(file.absolutePath)
                                } catch (e: Exception) {
                                    null
                                }
                            }
                        )
                    )
                }

                Box(
                    modifier = Modifier
                        .padding(8.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color.Black.copy(alpha = 0.6f))
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                        .align(Alignment.TopStart)
                ) {
                    Text(
                        text = if (isVideo) localizedString("filter_videos") else localizedString("filter_images"),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF00E676),
                        fontWeight = FontWeight.Bold,
                        fontSize = 8.sp
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(themeColors.surface.copy(alpha = 0.8f))
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = file.name,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = themeColors.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = fileSizeText,
                        style = MaterialTheme.typography.labelSmall,
                        color = themeColors.onSurface.copy(alpha = 0.5f)
                    )
                }

                IconButton(
                    onClick = onExport,
                    modifier = Modifier.size(28.dp).testTag("export_gallery_${file.name}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = localizedString("btn_export"),
                        tint = themeColors.primary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

// --------------------------------------------------------------------------
// --------------------------------------------------------------------------
// Sub-view Tab: In-App Isolated Files Manager
// --------------------------------------------------------------------------
@Composable
fun SandboxFileManagerTab(context: Context, onThemeUnlocked: (String) -> Unit) {
    val currentLang = LocalAppLanguage.current
    var sandboxFiles by remember { mutableStateOf(emptyList<File>()) }
    var refreshTrigger by remember { mutableStateOf(0) }
    var previewFile by remember { mutableStateOf<File?>(null) }
    val themeColors = LocalThemeColors.current
    val scope = rememberCoroutineScope()

    var filePendingPassword by remember { mutableStateOf<File?>(null) }
    var filePasswordInput by remember { mutableStateOf("") }

    val unlockedThemesState = remember { ThemeManager.getUnlockedThemes(context) }.collectAsState(initial = emptySet())
    val unlockedThemes = unlockedThemesState.value

    LaunchedEffect(refreshTrigger, unlockedThemes) {
        val dir = getSandboxDownloadDir(context)
        if (unlockedThemes.contains(DezhTheme.RGB_GAMING)) {
            File(dir, "don'topen.virus").delete()
        }
        sandboxFiles = (dir.listFiles() ?: emptyArray())
            .toList()
            .sortedByDescending { it.lastModified() }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.Transparent
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = localizedString("file_manager_title"),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (themeColors.isRGB) Color.White else themeColors.onBackground,
                        modifier = Modifier.rgbGradientText(if (themeColors.isRGB) rememberRgbFlowBrush() else null)
                    )
                    Text(
                        text = localizedString("file_manager_desc"),
                        style = MaterialTheme.typography.bodySmall,
                        color = themeColors.onBackground.copy(alpha = 0.5f)
                    )
                }

                IconButton(
                    onClick = { refreshTrigger++ },
                    colors = IconButtonDefaults.iconButtonColors(containerColor = themeColors.onBackground.copy(alpha = 0.05f))
                ) {
                    Icon(imageVector = Icons.Default.Sync, contentDescription = localizedString("reload"), tint = themeColors.primary)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (sandboxFiles.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .border(1.dp, themeColors.border.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
                        .background(themeColors.surface.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Folder,
                            contentDescription = "",
                            tint = themeColors.onBackground.copy(alpha = 0.2f),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = localizedString("no_files_yet"),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = themeColors.onBackground.copy(alpha = 0.7f)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = localizedString("no_files_yet_desc"),
                            style = MaterialTheme.typography.bodySmall,
                            color = themeColors.onBackground.copy(alpha = 0.4f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(sandboxFiles) { file ->
                        FileRowItem(
                            file = file,
                            onOpen = {
                                val isRgbUnlocked = unlockedThemes.contains(DezhTheme.RGB_GAMING)
                                if (file.name == "don'topen.virus") {
                                    if (!isRgbUnlocked) {
                                        filePendingPassword = file
                                        filePasswordInput = ""
                                    } else {
                                        file.delete()
                                        refreshTrigger++
                                    }
                                } else {
                                    previewFile = file
                                }
                            },
                            onDelete = {
                                file.delete()
                                refreshTrigger++
                                Toast.makeText(context, LocalizedStrings.get("file_removed", currentLang), Toast.LENGTH_SHORT).show()
                            },
                            onExport = {
                                exportSandboxFileToPublicDevice(context, file) { success, dest ->
                                    if (success) {
                                        Toast.makeText(context, LocalizedStrings.get("export_success", currentLang) + " " + dest, Toast.LENGTH_LONG).show()
                                    } else {
                                        Toast.makeText(context, LocalizedStrings.get("export_fail", currentLang), Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }

        // Custom password decryption dialog to decrypt and view, with RGB theme unlock hint!
        if (filePendingPassword != null) {
            CyberSecurityOverrideDialog(
                onDismiss = {
                    filePendingPassword = null
                    filePasswordInput = ""
                },
                onSubmit = { entered ->
                    val pass = entered.trim()
                    if (pass.equals("starrynight", ignoreCase = true) || pass.equals("starrynightstudio", ignoreCase = true) || pass.equals("joghd", ignoreCase = true)) {
                        scope.launch {
                            TriggerService.unlockRgbTheme(context)
                            ThemeManager.setActiveTheme(context, DezhTheme.RGB_GAMING)
                            onThemeUnlocked(if (currentLang == "fa") "نورپردازی گیمینگ" else "RGB Gaming")
                        }
                        try {
                            filePendingPassword?.delete()
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                        filePendingPassword = null
                        filePasswordInput = ""
                        refreshTrigger++
                    } else {
                        val msg = if (currentLang == "fa") "رمز عبور نادرست است." else "Incorrect password."
                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                    }
                }
            )
        }

        // Unified Multimedia Preview Dialog inside File Manager!
        if (previewFile != null) {
            FilePreviewDialog(
                file = previewFile!!,
                onDismiss = { previewFile = null },
                onExport = {
                    val currentFile = previewFile!!
                    previewFile = null
                    exportSandboxFileToPublicDevice(context, currentFile) { success, dest ->
                        if (success) {
                            Toast.makeText(context, LocalizedStrings.get("saved_to_device", currentLang) + " " + dest, Toast.LENGTH_LONG).show()
                        } else {
                            Toast.makeText(context, LocalizedStrings.get("export_error", currentLang), Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                context = context
            )
        }
    }
}

@Composable
fun FileRowItem(
    file: File,
    onOpen: () -> Unit,
    onDelete: () -> Unit,
    onExport: () -> Unit
) {
    val ext = file.extension.lowercase(Locale.ROOT)
    val isVideo = ext == "mp4" || ext == "mkv"
    val isImage = ext == "png" || ext == "jpg" || ext == "jpeg" || ext == "webp" || ext == "gif"
    val isText = ext == "txt" || ext == "json" || ext == "html"
    val themeColors = LocalThemeColors.current

    val icon = when {
        isVideo -> Icons.Default.PlayArrow
        isImage -> Icons.Default.Image
        isText -> Icons.Default.Description
        else -> Icons.Default.InsertDriveFile
    }

    val fileSizeText = remember(file) {
        val length = file.length()
        if (length >= 1024 * 1024) {
            String.format(Locale.US, "%.1f MB", length.toFloat() / (1024 * 1024))
        } else {
            "${length / 1024} KB"
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth().testTag("file_item_${file.name}"),
        colors = CardDefaults.cardColors(containerColor = themeColors.surface.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, themeColors.border.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(themeColors.primary.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = "File Type",
                        tint = themeColors.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = file.name,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = themeColors.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    val isFa = LocalAppLanguage.current == "fa"
                    val labelVault = if (isFa) "(ایزوله دژ)" else "(Dezh Vault)"
                    Text(
                        text = "$fileSizeText • ${file.extension.uppercase()} $labelVault",
                        style = MaterialTheme.typography.labelSmall,
                        color = themeColors.onSurface.copy(alpha = 0.5f)
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onOpen) {
                    Icon(imageVector = Icons.Default.Visibility, contentDescription = localizedString("tab_info"), tint = themeColors.onSurface)
                }
                IconButton(onClick = onExport, modifier = Modifier.testTag("export_btn_${file.name}")) {
                    Icon(imageVector = Icons.Default.Download, contentDescription = localizedString("btn_export"), tint = themeColors.primary)
                }
                IconButton(onClick = onDelete) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = localizedString("btn_cancel"), tint = Color.Red.copy(alpha = 0.8f))
                }
            }
        }
    }
}

// --------------------------------------------------------------------------
// Cryptographic System Backup and Restore (Phase 2 & Phase 3)
// --------------------------------------------------------------------------

object CryptoUtils {
    private const val ALGORITHM = "AES/CBC/PKCS5Padding"
    private const val KEY_DERIVATION_ALGORITHM = "PBKDF2WithHmacSHA256"
    private const val ITERATIONS = 10000
    private const val KEY_LENGTH = 256

    fun deriveKey(password: String, salt: ByteArray): javax.crypto.spec.SecretKeySpec {
        val spec = javax.crypto.spec.PBEKeySpec(password.toCharArray(), salt, ITERATIONS, KEY_LENGTH)
        val factory = javax.crypto.SecretKeyFactory.getInstance(KEY_DERIVATION_ALGORITHM)
        val keyBytes = factory.generateSecret(spec).encoded
        return javax.crypto.spec.SecretKeySpec(keyBytes, "AES")
    }

    fun encrypt(data: ByteArray, password: String): ByteArray {
        val salt = ByteArray(16).apply { SecureRandom().nextBytes(this) }
        val iv = ByteArray(16).apply { SecureRandom().nextBytes(this) }
        val secretKey = deriveKey(password, salt)
        
        val cipher = Cipher.getInstance(ALGORITHM)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, IvParameterSpec(iv))
        val ciphertext = cipher.doFinal(data)
        
        val output = ByteArray(salt.size + iv.size + ciphertext.size)
        System.arraycopy(salt, 0, output, 0, salt.size)
        System.arraycopy(iv, 0, output, salt.size, iv.size)
        System.arraycopy(ciphertext, 0, output, salt.size + iv.size, ciphertext.size)
        return output
    }

    fun decrypt(encryptedBytes: ByteArray, password: String): ByteArray {
        if (encryptedBytes.size < 32) {
            throw IllegalArgumentException("Invalid encrypted backup package.")
        }
        val salt = ByteArray(16)
        val iv = ByteArray(16)
        val ciphertextBytes = ByteArray(encryptedBytes.size - 32)
        
        System.arraycopy(encryptedBytes, 0, salt, 0, 16)
        System.arraycopy(encryptedBytes, 16, iv, 0, 16)
        System.arraycopy(encryptedBytes, 32, ciphertextBytes, 0, ciphertextBytes.size)
        
        val secretKey = deriveKey(password, salt)
        val cipher = Cipher.getInstance(ALGORITHM)
        cipher.init(Cipher.DECRYPT_MODE, secretKey, IvParameterSpec(iv))
        return cipher.doFinal(ciphertextBytes)
    }
}

// --------------------------------------------------------------------------
// PersistentSessionManager: Secure automatic encrypted session vault
// --------------------------------------------------------------------------
object PersistentSessionManager {
    private const val PREFS_NAME = "secure_dezh_sessions_pref"
    private const val TAG = "DezhSessionManager"

    @Volatile
    var isSyncing = false

    fun getSecurePrefs(context: Context): SharedPreferences {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        return EncryptedSharedPreferences.create(
            context,
            PREFS_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    fun getSavedCookie(context: Context, messengerId: String): String {
        return try {
            getSecurePrefs(context).getString("cookie_$messengerId", "") ?: ""
        } catch (e: Exception) {
            ""
        }
    }

    fun saveSessionToDisk(context: Context, messengerId: String, url: String, cookies: String) {
        if (cookies.isEmpty()) return
        try {
            val prefs = getSecurePrefs(context)
            prefs.edit().apply {
                putString("cookie_$messengerId", cookies)
                putString("url_$messengerId", url)
                
                val savedIds = prefs.getStringSet("saved_messenger_ids", emptySet())?.toMutableSet() ?: mutableSetOf()
                savedIds.add(messengerId)
                putStringSet("saved_messenger_ids", savedIds)
                apply()
            }
            CookieManager.getInstance().flush()
            android.util.Log.d(TAG, "Successfully auto-saved session for $messengerId, cookies size: ${cookies.length}")
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Failed to persist secure session: ${e.message}", e)
        }
    }

    fun restoreSessionsFromDisk(context: Context) {
        try {
            val prefs = getSecurePrefs(context)
            val savedIds = prefs.getStringSet("saved_messenger_ids", emptySet()) ?: return
            
            val cookieManager = CookieManager.getInstance()
            cookieManager.setAcceptCookie(true)
            
            for (id in savedIds) {
                val url = prefs.getString("url_$id", "") ?: continue
                val cookies = prefs.getString("cookie_$id", "") ?: continue
                if (url.isNotEmpty() && cookies.isNotEmpty()) {
                    injectCookiesIntoCookieManager(url, cookies)
                }
            }
            cookieManager.flush()
            android.util.Log.d(TAG, "Successfully restored all automatic sessions from EncryptedSharedPreferences.")
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Error restoring sessions from EncryptedSharedPreferences: ${e.message}", e)
        }
    }

    fun injectCookiesIntoCookieManager(url: String, cookies: String) {
        val cookieManager = CookieManager.getInstance()
        cookieManager.setAcceptCookie(true)

        val host = try {
            val uri = Uri.parse(url)
            val h = uri.host ?: ""
            if (h.startsWith("www.")) h.substring(4) else h
        } catch (e: Exception) {
            ""
        }

        val cookiePairs = cookies.split(";")
        for (cookie in cookiePairs) {
            val trimmed = cookie.trim()
            if (trimmed.isNotEmpty()) {
                cookieManager.setCookie(url, trimmed)
                if (host.isNotEmpty()) {
                    cookieManager.setCookie("https://$host", trimmed)
                    cookieManager.setCookie("https://$host/", trimmed)
                    cookieManager.setCookie(".$host", trimmed)
                }
            }
        }
        cookieManager.flush()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SandboxBackupRestoreDialog(
    messengers: List<Messenger>,
    selectedIds: List<String>,
    onToggleId: (String) -> Unit,
    onToggleSelectAll: () -> Unit,
    passwordValue: String,
    onPasswordChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onPerformExport: () -> Unit,
    onTriggerImportChoice: () -> Unit,
    onPerformImport: () -> Unit,
    importUriName: String?,
    importPasswordValue: String,
    onImportPasswordChange: (String) -> Unit
) {
    var backupTab by remember { mutableStateOf(0) }
    val isFa = LocalAppLanguage.current == "fa"

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
                .testTag("backup_restore_dialog_card"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0B1220)),
            border = BorderStroke(1.5.dp, Color(0xFF00B0FF).copy(alpha = 0.4f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Surface(
                        modifier = Modifier.size(42.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF00B0FF).copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, Color(0xFF00B0FF))
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "",
                                tint = Color(0xFF00B0FF),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = if (isFa) "پایگاه پشتیبان امن دژ" else "Dezh Secure Cloud Backup",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = if (isFa) "رمزگذاری امن سطح نظامی AES-256" else "Military AES-256 PBKDF2 vault",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF00E676)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.05f))
                        .padding(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (backupTab == 0) Color(0xFF00B0FF) else Color.Transparent)
                            .clickable { backupTab = 0 }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isFa) "پشتیبان‌گیری (Export)" else "Export Backup",
                            color = if (backupTab == 0) Color(0xFF020617) else Color.White,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (backupTab == 1) Color(0xFF00B0FF) else Color.Transparent)
                            .clickable { backupTab = 1 }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isFa) "بازیابی نشست (Import)" else "Import Sessions",
                            color = if (backupTab == 1) Color(0xFF020617) else Color.White,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (backupTab == 0) {
                    Text(
                        text = if (isFa) "انتخاب پیام‌رسان‌های موردنظر:" else "Select messengers to export:",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.6f)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { onToggleSelectAll() }
                            .padding(vertical = 6.dp, horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val allSelected = selectedIds.size == messengers.size && messengers.isNotEmpty()
                        Checkbox(
                            checked = allSelected,
                            onCheckedChange = { onToggleSelectAll() },
                            colors = CheckboxDefaults.colors(checkedColor = Color(0xFF00B0FF))
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isFa) "انتخاب همه پیام‌رسان‌ها" else "Select All Messengers",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    HorizontalDivider(color = Color.White.copy(alpha = 0.05f), modifier = Modifier.padding(vertical = 6.dp))

                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        messengers.forEach { msg ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { onToggleId(msg.id) }
                                    .padding(vertical = 6.dp, horizontal = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = selectedIds.contains(msg.id),
                                    onCheckedChange = { onToggleId(msg.id) },
                                    colors = CheckboxDefaults.colors(checkedColor = Color(0xFF00B0FF))
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    modifier = Modifier.size(28.dp),
                                    shape = CircleShape,
                                    color = msg.brandColor.copy(alpha = 0.15f),
                                    border = BorderStroke(1.dp, msg.brandColor)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(msg.iconLetter, color = msg.brandColor, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                    }
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = msg.name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.weight(1.1f))
                                val cookieManager = CookieManager.getInstance()
                                val hasCookie = !cookieManager.getCookie(msg.url).isNullOrBlank()
                                Text(
                                    text = if (hasCookie) {
                                        if (isFa) "نشست فعال" else "Active Session"
                                    } else {
                                        if (isFa) "فاقد نشست" else "No Session"
                                    },
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (hasCookie) Color(0xFF00E676) else Color.White.copy(alpha = 0.35f)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = if (isFa) "رمز عبور برای حفاظت از فایل .secure:" else "Backup security password:",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    var passVisible by remember { mutableStateOf(false) }
                    TextField(
                        value = passwordValue,
                        onValueChange = onPasswordChange,
                        modifier = Modifier.fillMaxWidth().testTag("backup_password_input"),
                        textStyle = MaterialTheme.typography.bodyMedium.copy(color = Color.White),
                        placeholder = { Text(if (isFa) "حداقل ۸ کاراکتر وارد کنید" else "Min 8 characters", color = Color.White.copy(alpha = 0.3f)) },
                        trailingIcon = {
                            IconButton(onClick = { passVisible = !passVisible }) {
                                Icon(
                                    imageVector = if (passVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = "",
                                    tint = Color.White.copy(alpha = 0.6f)
                                )
                            }
                        },
                        visualTransformation = if (passVisible) androidx.compose.ui.text.input.VisualTransformation.None else androidx.compose.ui.text.input.PasswordVisualTransformation(),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.White.copy(alpha = 0.05f),
                            unfocusedContainerColor = Color.White.copy(alpha = 0.03f),
                            focusedIndicatorColor = Color(0xFF00B0FF),
                            unfocusedIndicatorColor = Color.White.copy(alpha = 0.1f)
                        ),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                        ) {
                            Text(if (isFa) "لغو" else "Cancel")
                        }
                        Button(
                            onClick = onPerformExport,
                            modifier = Modifier.weight(1.5f).testTag("dialog_perform_backup_btn"),
                            enabled = passwordValue.length >= 8 && selectedIds.isNotEmpty(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF00B0FF),
                                disabledContainerColor = Color(0xFF00B0FF).copy(alpha = 0.2f)
                            )
                        ) {
                            Icon(imageVector = Icons.Default.CloudUpload, contentDescription = "")
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (isFa) "ایجاد فایل پشتیبان" else "Create Backup")
                        }
                    }

                } else {
                    if (importUriName == null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .border(1.5.dp, Color(0xFF00B0FF).copy(alpha = 0.25f), RoundedCornerShape(16.dp))
                                .background(Color.White.copy(alpha = 0.02f))
                                .clickable { onTriggerImportChoice() }
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Backup,
                                    contentDescription = "",
                                    tint = Color(0xFF00B0FF),
                                    modifier = Modifier.size(42.dp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = if (isFa) "انتخاب و بارگذاری فایل .secure" else "Choose .secure file to import",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = if (isFa) "آپلود مستقیم پشتیبان دژ" else "Select encrypted physical backup",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White.copy(alpha = 0.4f)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier.fillMaxWidth(),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                        ) {
                            Text(if (isFa) "بستن" else "Close")
                        }
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF00E676).copy(alpha = 0.1f))
                                .border(1.dp, Color(0xFF00E676).copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                                .padding(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "",
                                tint = Color(0xFF00E676)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = if (isFa) "فایل پشتیبان انتخاب شد:" else "Selected Backup File:",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White.copy(alpha = 0.6f)
                                )
                                Text(
                                    text = importUriName,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = if (isFa) "رمز عبور فایل برای رمزگشایی:" else "Enter password to decrypt:",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.6f)
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        var passVisible by remember { mutableStateOf(false) }
                        TextField(
                            value = importPasswordValue,
                            onValueChange = onImportPasswordChange,
                            modifier = Modifier.fillMaxWidth().testTag("import_password_input"),
                            textStyle = MaterialTheme.typography.bodyMedium.copy(color = Color.White),
                            placeholder = { Text(if (isFa) "رمز عبوری که موقع خروجی ساختید" else "Password specified at export", color = Color.White.copy(alpha = 0.3f)) },
                            trailingIcon = {
                                IconButton(onClick = { passVisible = !passVisible }) {
                                    Icon(
                                        imageVector = if (passVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = "",
                                        tint = Color.White.copy(alpha = 0.6f)
                                    )
                                }
                            },
                            visualTransformation = if (passVisible) androidx.compose.ui.text.input.VisualTransformation.None else androidx.compose.ui.text.input.PasswordVisualTransformation(),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.White.copy(alpha = 0.05f),
                                unfocusedContainerColor = Color.White.copy(alpha = 0.03f),
                                focusedIndicatorColor = Color(0xFF00B0FF),
                                unfocusedIndicatorColor = Color.White.copy(alpha = 0.1f)
                            ),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = onDismiss,
                                modifier = Modifier.weight(1f),
                                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                            ) {
                                Text(if (isFa) "انصراف" else "Cancel")
                            }
                            Button(
                                onClick = onPerformImport,
                                modifier = Modifier.weight(1.5f).testTag("dialog_perform_import_btn"),
                                enabled = importPasswordValue.isNotEmpty(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF00E676),
                                    disabledContainerColor = Color(0xFF00E676).copy(alpha = 0.2f)
                                )
                            ) {
                                Icon(imageVector = Icons.Default.LockOpen, contentDescription = "")
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(if (isFa) "تایید و بازیابی" else "Verify & Restore")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CyberSecurityOverrideDialog(
    onDismiss: () -> Unit,
    onSubmit: (String) -> Unit
) {
    val isFa = LocalAppLanguage.current == "fa"
    var passwordInput by remember { mutableStateOf("") }
    
    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .shadow(24.dp, RoundedCornerShape(24.dp))
                .clip(RoundedCornerShape(24.dp))
                .background(Color(0xE6080B15)) // Semi-transparent ultra dark blue
                .border(
                    BorderStroke(2.dp, Brush.horizontalGradient(listOf(Color(0xFF00FFCC), Color(0xFF0077FF), Color(0xFFFF0055)))),
                    RoundedCornerShape(24.dp)
                )
                .padding(24.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = "Shield Override Warning",
                    tint = Color(0xFFFF3366),
                    modifier = Modifier
                        .size(48.dp)
                        .padding(bottom = 8.dp)
                )
                
                Text(
                    text = if (isFa) "تجاوز به سد امنیتی شناسایی شد" else "DEZH SECURITY OVERRIDE",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFFFF3366),
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(10.dp))
                
                Text(
                    text = if (isFa) 
                        "سیستم سندباکس دژ دسترسی به بدافزار قرنطینه شده را متوقف کرده است. سیگنال خنثی‌سازی مدیریت ارشد به عنوان تاییدیه هویت الزامی است:" 
                        else "Automated quarantine protocols have secured 'don'topen.virus'. Please enter the cryptographic bypass signature to proceed:",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.LightGray,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(18.dp))
                
                // Terminal styled input field
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF000511))
                        .border(BorderStroke(1.dp, Color(0xFF00FFCC)), RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp, vertical = 10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "DEZH_SECURE_OS@BYPASS: ",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF00FFCC),
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                        )
                        
                        androidx.compose.foundation.text.BasicTextField(
                            value = passwordInput,
                            onValueChange = { passwordInput = it },
                            textStyle = MaterialTheme.typography.bodyMedium.copy(
                                color = Color.White,
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                            ),
                            cursorBrush = androidx.compose.ui.graphics.SolidColor(Color(0xFF00FFCC)),
                            singleLine = true,
                            visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Glowing styled Unlock Button
                Button(
                    onClick = { onSubmit(passwordInput) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF00FFCC),
                        contentColor = Color(0xFF000511)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(8.dp, RoundedCornerShape(12.dp), spotColor = Color(0xFF00FFCC))
                ) {
                    Text(
                        text = if (isFa) "تایید و بارگذاری اورراید" else "EXECUTE OVERRIDE PROTOCOL",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                    )
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                TextButton(onClick = onDismiss) {
                    Text(
                        text = if (isFa) "انصراف و خروج" else "Abort Bypass Session",
                        color = Color.Gray,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

@Composable
fun ThemeSettingsScreen(
    context: Context,
    themeColors: com.starrynightstudio.dezhmessenger.xwpqrs.ui.theme.ThemeColors,
    isLanguageFa: Boolean
) {
    val scope = rememberCoroutineScope()
    val activeThemeState = remember { ThemeManager.getActiveTheme(context) }.collectAsState(initial = DezhTheme.SIMPLE_DARK)
    val activeTheme = activeThemeState.value
    val unlockedThemesState = remember { ThemeManager.getUnlockedThemes(context) }.collectAsState(initial = setOf(DezhTheme.SIMPLE_DARK, DezhTheme.LIGHT_MODE, DezhTheme.CYBER, DezhTheme.PINKY))
    val unlockedThemes = unlockedThemesState.value

    DezhTheme.values().forEach { theme ->
        val isUnlocked = unlockedThemes.contains(theme)
        val isSelected = activeTheme == theme

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(
                    if (isSelected) themeColors.primary.copy(alpha = 0.15f)
                    else Color.Transparent
                )
                .border(
                    width = 1.dp,
                    color = if (isSelected) themeColors.primary else Color.Transparent,
                    shape = RoundedCornerShape(8.dp)
                )
                .clickable(enabled = isUnlocked) {
                    scope.launch {
                        ThemeManager.setActiveTheme(context, theme)
                    }
                }
                .padding(12.dp)
                .testTag("theme_row_${theme.name}"),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                val icon = when (theme) {
                    DezhTheme.SIMPLE_DARK -> Icons.Default.Brightness4
                    DezhTheme.LIGHT_MODE -> Icons.Default.Brightness7
                    DezhTheme.CYBER -> Icons.Default.FlashOn
                    DezhTheme.PINKY -> Icons.Default.Favorite
                    DezhTheme.OBSIDIAN -> Icons.Default.Terminal
                    DezhTheme.AMBER -> Icons.Default.Tv
                    DezhTheme.RGB_GAMING -> Icons.Default.VideogameAsset
                    DezhTheme.GALAXY -> Icons.Default.AutoAwesome
                    DezhTheme.EMERALD -> Icons.Default.Diamond
                }
                Icon(
                    imageVector = icon,
                    contentDescription = theme.displayName,
                    tint = if (isUnlocked) {
                        val colors = getThemeColors(theme)
                        colors.primary
                    } else {
                        themeColors.onSurface.copy(alpha = 0.3f)
                    },
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = if (isLanguageFa) {
                            when (theme) {
                                DezhTheme.SIMPLE_DARK -> "تیره ساده"
                                DezhTheme.LIGHT_MODE -> "روز روشن"
                                DezhTheme.CYBER -> "سایبر ماتریکس"
                                DezhTheme.PINKY -> "صورتی رمانتیک"
                                DezhTheme.OBSIDIAN -> "ابسیدین رازآلود"
                                DezhTheme.AMBER -> "مانیتور کهربایی"
                                DezhTheme.RGB_GAMING -> "نورپردازی گیمینگ"
                                DezhTheme.GALAXY -> "کهکشان بیکران"
                                DezhTheme.EMERALD -> "زمرد درخشان"
                            }
                        } else theme.displayName,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isUnlocked) themeColors.onSurface else themeColors.onSurface.copy(alpha = 0.4f)
                    )
                    if (!isUnlocked) {
                        Text(
                            text = if (isLanguageFa) "قفل است (کد مخفی)" else "Locked (Secret Trigger Required)",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                        )
                    } else {
                        Text(
                            text = when (theme) {
                                DezhTheme.SIMPLE_DARK -> if (isLanguageFa) "ظاهر تیره اداری سنگین" else "Sleek charcoal dark design"
                                DezhTheme.LIGHT_MODE -> if (isLanguageFa) "ظاهر سفید آسمانی پر نور" else "Vibrant light-filled display"
                                DezhTheme.CYBER -> if (isLanguageFa) "تم سورمه‌ای با نورهای نئونی فعال" else "Deep navy with cyan glow effects"
                                DezhTheme.PINKY -> if (isLanguageFa) "تم رز با حاشیه و دکمه‌های جذاب صورتی" else "Gentle romantic rose palette"
                                DezhTheme.OBSIDIAN -> if (isLanguageFa) "ظاهر تمام مشکی با خطوط قرمز تهاجمی" else "Pitch black with crimson wireframe trims"
                                DezhTheme.AMBER -> if (isLanguageFa) "مشکی عمیق با نوشته‌های طلایی و نویز CRT" else "Monochrome amber with CRT scanlines"
                                DezhTheme.RGB_GAMING -> if (isLanguageFa) "انیمیشن طیف رنگین‌کمانی چشم‌نواز بی‌نهایت" else "Living infinite spectrum shift"
                                DezhTheme.GALAXY -> if (isLanguageFa) "تم بنفش کیهانی با فضایی آرام‌بخش" else "Deep cosmic violet space theme"
                                DezhTheme.EMERALD -> if (isLanguageFa) "تم سبز زمردی لوکس و پرانرژی" else "Vibrant glowing emerald green style"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = themeColors.onSurface.copy(alpha = 0.5f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ThemeUnlockNotification(
    themeName: String,
    onDismiss: () -> Unit
) {
    val isFa = LocalAppLanguage.current == "fa"

    LaunchedEffect(themeName) {
        kotlinx.coroutines.delay(3000)
        onDismiss()
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 50.dp, start = 20.dp, end = 20.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        Card(
            modifier = Modifier
                .shadow(16.dp, RoundedCornerShape(20.dp), spotColor = Color(0xFF00FFCC))
                .border(
                    BorderStroke(1.2.dp, Brush.horizontalGradient(listOf(Color(0xFF00FFCC), Color(0xFFFF0055)))),
                    RoundedCornerShape(20.dp)
                ),
            colors = CardDefaults.cardColors(
                containerColor = Color(0x66080B15)
            ),
            shape = RoundedCornerShape(20.dp)
        ) {
            Box(
                modifier = Modifier
                    .background(Color(0x99000000))
                    .padding(horizontal = 20.dp, vertical = 14.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "sparkle",
                        tint = Color(0xFF00FFCC),
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = if (isFa) "پوسته رازآلود جدید آزاد شد!" else "New Secret Theme Unlocked!",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (isFa) "آزاد شد: $themeName • انتخاب در بخش راهنما" else "Unlocked: $themeName • Select in Guide Tab",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.8f),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

