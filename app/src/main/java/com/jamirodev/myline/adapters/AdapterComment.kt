package com.jamirodev.myline.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.jamirodev.myline.Constants
import com.jamirodev.myline.Model.ModelComment
import com.jamirodev.myline.R
import com.jamirodev.myline.databinding.ItemComentBinding

class AdapterComment : RecyclerView.Adapter<AdapterComment.HolderComment> {
    val context: Context
    val commentArrayList: ArrayList<ModelComment>
    private lateinit var binding: ItemComentBinding
    private lateinit var firebaseAuth: FirebaseAuth

    constructor(context: Context, commentArrayList: ArrayList<ModelComment>) {
        this.context = context
        this.commentArrayList = commentArrayList
        firebaseAuth = FirebaseAuth.getInstance()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderComment {
        binding = ItemComentBinding.inflate(LayoutInflater.from(context), parent, false)
        return HolderComment(binding.root)
    }

    override fun getItemCount(): Int {
        return commentArrayList.size
    }

    override fun onBindViewHolder(holder: HolderComment, position: Int) {
        val model = commentArrayList[position]
        val id = model.id
        val comment = model.comment
        val uid = model.uid
        val time = model.time

        val format_date = Constants.getDate(time.toLong())

        holder.Tv_date.text = format_date
        holder.Tv_comment.text = comment

        loadInformation(model, holder)
        holder.itemView.setOnClickListener {
            val uidUser = model.uid
            if (firebaseAuth.uid.equals(uidUser)) {
            dialogDeleteComment(model, holder)
            }
        }
    }

    private fun dialogDeleteComment(model: ModelComment, holder: AdapterComment.HolderComment) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Eliminar comentario")
        builder.setMessage("¿Quieres eliminar el comentario?")
            .setPositiveButton("Eliminar") { d, e ->
                val uidUser = model.uid
                if (firebaseAuth.uid.equals(uidUser)) {
                    val uidSeller = model.uid_seller
                    val idComment = model.id
                    val ref = FirebaseDatabase.getInstance().getReference("SellerComments")
                    ref.child(uidSeller).child("Comments").child(idComment)
                        .removeValue()
                        .addOnFailureListener { e ->
                            Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
                        }
                } else {
                    Toast.makeText(context, "Este comentario no es tuyo", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar") { d, e ->
                d.dismiss()
            }
            .show()
    }

    private fun loadInformation(model: ModelComment, holder: AdapterComment.HolderComment) {
        val uid = model.uid
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(uid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val names = "${snapshot.child("names").value}"
                    val image = "${snapshot.child("urlProfileImage").value}"

                    holder.Tv_names.text = names
                    try {
                        Glide.with(context)
                            .load(image)
                            .placeholder(R.drawable.usuario)
                            .into(holder.IvImage)
                    } catch (e: Exception) {
                        holder.IvImage.setImageResource(R.drawable.usuario)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })
    }

    inner class HolderComment(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val IvImage = binding.IvImageProfile
        val Tv_names = binding.TvNamesComment
        val Tv_date = binding.TvDateComment
        val Tv_comment = binding.TvComment
    }


}