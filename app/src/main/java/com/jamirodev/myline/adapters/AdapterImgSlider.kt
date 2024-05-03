package com.jamirodev.myline.adapters

import android.app.Dialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.bumptech.glide.Glide
import com.github.chrisbanes.photoview.PhotoView
import com.google.android.material.button.MaterialButton
import com.google.android.material.imageview.ShapeableImageView
import com.jamirodev.myline.Model.ModelImgSlider
import com.jamirodev.myline.R
import com.jamirodev.myline.databinding.ItemImageSliderBinding

class AdapterImgSlider : Adapter<AdapterImgSlider.HolderImageSlider> {

    private lateinit var binding: ItemImageSliderBinding
    private var context: Context
    private var imageArrayList: ArrayList<ModelImgSlider>

    constructor(context: Context, imageArrayList: ArrayList<ModelImgSlider>) {
        this.context = context
        this.imageArrayList = imageArrayList
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderImageSlider {
        binding = ItemImageSliderBinding.inflate(LayoutInflater.from(context), parent, false)
        return HolderImageSlider(binding.root)
    }

    override fun getItemCount(): Int {
        return imageArrayList.size
    }

    override fun onBindViewHolder(holder: HolderImageSlider, position: Int) {
        val modelImageSlider = imageArrayList[position]

        val imageUrl = modelImageSlider.imageUrl
        val imageCounter = "${position + 1} / ${imageArrayList.size}"

        holder.imageCounterTv.text = imageCounter
        try {
            Glide.with(context)
                .load(imageUrl)
                .placeholder(R.drawable.ic_imagen)
                .into(holder.imageIv)
        } catch (e: Exception) { }

        holder.imageIv.setOnClickListener {
            imageViewer(modelImageSlider.imageUrl)
        }
    }


    private fun imageViewer(image: String) {
        val Pv: PhotoView
        val Btn_close: MaterialButton
        val dialog = Dialog(context)
        dialog.setContentView(R.layout.dialog_box_img_viewer)
        Pv = dialog.findViewById(R.id.PV_image)
        Btn_close = dialog.findViewById(R.id.Btn_close_viewer)
        try {
            Glide.with(context)
                .load(image)
                .placeholder(R.drawable.new_message)
                .into(Pv)
        }catch (e:Exception) { }

        Btn_close.setOnClickListener { dialog.dismiss() }
        dialog.show()
        dialog.setCanceledOnTouchOutside(false)
    }


    inner class HolderImageSlider(itemView: View) : ViewHolder(itemView) {
        var imageIv: ShapeableImageView = binding.imageIV
        var imageCounterTv: TextView = binding.imageCounterTv
    }


}




