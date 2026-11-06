package cfd.invokeil.himadri.rcy.screens.emergency

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import cfd.invokeil.himadri.rcy.LocalLang
import cfd.invokeil.himadri.rcy.Routes
import cfd.invokeil.himadri.rcy.core.i18n.Lang
import cfd.invokeil.himadri.rcy.core.network.ApiClient
import cfd.invokeil.himadri.rcy.core.network.EmergencyRequestBody
import cfd.invokeil.himadri.rcy.core.network.EmergencyRequest
import cfd.invokeil.himadri.rcy.core.theme.BrandGreen
import cfd.invokeil.himadri.rcy.core.theme.BrandGreenDark
import cfd.invokeil.himadri.rcy.core.theme.BrandGreenSoft
import cfd.invokeil.himadri.rcy.core.theme.BrandRed
import cfd.invokeil.himadri.rcy.core.theme.Chart3
import cfd.invokeil.himadri.rcy.core.util.Geo
import cfd.invokeil.himadri.rcy.core.util.ShareUtil
import cfd.invokeil.himadri.rcy.ui.components.AppIcon
import cfd.invokeil.himadri.rcy.ui.components.BrandBtnStyle
import cfd.invokeil.himadri.rcy.ui.components.BrandButton
import cfd.invokeil.himadri.rcy.ui.components.BrandCard
import cfd.invokeil.himadri.rcy.ui.components.BrandField
import cfd.invokeil.himadri.rcy.ui.components.ChoiceChips
import cfd.invokeil.himadri.rcy.ui.components.errorText
import cfd.invokeil.himadri.rcy.core.i18n.mtl
import cfd.invokeil.himadri.rcy.ui.components.tl
import cfd.invokeil.himadri.rcy.vm.AppViewModel
import kotlinx.coroutines.launch
import retrofit2.HttpException

