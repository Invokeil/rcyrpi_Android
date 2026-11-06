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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import cfd.invokeil.himadri.rcy.core.network.ApiClient
import cfd.invokeil.himadri.rcy.core.network.ForgotBody
import cfd.invokeil.himadri.rcy.core.theme.BrandGradient
import cfd.invokeil.himadri.rcy.core.theme.BrandGreen
import cfd.invokeil.himadri.rcy.core.theme.BrandRed
import cfd.invokeil.himadri.rcy.ui.components.AppIcon
import cfd.invokeil.himadri.rcy.ui.components.BrandButton
import cfd.invokeil.himadri.rcy.ui.components.BrandField
import cfd.invokeil.himadri.rcy.ui.components.errorText
import cfd.invokeil.himadri.rcy.ui.components.tl
import kotlinx.coroutines.launch

/**
 * Forgot password — mirrors website forgot-password-page.tsx:
 * email/username field, send reset link, then a success card
 * (fp_sent_title / fp_sent_body + fp_fallback_note).
 *
 * Call sites (RCYNavGraph + ChildGraph) pass no vm — resolved via LocalAppVM.
 */
@Composable
fun ForgotScreen(
    onBack: () -> Unit,
    onSent: () -> Unit,
) {
    var email by remember { mutableStateOf("") }
    var sending by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var sent by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

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
                tl("fp_title"),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )
            Text(
                tl("fp_sub"),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }

        if (!sent) {
            BrandField(
                value = email,
                onValueChange = { email = it },
                label = tl("fp_email"),
                leading = AppIcon.of("mail"),
                keyboardType = KeyboardType.Email,
                isError = error == "required",
                supportingText = if (error == "required") tl("required_field") else null,
            )

            if (error != null && error != "required") {
                Text(
                    errorText(error),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                )
            }

            BrandButton(
                text = tl("fp_send"),
                onClick = {
                    if (email.isBlank()) {
                        error = "required"
                        return@BrandButton
                    }
                    error = null
                    sending = true
                    scope.launch {
                        runCatching { ApiClient.api.forgot(ForgotBody(id = email.trim())) }
                            .onSuccess { res ->
                                if (res.ok) sent = true else error = res.error ?: "error_generic"
                            }
                            .onFailure { e ->
                                error = if (e is retrofit2.HttpException) "http_${e.code()}"
                                else (e.message ?: "network_error")
                            }
                        sending = false
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                loading = sending,
                leading = AppIcon.of("send"),
            )
        } else {
            // ── sent confirmation card ─────────────────────────────────
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
                    tl("fp_sent_title"),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                )
                Text(
                    tl("fp_sent_body"),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.size(6.dp))
                Text(
                    tl("fp_fallback_note"),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
            // continue → tokenless reset entry (outer graph navigates to
            // the reset screen where the fallback link token is pasted)
            BrandButton(
                text = tl("signup_next"),
                onClick = onSent,
                modifier = Modifier.fillMaxWidth(),
                leading = AppIcon.of("arrow-left"),
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
