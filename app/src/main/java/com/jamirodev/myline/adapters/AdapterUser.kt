package com.jamirodev.myline.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.imageview.ShapeableImageView
import com.jamirodev.myline.Model.ModelUser
import com.jamirodev.myline.R
import com.jamirodev.myline.chat.ChatActivity

class AdapterUser(context: Context, listUser: List<ModelUser>) :
    RecyclerView.Adapter<AdapterUser.ViewHolder?>() {

        private val context: Context
        private val listUser: List<ModelUser>

        init {
            this.context = context
            this.listUser = listUser
        }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var user_name : TextView
        var user_email : TextView
        var user_image : ImageView   //ShapeableImageView change in case of error

        init {
            user_name = itemView.findViewById(R.id.Item_names)
            user_email = itemView.findViewById(R.id.Item_email)
            user_image = itemView.findViewById(R.id.Item_image)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View = LayoutInflater.from(context).inflate(R.layout.item_user,parent,false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return listUser.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user: ModelUser = listUser[position]

//        val displayName = if (user.getNames().isNullOrBlank()) {
//            "Usuario no identificado"
//        } else {
//            user.getNames()
//        }

        holder.user_name.text = user.getNames()
        holder.user_email.text = user.getEmail()
        Glide.with(context)
            .load(user.getUrlProfileImage())
            .placeholder(R.drawable.usuario)
            .into(holder.user_image)

        //Send uid to selected user
        holder.itemView.setOnClickListener {
            val intent = Intent(context, ChatActivity::class.java)
            intent.putExtra("uidSeller", user.getUid())
            context.startActivity(intent)
        }
    }
}