/**
 * Emergency Request form — mirrors website request-page.tsx:
 * 7 kind cards, urgency chips, kind-specific fields, district picker,
 * inline validation and a success card. Login-gated (server requires session).
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun EmergencyRequestScreen(vm: AppViewModel, nav: NavHostController) {
    val lang = LocalLang.current
    val context = LocalContext.current
    val session by vm.session.collectAsState()
    val scope = rememberCoroutineScope()

    // ── form state ────────────────────────────────────────────────────
    var kind by remember { mutableStateOf<String?>(null) }
    var urgency by remember { mutableStateOf("within_1_2_hr") }
    var phone by remember { mutableStateOf("") }
    var altPhone by remember { mutableStateOf("") }
    var patientName by remember { mutableStateOf("") }
    var hospital by remember { mutableStateOf("") }
    var bloodGroup by remember { mutableStateOf<String?>(null) }
    var patientType by remember { mutableStateOf<String?>(null) }
    var location by remember { mutableStateOf("") }
    var detailLocation by remember { mutableStateOf("") }
    var neededAt by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }

    var kindErr by remember { mutableStateOf<String?>(null) }
    var phoneErr by remember { mutableStateOf<String?>(null) }
    var hospitalErr by remember { mutableStateOf<String?>(null) }
    var bloodErr by remember { mutableStateOf<String?>(null) }
    var locErr by remember { mutableStateOf<String?>(null) }
    var neededErr by remember { mutableStateOf<String?>(null) }

    var submitting by remember { mutableStateOf(false) }
    var submitError by remember { mutableStateOf<String?>(null) }
    var sentRequest by remember { mutableStateOf<EmergencyRequest?>(null) }

    var showDistricts by remember { mutableStateOf(false) }
    var selDivision by remember { mutableStateOf<String?>(null) }

    // prefill from logged-in member once session is available
    androidx.compose.runtime.LaunchedEffect(session?.id) {
        if (phone.isBlank() && !session?.phone.isNullOrBlank()) phone = session?.phone ?: ""
        if (location.isBlank() && !session?.address?.district.isNullOrBlank()) location = session?.address?.district ?: ""
    }

    val needHospital = kind == "blood" || kind == "accident" || kind == "medical"
    val needBlood = kind == "blood"

    // ── guest gate ────────────────────────────────────────────────────
    if (session == null) {
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(48.dp))
            BrandCard(Modifier.fillMaxWidth()) {
                Column(
                    Modifier.fillMaxWidth().padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Box(
                        Modifier.size(64.dp).background(BrandRed.copy(alpha = 0.1f), CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(AppIcon.of("lock"), contentDescription = null, tint = BrandRed, modifier = Modifier.size(30.dp))
                    }
                    Spacer(Modifier.height(12.dp))
                    Text(
                        tl("req_title"),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        tl("req_login_note"),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(Modifier.height(16.dp))
                    BrandButton(tl("login_btn")) { nav.navigate("ext_login") }
                }
            }
        }
        return
    }

    // ── success card ──────────────────────────────────────────────────
    sentRequest?.let { req ->
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(48.dp))
            BrandCard(Modifier.fillMaxWidth()) {
                Column(
                    Modifier.fillMaxWidth().padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Box(
                        Modifier.size(80.dp).background(BrandGreenSoft, CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(AppIcon.of("check-circle"), contentDescription = null, tint = BrandGreen, modifier = Modifier.size(44.dp))
                    }
                    Spacer(Modifier.height(14.dp))
                    Text(
                        tl("req_success"),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = BrandGreen,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(Modifier.height(14.dp))
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceContainerHigh, RoundedCornerShape(12.dp))
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        SummaryRow(tl("req_kind"), kindLabel(req.kind, lang))
                        SummaryRow(tl("req_urgency"), urgLabel(req.urgency))
                        SummaryRow(tl("req_location"), Geo.districtLabel(req.location, lang))
                        SummaryRow(tl("req_phone"), req.phone)
                        req.hospital?.let { SummaryRow(tl("req_hospital"), it) }
                        req.blood_group?.let { SummaryRow(tl("req_blood_group"), it) }
                    }
                    Spacer(Modifier.height(16.dp))
                    BrandButton(tl("strip_view_all"), { nav.navigate(Routes.EMERGENCY_FEED) }, Modifier.fillMaxWidth())
                    BrandButton(
                        mtl("back_home"),
                        {
                            nav.navigate(Routes.HOME) {
                                popUpTo(Routes.HOME) { inclusive = true }
                                launchSingleTop = true
                            }
                        },
                        Modifier.fillMaxWidth(),
                        style = BrandBtnStyle.GHOST,
                    )
                }
            }
        }
        return
    }

    // ── main form ─────────────────────────────────────────────────────
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        // header
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                Modifier.size(56.dp).background(BrandRed.copy(alpha = 0.1f), RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(AppIcon.of("siren"), contentDescription = null, tint = BrandRed, modifier = Modifier.size(28.dp))
            }
            Spacer(Modifier.height(10.dp))
            Text(
                tl("req_title"),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                color = BrandRed,
                textAlign = TextAlign.Center,
            )
            Text(
                tl("req_sub"),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }

        // ── kind picker (7 cards) ─────────────────────────────────────
        BrandCard(Modifier.fillMaxWidth()) {
            Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(tl("req_kind"), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                REQ_KINDS.chunked(2).forEach { rowKinds ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        rowKinds.forEach { k ->
                            KindCard(
                                k = k,
                                lang = lang,
                                selected = kind == k.key,
                                modifier = Modifier.weight(1f),
                            ) {
                                kind = k.key
                                kindErr = null
                            }
                        }
                    }
                }
                kindErr?.let { Text(it, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = BrandRed) }
            }
        }

        // ── urgency ───────────────────────────────────────────────────
        BrandCard(Modifier.fillMaxWidth()) {
            Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(tl("req_urgency"), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                URGENCY_OPTIONS.chunked(2).forEach { chunk ->
                    ChoiceChips(chunk.map { it.first to tl(it.second) }, urgency) { urgency = it }
                }
            }
        }

        // ── fields ────────────────────────────────────────────────────
        BrandCard(Modifier.fillMaxWidth()) {
            Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                BrandField(patientName, { patientName = it }, tl("req_patient_name"))

                BrandField(
                    phone, { phone = it; phoneErr = null }, tl("req_phone"),
                    isError = phoneErr != null, supportingText = phoneErr,
                    keyboardType = KeyboardType.Phone,
                )
                BrandField(
                    altPhone, { altPhone = it }, tl("req_alt_phone"),
                    keyboardType = KeyboardType.Phone,
                )

                if (needHospital) {
                    BrandField(
                        hospital, { hospital = it; hospitalErr = null }, tl("req_hospital"),
                        isError = hospitalErr != null, supportingText = hospitalErr,
                    )
                }

                if (needBlood) {
                    Text(tl("req_blood_group"), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Geo.BLOOD_GROUPS.chunked(4).forEach { chunk ->
                        ChoiceChips(chunk.map { it to it }, bloodGroup) { bloodGroup = it; bloodErr = null }
                    }
                    bloodErr?.let { Text(it, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = BrandRed) }

                    Text(tl("req_patient_type"), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    PATIENT_TYPES.chunked(2).forEach { chunk ->
                        ChoiceChips(chunk.map { it.first to tl(it.second) }, patientType) { patientType = it }
                    }
                }

                // district picker (Geo)
                Text(tl("req_location"), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Row(
                    Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh, RoundedCornerShape(12.dp))
                        .clickable { showDistricts = true }
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        if (location.isBlank()) (if (lang.isBn) "জেলা বাছুন" else "Select a district")
                        else Geo.districtLabel(location, lang),
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (location.isBlank()) MaterialTheme.colorScheme.onSurfaceVariant
                        else MaterialTheme.colorScheme.onSurface,
                    )
                    Icon(
                        AppIcon.of("chevron-down"), contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp),
                    )
                }
                locErr?.let { Text(it, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = BrandRed) }

                BrandField(detailLocation, { detailLocation = it }, tl("req_detail_location"))
                BrandField(
                    neededAt, { neededAt = it; neededErr = null }, tl("req_needed_at"),
                    isError = neededErr != null, supportingText = neededErr,
                )
                BrandField(
                    note,
                    { if (it.length <= 600) note = it },
                    tl("req_note"),
                    singleLine = false,
                    minLines = 3,
                    supportingText = "${note.length}/600",
                )
            }
        }

        // ── urgent note: volunteers get notified ──────────────────────
        Row(
            Modifier
                .fillMaxWidth()
                .background(BrandRed.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Icon(AppIcon.of("siren"), contentDescription = null, tint = BrandRed, modifier = Modifier.size(20.dp))
            Column {
                Text(
                    if (lang.isBn) "রিকোয়েস্ট পাঠালে স্বেচ্ছাসেবীরা সাথে সাথেই নোটিফিকেশন পাবে — সাড়া দিলে এই বার্তা পাঠাবে:"
                    else "Volunteers are notified instantly when you submit — when responding they will send:",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = BrandRed,
                )
                Text(
                    "\u201C" + tl("otap_default_msg") + "\u201D",
                    style = MaterialTheme.typography.bodySmall,
                    color = BrandRed.copy(alpha = 0.85f),
                )
            }
        }

        // ── national emergency helper ─────────────────────────────────
        Row(
            Modifier
                .fillMaxWidth()
                .background(BrandRed, RoundedCornerShape(12.dp))
                .clickable { ShareUtil.dial(context, "999") }
                .padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(AppIcon.of("phone"), contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
            Column {
                Text(mtl("call_emergency"), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = Color.White)
                Text(
                    mtl("national_emergency"),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.85f),
                )
            }
        }

        // ── submit ────────────────────────────────────────────────────
        submitError?.let {
            Text(
                it,
                style = MaterialTheme.typography.bodySmall,
                color = BrandRed,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        BrandButton(
            tl("req_submit"),
            {
                // validate
                kindErr = null; phoneErr = null; hospitalErr = null
                bloodErr = null; locErr = null; neededErr = null
                submitError = null
                var ok = true
                if (kind == null) { kindErr = tl("required_field"); ok = false }
                if (phone.filter { ch -> ch.isDigit() }.length < 6) { phoneErr = tl("required_field"); ok = false }
                if (location.isBlank()) { locErr = tl("required_field"); ok = false }
                if (needHospital && hospital.isBlank()) { hospitalErr = tl("required_field"); ok = false }
                if (needBlood && bloodGroup == null) { bloodErr = tl("required_field"); ok = false }
                if (urgency == "scheduled" && neededAt.isBlank()) { neededErr = tl("required_field"); ok = false }
                if (!ok) return@BrandButton

                submitting = true
                scope.launch {
                    runCatching {
                        ApiClient.api.submitEmergencyRequest(
                            EmergencyRequestBody(
                                kind = kind ?: "other",
                                urgency = urgency,
                                // length caps mirror the website's zod schema
                                // (longer input → 400 invalid_input server-side)
                                patient_name = patientName.trim().take(120).ifBlank { null },
                                phone = phone.trim().take(20),
                                alt_phone = altPhone.trim().take(20).ifBlank { null },
                                hospital = if (needHospital) hospital.trim().take(160) else null,
                                blood_group = if (needBlood) bloodGroup?.take(4) else null,
                                patient_type = if (needBlood) patientType?.trim()?.take(60) else null,
                                location = location.trim().take(120),
                                detail_location = detailLocation.trim().take(240).ifBlank { null },
                                needed_at = neededAt.trim().take(40).ifBlank { null },
                                note = note.trim().take(600).ifBlank { null },
                            )
                        )
                    }
                        .onSuccess { r ->
                            submitting = false
                            val req = r.request
                            if (req != null) {
                                sentRequest = req
                            } else {
                                submitError = r.error?.let { code -> errorText(code).ifBlank { null } } ?: tl("error_generic")
                            }
                        }
                        .onFailure { t ->
                            submitting = false
                            submitError = if (t is HttpException && t.code() == 401) {
                                tl("req_login_note")
                            } else {
                                errorText(null) + (t.message ?: "")
                            }
                        }
                }
            },
            Modifier.fillMaxWidth(),
            enabled = !submitting,
            loading = submitting,
        )

        Spacer(Modifier.height(20.dp))
    }

    // ── district picker sheet ─────────────────────────────────────────
    if (showDistricts) {
        ModalBottomSheet(onDismissRequest = { showDistricts = false }) {
            Column(
                Modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text(tl("req_location"), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    val activeDivision = selDivision
                        ?: Geo.findDistrict(location)?.div
                        ?: Geo.allDivisions.firstOrNull()?.en
                    Geo.allDivisions.forEach { d ->
                        BrandChip(
                            label = if (lang.isBn) d.bn else d.en,
                            selected = activeDivision == d.en,
                        ) { selDivision = d.en }
                    }
                }
                HorizontalDivider()
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    val div = selDivision
                        ?: Geo.findDistrict(location)?.div
                        ?: Geo.allDivisions.firstOrNull()?.en
                    Geo.districtsOf(div).forEach { d ->
                        BrandChip(
                            label = if (lang.isBn) d.bn else d.en,
                            selected = location == d.en,
                        ) {
                            location = d.en
                            locErr = null
                            showDistricts = false
                        }
                    }
                }
            }
        }
    }
}

// ── kind card (selector) ───────────────────────────────────────────────
@Composable
private fun KindCard(k: ReqKind, lang: Lang, selected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Column(
        modifier
            .background(
                if (selected) BrandRed.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surfaceContainerHigh,
                RoundedCornerShape(14.dp),
            )
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Box(
            Modifier.size(38.dp).background(k.color.copy(alpha = 0.14f), RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(AppIcon.of(k.icon), contentDescription = null, tint = k.color, modifier = Modifier.size(20.dp))
        }
        Text(
            if (lang.isBn) k.bn else k.en,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = if (selected) BrandRed else MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
        )
    }
}

// ── brand-styled chip (divisions / districts) ──────────────────────────
@Composable
private fun BrandChip(label: String, selected: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label) },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = BrandRed.copy(alpha = 0.12f),
            selectedLabelColor = BrandRed,
        ),
    )
}

@Composable
private fun SummaryRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.End,
            modifier = Modifier.padding(start = 16.dp),
        )
    }
}

@Composable
private fun kindLabel(key: String, lang: Lang): String =
    REQ_KINDS.firstOrNull { it.key == key }?.let { if (lang.isBn) it.bn else it.en } ?: key

@Composable
internal fun urgLabel(v: String): String = when (v) {
    "immediate" -> tl("urg_immediate")
    "within_1_2_hr" -> tl("urg_1_2")
    "afternoon" -> tl("urg_afternoon")
    "evening" -> tl("urg_evening")
    "tomorrow" -> tl("urg_tomorrow")
    "scheduled" -> tl("urg_scheduled")
    else -> v
}

// ── static config ──────────────────────────────────────────────────────
private data class ReqKind(val key: String, val icon: String, val bn: String, val en: String, val color: Color)

/** Kind labels — not in the server dictionary (site hardcodes them too). */
private val REQ_KINDS = listOf(
    ReqKind("blood", "blood", "রক্ত প্রয়োজন", "Blood Needed", BrandRed),
    ReqKind("accident", "zap", "দুর্ঘটনা", "Accident", Chart3),
    ReqKind("fire", "fire", "অগ্নিকাণ্ড", "Fire", BrandRed),
    ReqKind("flood", "waves", "বন্যা", "Flood", BrandGreen),
    ReqKind("medical", "stethoscope", "মেডিকেল", "Medical", BrandGreen),
    ReqKind("missing", "search", "নিখোঁজ", "Missing", Color(0xFF62626E)),
    ReqKind("other", "help", "অন্যান্য", "Other", BrandGreenDark),
)

/** (stored value, dictionary key) — values match the website's DB payloads. */
private val URGENCY_OPTIONS = listOf(
    "immediate" to "urg_immediate",
    "within_1_2_hr" to "urg_1_2",
    "afternoon" to "urg_afternoon",
    "evening" to "urg_evening",
    "tomorrow" to "urg_tomorrow",
    "scheduled" to "urg_scheduled",
)

private val PATIENT_TYPES = listOf(
    "Pregnant" to "pt_pregnant",
    "Surgery" to "pt_surgery",
    "Child" to "pt_child",
    "Elderly" to "pt_elderly",
    "Hand broken" to "pt_hand",
    "Leg broken" to "pt_leg",
    "Cancer / Chronic" to "pt_cancer",
    "Other" to "pt_other",
)
