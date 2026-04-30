package com.lwg.challenge.utils

import co.touchlab.kermit.Logger as KermitLogger
import co.touchlab.kermit.Severity

object Logger {

    private const val TAG = "promise"

    init {
        KermitLogger.setTag(TAG)

        if (isDebug) {
            KermitLogger.setMinSeverity(Severity.Info)
        } else {
            KermitLogger.setMinSeverity(Severity.Assert)
        }
    }

    fun i(message: String) {
        KermitLogger.i { message }
    }

    fun d(message: String) {
        KermitLogger.i { message }
    }

    fun w(message: String) {
        KermitLogger.w { message }
    }

    fun e(message: String, throwable: Throwable? = null) {
        KermitLogger.e(throwable) { message }
    }

    fun v(message: String) {
        KermitLogger.v { message }
    }
}
