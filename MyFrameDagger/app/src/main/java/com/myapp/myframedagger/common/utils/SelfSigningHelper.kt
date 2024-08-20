package com.myapp.myframedagger.common.utils

import android.content.Context
import android.util.Log
import com.myapp.myframedagger.R
import okhttp3.OkHttpClient
import java.io.IOException
import java.io.InputStream
import java.security.KeyManagementException
import java.security.KeyStore
import java.security.KeyStoreException
import java.security.NoSuchAlgorithmException
import java.security.cert.Certificate
import java.security.cert.CertificateException
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import javax.inject.Inject
import javax.inject.Singleton
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager

@Singleton
class SelfSigningHelper @Inject constructor(
     context: Context
) {
    lateinit var tmf: TrustManagerFactory
    lateinit var sslContext: SSLContext
    private val TAG = "SelfSigningHelper"

    init {
        val cf: CertificateFactory
        val ca: Certificate

        val caInput: InputStream

        try {
            Log.d(TAG, "asdfdfsdfssdf ")
            cf = CertificateFactory.getInstance("X.509")
            Log.d(TAG, "2222222222222")

            caInput = context.resources.openRawResource(R.raw.mycert)
            Log.d("caInput", "$caInput ")
            ca = cf.generateCertificate(caInput)
            println("ca = ${(ca as X509Certificate).subjectDN}")

            // Create a KeyStore containing our trusted CAs
            val keyStoreType = KeyStore.getDefaultType()
            val keyStore = KeyStore.getInstance(keyStoreType)
            with(keyStore) {
                load(null, null)
                keyStore.setCertificateEntry("ca", ca)
            }

            // Create a TrustManager that trusts the CAs in our KeyStore
            val tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm()
            tmf = TrustManagerFactory.getInstance(tmfAlgorithm)
            tmf.init(keyStore)

            // Create an SSLContext that uses our TrustManager
            sslContext = SSLContext.getInstance("TLS")
            sslContext.init(null, tmf.trustManagers, java.security.SecureRandom())

            caInput.close()
        } catch (e: KeyStoreException) {
            e.printStackTrace()
        } catch (e: CertificateException) {
            e.printStackTrace()
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        } catch (e: KeyManagementException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    // DI 를 사용하지 않을 경우 아래 함수를 OkHttpClient로 사용.
    fun setSSLOkHttp(builder: OkHttpClient.Builder): OkHttpClient.Builder {
        builder.sslSocketFactory(sslContext.socketFactory, tmf.trustManagers[0] as X509TrustManager)

        return builder
    }
}