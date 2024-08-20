package com.myapp.testrfid.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.myapp.testrfid.databinding.RfidTagRowBinding
import com.myapp.testrfid.databinding.TagSaveRowBinding
import com.myapp.testrfid.model.TagItem

class TagSaveAdapter : RecyclerView.Adapter<TagSaveAdapter.MyViewHolder> (){
    private var datalist = mutableListOf<TagItem>()

    inner class MyViewHolder(private val binding: TagSaveRowBinding)
        : RecyclerView.ViewHolder(binding.root){
        fun bind(data: TagItem){
            with(binding){
                hexValueTextView.text = data.tagHexValue
                tagValueTextView.text = data.tagValue
                rssiMvTextView.text = data.rssiValue
                //rssiMvTextView.text = String.format("%.1f", data.rssiMv?.average()) // data.mRssiMv.average().toString()
                dupCountTextView.text = data.dupCount.toString()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding= TagSaveRowBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return MyViewHolder(binding)
    }

    override fun getItemCount(): Int = datalist.size

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.bind(datalist[position])

    }

//    @SuppressLint("NotifyDataSetChanged")
//    fun addItem(item: TagSaveItem) {
//        datalist.add(item)
//        notifyDataSetChanged()
//    }
    fun addItem2(item: TagItem) {
        datalist.add(item)
        notifyDataSetChanged()
    }
    @SuppressLint("NotifyDataSetChanged")
    fun clearData(){
        datalist.clear()
        notifyDataSetChanged()
    }
    fun addListItem(items: List<TagItem>){
//        val result = mutableListOf<BoxIpGoModel>()
//        items.forEach {
//           result.add(BoxIpGoModel(it.mTagValue!!, "success", "boxnumber"))
//         }
        datalist = items.toMutableList()

    }
}