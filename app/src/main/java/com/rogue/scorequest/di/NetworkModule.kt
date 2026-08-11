package com.rogue.scorequest.di

import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

val networkModule = module {
    single {
        OkHttpClient.Builder()
            .addInterceptor(
                HttpLoggingInterceptor()
                    .apply {
                        level =
                            HttpLoggingInterceptor.Level.BODY
                    }
            )
            .build()
    }
    single {
        Retrofit.Builder()
            .baseUrl("https://placeholder.com/")
            .client(get())
            .addConverterFactory(
                Json.asConverterFactory(
                    "application/json".toMediaType()
                )
            )
            .build()
    }

    // TODO: register API interfaces here, e.g.
    // single { get<Retrofit>().create(SomeApi::class.java) }
}
