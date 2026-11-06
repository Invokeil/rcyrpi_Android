package cfd.invokeil.himadri.rcy.screens.misc

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import cfd.invokeil.himadri.rcy.LocalLang
import cfd.invokeil.himadri.rcy.core.i18n.mtl
import cfd.invokeil.himadri.rcy.core.network.Address
import cfd.invokeil.himadri.rcy.core.network.ApiClient
import cfd.invokeil.himadri.rcy.core.network.ProfileUpdateBody
import cfd.invokeil.himadri.rcy.core.theme.BrandRed
import cfd.invokeil.himadri.rcy.core.util.Geo
import cfd.invokeil.himadri.rcy.ui.components.AppIcon
import cfd.invokeil.himadri.rcy.ui.components.BrandButton
import cfd.invokeil.himadri.rcy.ui.components.BrandCard
import cfd.invokeil.himadri.rcy.ui.components.BrandField
import cfd.invokeil.himadri.rcy.ui.components.ChoiceChips
import cfd.invokeil.himadri.rcy.ui.components.EmptyState
import cfd.invokeil.himadri.rcy.ui.components.ErrorState
import cfd.invokeil.himadri.rcy.ui.components.ListSkeleton
import cfd.invokeil.himadri.rcy.ui.components.SectionHeading
import cfd.invokeil.himadri.rcy.ui.components.rememberApiState
import cfd.invokeil.himadri.rcy.ui.components.tl
import cfd.invokeil.himadri.rcy.vm.AppViewModel
import androidx.navigation.NavHostController
import kotlinx.coroutines.launch

/**
 * Profile edit — mirrors the edit variant of website profile-page.tsx:
 * personal fields + blood group + address (BD geo pickers). Username is
 * read-only (site rule: it can NEVER change). Save → POST /api/profile.
 *
 * NOTE: lives in screens.misc (not screens.profile) because the existing
 * ChildGraph import is `cfd.invokeil.himadri.rcy.screens.misc.ProfileEditScreen`.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileEditScreen(vm: AppViewModel, nav: NavHostController) {
    val scope = rememberCoroutineScope()
    val lang = LocalLang.current
    val context = LocalContext.current

    val st = rememberApiState("profile_edit") { ApiClient.api.profile() }
    val profile = st.data?.profile

    // ── editable state ─────────────────────────────────────────────────
    var fullName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var altPhone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var blood by remember { mutableStateOf<String?>(null) }
    var division by remember { mutableStateOf<String?>(null) }
    var district by remember { mutableStateOf<String?>(null) }
    var upazila by remember { mutableStateOf<String?>(null) }
    var thana by remember { mutableStateOf<String?>(null) }
    var ward by remember { mutableStateOf("") }
    var para by remember { mutableStateOf("") }

    val errs = remember { mutableStateMapOf<String, String>() }
    var saving by remember { mutableStateOf(false) }
    var saveErr by remember { mutableStateOf<String?>(null) } // raw code → mapped at display
    var picker by remember { mutableStateOf<String?>(null) } // district | upazila | thana
    var prefilled by remember { mutableStateOf(false) }

    // prefill once profile arrives (SessionUser has no alt_phone — stays blank)
    LaunchedEffect(profile?.id) {
        if (profile != null && !prefilled) {
            prefilled = true
            fullName = profile.full_name
            phone = profile.phone.orEmpty()
            email = profile.email.orEmpty()
            blood = profile.blood_group
            val a = profile.address
            district = a?.district?.takeIf { it.isNotBlank() }
            division = district?.let { Geo.findDistrict(it)?.div }
            upazila = a?.upazila?.takeIf { it.isNotBlank() }
            thana = a?.thana?.takeIf { it.isNotBlank() }
            ward = a?.ward.orEmpty()
            para = a?.para.orEmpty()
        }
    }

    // pre-translated messages
    val msgRequired = tl("required_field")
    val msgEmail = tl("fp_invalid_email")
    val msgSaved = tl("prof_saved")
    val msgGeneric = tl("error_generic")

    val districts = division?.let { Geo.districtsOf(it) } ?: emptyList()
    val upazilas = district?.let { Geo.upazilas(it) } ?: emptyList()
    val thanas = district?.let { Geo.thanas(it) } ?: emptyList()

    fun pairLabel(p: Pair<String, String>): String = if (lang.isBn) p.second else p.first

    fun save() {
        val e = mutableMapOf<String, String>()
        // website PATCH /api/profile zod schema — full_name min 3 · email ·
        // phone min 10 · address district/upazila/thana min 2 (else 400)
        if (fullName.trim().length < 3) e["name"] = msgRequired
        val em = email.trim()
        if (em.isEmpty() || !em.contains('@')) e["email"] = msgEmail
        val digits = phone.filter { it.isDigit() }
        if (digits.length < 10) e["phone"] = msgRequired
        if ((district ?: "").length < 2) e["district"] = msgRequired
        if ((upazila ?: "").length < 2) e["upazila"] = msgRequired
        if ((thana ?: "").length < 2) e["thana"] = msgRequired
        if (e.isNotEmpty()) {
            errs.clear()
            errs.putAll(e)
            return
        }
        errs.clear()
        saving = true
        saveErr = null
        scope.launch {
            runCatching {
                ApiClient.api.updateProfile(
                    ProfileUpdateBody(
                        full_name = fullName.trim(),
                        phone = phone.trim(),
                        alt_phone = altPhone.trim().ifBlank { null },
                        email = email.trim(),
                        blood_group = blood,
                        address = Address(
                            district = district.orEmpty(),
                            upazila = upazila.orEmpty(),
                            thana = thana.orEmpty(),
                            ward = ward.trim(),
                            para = para.trim(),
                        ),
                    )
                )
            }
                .onSuccess { res ->
                    if (res.profile != null) {
                        Toast.makeText(context, msgSaved, Toast.LENGTH_SHORT).show()
                        nav.popBackStack()
                    } else {
                        saveErr = res.error ?: "error_generic"
                    }
                }
                .onFailure { saveErr = "network_error" }
            saving = false
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { nav.popBackStack() }) {
                Icon(AppIcon.of("arrow-left"), contentDescription = null)
            }
            SectionHeading(
                title = tl("prof_title"),
                subtitle = tl("prof_sub"),
                modifier = Modifier.weight(1f),
            )
        }

        when {
            st.loading -> ListSkeleton(4)
            st.error != null -> ErrorState(st.error) { scope.launch { st.load() } }
            profile == null -> EmptyState(AppIcon.of("user"), tl("error_generic"))
            else -> {
                // ── username (locked) ──────────────────────────────────
                BrandField(
                    value = profile.username,
                    onValueChange = {},
                    label = tl("signup_username"),
                    leading = AppIcon.of("user"),
                    readOnly = true,
                    supportingText = tl("prof_username_locked"),
                )

                // ── personal ───────────────────────────────────────────
                BrandField(
                    value = fullName,
                    onValueChange = { fullName = it },
                    label = tl("signup_fullname"),
                    isError = errs.containsKey("name"),
                    supportingText = errs["name"],
                )
                BrandField(
                    value = email,
                    onValueChange = { email = it },
                    label = tl("signup_email"),
                    keyboardType = KeyboardType.Email,
                    isError = errs.containsKey("email"),
                    supportingText = errs["email"],
                )
                BrandField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = tl("signup_phone"),
                    keyboardType = KeyboardType.Phone,
                    isError = errs.containsKey("phone"),
                    supportingText = errs["phone"],
                )
                BrandField(
                    value = altPhone,
                    onValueChange = { altPhone = it },
                    label = tl("signup_altphone"),
                    keyboardType = KeyboardType.Phone,
                )

                Text(
                    tl("signup_blood"),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Box(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())) {
                    ChoiceChips(
                        options = Geo.BLOOD_GROUPS.map { it to it },
                        selected = blood,
                        onSelect = { v -> blood = if (blood == v) null else v },
                    )
                }

                // ── address ────────────────────────────────────────────
                Text(
                    tl("prof_address"),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Box(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())) {
                    ChoiceChips(
                        options = Geo.allDivisions.map { it.en to (if (lang.isBn) it.bn else it.en) },
                        selected = division,
                        onSelect = { v ->
                            division = v
                            district = null
                            upazila = null
                            thana = null
                        },
                    )
                }
                PickerField(
                    label = tl("addr_district"),
                    value = district?.let { Geo.districtLabel(it, lang) },
                    placeholder = tl("addr_pick_first"),
                    enabled = division != null,
                    onOpen = { picker = "district" },
                    isError = errs.containsKey("district"),
                )
                PickerField(
                    label = tl("addr_upazila"),
                    value = upazila?.let { en -> upazilas.firstOrNull { it.first == en }?.let { pairLabel(it) } },
                    placeholder = tl("addr_pick_first"),
                    enabled = district != null,
                    onOpen = { picker = "upazila" },
                    isError = errs.containsKey("upazila"),
                )
                PickerField(
                    label = tl("addr_thana"),
                    value = thana?.let { en -> thanas.firstOrNull { it.first == en }?.let { pairLabel(it) } },
                    placeholder = tl("addr_pick_first"),
                    enabled = district != null,
                    onOpen = { picker = "thana" },
                    isError = errs.containsKey("thana"),
                )
                BrandField(
                    value = ward,
                    onValueChange = { ward = it },
                    label = tl("addr_ward"),
                    keyboardType = KeyboardType.Number,
                )
                BrandField(
                    value = para,
                    onValueChange = { para = it },
                    label = tl("addr_para"),
                )

                if (saveErr != null) {
                    Text(
                        when {
                            saveErr?.contains("email", ignoreCase = true) == true -> tl("prof_email_taken")
                            saveErr?.contains("phone", ignoreCase = true) == true -> tl("prof_phone_taken")
                            else -> msgGeneric
                        },
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                    )
                }

                BrandButton(
                    text = tl("prof_save"),
                    onClick = { save() },
                    modifier = Modifier.fillMaxWidth(),
                    loading = saving,
                )
            }
        }
    }

    // ── geo picker bottom sheet ────────────────────────────────────────
    if (picker != null) {
        ModalBottomSheet(onDismissRequest = { picker = null }) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 28.dp),
            ) {
                when (picker) {
                    "district" -> {
                        var q by remember { mutableStateOf("") }
                        BrandField(
                            value = q,
                            onValueChange = { q = it },
                            label = mtl("search_ph"),
                            singleLine = true,
                        )
                        Spacer(Modifier.size(8.dp))
                        val filtered = districts.filter {
                            it.en.contains(q, ignoreCase = true) || it.bn.contains(q)
                        }
                        LazyColumn(Modifier.fillMaxWidth().heightIn(max = 420.dp)) {
                            items(filtered, key = { it.en }) { d ->
                                SheetRow(
                                    label = if (lang.isBn) d.bn else d.en,
                                    selected = district == d.en,
                                ) {
                                    district = d.en
                                    upazila = null
                                    thana = null
                                    picker = null
                                }
                            }
                        }
                    }
                    "upazila" -> {
                        LazyColumn(Modifier.fillMaxWidth().heightIn(max = 420.dp)) {
                            items(upazilas) { p ->
                                SheetRow(label = pairLabel(p), selected = upazila == p.first) {
                                    upazila = p.first
                                    picker = null
                                }
                            }
                        }
                    }
                    "thana" -> {
                        LazyColumn(Modifier.fillMaxWidth().heightIn(max = 420.dp)) {
                            items(thanas) { p ->
                                SheetRow(label = pairLabel(p), selected = thana == p.first) {
                                    thana = p.first
                                    picker = null
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ── file-private helpers ───────────────────────────────────────────────

@Composable
private fun PickerField(
    label: String,
    value: String?,
    placeholder: String?,
    enabled: Boolean,
    onOpen: () -> Unit,
    isError: Boolean = false,
) {
    Box {
        BrandField(
            value = value.orEmpty(),
            onValueChange = {},
            label = label,
            readOnly = true,
            placeholder = placeholder,
            isError = isError,
            trailing = {
                Icon(
                    AppIcon.of("chevron-down"),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            },
        )
        Box(Modifier.matchParentSize().clickable(enabled = enabled, onClick = onOpen))
    }
}

@Composable
private fun SheetRow(label: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            modifier = Modifier.weight(1f),
        )
        if (selected) {
            Icon(AppIcon.of("check"), contentDescription = null, tint = BrandRed, modifier = Modifier.size(18.dp))
        }
    }
}
