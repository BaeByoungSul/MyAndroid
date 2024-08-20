package com.myapp.testrfid


import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager

import com.myapp.testrfid.adapter.MainAdapter


import com.myapp.testrfid.databinding.ActivityMainBinding
import com.myapp.testrfid.model.TagItem
import com.myapp.testrfid.service.RfidBindService
import com.myapp.testrfid.util.Constants.READER_INVENTORY_ACTION
import com.myapp.testrfid.util.Constants.READER_INVENTORY_DATA
import com.myapp.testrfid.util.Constants.READER_INVENTORY_DATA2
import com.myapp.testrfid.util.Constants.READER_START_FAIL
import com.myapp.testrfid.util.Constants.READER_START_STOP
import com.myapp.testrfid.util.Constants.READER_START_STOP_DATA
import com.myapp.testrfid.util.Constants.READER_START_SUCCESS
import com.myapp.testrfid.util.Constants.SCANNER_SCAN_ACTION
import com.myapp.testrfid.util.Constants.SCANNER_SCAN_DATA
import com.myapp.testrfid.util.ReaderType
import com.myapp.testrfid.view.TagSaveActivity
import com.myapp.testrfid.viewmodel.MainViewModel

// 1. 권한이 있을 경우 서비스 바인드
// 2. 서비스에서 보내는 메세지 받아서 처리 ( mServiceReceiver )
// 3. 서비스가 정상 실행 여부
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: MainViewModel
    private lateinit var mReaderType : ReaderType

    private val myListAdapter: MainAdapter by lazy {
        MainAdapter()
    }

    var myService: RfidBindService? = null
    var isService = false

    private val connection = object : ServiceConnection{
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as RfidBindService.MyBinder
            myService = binder.getService()
            isService = true

            binding.readerRadioButton.isEnabled = true
            binding.scannerRadioButton.isEnabled = true
            mReaderType = myService!!.getCurrentReader()
            if (mReaderType == ReaderType.SCANNER) {
                binding.scannerRadioButton.isChecked = true
            }else if (mReaderType == ReaderType.RFID_READER) {
                binding.readerRadioButton.isChecked = true
            }
            Log.d(TAG, "onServiceConnected: ")
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            isService = false
            Log.d(TAG, "onServiceDisconnected: ")
        }

    }
    //콜백 인스턴스 생성
    private val callback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            // 뒤로 버튼 이벤트 처리
            showToast("뒤로가기 클릭")

        }
    }
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

        checkPermission()

        viewModel = ViewModelProvider(this).get(modelClass = MainViewModel::class.java)

        initRecyclerView()

        viewModel.mItemMapLiveData.observe(this){

            myListAdapter.clearData()
            it.map { (key, aa) ->
                //list.add(0, aa)
                myListAdapter.addItem(aa)
                Log.d(TAG, "observe:$key ${aa.tagHexValue} ${aa.rssiValue} ${aa.dupCount}")
            }
        }

        this.onBackPressedDispatcher.addCallback(this, callback) //위에서 생성한 콜백 인스턴스 붙여주기

        binding.radioGroup1.setOnCheckedChangeListener { group, checkedId ->
            Log.d(TAG, "onCreate: $checkedId")
            if (myService?.getReader()?.isOperationRunning == true) return@setOnCheckedChangeListener

            viewModel.removeAllItem()
            when(checkedId) {
                binding.readerRadioButton.id -> {myService?.setReader(ReaderType.RFID_READER)}
                binding.scannerRadioButton.id -> {myService?.setReader(ReaderType.SCANNER)}
            }
        }

        binding.startAndStopButton.setOnClickListener {

            if ( binding.progressBar.visibility == View.GONE){
                binding.progressBar.visibility = View.VISIBLE
            }else if( binding.progressBar.visibility == View.VISIBLE){
                binding.progressBar.visibility = View.GONE
            }
           myService?.startAndStopInventory()
           // viewModel.testAddItem()
        }
        binding.gotoButton.setOnClickListener {

            if (myService?.getReader()?.isOperationRunning == true) return@setOnClickListener

            startActivity(Intent(this, TagSaveActivity::class.java))
        }
    }
    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume: ")
        registerReceiver()
    }
    override fun onPause() {
        super.onPause()
        Log.d(TAG, "onPause: ")
        unregisterReceiver(mServiceReceiver)
    }
    override fun onDestroy() {
        super.onDestroy()
        if (isService) {  // 서비스가 실행되고 있을 때
            unbindService(connection)  // 바인드 해제
            isService = false
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                binding.progressBar.visibility = View.VISIBLE

                Handler(Looper.getMainLooper()).postDelayed({
                    serviceBind()
                    //binding.progressBar.visibility = View.GONE
                }, 1000)
            }
        }
    }



    private fun initRecyclerView(){
        //val adapter=MainAdapter2() //어댑터 객체 만듦
        //adapter.datalist=mDatas //데이터 넣어줌
        //myListAdapter2 .datalist =mDatas
        binding.recyclerView.adapter=myListAdapter //리사이클러뷰에 어댑터 연결
        binding.recyclerView.layoutManager=LinearLayoutManager(this) //레이아웃 매니저 연결
        binding.recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL,false)
        binding.recyclerView.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
    }
    private fun serviceBind() {  // 버튼 이벤트
        val intent = Intent(this, RfidBindService::class.java)
        bindService(intent, connection, Context.BIND_AUTO_CREATE)
        // 서비스를 호출하면서 커넥션을 같이 넘겨준다.
        // BIND_AUTO_CREATE : 서비스가 생성되어 있지 않으면 생성 후 바인딩을 하고 생성되어 있으면 바로 바인딩
    }

    private fun setWhenStart(isSuccess:Boolean){
        binding.progressBar.visibility = View.GONE
        if (isSuccess) {

            binding.gotoButton.isEnabled = true
            binding.startAndStopButton.isEnabled = true
        }else {
            binding.gotoButton.isEnabled = false
            binding.startAndStopButton.isEnabled = false
        }
    }
    private fun setWhenInventorying(isInventorying:Boolean){
        if (isInventorying){
            binding.progressBar.visibility=View.VISIBLE
            binding.gotoButton.isEnabled = false
            binding.scannerRadioButton.isEnabled = false
            binding.readerRadioButton.isEnabled = false
        }else{
            binding.progressBar.visibility=View.GONE
            binding.gotoButton.isEnabled = true
            binding.scannerRadioButton.isEnabled = true
            binding.readerRadioButton.isEnabled = true
        }
    }
    private fun showToast(msg: String?) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }


    private fun checkPermission(){
        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            binding.progressBar.visibility = View.VISIBLE
            Handler(Looper.getMainLooper()).postDelayed({
                serviceBind()
                //binding.progressBar.visibility = View.GONE
            }, 1000)

        }
        // 교육용 팝업
        else if (shouldShowRequestPermissionRationale(
                android.Manifest.permission.CAMERA
            )
        ) {
            showPermissionInfoDialog()
        }
        else {
            requestPermission()
        }
    }
    private fun requestPermission(){
        ActivityCompat.requestPermissions(
            this,
            arrayOf(android.Manifest.permission.CAMERA),
            CAMERA_PERMISSION
        )
    }
    private fun showPermissionInfoDialog(){
        AlertDialog.Builder(this).apply {
            setMessage("바코드 스캔을 하기 위해서 카메라 사용 권한이 필요합니다.")
            setNegativeButton("취소", null)
            setPositiveButton("동의") { _ , _->
                requestPermission()
            }
        }.show()
    }

    /// 바인드 서비스에서 보내는 Action
    private fun registerReceiver(){
        val filter = IntentFilter()
        filter.addAction(READER_INVENTORY_ACTION)
        filter.addAction(READER_START_SUCCESS)
        filter.addAction(READER_START_FAIL)
        filter.addAction(READER_START_STOP)
        filter.addAction(SCANNER_SCAN_ACTION)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(mServiceReceiver, filter, RECEIVER_NOT_EXPORTED)
        }else @SuppressLint("UnspecifiedRegisterReceiverFlag")
        {
            registerReceiver(mServiceReceiver, filter)
        }
    }
    // 바인드 서비스에서 보낸 메세지 받아서 처리
    private val mServiceReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {

            val action = intent?.action
            //Log.d(TAG, "onReceive: $action")
            if (action == READER_START_SUCCESS ){  // reader.start() 성공

                setWhenStart(true)

            }else if (action == READER_START_FAIL) { // reader.start() 실패

                setWhenStart(false)
            }else if (action == READER_START_STOP) { // reader.inventory 시작/종료
                val mIsInventorying = intent.getBooleanExtra(READER_START_STOP_DATA, false)
                if (mIsInventorying) {
                    setWhenInventorying(true)
                }else{
                    setWhenInventorying(false)
                }
            }else if ( action == SCANNER_SCAN_ACTION){
                binding.progressBar.visibility=View.GONE
                val barcode = intent.getStringExtra(SCANNER_SCAN_DATA)
                //barcode?.let { viewModel.testAddItem(it) }
            }else if (action == READER_INVENTORY_ACTION){
                val tagItem = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    intent.getParcelableExtra(
                        READER_INVENTORY_DATA, TagItem::class.java)
                } else {
                    intent.getParcelableExtra<TagItem>(READER_INVENTORY_DATA)
                }
                val tagData = intent.getStringExtra(READER_INVENTORY_DATA2)
                    
                Log.d(TAG, "onReceive: $tagData")
                Log.d(TAG, "onReceive: $tagItem")
                tagItem?.let {
                    Log.d(TAG, "onReceive: $it")
                    viewModel.addTagItem(it)
                }

            }

        }
    }

    companion object {
        private const val TAG  ="MainActivity"
        private const val CAMERA_PERMISSION = 1000
//        private const val READER_START_SUCCESS = "reader.start.success"
//        private const val READER_START_FAIL = "reader.start.fail"
//        private const val READER_INVENTORY_ACTION = "reader.inventory.action"
//        private const val READER_INVENTORY_DATA = "reader.inventory.data"
//        private const val READER_START_STOP = "reader.start.stop"
//        private const val READER_START_STOP_DATA = "reader.start.stop.data"
//        private const val SCANNER_SCAN_ACTION = "scanner.scan.action"
//        private const val SCANNER_SCAN_DATA = "scanner.scan.data"
    }

}