package com.myapp.myframedagger


import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.myapp.myframedagger.common.model.LoginResponse
import com.myapp.myframedagger.common.retrofit_simple.AlbumService
import com.myapp.myframedagger.common.retrofit_simple.RetrofitInstance
import com.myapp.myframedagger.databinding.ActivityMainBinding
import com.myapp.myframedagger.service.ScanService
import com.myapp.myframedagger.view.RetrofitTestActivity
import com.myapp.myframedagger.viewmodel.AppViewModelFactory
import com.myapp.myframedagger.viewmodel.MainViewModel
import com.myapp.myframedagger.viewmodel.RetrofitTestViewModel
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject


class MainActivity : AppCompatActivity() {
    companion object {
        const val TAG = "MainActivity"
    }
    //private lateinit var apiRepository: WebApiRepository
    // @Inject   lateinit var apiService: ApiService


    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: MainViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val serviceIntent = Intent(
            this,
            ScanService::class.java
        )
        startService(serviceIntent);

        viewModel = ViewModelProvider(this)[MainViewModel::class.java]


        //DaggerRetrofitComponent.create().inject(this)
         //Dagg.create().inject(this)
        //apiRepository= DaggerRetrofitComponent.create().getApiService()



        val retService : AlbumService = RetrofitInstance.getRetrofit().create(AlbumService::class.java)

        viewModel.toastMessage.observe(this){
            Log.d(TAG, "onCreate: $it")
            showToast(it)
        }

        viewModel.loginSuccess.observe(this){
            binding.resultTextView.text = "Access Token\n"
            binding.resultTextView.append ( (it as LoginResponse).accessToken)
            binding.resultTextView.append ( "\nRefresh Token\n")
            binding.resultTextView.append (it.refreshToken.token )

            binding.resultTextView.append ( "\nExpire Datetime\n")
            val format = SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.KOREA)
            val expireDateFormat= format.format(it.refreshToken.expires)
            binding.resultTextView.append (expireDateFormat)
            //binding.resultTextView.append (it.refreshToken.expires.toString())
            //Log.d(TAG, "onCreate: $it")
        }
        viewModel.getDataSuccess.observe(this){
            it.forEach {
                Log.d(TAG, "onCreate: ${it.maktx}")
            }
        }
        viewModel.execSuccess.observe(this){
            Log.d(TAG, "onCreate: $it")
        }
        viewModel.getAlbumSuccess.observe(this){
            Log.d(TAG, "onCreate: $it")
            //val albumList= it.body()?.listIterator()
//            val albumList= it.body()
//            albumList?.forEach {
//                Log.d(TAG, "onCreate: ${it.title}")
//            }
        }
        binding.showActivityButton.setOnClickListener {
            startActivity(
                Intent(this, RetrofitTestActivity::class.java).apply {
                    putExtra("url", "http://172.17.0.236:5555")
                }
            )
        }
        binding.retrofitButton.setOnClickListener {

            viewModel.getAlbums()


//            lifecycleScope.launch {
//                val  response: Response<Album> = retService.getAlbums()
//                responseLiveData.postValue(response)
//            }

//            liveData {
//                val  response: Response<Album> = retService.getAlbums()
//                emit(response)
//            }

        }

        binding.loginTestButton.setOnClickListener {
            viewModel.loginUser("bsbae1@kolon.com", "123456")

//            val loginRequest = LoginRequest(
//                password = "123456",
//                email = "bsbae1@kolon.com"
//            )
//            lifecycleScope.launch {
//                val response = RetrofitObject.api.authLogin(loginRequest)
//                Log.d(TAG, "onCreate: ${response.body()}")
//            }

        }
        binding.getDatesetButton.setOnClickListener {
            viewModel.getDataSet()
        }
        binding.execTestButton.setOnClickListener {
            viewModel.saveData("111111", "33333333")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        val intent = Intent(this, ScanService::class.java)
        stopService(intent)
    }
    private fun showToast(msg: String?) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }
    private fun convertLongToTime(time: Long): String {
        val dateTime = java.util.Date(time * 1000)
        // val format = SimpleDateFormat("HH:mm:ss dd/MM/yyyy", Locale.KOREA)
        val format = SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.KOREA)
        return format.format(dateTime)
    }

}