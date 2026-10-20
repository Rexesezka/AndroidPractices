package com.example.myapp

import android.app.Application
import com.example.myapp.di.ServiceLocator

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        ServiceLocator.init(this)
    }
}
