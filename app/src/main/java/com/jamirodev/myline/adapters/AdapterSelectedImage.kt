package com.jamirodev.myline.adapters

import android.app.Dialog
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.jamirodev.myline.Model.ModelImageSelected
import com.jamirodev.myline.R
import com.jamirodev.myline.databinding.ItemSelectedImagesBinding

class AdapterSelectedImage(
    private val context: Context,
    private val selectedImagesArrayList: ArrayList<ModelImageSelected>,
    private val idAnnounce: String
) : Adapter<AdapterSelectedImage.HolderSelectedImages>() {
    private lateinit var binding: ItemSelectedImagesBinding

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderSelectedImages {
        binding = ItemSelectedImagesBinding.inflate(LayoutInflater.from(context), parent, false)
        return HolderSelectedImages(binding.root)
    }

    override fun getItemCount(): Int {
        return selectedImagesArrayList.size
    }

    override fun onBindViewHolder(holder: HolderSelectedImages, position: Int) {
        val model = selectedImagesArrayList[position]
        val imageUrl = model.imageUrl
        val imageUri = model.imageUri
        if (model.fromInternet) {
            try {
                Glide.with(context)
                    .load(imageUrl)
                    .placeholder(R.drawable.ic_imagen)
                    .into(holder.item_image)
            } catch (e: Exception) {
            }
        } else {
            try {
                Glide.with(context)
                    .load(imageUri)
                    .placeholder(R.drawable.ic_imagen)
                    .into(holder.item_image)
            } catch (e: Exception) {
            }
        }
        holder.btn_close.setOnClickListener {
            if (model.fromInternet) {
                val Btn_agree: MaterialButton
                val Btn_cancel: MaterialButton
                val dialog = Dialog(context)

                dialog.setContentView(R.layout.box_d_delete_image)

                Btn_agree = dialog.findViewById(R.id.Btn_agree)
                Btn_cancel = dialog.findViewById(R.id.Btn_cancel)

                Btn_agree.setOnClickListener {
                    deleteImageFirebase(model, holder, position)
                    dialog.dismiss()
                }
                Btn_cancel.setOnClickListener {
                    dialog.dismiss()
                }
                dialog.show()
                dialog.setCanceledOnTouchOutside(false)
            } else {
                selectedImagesArrayList.remove(model)
                notifyDataSetChanged()
            }
        }
    }

    private fun deleteImageFirebase(
        model: ModelImageSelected,
        holder: AdapterSelectedImage.HolderSelectedImages,
        position: Int
    ) {
        val idImage = model.id
        val ref = FirebaseDatabase.getInstance().getReference("Announcements")
        ref.child(idAnnounce).child("Images").child(idImage)
            .removeValue()
            .addOnSuccessListener {
                try {
                    selectedImagesArrayList.remove(model)
                    deleteImageStorage(model)
                    notifyItemRemoved(position)
                } catch (e: Exception) {

                }
            }
            .addOnFailureListener {
            }
    }

    private fun deleteImageStorage(model: ModelImageSelected) {
        val routeImage = "Announcements/"+model.id
        val ref = FirebaseStorage.getInstance().getReference(routeImage)
        ref.delete()
            .addOnSuccessListener {
                Toast.makeText(context, "Eliminado", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "{$e.message}", Toast.LENGTH_SHORT).show()
                Log.e("!!!!!", "Error al elimnar la imagen de la db: ${e.message}")
                Log.e("!!!!!", "route image 2: ${routeImage}")
            }
    }

    inner class HolderSelectedImages(itemView: View) : ViewHolder(itemView) {
        var item_image = binding.itemImage
        var btn_close = binding.cerrarItem
    }
}