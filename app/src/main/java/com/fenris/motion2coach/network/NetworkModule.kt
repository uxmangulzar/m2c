package com.fenris.motion2coach.network

import android.content.Context
import com.fenris.motion2coach.BuildConfig
import com.fenris.motion2coach.util.Constants.Companion.BASE_URL
import com.fenris.motion2coach.util.HelperKeys
import com.fenris.motion2coach.util.SessionManager
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
//    @Singleton
//    @Provides
//    fun provideHttpClient(
//        @ApplicationContext app: Context
//    ): OkHttpClient {
//
//        val interceptor = HttpLoggingInterceptor()
//        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY)
//
//
//
//        return OkHttpClient
//            .Builder()
//            .cookieJar(UvCookieJar(app))
//            .connectTimeout(30, TimeUnit.SECONDS)
//            .writeTimeout(5, TimeUnit.MINUTES)
//            .readTimeout(5, TimeUnit.MINUTES)
//            .addInterceptor(interceptor)
//            .build()
//    }
    @Provides
    @Singleton
    fun provideHttpClient(
        @ApplicationContext app: Context
    ): OkHttpClient {

        //build client
        return OkHttpClient.Builder()

            //create anonymous interceptor in the lambda and override intercept
            // passing in Interceptor.Chain parameter
            .addInterceptor { chain ->

                //return response
                chain.proceed(
                    //create request
                    chain.request()
                        .newBuilder()

                        //add headers to the request builder
                        .also {
                            it.addHeader("appversion", SessionManager.getStringPref(HelperKeys.APP_VERSION,app))
                            it.addHeader("devicetype", "android")
//                            it.addHeader("deviceid", "value_2")
                        }.build()

                )
            }
            //add timeouts, logging
            .also { okHttpClient ->

                okHttpClient.cookieJar(UvCookieJar(app))

                okHttpClient.connectTimeout(30, TimeUnit.SECONDS)
                okHttpClient.readTimeout(5, TimeUnit.SECONDS)
                //log if in debugging phase
                if (BuildConfig.DEBUG) {
                    val httpLoggingInterceptor = HttpLoggingInterceptor().apply {

                        level = HttpLoggingInterceptor.Level.BODY
                    }

                    okHttpClient.addInterceptor(httpLoggingInterceptor)
                }
            }
            .build()


    }
    @Singleton
    @Provides
    fun provideConverterFactory(): GsonConverterFactory =
        GsonConverterFactory.create()

    @Singleton
    @Provides
    fun provideRetrofit(
        okHttpClient: OkHttpClient,
        gsonConverterFactory: GsonConverterFactory
    ): Retrofit {
        val gson = GsonBuilder()
            .setLenient()
            .create()
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .addConverterFactory(gsonConverterFactory)

            .build()
    }


    @Singleton
    @Provides
    fun provideCurrencyService(retrofit: Retrofit): ApiInterface =
        retrofit.create(ApiInterface::class.java)

}