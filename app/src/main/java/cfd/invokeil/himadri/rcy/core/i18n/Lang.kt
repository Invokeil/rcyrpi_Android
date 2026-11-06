package cfd.invokeil.himadri.rcy.core.i18n

import kotlinx.serialization.Serializable

/** Which language the UI is rendered in. Site default: BN. */
enum class Lang(val code: String) {
    BN("bn"), EN("en");
    val isBn: Boolean get() = this == BN
    companion object {
        fun from(s: String?) = entries.firstOrNull { it.code == s } ?: BN
    }
}

/** Bilingual text stored on server content items (notices, events...). */
@Serializable
data class LText(
    val en: String? = null,
    val bn: String? = null,
) {
    fun pick(lang: Lang): String = if (lang == Lang.BN) {
        bn?.takeIf { it.isNotBlank() } ?: en ?: ""
    } else {
        en?.takeIf { it.isNotBlank() } ?: bn ?: ""
    }

    companion object {
        val EMPTY = LText("", "")
    }
}
