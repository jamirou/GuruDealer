package com.jamirodev.myline.Model

class ModelChat {
    var id_message: String = ""
    var typeMessage: String = ""
    var message: String = ""
    var senderUid: String = ""
    var receiverUid: String = ""
    var time : Long = 0

    constructor()
    constructor(
        id_message: String,
        typeMessage: String,
        message: String,
        senderUid: String,
        receiverUid: String,
        time: Long
    ) {
        this.id_message = id_message
        this.typeMessage = typeMessage
        this.message = message
        this.senderUid = senderUid
        this.receiverUid = receiverUid
        this.time = time
    }

}