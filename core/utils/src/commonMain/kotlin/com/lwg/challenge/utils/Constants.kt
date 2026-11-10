package com.lwg.challenge.utils

object Constants {

    private const val PROD_BASE_URL = "https://api.woo-challenge.shop/"

    // local.properties 의 challenge_api_base_url_android/_ios. 평문 HTTP 는 Android debug 빌드에서만 허용된다.
    private val DEV_BASE_URL: String = BuildKonfig.DEV_BASE_URL.ifEmpty { PROD_BASE_URL }

    val BASE_URL: String = if (isDebug) {
//        PROD_BASE_URL
        DEV_BASE_URL
    } else {
        PROD_BASE_URL
    }
}
