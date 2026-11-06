package cfd.invokeil.himadri.rcy.screens.auth

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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import cfd.invokeil.himadri.rcy.LocalAppVM
import cfd.invokeil.himadri.rcy.LocalLang
import cfd.invokeil.himadri.rcy.core.i18n.mtl
import cfd.invokeil.himadri.rcy.core.network.Address
import cfd.invokeil.himadri.rcy.core.network.ApiClient
import cfd.invokeil.himadri.rcy.core.network.SignupBody
import cfd.invokeil.himadri.rcy.core.theme.BrandGradient
import cfd.invokeil.himadri.rcy.core.theme.BrandRed
import cfd.invokeil.himadri.rcy.core.util.Geo
import cfd.invokeil.himadri.rcy.ui.components.AppIcon
import cfd.invokeil.himadri.rcy.ui.components.BrandBtnStyle
import cfd.invokeil.himadri.rcy.ui.components.BrandButton
import cfd.invokeil.himadri.rcy.ui.components.BrandCard
import cfd.invokeil.himadri.rcy.ui.components.BrandField
import cfd.invokeil.himadri.rcy.ui.components.BrandPasswordField
import cfd.invokeil.himadri.rcy.ui.components.ChoiceChips
import cfd.invokeil.himadri.rcy.ui.components.tl
import kotlinx.coroutines.launch

/**
 * Member signup — mirrors website signup-page.tsx:
 * 4-step wizard (Account → Personal → Address → Review) with progress dots,
 * real BD geo pickers (division chips → district/upazila/thana bottom sheets),
 * inline validation and friendly error mapping on submit.
 *
 * Call sites (RCYNavGraph + ChildGraph) pass no vm — resolved via LocalAppVM.
 */
