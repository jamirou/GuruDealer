package com.jamirodev.myline.adapters

import android.content.Context
import android.content.Intent
import android.graphics.Color
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
import com.jamirodev.myline.DetailAnnounce.DetailAnnonce
import com.jamirodev.myline.Filter.FilterAd
import com.jamirodev.myline.Model.ModelAnnounce
import com.jamirodev.myline.R
import com.jamirodev.myline.databinding.ItemAnnouncesNewVersionBinding

class AdapterAnnounce : RecyclerView.Adapter<AdapterAnnounce.HolderAnnounce>, Filterable {

    private lateinit var binding: ItemAnnouncesNewVersionBinding
    private var context: Context
    var announceArrayList: ArrayList<ModelAnnounce>
    private var firebaseAuth: FirebaseAuth
    private var filterList : ArrayList<ModelAnnounce>
    private var filter : FilterAd ?= null

    constructor(context: Context, announceArrayList: ArrayList<ModelAnnounce>) {
        this.context = context
        this.announceArrayList = announceArrayList
        firebaseAuth = FirebaseAuth.getInstance()
        this.filterList = announceArrayList
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderAnnounce {
        binding = ItemAnnouncesNewVersionBinding.inflate(LayoutInflater.from(context), parent, false)
        return HolderAnnounce(binding.root)
    }

    override fun getItemCount(): Int {
        return announceArrayList.size
    }

    override fun onBindViewHolder(holder: HolderAnnounce, position: Int) {
        val modelAnnounce = announceArrayList[position]

        val title = modelAnnounce.title
        val description = modelAnnounce.description
        val direction = modelAnnounce.direction
        val condition = modelAnnounce.condition
        val price = modelAnnounce.price
        val time = modelAnnounce.time

        holder.itemView.setOnClickListener {
            val intent = Intent(context, DetailAnnonce::class.java)
            intent.putExtra("idAnnounce", modelAnnounce.id)
            context.startActivity(intent)
        }

        val formatDate = Constants.getDate(time)

        loadFirstImageAnnounce(modelAnnounce, holder)

        checkFavorite(modelAnnounce, holder)

        holder.Tv_title.text = title
        holder.Tv_description.text = description
        holder.Tv_direction.text = direction
        holder.Tv_condition.text = condition
        holder.Tv_price.text = price
        holder.Tv_date.text = formatDate

        holder.Ib_fav.setOnClickListener {
            val favorite = modelAnnounce.favorite
            if (favorite) {
                Constants.deleteFavorite(context, modelAnnounce.id)
            } else {
                Constants.addAdFavorite(context, modelAnnounce.id)
            }
        }
        if (condition.equals("Nuevo")) {
            holder.Tv_condition.setTextColor(Color.parseColor("#F8F6F4"))
        } else if (condition.equals("Usado")) {
            holder.Tv_condition.setTextColor(Color.parseColor("#F8F6F4"))
        } else if (condition.equals("Renovado")) {
            holder.Tv_condition.setTextColor(Color.parseColor("#F8F6F4"))
        }

    }

    private fun checkFavorite(modelAnnounce: ModelAnnounce, holder: AdapterAnnounce.HolderAnnounce) {
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(firebaseAuth.uid!!).child("Favorites").child(modelAnnounce.id)
            .addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    val favorite = snapshot.exists()
                    modelAnnounce.favorite = favorite
                    if (favorite) {
                        holder.Ib_fav.setImageResource(R.drawable.ic_favorite)
                    } else {
                        holder.Ib_fav.setImageResource(R.drawable.ic_no_fav)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })
    }

    private fun loadFirstImageAnnounce(
        modelAnnounce: ModelAnnounce,
        holder: AdapterAnnounce.HolderAnnounce
    ) {
        val idAnnounce = modelAnnounce.id

        val ref = FirebaseDatabase.getInstance().getReference("Announcements")
        ref.child(idAnnounce).child("Images").limitToFirst(1)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (ds in snapshot.children) {
                        val imageUrl = "${ds.child("imageUrl").value}"
                        try {
                            Glide.with(context)
                                .load(imageUrl)
                                .placeholder(R.drawable.ic_imagen)
                                .into(holder.imageIV)
                        } catch (e: Exception) {

                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })
    }

    inner class HolderAnnounce(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var imageIV = binding.imageIV
        var Tv_title = binding.TvTitle
        var Tv_description = binding.TvDescription
        var Tv_direction = binding.TvDirection
        var Tv_condition = binding.TvCondition
        var Tv_price = binding.TvPrice
        var Tv_date = binding.TvDate
        var Ib_fav = binding.IbFav
    }

    override fun getFilter(): Filter {
        if (filter == null) {
            filter = FilterAd(this, filterList)
        }
        return filter as FilterAd
    }


}