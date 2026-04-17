package com.lwg.challenge

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform