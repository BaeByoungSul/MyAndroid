package com.myapp.testrfid.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.myapp.testrfid.adapter.BoxIpGoModel
import com.myapp.testrfid.model.TagItem


class MainViewModel: ViewModel() {
    private val _isLoading: MutableLiveData<Boolean> = MutableLiveData(false)
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _toastMessage: MutableLiveData<String?> = MutableLiveData()
    val toastMessage: LiveData<String?> get() = _toastMessage


    private val _mItemHashMap: HashMap<String, TagItem> = HashMap<String, TagItem>()
    val mItemMapLiveData: MutableLiveData<HashMap<String, TagItem>> = MutableLiveData()

    private val _mTagList3 : MutableLiveData<ArrayList<TagItem>> = MutableLiveData()
    val mTagList3: LiveData<ArrayList<TagItem>> get() = _mTagList3


    fun addTagItem(tagItem: TagItem){
        val storedItem = _mItemHashMap[tagItem.tagValue]
        if( storedItem != null ){
            // 수정
            storedItem.rssiValue = tagItem.rssiValue ?: ""
            storedItem.dupCount += 1
            _mItemHashMap[tagItem.tagValue] = storedItem
        }else {
            _mItemHashMap[tagItem.tagValue] = tagItem
        }
        ///mItemMapLiveData.value = _mItemHashMap
        mItemMapLiveData.postValue(_mItemHashMap)
    }
    fun removeAllItem() {
        _mItemHashMap.clear()
        mItemMapLiveData.postValue(_mItemHashMap)
    }
    companion object {
        private const val TAG = "BoxIpGoViewModel"
    }

}


