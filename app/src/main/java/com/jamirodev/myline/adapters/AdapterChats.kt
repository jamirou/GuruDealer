package com.jamirodev.myline.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.jamirodev.myline.Constants
import com.jamirodev.myline.Model.ModelChats
import com.jamirodev.myline.R
import com.jamirodev.myline.chat.ChatActivity
import com.jamirodev.myline.chat.SearchChat
import com.jamirodev.myline.databinding.ItemChatsBinding

class AdapterChats : RecyclerView.Adapter<AdapterChats.HolderChats>, Filterable {
    private var context: Context
    var chatsArrayList: ArrayList<ModelChats>
    private lateinit var binding: ItemChatsBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private var myUid = ""
    private var filterList: ArrayList<ModelChats>
    private var filter: SearchChat? = null

    constructor(context: Context, chatsArrayList: ArrayList<ModelChats>) {
        this.context = context
        this.chatsArrayList = chatsArrayList
        this.filterList = chatsArrayList
        firebaseAuth = FirebaseAuth.getInstance()
        myUid = firebaseAuth.uid!!
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderChats {
        binding = ItemChatsBinding.inflate(LayoutInflater.from(context), parent, false)
        return HolderChats(binding.root)
    }

    override fun getItemCount(): Int {
        return chatsArrayList.size
    }

    override fun onBindViewHolder(holder: HolderChats, position: Int) {
        val modelChats = chatsArrayList[position]

        loadLastMessage(modelChats, holder)

        holder.itemView.setOnClickListener {
            val uidReceived = modelChats.uidReceived
            if (uidReceived != null) {
                val intent = Intent(context, ChatActivity::class.java)
                intent.putExtra("uidSeller", uidReceived)
                context.startActivity(intent)

            }
        }
    }

    private fun loadLastMessage(modelChats: ModelChats, holder: AdapterChats.HolderChats) {
        val chatKey = modelChats.keyChat
        val ref = FirebaseDatabase.getInstance().getReference("Chats")
        ref.child(chatKey).limitToLast(1)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (ds in snapshot.children) {
                        val senderUid = "${ds.child("senderUid").value}"
                        val idMessage = "${ds.child("idMessage").value}"
                        val message = "${ds.child("message").value}"
                        val receiverUid = "${ds.child("receiverUid").value}"
                        val time = ds.child("time").value as Long
                        val typeMessage = "${ds.child("typeMessage").value}"

                        val format_date_hour = Constants.getDateHour(time)

                        modelChats.senderUid = senderUid
                        modelChats.idMessage = idMessage
                        modelChats.message = message
                        modelChats.receiverUid = receiverUid
                        modelChats.typeMessage = typeMessage

                        holder.Tv_date.text = "$format_date_hour"

                        if (typeMessage == Constants.MESSAGE_TYPE_TEXT) {
                            holder.Tv_last_message.text = message
                        } else {
                            holder.Tv_last_message.text = "Imagen"
                        }

                        loadInfoUserReceived(modelChats, holder)

                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })
    }

    private fun loadInfoUserReceived(modelChats: ModelChats, holder: AdapterChats.HolderChats) {
        val senderUid = modelChats.senderUid
        val receiverUid = modelChats.receiverUid

        var uidReceived = ""
        if (senderUid == myUid) {
            uidReceived = receiverUid
        } else {
            uidReceived = senderUid
        }
        modelChats.uidReceived = uidReceived
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(uidReceived)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val names = "${snapshot.child("names").value}"
                    val image = "${snapshot.child("urlProfileImage").value}"

                    modelChats.names = names
                    modelChats.urlProfileImage = image

                    holder.Tv_names.text = names

                    try {
                        Glide.with(context)
                            .load(image)
                            .placeholder(R.drawable.usuario)
                            .into(holder.Iv_profile)
                    } catch (e: Exception) {
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })
    }

    inner class HolderChats(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var Iv_profile = binding.IvProfile
        var Tv_names = binding.TvNames
        var Tv_last_message = binding.TvLastMessage
        var Tv_date = binding.TvDate
    }

    override fun getFilter(): Filter {
        if (filter == null) {
            filter = SearchChat(this, filterList)
        }
        return filter!!
    }

}