package com.jamirodev.myline

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.text.format.DateFormat
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.Arrays
import java.util.Calendar
import java.util.Locale

object Constants {

    const val MESSAGE_TYPE_TEXT = "TEXTO"
    const val MESSAGE_TYPE_IMAGE = "IMAGEN"

    const val announcement_available = "Disponible"
    const val announcement_sold = "Vendido"

    const val NOTIFICATION_NEW_MESSAGE = "NOTIFICATION_NEW_MESSAGE"
    const val FCM_SERVER_KEY =
        "YOUR_API_KEY"

    val category = arrayOf(
        "Todo",
        "Para fumar",
        "Cultivo",
        "Alimentos",
        "Ropa",
        "Educación",
        "Guardar",
        "Otros",
    )
    val categoryIcon = arrayOf(
        R.drawable.appiconremoved1,
        R.drawable.guru2,
        R.drawable.guru3,
        R.drawable.guru4,
        R.drawable.guru5,
        R.drawable.guru6,
        R.drawable.guru7,
        R.drawable.guru8,
    )

    val conditions = arrayOf(
        "Nuevo",
        "Usado",
        "Renovado"
    )
    fun getTimeDis(): Long {
        return System.currentTimeMillis()
    }
    fun getDate(time: Long): String {
        val calendar = Calendar.getInstance(Locale.ENGLISH)
        calendar.timeInMillis = time

        return DateFormat.format("dd/MM/yyyy", calendar).toString()
    }

    fun getDateHour(time: Long): String {
        val calendar = Calendar.getInstance(Locale.ENGLISH)
        calendar.timeInMillis = time
        return DateFormat.format("dd/MM/yyyy hh:mm:a", calendar).toString()
    }

    fun addAdFavorite(context: Context, idAnnounce: String) {
        val firebaseAuth = FirebaseAuth.getInstance()
        val time = Constants.getTimeDis()

        val hashMap = HashMap<String, Any>()
        hashMap["idAnnounce"] = idAnnounce
        hashMap["time"] = time

        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(firebaseAuth.uid!!).child("Favorites").child(idAnnounce)
            .setValue(hashMap)
            .addOnSuccessListener {

            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    fun deleteFavorite(context: Context, idAnnounce: String) {
        val firebaseAuth = FirebaseAuth.getInstance()

        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(firebaseAuth.uid!!).child("Favorites").child(idAnnounce)
            .removeValue()
            .addOnSuccessListener {
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    fun mapIntent(context: Context, latitud: Double, longitud: Double) {
        val googleMapIntentUri =
            Uri.parse("https://maps.google.com/maps?daddr=${latitud},${longitud}")

        val mapIntent = Intent(Intent.ACTION_VIEW, googleMapIntentUri)
        mapIntent.setPackage("com.google.android.apps.maps")
        if (mapIntent.resolveActivity(context.packageManager) != null) {
            context.startActivity(mapIntent)
        } else {
            Toast.makeText(
                context,
                "Debes instalar google Maps para realizar esta acción",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    fun callIntent(context: Context, tef: String) {
        val intent = Intent(Intent.ACTION_CALL)
        intent.setData(Uri.parse("tel:$tef"))
        context.startActivity(intent)
    }

    fun routeChat(receiverUid: String, senderUid: String): String {
        val arrayUid = arrayOf(receiverUid, senderUid)
        Arrays.sort(arrayUid)
        return "${arrayUid[0]}_${arrayUid[1]}"
    }

    fun increaseViews(idAnnounce: String) {
        val ref = FirebaseDatabase.getInstance().getReference("Announcements")
        ref.child(idAnnounce)
            .addListenerForSingleValueEvent(object: ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    var actualViews = "${snapshot.child("counterViews").value}"
                    if (actualViews == "" || actualViews == "null") {
                        actualViews = "0"
                    }
                    val newView = actualViews.toLong() + 1

                    val hashMap = HashMap<String, Any>()
                    hashMap["counterViews"] = newView

                    val dbRef = FirebaseDatabase.getInstance().getReference("Announcements")
                    dbRef.child(idAnnounce)
                        .updateChildren(hashMap)
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })
    }

}