package cfd.invokeil.himadri.rcy.core.util

import android.content.Context
import cfd.invokeil.himadri.rcy.core.i18n.Lang
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer

/**
 * Bangladesh geo dataset loaded from assets — mirrors the website's
 * src/data/geo (63 districts, ~481 upazilas, city-corporation thanas).
 */
object Geo {

    @Serializable
    data class District(
        val en: String,
        val bn: String,
        val div: String = "",
        val lat: Double = 0.0,
        val lng: Double = 0.0,
        val up: List<List<String>> = emptyList(),
        val th: List<List<String>>? = null,
    )

    @Serializable
    data class Division(val en: String, val bn: String)

    private var districts: List<District> = emptyList()
    private var divisions: List<Division> = emptyList()
    val BLOOD_GROUPS = listOf("A+", "A-", "B+", "B-", "O+", "O-", "AB+", "AB-")

    fun load(context: Context) {
        if (districts.isNotEmpty()) return
        runCatching {
            val dJson = context.assets.open("geo_districts.json").bufferedReader().use { it.readText() }
            districts = appJson.decodeFromString(ListSerializer(District.serializer()), dJson)
            val mJson = context.assets.open("geo_meta.json").bufferedReader().use { it.readText() }
            val meta = appJson.parseToJsonElement(mJson)
            divisions = appJson.decodeFromJsonElement(
                ListSerializer(Division.serializer()),
                meta.jsonObjectSafe("divisions"),
            )
        }
    }

    private fun kotlinx.serialization.json.JsonElement.jsonObjectSafe(key: String): kotlinx.serialization.json.JsonElement =
        (this as kotlinx.serialization.json.JsonObject)[key]!!

    val all: List<District> get() = districts
    val allDivisions: List<Division> get() = divisions

    fun findDistrict(en: String): District? = districts.firstOrNull { it.en == en }

    fun districtsOf(divisionEn: String?): List<District> =
        if (divisionEn.isNullOrBlank()) districts else districts.filter { it.div == divisionEn }

    /** Upazila options — bilingual [en, bn] pairs. */
    fun upazilas(districtEn: String): List<Pair<String, String>> =
        findDistrict(districtEn)?.up?.map { (it.getOrNull(0) ?: "") to (it.getOrNull(1) ?: "") } ?: emptyList()

    /**
     * Thana options = city-corporation thanas + own upazilas (sadar kept
     * as-is, others suffixed with "(থানা)") — same rule as the website.
     */
    fun thanas(districtEn: String): List<Pair<String, String>> {
        val d = findDistrict(districtEn) ?: return emptyList()
        val own = d.up.map { pair ->
            val en = pair.getOrNull(0) ?: ""
            val bn = pair.getOrNull(1) ?: ""
            if (bn.contains("সদর")) en to bn else en to "$bn (থানা)"
        }
        val cityThanas = d.th?.map { (it.getOrNull(0) ?: "") to (it.getOrNull(1) ?: "") } ?: emptyList()
        return cityThanas + own
    }

    fun districtLabel(en: String, lang: Lang): String {
        val d = findDistrict(en) ?: return en
        return if (lang == Lang.BN) d.bn else d.en
    }
}
