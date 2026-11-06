package cfd.invokeil.himadri.rcy.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import cfd.invokeil.himadri.rcy.core.theme.BrandGreen
import cfd.invokeil.himadri.rcy.core.theme.BrandGradient
import cfd.invokeil.himadri.rcy.core.theme.BrandRed
import cfd.invokeil.himadri.rcy.core.theme.GreenGradient
import kotlinx.coroutines.delay

// ── BrandButton — site's pill CTA (radius 999; red/green/outline/ghost) ─
enum class BrandBtnStyle { PRIMARY, GREEN, OUTLINE, GHOST }

@Composable
fun BrandButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: BrandBtnStyle = BrandBtnStyle.PRIMARY,
    enabled: Boolean = true,
    loading: Boolean = false,
    leading: ImageVector? = null,
) {
    val shape = RoundedCornerShape(999.dp)
    val padding = PaddingValues(horizontal = 22.dp, vertical = 12.dp)
    when (style) {
        BrandBtnStyle.PRIMARY -> Button(
            onClick = onClick, enabled = enabled && !loading, shape = shape, modifier = modifier,
            colors = ButtonDefaults.buttonColors(
                containerColor = BrandRed, contentColor = Color.White,
                disabledContainerColor = BrandRed.copy(alpha = 0.45f),
            ),
            contentPadding = padding,
        ) { BtnContent(text, loading, leading) }
        BrandBtnStyle.GREEN -> Button(
            onClick = onClick, enabled = enabled && !loading, shape = shape, modifier = modifier,
            colors = ButtonDefaults.buttonColors(
                containerColor = BrandGreen, contentColor = Color.White,
                disabledContainerColor = BrandGreen.copy(alpha = 0.45f),
            ),
            contentPadding = padding,
        ) { BtnContent(text, loading, leading) }
        BrandBtnStyle.OUTLINE -> OutlinedButton(
            onClick = onClick, enabled = enabled && !loading, shape = shape, modifier = modifier,
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = BrandRed, disabledContentColor = BrandRed.copy(alpha = 0.45f),
            ),
            contentPadding = padding,
        ) { BtnContent(text, loading, leading) }
        BrandBtnStyle.GHOST -> TextButton(
            onClick = onClick, enabled = enabled && !loading, shape = shape, modifier = modifier,
            colors = ButtonDefaults.textButtonColors(contentColor = BrandRed),
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
        ) { BtnContent(text, loading, leading) }
    }
}

@Composable
private fun BtnContent(text: String, loading: Boolean, leading: ImageVector?) {
    if (loading) {
        CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp, color = Color.White)
    } else {
        leading?.let { Icon(it, contentDescription = null, Modifier.size(18.dp)) }
        Spacer(Modifier.size(6.dp))
    }
    Text(text, style = MaterialTheme.typography.labelLarge)
}

/** Gradient pill — hero/join CTA (135° brand gradient). */
@Composable
fun BrandGradientButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    green: Boolean = false,
    enabled: Boolean = true,
    loading: Boolean = false,
) {
    val shape = RoundedCornerShape(999.dp)
    Row(
        modifier = modifier
            .defaultMinSize(minHeight = 48.dp)
            .background(if (green) GreenGradient else BrandGradient, shape)
            .then(if (enabled && !loading) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 24.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        if (loading) {
            CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp, color = Color.White)
            Spacer(Modifier.size(8.dp))
        }
        Text(
            text, color = if (enabled) Color.White else Color.White.copy(alpha = 0.6f),
            style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold,
        )
    }
}

// ── BrandCard — site card: radius 12, bordered surface ─────────────────
@Composable
fun BrandCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    if (onClick != null) {
        Card(
            onClick = onClick, modifier = modifier, shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = CardDefaults.outlinedCardBorder(),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        ) { content() }
    } else {
        Card(
            modifier = modifier, shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = CardDefaults.outlinedCardBorder(),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        ) { content() }
    }
}

