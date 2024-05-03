package com.jamirodev.myline.adapters

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.github.chrisbanes.photoview.PhotoView
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.jamirodev.myline.Constants
import com.jamirodev.myline.Model.ModelChat
import com.jamirodev.myline.R

class AdapterChat : RecyclerView.Adapter<AdapterChat.HolderChat> {
    private val context: Context
    private val chatArray: ArrayList<ModelChat>
    private val firebaseAuth: FirebaseAuth
    private var chatRoute = ""

    companion object {
        private const val MENSAJE_IZQUIERDO = 0
        private const val MENSAJE_DERECHO = 1
    }

    constructor(context: Context, chatArray: ArrayList<ModelChat>) {
        this.context = context
        this.chatArray = chatArray
        firebaseAuth = FirebaseAuth.getInstance()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderChat {
        if (viewType == MENSAJE_DERECHO) {
            val view = LayoutInflater.from(context).inflate(R.layout.item_chat_right, parent, false)
            return HolderChat(view)
        } else {
            val view = LayoutInflater.from(context).inflate(R.layout.item_chat_left, parent, false)
            return HolderChat(view)
        }
    }

    override fun getItemCount(): Int {
        return chatArray.size
    }

    override fun getItemViewType(position: Int): Int {
        if (chatArray[position].senderUid == firebaseAuth.uid) {
            return MENSAJE_DERECHO
        } else {
            return MENSAJE_IZQUIERDO
        }
    }

    override fun onBindViewHolder(holder: HolderChat, position: Int) {
        val modelChat = chatArray[position]
        val message = modelChat.message
        val typeMessage = modelChat.typeMessage
        val time = modelChat.time

        val format_date_hour = Constants.getDateHour(time)
        holder.Tv_time_message.text = format_date_hour

        if (typeMessage == Constants.MESSAGE_TYPE_TEXT) {
            holder.Tv_message.visibility = View.VISIBLE
            holder.Iv_message.visibility = View.GONE
            holder.Tv_message.text = message

            if (modelChat.senderUid.equals(firebaseAuth.uid)) {
                holder.itemView.setOnClickListener {
                    val options = arrayOf<CharSequence>("Eliminar mensaje", "Cancelar")
                    val builder: AlertDialog.Builder = AlertDialog.Builder(holder.itemView.context)
                    builder.setItems(
                        options,
                        DialogInterface.OnClickListener { dialogInterface, i ->
                            if (i == 0) {
                                deleteMessage(position, holder, modelChat)
                            }
                        })
                    builder.show()
                }
            }

        } else {
            holder.Tv_message.visibility = View.GONE
            holder.Iv_message.visibility = View.VISIBLE

            try {
                if (isValidContextForGlide(context)) {
                    Glide.with(context.applicationContext)
                        .load(message)
                        .placeholder(R.drawable.new_message)
                        .error(R.drawable.broken_image)
                        .into(holder.Iv_message)
                }
            } catch (e: Exception) {
            }
            if (modelChat.senderUid.equals(firebaseAuth.uid)) {
                holder.itemView.setOnClickListener {
                    val options = arrayOf<CharSequence>("Ver", "Eliminar", "Cancelar")
                    val builder: AlertDialog.Builder = AlertDialog.Builder(holder.itemView.context)
                    builder.setItems(
                        options,
                        DialogInterface.OnClickListener { dialogInterface, i ->
                            if (i == 0) {
                                imageViewer(modelChat.message)
                            } else if (i == 1) {
                                deleteMessage(position, holder, modelChat)
                            }
                        })
                    builder.show()
                }
            }

            else if (!modelChat.senderUid.equals(firebaseAuth.uid)) {
                holder.itemView.setOnClickListener {
                    val options = arrayOf<CharSequence>("Ver imagen", "Cancelar")
                    val builder: AlertDialog.Builder = AlertDialog.Builder(holder.itemView.context)
                    builder.setItems(
                        options,
                        DialogInterface.OnClickListener { dialogInterface, i ->
                            if (i == 0) {
                                imageViewer(modelChat.message)
                            }
                        })
                    builder.show()
                }
            }
        }
    }

    private fun isValidContextForGlide(context: Context?): Boolean {
        if (context == null) {
            return false
        }

        if (context is Activity) {
            val activity = context as Activity
            return !activity.isDestroyed && !activity.isFinishing
        }

        return true
    }

    private fun deleteMessage(position: Int, holder: AdapterChat.HolderChat, modelChat: ModelChat) {
        chatRoute = Constants.routeChat(modelChat.receiverUid, modelChat.senderUid)
        val ref = FirebaseDatabase.getInstance().reference.child("Chats")
        ref.child(chatRoute).child(chatArray.get(position).id_message)
            .removeValue()
            .addOnFailureListener { e ->
                Toast.makeText(holder.itemView.context, e.message, Toast.LENGTH_SHORT).show()
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

    inner class HolderChat(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var Tv_message: TextView = itemView.findViewById(R.id.Tv_message)
        var Iv_message: ImageView = itemView.findViewById(R.id.Iv_message)
        var Tv_time_message: TextView = itemView.findViewById(R.id.Tv_time_message)
    }
}