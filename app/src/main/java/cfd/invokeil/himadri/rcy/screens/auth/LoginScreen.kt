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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import cfd.invokeil.himadri.rcy.LocalAppVM
import cfd.invokeil.himadri.rcy.core.network.ApiClient
import cfd.invokeil.himadri.rcy.core.network.LoginBody
import cfd.invokeil.himadri.rcy.core.theme.BrandGradient
import cfd.invokeil.himadri.rcy.core.theme.BrandRed
import cfd.invokeil.himadri.rcy.ui.components.AppIcon
import cfd.invokeil.himadri.rcy.ui.components.BrandButton
import cfd.invokeil.himadri.rcy.ui.components.BrandCard
import cfd.invokeil.himadri.rcy.ui.components.BrandField
import cfd.invokeil.himadri.rcy.ui.components.BrandPasswordField
import cfd.invokeil.himadri.rcy.ui.components.ChoiceChips
import cfd.invokeil.himadri.rcy.ui.components.errorText
import cfd.invokeil.himadri.rcy.ui.components.tl
import kotlinx.coroutines.launch

/**
 * Member login — mirrors website login-page.tsx:
 * red gradient brand header, member/admin role chips, id + password,
 * friendly error mapping (Helpers.errorText), forgot + signup links
 * and a muted demo-accounts hint card.
 *
 * Call sites (RCYNavGraph + ChildGraph) pass no vm — resolved via LocalAppVM.
 */
@Composable
fun LoginScreen(
    onBack: () -> Unit,
    onSignup: () -> Unit,
    onForgot: () -> Unit,
    onSuccess: () -> Unit,
) {
    val vm = LocalAppVM.current
    val scope = rememberCoroutineScope()

    var role by remember { mutableStateOf("member") }
    var id by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var showValidation by remember { mutableStateOf(false) }

    val idMissing = showValidation && id.isBlank()
    val pwMissing = showValidation && password.isBlank()

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
                tl("login_title"),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )
            Text(
                tl("login_sub"),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }

        // ── member / admin role tabs ───────────────────────────────────
        ChoiceChips(
            options = listOf(
                "member" to tl("login_tab_member"),
                "admin" to tl("login_tab_admin"),
            ),
            selected = role,
            onSelect = { role = it },
        )

        BrandField(
            value = id,
            onValueChange = { id = it },
            label = tl("login_id"),
            leading = AppIcon.of("user"),
            isError = idMissing,
            supportingText = if (idMissing) tl("required_field") else null,
        )
        BrandPasswordField(
            value = password,
            onValueChange = { password = it },
            label = tl("login_password"),
            isError = pwMissing,
            supportingText = if (pwMissing) tl("required_field") else null,
        )

        if (error != null) {
            Text(
                errorText(error),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
            )
        }

        BrandButton(
            text = tl("login_btn"),
            onClick = {
                if (id.isBlank() || password.isBlank()) {
                    showValidation = true
                    return@BrandButton
                }
                error = null
                loading = true
                scope.launch {
                    runCatching {
                        ApiClient.api.login(LoginBody(id = id.trim(), password = password, role = role))
                    }
                        .onSuccess { res ->
                            val user = res.user
                            if (user != null) {
                                vm.onLoginSuccess(user)
                                onSuccess()
                            } else {
                                // invalid_credentials / suspended / not_admin →
                                // Helpers.errorText maps all of them to error_generic
                                error = res.error ?: "error_generic"
                            }
                        }
                        .onFailure { e ->
                            error = if (e is retrofit2.HttpException) "http_${e.code()}"
                            else (e.message ?: "network_error")
                        }
                    loading = false
                }
            },
            modifier = Modifier.fillMaxWidth(),
            loading = loading,
            leading = AppIcon.of("login"),
        )

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            TextButton(onClick = onForgot) {
                Text(
                    tl("login_forgot"),
                    color = BrandRed,
                    style = MaterialTheme.typography.labelLarge,
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
                modifier = Modifier.clickable { onSignup() },
            )
        }

        // ── demo accounts (README / seed) — muted hint card ────────────
        BrandCard(Modifier.fillMaxWidth()) {
            Column(
                Modifier.fillMaxWidth().padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                Text(
                    tl("login_demo_hint"),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    "admin / ChangeMe@123",
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    "rakib01 / ChangeMe@123",
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
