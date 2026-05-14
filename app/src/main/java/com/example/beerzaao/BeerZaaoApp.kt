package com.example.beerzaao

import android.app.Application
import com.example.beerzaao.data.local.FundDatabase
import com.example.beerzaao.data.remote.ApiService
import com.example.beerzaao.data.remote.interceptor.JsonpInterceptor
import com.example.beerzaao.data.repository.FundRepository
import com.example.beerzaao.data.repository.LocalFundRepository
import com.example.beerzaao.ui.update.UpdateManager
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.util.concurrent.TimeUnit

class BeerZaaoApp : Application() {

    lateinit var fundRepository: FundRepository
        private set
    lateinit var localFundRepository: LocalFundRepository
        private set
    lateinit var updateManager: UpdateManager
        private set
    override fun onCreate() {
        super.onCreate()
        initDependencies()
    }

    private fun initDependencies() {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(JsonpInterceptor())
            .addInterceptor(loggingInterceptor)
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl("https://placeholder.example.com/")
            .client(okHttpClient)
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val apiService = retrofit.create(ApiService::class.java)

        val database = FundDatabase.getDatabase(this)
        val fundDao = database.fundDao()

        fundRepository = FundRepository(apiService)
        localFundRepository = LocalFundRepository(fundDao)
        updateManager = UpdateManager(this)
    }
}
