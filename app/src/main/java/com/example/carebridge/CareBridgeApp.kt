package com.example.carebridge

import android.app.Application
import android.content.Context

/**
 * Custom Application class to provide a global context.
 */
class CareBridgeApp : Application() {
    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    companion object {
        private var instance: CareBridgeApp? = null

        fun getContext(): Context {
            return instance!!.applicationContext
        }
    }
}
