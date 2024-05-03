package com.jamirodev.myline.Model

class ModelUser {
    private var uid: String = ""
    private var email: String = ""
    private var urlProfileImage: String = ""
    private var names: String = ""


    constructor()

    constructor(
        uid: String,
        email: String,
        urlProfileImage: String,
        names: String,
    ) {
        this.uid = uid
        this.email = email
        this.urlProfileImage = urlProfileImage
        this.names = names
    }

    //getters y setters
    fun getUid(): String? {
        return uid
    }

    fun setUid(uid: String) {
        this.uid = uid
    }


    fun getEmail(): String? {
        return email
    }

    fun setEmail(email: String) {
        this.email = email
    }

    fun getUrlProfileImage(): String? {
        return urlProfileImage
    }

    fun setUrlProfileImage(urlProfileImage: String) {
        this.urlProfileImage = urlProfileImage
    }

    fun getNames(): String? {
        return names
    }

    fun setNames(nombres: String) {
        this.names = nombres
    }

}