package com.rogue.scorequest

import android.app.Application
import com.rogue.scorequest.di.appModule
import com.rogue.scorequest.di.databaseModule
import com.rogue.scorequest.di.networkModule
import com.rogue.scorequest.di.preferencesModule
import com.rogue.scorequest.di.viewModelModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@MyApplication)
            modules(
                appModule,
                networkModule,
                databaseModule,
                preferencesModule,
                viewModelModule
            )
        }
    }
}
