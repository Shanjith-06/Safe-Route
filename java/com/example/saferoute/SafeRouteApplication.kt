package com.example.saferoute

import android.app.Application
import com.example.saferoute.di.AppContainer
import com.example.saferoute.di.DefaultAppContainer

class SafeRouteApplication : Application() {

    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = DefaultAppContainer(this)
    }
}
