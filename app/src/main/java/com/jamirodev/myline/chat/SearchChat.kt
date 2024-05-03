package com.jamirodev.myline.chat

import android.widget.Filter
import com.jamirodev.myline.Model.ModelChats
import com.jamirodev.myline.adapters.AdapterChats
import java.util.Locale

class SearchChat : Filter {

    private val adapterChats: AdapterChats
    private val filterList: ArrayList<ModelChats>

    constructor(adapterChats: AdapterChats, filterList: ArrayList<ModelChats>) : super() {
        this.adapterChats = adapterChats
        this.filterList = filterList
    }


    override fun performFiltering(filter: CharSequence?): FilterResults {
        var filter: CharSequence?=filter
        val results = FilterResults()

        if (!filter.isNullOrEmpty()) {
            filter = filter.toString().uppercase(Locale.getDefault())
            val filterModels = ArrayList<ModelChats>()
            for (i in filterList.indices) {
                if (filterList[i].names.uppercase().contains(filter)) {
                    filterModels.add(filterList[i])
                }
            }
            results.count = filterModels.size
            results.values = filterModels
        } else {
            results.count = filterList.size
            results.values = filterList
        }
        return results
    }

    override fun publishResults(filter: CharSequence, results: FilterResults) {
        adapterChats.chatsArrayList = results.values as ArrayList<ModelChats>
        adapterChats.notifyDataSetChanged()
    }
}