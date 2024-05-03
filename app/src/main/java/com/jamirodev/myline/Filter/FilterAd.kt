package com.jamirodev.myline.Filter

import android.widget.Filter
import com.jamirodev.myline.Model.ModelAnnounce
import com.jamirodev.myline.adapters.AdapterAnnounce
import java.util.Locale

class FilterAd(
    private val adapter: AdapterAnnounce,
    private val filterList: ArrayList<ModelAnnounce>
) : Filter() {
    override fun performFiltering(filter: CharSequence?): FilterResults {
        var filter = filter
        val result = FilterResults()
        if (!filter.isNullOrEmpty()) {
            filter = filter.toString().uppercase(Locale.getDefault())
            val filterModel = ArrayList<ModelAnnounce>()
            for (i in filterList.indices) {
                if (filterList[i].marca.uppercase(Locale.getDefault()).contains(filter) ||
                    filterList[i].category.uppercase(Locale.getDefault()).contains(filter) ||
                    filterList[i].condition.uppercase(Locale.getDefault()).contains(filter) ||
                    filterList[i].title.uppercase(Locale.getDefault()).contains(filter)
                ) {
                    filterModel.add(filterList[i])
                }
            }
            result.count = filterModel.size
            result.values = filterModel
        } else {
            result.count = filterList.size
            result.values = filterList
        }
        return result
    }

    override fun publishResults(filter: CharSequence?, results: FilterResults) {
        adapter.announceArrayList = results.values as ArrayList<ModelAnnounce>
        adapter.notifyDataSetChanged()
    }
}