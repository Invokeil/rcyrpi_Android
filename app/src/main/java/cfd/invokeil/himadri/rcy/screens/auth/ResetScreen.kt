package cfd.invokeil.himadri.rcy.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import cfd.invokeil.himadri.rcy.core.network.ApiClient
import cfd.invokeil.himadri.rcy.core.network.ResetBody
import cfd.invokeil.himadri.rcy.core.theme.BrandGradient
import cfd.invokeil.himadri.rcy.core.theme.BrandGreen
import cfd.invokeil.himadri.rcy.core.theme.BrandRed
import cfd.invokeil.himadri.rcy.ui.components.AppIcon
import cfd.invokeil.himadri.rcy.ui.components.BrandButton
import cfd.invokeil.himadri.rcy.ui.components.BrandField
import cfd.invokeil.himadri.rcy.ui.components.BrandPasswordField
import cfd.invokeil.himadri.rcy.ui.components.errorText
import cfd.invokeil.himadri.rcy.ui.components.tl
import kotlinx.coroutines.launch

/**
 * Reset password — mirrors website reset-password-page.tsx.
 * Deep-link route carries the emailed token; the tokenless entry
 * (after the forgot screen) lets the user paste the fallback link's token.
 * On success shows rp_updated and returns to login via [onDone].
 *
 * Call sites (RCYNavGraph) pass no vm — resolved via LocalAppVM.
 */
@Composable
fun ResetScreen(
    token: String?,
    onBack: () -> Unit,
    onDone: () -> Unit,
) {
    var tokenInput by remember { mutableStateOf(token.orEmpty()) }
    var pw by remember { mutableStateOf("") }
    var pw2 by remember { mutableStateOf("") }
    var localErr by remember { mutableStateOf<String?>(null) } // required | short | mismatch
    var serverErr by remember { mutableStateOf<String?>(null) } // raw code from API/transport
    var busy by remember { mutableStateOf(false) }
    var done by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    fun submit() {
        localErr = null
        serverErr = null
        if (tokenInput.isBlank()) {
            localErr = "required"
            return
        }
        if (pw.length < 6) {
            localErr = "short"
            return
        }
        if (pw != pw2) {
            localErr = "mismatch"
            return
        }
        busy = true
        scope.launch {
            runCatching { ApiClient.api.reset(ResetBody(token = tokenInput.trim(), password = pw)) }
                .onSuccess { res ->
                    if (res.ok) done = true else serverErr = res.error ?: "invalid_token"
                }
                .onFailure { e ->
                    serverErr = if (e is retrofit2.HttpException) "http_${e.code()}"
                    else (e.message ?: "network_error")
                }
            busy = false
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
                Spacer(Modifier.size(12.dp))
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
                tl("rp_title"),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )
            Text(
                tl("rp_sub"),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }

        if (done) {
            // ── updated confirmation ───────────────────────────────────
            Column(
                Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(
                    Modifier
                        .size(56.dp)
                        .background(BrandGreen.copy(alpha = 0.12f), CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        AppIcon.of("check"),
                        contentDescription = null,
                        tint = BrandGreen,
                        modifier = Modifier.size(28.dp),
                    )
                }
                Spacer(Modifier.size(10.dp))
                Text(
                    tl("rp_updated"),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                )
            }
            BrandButton(
                text = tl("rp_update"),
                onClick = onDone,
                modifier = Modifier.fillMaxWidth(),
            )
        } else {
            if (token == null) {
                // pasted token from the email's fallback secure link
                BrandField(
                    value = tokenInput,
                    onValueChange = { tokenInput = it },
                    label = "Token",
                    supportingText = tl("fp_fallback_note"),
                    isError = localErr == "required",
                )
            }
            BrandPasswordField(
                value = pw,
                onValueChange = { pw = it },
                label = tl("rp_new_pw"),
                isError = localErr == "short",
            )
            BrandPasswordField(
                value = pw2,
                onValueChange = { pw2 = it },
                label = tl("rp_confirm_pw"),
                isError = localErr == "mismatch",
            )

            if (localErr != null) {
                Text(
                    when (localErr) {
                        "required" -> tl("required_field")
                        "short" -> tl("prof_pw_short")
                        else -> tl("prof_pw_mismatch")
                    },
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            if (serverErr != null) {
                // invalid/expired token (or any 4xx) → rp_invalid, else generic
                val msg = if (serverErr?.startsWith("http_") == true ||
                    serverErr?.contains("token", ignoreCase = true) == true
                ) tl("rp_invalid") else errorText(serverErr)
                Text(
                    msg,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                )
            }

            BrandButton(
                text = tl("rp_update"),
                onClick = { submit() },
                modifier = Modifier.fillMaxWidth(),
                loading = busy,
                leading = AppIcon.of("lock"),
            )
        }

        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                AppIcon.of("arrow-left"),
                contentDescription = null,
                tint = BrandRed,
                modifier = Modifier.size(16.dp),
            )
            Spacer(Modifier.size(4.dp))
            Text(
                tl("fp_back"),
                color = BrandRed,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable { onBack() },
            )
        }
    }
}
