package com.myapp.myframedagger.view

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.myapp.myframedagger.R
import com.myapp.myframedagger.common.di.DaggerRetrofitComponent
import com.myapp.myframedagger.databinding.ActivityRetrofitTestBinding
import com.myapp.myframedagger.viewmodel.AppViewModelFactory
import com.myapp.myframedagger.viewmodel.RetrofitTestViewModel
import javax.inject.Inject

class RetrofitTestActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRetrofitTestBinding

    @Inject
    lateinit var viewModelFactory: AppViewModelFactory
    private lateinit var viewModel: RetrofitTestViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityRetrofitTestBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        DaggerRetrofitComponent.builder().build().inject(this)
        //DaggerRetrofitComponent.create().inject(this)

        viewModel = ViewModelProvider(this,viewModelFactory)[RetrofitTestViewModel::class.java]

        binding.retrofitTestButton.setOnClickListener {
            viewModel.loginUser3("bsbae1@kolon.com", "123456")
        }
    }
}