private val STEPS = listOf("st_account", "st_personal", "st_address", "st_review")
private val USERNAME_RE = Regex("^[a-z0-9_]{3,20}$")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignupScreen(
    onBack: () -> Unit,
    onLogin: () -> Unit,
    onSuccess: () -> Unit,
) {
    val vm = LocalAppVM.current
    val scope = rememberCoroutineScope()
    val lang = LocalLang.current

    // ── form state ─────────────────────────────────────────────────────
    var step by remember { mutableIntStateOf(0) }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var altPhone by remember { mutableStateOf("") }
    var blood by remember { mutableStateOf<String?>(null) }
    var division by remember { mutableStateOf<String?>(null) }
    var district by remember { mutableStateOf<String?>(null) }
    var upazila by remember { mutableStateOf<String?>(null) }
    var thana by remember { mutableStateOf<String?>(null) }
    var ward by remember { mutableStateOf("") }
    var para by remember { mutableStateOf("") }

    val errs = remember { mutableStateMapOf<String, String>() }
    var submitting by remember { mutableStateOf(false) }
    var apiError by remember { mutableStateOf<String?>(null) }
    var picker by remember { mutableStateOf<String?>(null) } // district | upazila | thana

    // pre-translated messages (tl is @Composable — capture before plain lambdas)
    val msgRequired = tl("required_field")
    val msgPwShort = tl("prof_pw_short")
    val msgPwMismatch = tl("prof_pw_mismatch")
    val msgEmail = tl("fp_invalid_email")
    val msgGeneric = tl("error_generic")
    val msgUsername = if (lang.isBn) "a-z, 0-9, _ — ৩ থেকে ২০ অক্ষর"
    else "a-z, 0-9 and _ — 3 to 20 characters"
    val msgPhone = if (lang.isBn) "১১ সংখ্যার নম্বর দিন (01 দিয়ে শুরু)"
    else "11-digit BD number starting with 01"

    val districts = division?.let { Geo.districtsOf(it) } ?: emptyList()
    val upazilas = district?.let { Geo.upazilas(it) } ?: emptyList()
    val thanas = district?.let { Geo.thanas(it) } ?: emptyList()

    fun pairLabel(p: Pair<String, String>): String = if (lang.isBn) p.second else p.first

    fun validate(s: Int): Boolean {
        val e = mutableMapOf<String, String>()
        when (s) {
            0 -> {
                val u = username.trim()
                if (u.isEmpty()) e["username"] = msgRequired
                else if (!USERNAME_RE.matches(u)) e["username"] = msgUsername
                if (password.length < 6) e["password"] = msgPwShort
                if (confirm != password) e["confirm"] = msgPwMismatch
            }
            1 -> {
                if (fullName.trim().isEmpty()) e["name"] = msgRequired
                if (!email.trim().contains('@')) e["email"] = msgEmail
                val digits = phone.filter { it.isDigit() }
                if (digits.isEmpty()) e["phone"] = msgRequired
                else if (digits.length < 11 || !digits.startsWith("01")) e["phone"] = msgPhone
            }
            2 -> {
                if (division == null) e["division"] = msgRequired
                if (district == null) e["district"] = msgRequired
            }
        }
        errs.clear()
        errs.putAll(e)
        return e.isEmpty()
    }

    fun submit() {
        if (!validate(step)) return
        if (step < 3) {
            step++
            return
        }
        submitting = true
        apiError = null
        scope.launch {
            runCatching {
                ApiClient.api.signup(
                    SignupBody(
                        username = username.trim(),
                        full_name = fullName.trim(),
                        email = email.trim(),
                        phone = phone.trim(),
                        alt_phone = altPhone.trim().ifBlank { null },
                        password = password,
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
                    val user = res.user
                    if (user != null) {
                        vm.onLoginSuccess(user)
                        onSuccess()
                    } else {
                        // username_taken / email_taken / phone_taken / invalid_district
                        // → error_generic + raw code shown below
                        apiError = res.error ?: "error_generic"
                    }
                }
                .onFailure { e ->
                    apiError = if (e is retrofit2.HttpException) "http_${e.code()}"
                    else (e.message ?: "network_error")
                }
            submitting = false
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .systemBarsPadding()
            .imePadding()
            .padding(horizontal = 20.dp)
            .padding(top = 8.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Row {
            IconButton(onClick = onBack) {
                Icon(AppIcon.of("arrow-left"), contentDescription = null)
            }
        }

        // ── red gradient brand header block ────────────────────────────
        Box(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(BrandGradient)
                .padding(16.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .size(46.dp)
                        .background(Color.White.copy(alpha = 0.16f), CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    AsyncImage(
                        model = "file:///android_asset/logos/bdrcs.png",
                        contentDescription = null,
                        modifier = Modifier.size(34.dp),
                        contentScale = ContentScale.Fit,
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        tl("brand"),
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        tl("brand_sub"),
                        color = Color.White.copy(alpha = 0.85f),
                        style = MaterialTheme.typography.labelSmall,
                    )
                }
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                tl("signup_title"),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )
            Text(
                tl("signup_sub"),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }

        // ── progress dots + step label ─────────────────────────────────
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            STEPS.forEachIndexed { i, _ ->
                Box(
                    Modifier
                        .size(width = if (i == step) 22.dp else 8.dp, height = 8.dp)
                        .background(
                            if (i <= step) BrandRed else MaterialTheme.colorScheme.outline,
                            CircleShape,
                        ),
                )
            }
        }
        Text(
            tl(STEPS[step]) + " · ${step + 1}/${STEPS.size}",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = BrandRed,
        )

        // ── STEP 1 — account ───────────────────────────────────────────
        if (step == 0) {
            BrandField(
                value = username,
                onValueChange = { username = it },
                label = tl("signup_username"),
                leading = AppIcon.of("user"),
                isError = errs.containsKey("username"),
                supportingText = errs["username"],
            )
            BrandPasswordField(
                value = password,
                onValueChange = { password = it },
                label = tl("signup_password"),
                isError = errs.containsKey("password"),
                supportingText = errs["password"],
            )
            BrandPasswordField(
                value = confirm,
                onValueChange = { confirm = it },
                label = tl("signup_confirmpass"),
                isError = errs.containsKey("confirm"),
                supportingText = errs["confirm"],
            )
        }

        // ── STEP 2 — personal ──────────────────────────────────────────
        if (step == 1) {
            BrandField(
                value = fullName,
                onValueChange = { fullName = it },
                label = tl("signup_fullname"),
                leading = AppIcon.of("user"),
                isError = errs.containsKey("name"),
                supportingText = errs["name"],
            )
            BrandField(
                value = email,
                onValueChange = { email = it },
                label = tl("signup_email"),
                leading = AppIcon.of("mail"),
                keyboardType = KeyboardType.Email,
                isError = errs.containsKey("email"),
                supportingText = errs["email"],
            )
            BrandField(
                value = phone,
                onValueChange = { phone = it },
                label = tl("signup_phone"),
                leading = AppIcon.of("phone"),
                keyboardType = KeyboardType.Phone,
                isError = errs.containsKey("phone"),
                supportingText = errs["phone"],
            )
            BrandField(
                value = altPhone,
                onValueChange = { altPhone = it },
                label = tl("signup_altphone"),
                leading = AppIcon.of("smartphone"),
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
        }

        // ── STEP 3 — address ───────────────────────────────────────────
        if (step == 2) {
            Text(
                tl("addr_district"),
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
                        errs.remove("division")
                        errs.remove("district")
                    },
                )
            }
            if (errs.containsKey("division")) {
                Text(
                    errs["division"].orEmpty(),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.labelSmall,
                )
            }

            PickerField(
                label = tl("addr_district"),
                value = district?.let { Geo.districtLabel(it, lang) },
                placeholder = tl("addr_pick_first"),
                enabled = division != null,
                error = errs["district"],
                onOpen = { picker = "district" },
            )
            PickerField(
                label = tl("addr_upazila"),
                value = upazila?.let { en -> upazilas.firstOrNull { it.first == en }?.let { pairLabel(it) } },
                placeholder = tl("addr_pick_first"),
                enabled = district != null,
                error = null,
                onOpen = { picker = "upazila" },
            )
            PickerField(
                label = tl("addr_thana"),
                value = thana?.let { en -> thanas.firstOrNull { it.first == en }?.let { pairLabel(it) } },
                placeholder = tl("addr_pick_first"),
                enabled = district != null,
                error = null,
                onOpen = { picker = "thana" },
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
        }

        // ── STEP 4 — review ────────────────────────────────────────────
        if (step == 3) {
            ReviewCard(
                title = tl("st_account"),
                stepIndex = 0,
                onEdit = { step = it },
                rows = listOf(
                    tl("signup_username") to username.trim(),
                    tl("signup_password") to "••••••••",
                ),
            )
            ReviewCard(
                title = tl("st_personal"),
                stepIndex = 1,
                onEdit = { step = it },
                rows = listOf(
                    tl("signup_fullname") to fullName.trim().ifBlank { "—" },
                    tl("signup_email") to email.trim().ifBlank { "—" },
                    tl("signup_phone") to phone.trim().ifBlank { "—" },
                    tl("signup_altphone") to altPhone.trim().ifBlank { "—" },
                    tl("signup_blood") to blood.orEmpty().ifBlank { "—" },
                ),
            )
            ReviewCard(
                title = tl("st_address"),
                stepIndex = 2,
                onEdit = { step = it },
                rows = listOf(
                    tl("addr_district") to (district?.let { Geo.districtLabel(it, lang) } ?: "—"),
                    tl("addr_upazila") to
                        (upazila?.let { en -> upazilas.firstOrNull { it.first == en }?.let { pairLabel(it) } } ?: "—"),
                    tl("addr_thana") to
                        (thana?.let { en -> thanas.firstOrNull { it.first == en }?.let { pairLabel(it) } } ?: "—"),
                    tl("addr_ward") to ward.trim().ifBlank { "—" },
                    tl("addr_para") to para.trim().ifBlank { "—" },
                ),
            )
        }

        if (apiError != null) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    msgGeneric,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    apiError.orEmpty(),
                    style = MaterialTheme.typography.labelSmall,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        // ── step nav ───────────────────────────────────────────────────
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            if (step > 0) {
                BrandButton(
                    text = tl("signup_back"),
                    onClick = {
                        errs.clear()
                        apiError = null
                        step--
                    },
                    style = BrandBtnStyle.OUTLINE,
                )
            }
            Box(Modifier.weight(1f)) {
                BrandButton(
                    text = if (step < 3) tl("signup_next") else tl("signup_submit"),
                    onClick = { submit() },
                    modifier = Modifier.fillMaxWidth(),
                    loading = submitting,
                )
            }
        }

        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                tl("login_no_account") + " ",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                tl("login_signup"),
                style = MaterialTheme.typography.bodyMedium,
                color = BrandRed,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable { onLogin() },
            )
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
                                    errs.remove("district")
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

/** readOnly field + full-surface tap overlay that opens a picker sheet. */
@Composable
private fun PickerField(
    label: String,
    value: String?,
    placeholder: String?,
    enabled: Boolean,
    error: String?,
    onOpen: () -> Unit,
) {
    Box {
        BrandField(
            value = value.orEmpty(),
            onValueChange = {},
            label = label,
            readOnly = true,
            isError = error != null,
            supportingText = error,
            placeholder = placeholder,
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

/** Review group card with edit (jump-back-to-step) affordance. */
@Composable
private fun ReviewCard(
    title: String,
    stepIndex: Int,
    onEdit: (Int) -> Unit,
    rows: List<Pair<String, String>>,
) {
    BrandCard(Modifier.fillMaxWidth()) {
        Column(
            Modifier.fillMaxWidth().padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                )
                IconButton(onClick = { onEdit(stepIndex) }, modifier = Modifier.size(32.dp)) {
                    Icon(
                        AppIcon.of("edit"),
                        contentDescription = null,
                        tint = BrandRed,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
            rows.forEach { (label, value) ->
                Row {
                    Text(
                        label,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.width(110.dp),
                    )
                    Text(
                        value,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}