// ── PillBadge — severity/category chip ─────────────────────────────────
@Composable
fun PillBadge(text: String, color: Color = BrandRed, modifier: Modifier = Modifier, soft: Boolean = true) {
    Box(
        modifier = modifier
            .background(if (soft) color.copy(alpha = 0.12f) else color, RoundedCornerShape(999.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp),
    ) {
        Text(text, color = if (soft) color else Color.White, style = MaterialTheme.typography.labelMedium)
    }
}

// ── BlinkDot — LIVE pulse indicator ────────────────────────────────────
@Composable
fun BlinkDot(color: Color = BrandRed, size: Int = 8) {
    val visible by produceState(initialValue = true) {
        while (true) {
            delay(600)
            value = !value
        }
    }
    Box(Modifier.size(size.dp).background(if (visible) color else color.copy(alpha = 0.25f), CircleShape))
}

// ── SectionHeading — accent bar + title (+ subtitle, + action) ────────
@Composable
fun SectionHeading(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    accent: Color = BrandRed,
    action: (@Composable () -> Unit)? = null,
) {
    Column(modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(width = 4.dp, height = 18.dp).background(accent, RoundedCornerShape(2.dp)))
            Spacer(Modifier.size(8.dp))
            Text(
                title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f),
            )
            action?.invoke()
        }
        if (!subtitle.isNullOrBlank()) {
            Spacer(Modifier.height(2.dp))
            Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

// ── StatCard — home stats grid ─────────────────────────────────────────
@Composable
fun StatCard(icon: ImageVector, value: String, label: String, tint: Color, modifier: Modifier = Modifier) {
    BrandCard(modifier) {
        Column(Modifier.fillMaxWidth().padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                Modifier.size(40.dp).background(tint.copy(alpha = 0.12f), CircleShape),
                contentAlignment = Alignment.Center,
            ) { Icon(icon, null, tint = tint, modifier = Modifier.size(22.dp)) }
            Spacer(Modifier.height(8.dp))
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(
                label, style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1,
            )
        }
    }
}

// ── BrandField / BrandPasswordField ────────────────────────────────────
@Composable
fun BrandField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    singleLine: Boolean = true,
    isError: Boolean = false,
    supportingText: String? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    leading: ImageVector? = null,
    trailing: (@Composable () -> Unit)? = null,
    minLines: Int = 1,
    readOnly: Boolean = false,
    placeholder: String? = null,
    keyboardType: androidx.compose.ui.text.input.KeyboardType = androidx.compose.ui.text.input.KeyboardType.Text,
) {
    val colors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = BrandRed,
        focusedLabelColor = BrandRed,
        cursorColor = BrandRed,
    )
    OutlinedTextField(
        value = value, onValueChange = onValueChange, label = { Text(label) },
        modifier = modifier.fillMaxWidth(),
        singleLine = singleLine, minLines = minLines,
        isError = isError, colors = colors, readOnly = readOnly,
        supportingText = supportingText?.let { { Text(it) } },
        visualTransformation = visualTransformation,
        leadingIcon = leading?.let { { Icon(it, null, tint = MaterialTheme.colorScheme.onSurfaceVariant) } },
        trailingIcon = trailing,
        placeholder = placeholder?.let { { Text(it) } },
        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = keyboardType),
    )
}

@Composable
fun BrandPasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
    supportingText: String? = null,
) {
    var show by remember { mutableStateOf(false) }
    BrandField(
        value = value, onValueChange = onValueChange, label = label, modifier = modifier,
        isError = isError, supportingText = supportingText,
        visualTransformation = if (show) VisualTransformation.None else PasswordVisualTransformation(),
        leading = Icons.Filled.Lock,
        trailing = {
            IconButton(onClick = { show = !show }) {
                Icon(
                    if (show) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
    )
}

// ── ChoiceChips — value/label chip row ─────────────────────────────────
@Composable
fun ChoiceChips(
    options: List<Pair<String, String>>,
    selected: String?,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        options.forEach { (value, label) ->
            FilterChip(
                selected = selected == value,
                onClick = { onSelect(value) },
                label = { Text(label) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = BrandRed.copy(alpha = 0.12f),
                    selectedLabelColor = BrandRed,
                ),
            )
        }
    }
}
