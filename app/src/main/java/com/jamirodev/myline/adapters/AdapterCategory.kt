package com.jamirodev.myline.adapters

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.jamirodev.myline.Model.ModelCategory
import com.jamirodev.myline.R
import com.jamirodev.myline.RvListenerCategory
import com.jamirodev.myline.databinding.ItemCategoryStartBinding
import java.util.Random

class AdapterCategory(
    private val context: Context,
    private val categoryArrayList: ArrayList<ModelCategory>,
    private val rvListenerCategory: RvListenerCategory
): Adapter<AdapterCategory.HolderCategory>() {

    private lateinit var binding: ItemCategoryStartBinding
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderCategory {
        binding = ItemCategoryStartBinding.inflate(LayoutInflater.from(context), parent, false)
        return HolderCategory(binding.root)
    }

    override fun getItemCount(): Int {
        return categoryArrayList.size
    }

    override fun onBindViewHolder(holder: HolderCategory, position: Int) {
        val modelCategory = categoryArrayList[position]

        val icon = modelCategory.icon
        val category = modelCategory.category
        val color = ContextCompat.getColor(context, R.color.navi3)

        holder.categoryIconIv.setImageResource(icon)
        holder.categoryIv.text = category
        holder.categoryIconIv.setBackgroundColor(color)

        holder.itemView.setOnClickListener {
            rvListenerCategory.onCategoryClick(modelCategory)
        }

    }

    inner class HolderCategory(itemView: View) :ViewHolder(itemView) {
        var categoryIconIv = binding.categoryIconIV
        var categoryIv = binding.TvCategory
    }

}