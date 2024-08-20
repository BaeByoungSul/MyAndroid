package com.myapp.myframedagger.common.retrofit_simple

import okhttp3.OkHttpClient
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

//class UnsafeOkHttpClient {
//    companion object {
//        fun getUnsafeOkHttpClient(): OkHttpClient.Builder {
//            try {
//                // Create a trust manager that does not validate certificate chains
//                val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
//                    @Throws(CertificateException::class)
//                    override fun checkClientTrusted(chain: Array<java.security.cert.X509Certificate>, authType: String) {
//                    }
//
//                    @Throws(CertificateException::class)
//                    override fun checkServerTrusted(chain: Array<java.security.cert.X509Certificate>, authType: String) {
//                    }
//
//                    override fun getAcceptedIssuers(): Array<java.security.cert.X509Certificate> {
//                        return arrayOf()
//                    }
//                })
//
//                // Install the all-trusting trust manager
//                val sslContext = SSLContext.getInstance("SSL")
//                sslContext.init(null, trustAllCerts, java.security.SecureRandom())
//                // Create an ssl socket factory with our all-trusting manager
//                val sslSocketFactory = sslContext.socketFactory
//
//                val builder = OkHttpClient.Builder()
//                builder.sslSocketFactory(sslSocketFactory, trustAllCerts[0] as X509TrustManager)
//                builder.hostnameVerifier { _, _ -> true }
//
//                return builder
//            } catch (e: Exception) {
//                throw RuntimeException(e)
//            }
//        }
//    }
//}
class UnsafeOkHttpClient {
    companion object {
        fun getUnsafeOkHttpClient(): OkHttpClient {
            try {
                val x509 = object :X509TrustManager{
                    override fun checkClientTrusted(
                        chain: Array<out X509Certificate>?,
                        authType: String?
                    ) {  }

                    override fun checkServerTrusted(
                        chain: Array<out X509Certificate>?,
                        authType: String?
                    ) {  }

                    override fun getAcceptedIssuers(): Array<X509Certificate> {
                        return arrayOf()
                    }
                }
                // Create a trust manager that does not validate certificate chains
                val trustAllCerts = arrayOf<TrustManager>(x509)


                // Install the all-trusting trust manager
                val sslContext = SSLContext.getInstance("SSL")
                sslContext.init(null, trustAllCerts, SecureRandom())

                // Create an ssl socket factory with our all-trusting manager
                val sslSocketFactory: SSLSocketFactory = sslContext.socketFactory
                val builder = OkHttpClient.Builder()
                builder.sslSocketFactory(
                    sslSocketFactory,
                    trustAllCerts[0] as X509TrustManager
                )
                builder.hostnameVerifier(HostnameVerifier { _, _ -> true })
                return builder.build()
            } catch (e: Exception) {
                throw RuntimeException(e)
            }
        }
    }

}