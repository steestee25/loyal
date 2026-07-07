package com.app.loyal.data

import com.app.loyal.BRANDFETCH_CLIENT_ID

fun brandLogoUrl(domain: String): String {
    return "https://cdn.brandfetch.io/$domain/w/200/h/200?c=$BRANDFETCH_CLIENT_ID"
}