package com.app.loyal

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform