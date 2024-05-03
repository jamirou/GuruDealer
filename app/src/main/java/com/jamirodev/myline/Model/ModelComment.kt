package com.jamirodev.myline.Model

class ModelComment {

    var id = ""
    var time = ""
    var uid = ""
    var uid_seller = ""
    var comment = ""

    constructor()
    constructor(id: String, time: String, uid: String, uid_seller: String, comment: String) {
        this.id = id
        this.time = time
        this.uid = uid
        this.uid_seller = uid_seller
        this.comment = comment
    }

}