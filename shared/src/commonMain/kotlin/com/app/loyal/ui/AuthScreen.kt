package com.app.loyal.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.app.loyal.barcode.BackArrowIcon
import com.app.loyal.i18n.LocalStrings
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import kotlinx.coroutines.launch

private sealed class AuthStep {
    data object Welcome : AuthStep()
    data object EnterEmail : AuthStep()
    data object EnterPassword : AuthStep()
}

@Composable
fun AuthScreen(supabase: SupabaseClient) {
    var step by remember { mutableStateOf<AuthStep>(AuthStep.Welcome) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var message by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val strings = LocalStrings.current

    when (step) {
        is AuthStep.Welcome -> WelcomeStep(
            onAccediClick = { step = AuthStep.EnterEmail }
        )
        is AuthStep.EnterEmail -> EmailStep(
            email = email,
            onEmailChange = { email = it },
            onBack = { step = AuthStep.Welcome },
            onNext = { step = AuthStep.EnterPassword }
        )
        is AuthStep.EnterPassword -> PasswordStep(
            password = password,
            onPasswordChange = { password = it },
            error = error,
            message = message,
            onBack = { step = AuthStep.EnterEmail },
            onLogin = {
                error = null
                message = null
                scope.launch {
                    try {
                        supabase.auth.signInWith(Email) {
                            this.email = email
                            this.password = password
                        }
                    } catch (e: Exception) {
                        error = e.message ?: e.toString()
                    }
                }
            },
            onRegister = {
                error = null
                message = null
                scope.launch {
                    try {
                        supabase.auth.signUpWith(Email) {
                            this.email = email
                            this.password = password
                        }
                        message = strings.registrationMessage
                    } catch (e: Exception) {
                        error = e.message ?: e.toString()
                    }
                }
            }
        )
    }
}

@Composable
private fun WelcomeStep(onAccediClick: () -> Unit) {
    val strings = LocalStrings.current
    Scaffold { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                // Placeholder grafica di benvenuto, da sostituire con l'illustrazione finale.
                Box(
                    modifier = Modifier
                        .size(200.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "LOGO",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Button(
                onClick = onAccediClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(strings.login)
            }
        }
    }
}

@Composable
private fun AuthHeader(title: String, onBack: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .clickable(onClick = onBack)
                .padding(4.dp)
        ) {
            BackArrowIcon(
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(24.dp)
            )
        }
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun EmailStep(
    email: String,
    onEmailChange: (String) -> Unit,
    onBack: () -> Unit,
    onNext: () -> Unit
) {
    val strings = LocalStrings.current
    Scaffold { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp)
        ) {
            AuthHeader(title = strings.enterEmailTitle, onBack = onBack)
            Spacer(Modifier.height(32.dp))
            OutlinedTextField(
                value = email,
                onValueChange = onEmailChange,
                label = { Text(strings.email) },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = onNext,
                enabled = email.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(strings.next)
            }
        }
    }
}

@Composable
private fun PasswordStep(
    password: String,
    onPasswordChange: (String) -> Unit,
    error: String?,
    message: String?,
    onBack: () -> Unit,
    onLogin: () -> Unit,
    onRegister: () -> Unit
) {
    val strings = LocalStrings.current
    Scaffold { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp)
        ) {
            AuthHeader(title = strings.enterPasswordTitle, onBack = onBack)
            Spacer(Modifier.height(32.dp))
            OutlinedTextField(
                value = password,
                onValueChange = onPasswordChange,
                label = { Text(strings.password) },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = onLogin,
                enabled = password.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(strings.login)
            }
            Spacer(Modifier.height(8.dp))
            OutlinedButton(
                onClick = onRegister,
                enabled = password.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(strings.register)
            }
            error?.let {
                Spacer(Modifier.height(16.dp))
                Text(it)
            }
            message?.let {
                Spacer(Modifier.height(16.dp))
                Text(it)
            }
        }
    }
}
