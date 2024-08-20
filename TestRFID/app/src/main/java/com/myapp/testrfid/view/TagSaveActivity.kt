package com.myapp.testrfid.view

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.ServiceConnection
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.myapp.testrfid.R
import com.myapp.testrfid.adapter.TagSaveAdapter
import com.myapp.testrfid.databinding.ActivityTagSaveBinding
import com.myapp.testrfid.model.TagItem
import com.myapp.testrfid.service.RfidBindService
import com.myapp.testrfid.util.Constants
import com.myapp.testrfid.util.Constants.READER_INVENTORY_ACTION
import com.myapp.testrfid.util.Constants.READER_INVENTORY_DATA
import com.myapp.testrfid.util.Constants.READER_START_FAIL
import com.myapp.testrfid.util.Constants.READER_START_STOP
import com.myapp.testrfid.util.Constants.READER_START_STOP_DATA
import com.myapp.testrfid.util.Constants.READER_START_SUCCESS
import com.myapp.testrfid.util.ReaderType
import com.myapp.testrfid.viewmodel.TagSaveViewModel

// 1. onCreate 서비스 바인드: 메인화면에서 카메라 권한 부여됨
// 2. 서비스에서 보내는 메세지 받아서 처리 ( mServiceReceiver )
// 3. 서비스가 정상 바인딩 여부에 따라서
class TagSaveActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTagSaveBinding
    private lateinit var viewModel: TagSaveViewModel
    private lateinit var listAdapter: TagSaveAdapter
    private lateinit var mReaderType : ReaderType

    var myService: RfidBindService? = null
    var isServiceActive = false

    private val connection = object : ServiceConnection{
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as RfidBindService.MyBinder
            myService = binder.getService()
            isServiceActive = true

            mReaderType = myService!!.getCurrentReader()
            if (mReaderType == ReaderType.SCANNER) {
                binding.scannerRadioButton.isChecked = true
            }else if (mReaderType == ReaderType.RFID_READER) {
                binding.readerRadioButton.isChecked = true
            }

        }

        override fun onServiceDisconnected(name: ComponentName?) {
            isServiceActive = false
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityTagSaveBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val intent = Intent(this, RfidBindService::class.java)
        bindService(intent, connection, Context.BIND_AUTO_CREATE)

        viewModel = ViewModelProvider(this).get(modelClass = TagSaveViewModel::class.java)

        initRecyclerView()

        viewModel.mItemMapLiveData.observe(this){

            listAdapter.clearData()
            it.map { (key, aa) ->
                //list.add(0, aa)
                listAdapter.addItem2(aa)
                //Log.d(MainActivity.TAG, "observe:$key ${aa.mTagHexValue} ${aa.mRssiValue} ${aa.mDupCount}")
            }

        }

        binding.radioGroup1.setOnCheckedChangeListener { group, checkedId ->
            Log.d(TAG, "onCreate: $checkedId")
            viewModel.removeAllItem()
            when(checkedId) {
                binding.readerRadioButton.id -> {myService?.setReader(ReaderType.RFID_READER)}
                binding.scannerRadioButton.id -> {myService?.setReader(ReaderType.SCANNER)}
            }
        }
        binding.saveButton.setOnClickListener {
            viewModel.saveData()
        }
//        Handler(Looper.getMainLooper()).postDelayed({
//            val intent = Intent(this, RfidBindService::class.java)
//            bindService(intent, connection, Context.BIND_AUTO_CREATE)
//        }, 1000)

    }

    override fun onResume() {
        super.onResume()
        registerReceiver()
    }
    override fun onPause() {
        super.onPause()
        unregisterReceiver(mRfidReceiver)

    }
    override fun onDestroy() {
        super.onDestroy()
        if (isServiceActive) {  // 서비스가 실행되고 있을 때
            unbindService(connection)  // 바인드 해제
            isServiceActive = false
        }
    }
    private fun initRecyclerView(){
        listAdapter = TagSaveAdapter()

        binding.recyclerView.adapter=listAdapter //리사이클러뷰에 어댑터 연결
        binding.recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL,false)
        binding.recyclerView.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
    }
    private fun setWhenStart(isSuccess:Boolean){
        binding.progressBar.visibility = View.GONE
        if (isSuccess){
            binding.saveButton.isEnabled = true
        }else {
            binding.saveButton.isEnabled = false
        }
    }
    private fun setWhenInventorying(isInventorying:Boolean){
        if (isInventorying){
            binding.progressBar.visibility=View.VISIBLE
            binding.scannerRadioButton.isEnabled = false
            binding.readerRadioButton.isEnabled = false
            binding.saveButton.isEnabled = false
        }else{
            binding.progressBar.visibility=View.GONE
            binding.scannerRadioButton.isEnabled = true
            binding.readerRadioButton.isEnabled = true
            binding.saveButton.isEnabled = true
        }
    }

    private fun registerReceiver(){
        val filter = IntentFilter()
        filter.addAction(READER_INVENTORY_ACTION)
        filter.addAction(READER_START_SUCCESS)
        filter.addAction(READER_START_FAIL)
        filter.addAction(READER_START_STOP)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(mRfidReceiver, filter, RECEIVER_NOT_EXPORTED)
        }else @SuppressLint("UnspecifiedRegisterReceiverFlag")
        {
            registerReceiver(mRfidReceiver, filter)
        }
    }

    // 바인드 서비스에서 보낸 broadcast Intent
    private val mRfidReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {

            val action = intent?.action
            //Log.d(TAG, "onReceive: $action")
            if (action == READER_START_SUCCESS){
                setWhenStart(true)
            }else if (action == READER_START_FAIL) {
                setWhenStart(false)

            }else if (action == READER_START_STOP) {
                val mIsInventorying = intent.getBooleanExtra(READER_START_STOP_DATA, false)
                if (mIsInventorying) {
                    setWhenInventorying(true)
                }else{
                    setWhenInventorying(false)
                }
            }else if ( action == Constants.SCANNER_SCAN_ACTION){
                binding.progressBar.visibility=View.GONE
                val barcode = intent.getStringExtra(Constants.SCANNER_SCAN_DATA)
                //barcode?.let { viewModel.testAddItem(it) }
            }else if (action == READER_INVENTORY_ACTION){
                val tagItem = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    intent.getParcelableExtra(
                        READER_INVENTORY_DATA, TagItem::class.java)
                } else {
                    intent.getParcelableExtra<TagItem>(READER_INVENTORY_DATA)
                }

                Log.d(TAG, "onReceive: $tagItem")
                tagItem?.let {
                    Log.d(TAG, "onReceive: $it")
                    viewModel.addTagItem(it)
                }

            }
       }
    }
    companion object {
        private const val TAG  ="TagSaveActivity"
    }

}