package com.jamirodev.myline.Model

class ModelAnnounce {

    var id: String = ""
    var uid: String = ""
    var marca: String = ""
    var category: String = ""
    var condition: String = ""
    var direction: String = ""
    var price: String = ""
    var title: String = ""
    var description: String = ""
    var status: String = ""
    var time: Long = 0
    var latitud = 0.0
    var longitud = 0.0
    var favorite = false
    var counterViews = 0

    constructor()
    constructor(
        id: String,
        uid: String,
        marca: String,
        category: String,
        condition: String,
        direction: String,
        price: String,
        title: String,
        description: String,
        status: String,
        time: Long,
        latitud: Double,
        longitud: Double,
        favorite: Boolean,
        counterViews: Int
    ) {
        this.id = id
        this.uid = uid
        this.marca = marca
        this.category = category
        this.condition = condition
        this.direction = direction
        this.price = price
        this.title = title
        this.description = description
        this.status = status
        this.time = time
        this.latitud = latitud
        this.longitud = longitud
        this.favorite = favorite
        this.counterViews = counterViews
    }


}