package com.example.nutriscanai

import android.app.Application
import android.content.Context

/**
 * Custom Application class to provide a global context.
 */
class NutriScanApp : Application() {
    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    companion object {
        private var instance: NutriScanApp? = null

        fun getContext(): Context {
            return instance!!.applicationContext
        }
    }
}
