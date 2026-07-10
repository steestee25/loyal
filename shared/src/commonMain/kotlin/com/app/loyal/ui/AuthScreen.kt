package com.app.loyal.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import kotlinx.coroutines.launch

@Composable
fun AuthScreen(supabase: SupabaseClient) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var message by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    Scaffold { padding ->
        Column(modifier = Modifier.fillMaxWidth().padding(padding).padding(16.dp)) {
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                modifier = Modifier.fillMaxWidth()
            )
            Button(onClick = {
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
            }) {
                Text("Accedi")
            }
            Button(onClick = {
                error = null
                message = null
                scope.launch {
                    try {
                        supabase.auth.signUpWith(Email) {
                            this.email = email
                            this.password = password
                        }
                        message = "Registrazione avvenuta. Se la conferma email è attiva su Supabase, controlla la posta prima di accedere."
                    } catch (e: Exception) {
                        error = e.message ?: e.toString()
                    }
                }
            }) {
                Text("Registrati")
            }
            error?.let { Text(it) }
            message?.let { Text(it) }
        }
    }
}