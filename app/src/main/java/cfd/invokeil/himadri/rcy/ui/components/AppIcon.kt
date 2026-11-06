package cfd.invokeil.himadri.rcy.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Attachment
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Bloodtype
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationImportant
import androidx.compose.material.icons.filled.Paid
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.Work
import androidx.compose.material.icons.filled.Water
import androidx.compose.material.icons.filled.Waves
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Person
import androidx.compose.ui.graphics.vector.ImageVector
import cfd.invokeil.himadri.rcy.core.network.SessionUser

/**
 * Website uses lucide icons by string name via its AppIcon registry.
 * This mapper resolves the same names to Material icons (fallback: heart).
 */
object AppIcon {
    fun of(name: String?): ImageVector = when (name?.lowercase()?.trim()) {
        "heart", "heart-handshake", "droplet", "droplets", "blood" -> Icons.Filled.Bloodtype
        "siren", "alert", "alert-triangle", "triangle-alert" -> Icons.Filled.Warning
        "bell", "bell-ring", "notification" -> Icons.Filled.Notifications
        "calendar", "calendar-days", "date" -> Icons.Filled.CalendarMonth
        "event" -> Icons.Filled.Event
        "clock", "timer", "time" -> Icons.Filled.Schedule
        "hourglass" -> Icons.Filled.HourglassTop
        "map-pin", "map", "location", "pin" -> Icons.Filled.LocationOn
        "phone", "phone-call", "call" -> Icons.Filled.Call
        "smartphone", "mobile" -> Icons.Filled.PhoneAndroid
        "mail", "email", "at-sign" -> Icons.Filled.Email
        "user", "users", "user-plus", "member" -> Icons.Filled.Person
        "group", "users-2", "community" -> Icons.Filled.Group
        "megaphone", "broadcast", "announce" -> Icons.Filled.Campaign
        "home" -> Icons.Filled.Home
        "search" -> Icons.Filled.Search
        "menu", "hamburger" -> Icons.Filled.Menu
        "settings", "cog" -> Icons.Filled.Settings
        "shield", "shield-check" -> Icons.Filled.Shield
        "lock", "key" -> Icons.Filled.Lock
        "verified", "badge-check" -> Icons.Filled.Verified
        "badge", "id", "id-card" -> Icons.Filled.Badge
        "qr", "qr-code" -> Icons.Filled.QrCode2
        "download" -> Icons.Filled.Download
        "print" -> Icons.Filled.Print
        "share", "share-2" -> Icons.Filled.Share
        "copy" -> Icons.Filled.Collections
        "edit", "pencil" -> Icons.Filled.Edit
        "trash", "delete" -> Icons.Filled.Delete
        "check", "check-circle", "circle-check" -> Icons.Filled.CheckCircle
        "x", "close" -> Icons.Filled.Close
        "plus", "add" -> Icons.Filled.Add
        "send" -> Icons.AutoMirrored.Filled.Send
        "link", "external-link" -> Icons.Filled.Link
        "globe", "language" -> Icons.Filled.Language
        "sun", "weather" -> Icons.Filled.WbSunny
        "cloud", "cloud-rain" -> Icons.Filled.Cloud
        "waves", "flood" -> Icons.Filled.Waves
        "water" -> Icons.Filled.Water
        "fire", "flame" -> Icons.Filled.LocalFireDepartment
        "zap", "flash", "lightning" -> Icons.Filled.FlashOn
        "stethoscope", "medical", "health" -> Icons.Filled.MedicalServices
        "hospital" -> Icons.Filled.HealthAndSafety
        "activity", "pulse" -> Icons.Filled.ShowChart
        "help", "help-circle", "question" -> Icons.Filled.Help
        "info" -> Icons.Filled.Info
        "trophy", "award", "medal", "crown" -> Icons.Filled.EmojiEvents
        "star" -> Icons.Filled.Star
        "leaderboard", "rank", "bar-chart" -> Icons.Filled.Leaderboard
        "gift" -> Icons.Filled.CardGiftcard
        "wallet", "money", "banknote", "coins", "taka" -> Icons.Filled.Paid
        "receipt" -> Icons.Filled.Description
        "file", "file-text", "document", "certificate" -> Icons.Filled.Description
        "handshake" -> Icons.Filled.Handshake
        "volunteer", "hands" -> Icons.Filled.Work
        "settings-2", "sliders" -> Icons.Filled.Settings
        "logout", "sign-out" -> Icons.Filled.Logout
        "login", "sign-in" -> Icons.Filled.Lock
        "eye", "view" -> Icons.Filled.Visibility
        "eye-off" -> Icons.Filled.VisibilityOff
        "refresh", "rotate" -> Icons.Filled.Refresh
        "speaker", "volume" -> Icons.AutoMirrored.Filled.VolumeUp
        "attach", "paperclip" -> Icons.Filled.Attachment
        "arrow-left", "back" -> Icons.AutoMirrored.Filled.ArrowBack
        "chevron-down" -> Icons.Filled.ExpandMore
        "chevron-up" -> Icons.Filled.ExpandLess
        "building", "school" -> Icons.Filled.Build
        "wrench", "tool" -> Icons.Filled.Build
        else -> Icons.Filled.Favorite
    }

    /** bottom-bar icons */
    val home = Icons.Filled.Home
    val notices = Icons.Filled.Notifications
    val noticesOutlined = Icons.Outlined.Notifications
    val events = Icons.Filled.Event
    val profile = Icons.Filled.Person
    val profileOutlined = Icons.Outlined.Person
    val leaderboard = Icons.Filled.Leaderboard
    val payments = Icons.Filled.Paid
    val request = Icons.Filled.NotificationImportant
    val settings = Icons.Filled.Settings
}

/** Avatar initial + blood-group fallback for members without a photo. */
fun SessionUser.initials(): String = full_name.split(" ")
    .filter { it.isNotBlank() }.take(2).mapNotNull { it.firstOrNull()?.uppercaseChar() }
    .joinToString("").ifBlank { username.take(2).uppercase() }
