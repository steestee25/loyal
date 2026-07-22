package com.app.loyal.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.foundation.Canvas
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.app.loyal.i18n.AppLanguage
import com.app.loyal.i18n.LocalStrings
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    userEmail: String,
    supabase: SupabaseClient,
    language: AppLanguage,
    onLanguageChange: (AppLanguage) -> Unit,
    onLogoutClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalStrings.current
    var showPasswordSheet by remember { mutableStateOf(false) }
    var showPreferencesSheet by remember { mutableStateOf(false) }

    Column(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = strings.settingsTitle,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        ProfileCard(userEmail = userEmail)

        SettingsCard(title = strings.changePassword, onClick = { showPasswordSheet = true })
        SettingsCard(title = strings.preferences, onClick = { showPreferencesSheet = true })
        SettingsCard(title = strings.logout, onClick = onLogoutClick)
    }

    if (showPasswordSheet) {
        ChangePasswordSheet(
            supabase = supabase,
            onDismiss = { showPasswordSheet = false }
        )
    }
    if (showPreferencesSheet) {
        PreferencesSheet(
            language = language,
            onLanguageChange = onLanguageChange,
            onDismiss = { showPreferencesSheet = false }
        )
    }
}

/** Card profilo (non cliccabile): avatar profilo + email affiancati. */
@Composable
private fun ProfileCard(userEmail: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar: icona profilo disegnata (riusata dalla home) su cerchio tenue.
            Row(
                modifier = Modifier
                    .size(48.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ProfileIcon(
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(28.dp)
                )
            }
            Text(
                text = userEmail,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

/** Card cliccabile riutilizzata per "cambia password", "preferenze" e "logout". */
@Composable
private fun SettingsCard(
    title: String,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = title, style = MaterialTheme.typography.bodyLarge)
            ChevronRightIcon(
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

/** Bottom sheet per cambiare la password dell'utente via Supabase. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChangePasswordSheet(
    supabase: SupabaseClient,
    onDismiss: () -> Unit
) {
    val strings = LocalStrings.current
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()

    var newPassword by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var message by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = {
            BottomSheetDefaults.DragHandle(color = MaterialTheme.colorScheme.outlineVariant)
        }
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = strings.changePassword,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            OutlinedTextField(
                value = newPassword,
                onValueChange = { newPassword = it },
                label = { Text(strings.newPassword) },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )
            Button(
                onClick = {
                    error = null
                    message = null
                    loading = true
                    scope.launch {
                        try {
                            supabase.auth.updateUser { password = newPassword }
                            message = strings.passwordUpdated
                            newPassword = ""
                        } catch (e: Exception) {
                            error = e.message ?: e.toString()
                        } finally {
                            loading = false
                        }
                    }
                },
                enabled = newPassword.isNotBlank() && !loading,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(strings.save)
            }
            error?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
            }
            message?.let {
                Text(it, color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

/** Bottom sheet delle preferenze: per ora solo la scelta della lingua. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PreferencesSheet(
    language: AppLanguage,
    onLanguageChange: (AppLanguage) -> Unit,
    onDismiss: () -> Unit
) {
    val strings = LocalStrings.current
    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = {
            BottomSheetDefaults.DragHandle(color = MaterialTheme.colorScheme.outlineVariant)
        }
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)) {
            Text(
                text = strings.language,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)
            )
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant,
                modifier = Modifier.padding(horizontal = 24.dp).padding(top = 8.dp, bottom = 8.dp)
            )
            AppLanguage.entries.forEach { lang ->
                LanguageOption(
                    label = lang.displayName,
                    selected = lang == language,
                    onClick = { onLanguageChange(lang) }
                )
            }
        }
    }
}

/** Riga di selezione lingua, stessa "pillola con spunta" usata per l'ordinamento. */
@Composable
private fun LanguageOption(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(percent = 50)
    val contentColor =
        if (selected) MaterialTheme.colorScheme.primary
        else MaterialTheme.colorScheme.onSurface

    val containerModifier =
        if (selected) {
            Modifier
                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f), shape)
                .border(1.dp, MaterialTheme.colorScheme.primary, shape)
        } else {
            Modifier
        }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 2.dp)
            .clip(shape)
            .then(containerModifier)
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = contentColor
        )
        if (selected) {
            SettingsCheckIcon(
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

/** Freccetta ">" a destra delle card, disegnata a mano con Canvas. */
@Composable
private fun ChevronRightIcon(color: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val stroke = 2.dp.toPx()
        val w = size.width
        val h = size.height
        drawLine(color, Offset(w * 0.40f, h * 0.25f), Offset(w * 0.62f, h * 0.50f), stroke, cap = StrokeCap.Round)
        drawLine(color, Offset(w * 0.62f, h * 0.50f), Offset(w * 0.40f, h * 0.75f), stroke, cap = StrokeCap.Round)
    }
}

/** Spunta di conferma per l'opzione selezionata, disegnata a mano con Canvas. */
@Composable
private fun SettingsCheckIcon(color: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val stroke = 2.dp.toPx()
        val w = size.width
        val h = size.height
        drawLine(color, Offset(w * 0.15f, h * 0.55f), Offset(w * 0.42f, h * 0.80f), stroke, cap = StrokeCap.Round)
        drawLine(color, Offset(w * 0.42f, h * 0.80f), Offset(w * 0.85f, h * 0.25f), stroke, cap = StrokeCap.Round)
    }
}
