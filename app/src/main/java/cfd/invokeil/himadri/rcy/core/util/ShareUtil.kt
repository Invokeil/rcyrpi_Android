package cfd.invokeil.himadri.rcy.core.util

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.provider.Settings
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream

/** One-tap emergency response + share actions (Call / WhatsApp / SMS / Map / share). */
object ShareUtil {

    fun dial(context: Context, phone: String) {
        runCatching {
            context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:${phone.filter { it.isDigit() || it == '+' }}")).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        }
    }

    fun whatsapp(context: Context, phone: String, text: String? = null) {
        val digits = phone.filter { it.isDigit() }
        val normalized = when {
            digits.startsWith("880") -> digits
            digits.startsWith("0") -> "88$digits"
            else -> "880$digits"
        }
        val url = "https://wa.me/$normalized" + (text?.let { "?text=${Uri.encode(it)}" } ?: "")
        runCatching {
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        }.onFailure {
            dial(context, phone)
        }
    }

    fun sms(context: Context, phone: String, body: String? = null) {
        val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:${phone.filter { it.isDigit() || it == '+' }}"))
        body?.let { intent.putExtra("sms_body", it) }
        runCatching { context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }
    }

    /** Opens a raw URL (map_link, notice link, gateway checkout…). */
    fun openUrl(context: Context, url: String) {
        runCatching {
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        }
    }

    /** Google Maps search for a free-text place. */
    fun openMap(context: Context, query: String) {
        openUrl(context, "https://www.google.com/maps/search/?api=1&query=${Uri.encode(query)}")
    }

    fun shareText(context: Context, subject: String, text: String) {
        val send = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, text)
        }
        runCatching { context.startActivity(Intent.createChooser(send, subject).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }
    }

    /** Copies raw text to clipboard. */
    fun copy(context: Context, label: String, text: String) {
        val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
        cm.setPrimaryClip(android.content.ClipData.newPlainText(label, text))
        android.widget.Toast.makeText(context, label, android.widget.Toast.LENGTH_SHORT).show()
    }

    fun appSettings(context: Context) {
        runCatching {
            context.startActivity(
                Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", context.packageName, null))
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        }
    }

    /** Writes a bitmap to cache/shares and returns a shareable content URI. */
    fun bitmapUri(context: Context, bitmap: Bitmap, name: String): Uri {
        val dir = File(context.cacheDir, "shares").apply { mkdirs() }
        val file = File(dir, "$name.png")
        FileOutputStream(file).use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    }

    fun shareImage(context: Context, uri: Uri, text: String) {
        val send = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_TEXT, text)
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        runCatching { context.startActivity(Intent.createChooser(send, text).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }
    }
}
