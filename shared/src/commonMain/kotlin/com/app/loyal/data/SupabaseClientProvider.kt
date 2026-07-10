package com.app.loyal.data

import com.app.loyal.SUPABASE_URL
import com.app.loyal.SUPABASE_ANON_KEY
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.postgrest.Postgrest

fun createAppSupabaseClient() = createSupabaseClient(
    supabaseUrl = SUPABASE_URL,
    supabaseKey = SUPABASE_ANON_KEY
) {
    install(Auth)
    install(Postgrest)
}