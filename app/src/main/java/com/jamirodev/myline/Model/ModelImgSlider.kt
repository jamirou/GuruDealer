package com.jamirodev.myline.Model

class ModelImgSlider {

    var id : String = ""
    var imageUrl : String = ""
    var email: String = ""


    constructor()
    constructor(id: String, imageUrl: String, email: String) {
        this.id = id
        this.imageUrl = imageUrl
        this.email = email

    }

}