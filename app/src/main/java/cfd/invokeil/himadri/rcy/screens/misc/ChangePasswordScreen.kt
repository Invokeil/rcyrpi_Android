package cfd.invokeil.himadri.rcy.screens.misc

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import cfd.invokeil.himadri.rcy.core.network.ApiClient
import cfd.invokeil.himadri.rcy.core.network.ChangePasswordBody
import cfd.invokeil.himadri.rcy.core.theme.BrandRed
import cfd.invokeil.himadri.rcy.ui.components.AppIcon
import cfd.invokeil.himadri.rcy.ui.components.BrandButton
import cfd.invokeil.himadri.rcy.ui.components.BrandCard
import cfd.invokeil.himadri.rcy.ui.components.BrandPasswordField
import cfd.invokeil.himadri.rcy.ui.components.SectionHeading
import cfd.invokeil.himadri.rcy.ui.components.tl
import cfd.invokeil.himadri.rcy.vm.AppViewModel
import kotlinx.coroutines.launch

/**
 * Change password — mirrors the password card of website profile-page.tsx:
 * current + new + confirm fields, client validation (short / mismatch),
 * POST /api/profile/password → toast prof_pw_changed + back.
 * HTTP 400/401 maps to prof_pw_wrong.
 */
@Composable
fun ChangePasswordScreen(vm: AppViewModel, nav: NavHostController) {
    var current by remember { mutableStateOf("") }
    var newPw by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }
    var err by remember { mutableStateOf<String?>(null) } // raw code → mapped at display
    var busy by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val msgChanged = tl("prof_pw_changed")

    fun submit() {
        err = null
        if (current.isBlank()) {
            err = "required"
            return
        }
        if (newPw.length < 6) {
            err = "short"
            return
        }
        if (newPw != confirm) {
            err = "mismatch"
            return
        }
        busy = true
        scope.launch {
            runCatching { ApiClient.api.changePassword(ChangePasswordBody(current = current, password = newPw)) }
                .onSuccess { res ->
                    if (res.ok) {
                        Toast.makeText(context, msgChanged, Toast.LENGTH_SHORT).show()
                        nav.popBackStack()
                    } else {
                        err = res.error ?: "error_generic"
                    }
                }
                .onFailure { e ->
                    err = if (e is retrofit2.HttpException && (e.code() == 400 || e.code() == 401)) "wrong"
                    else "error_generic"
                }
            busy = false
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .imePadding()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { nav.popBackStack() }) {
                Icon(AppIcon.of("arrow-left"), contentDescription = null)
            }
            SectionHeading(
                title = tl("prof_password"),
                subtitle = tl("prof_sub"),
                modifier = Modifier.weight(1f),
            )
        }

        BrandCard(Modifier.fillMaxWidth()) {
            Column(
                Modifier.fillMaxWidth().padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                BrandPasswordField(
                    value = current,
                    onValueChange = { current = it },
                    label = tl("prof_current_pw"),
                    isError = err == "required",
                )
                BrandPasswordField(
                    value = newPw,
                    onValueChange = { newPw = it },
                    label = tl("prof_new_pw"),
                    isError = err == "short",
                )
                BrandPasswordField(
                    value = confirm,
                    onValueChange = { confirm = it },
                    label = tl("prof_confirm_pw"),
                    isError = err == "mismatch",
                )

                if (err != null) {
                    Text(
                        when (err) {
                            "required" -> tl("required_field")
                            "short" -> tl("prof_pw_short")
                            "mismatch" -> tl("prof_pw_mismatch")
                            "wrong" -> tl("prof_pw_wrong")
                            else -> tl("error_generic")
                        },
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
        }
    }
}
