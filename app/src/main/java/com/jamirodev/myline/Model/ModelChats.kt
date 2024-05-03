package com.jamirodev.myline.Model

class ModelChats {

    var urlProfileImage: String = "" //Receiver image
    var names: String = "" //Receiver name
    var keyChat: String = ""
    var uidReceived: String = ""
    var idMessage: String = ""
    var typeMessage: String = ""
    var message: String = ""
    var senderUid: String = "" //Sender Uid
    var receiverUid: String = "" //Receiver Uid
    var time: Long = 0

    constructor()
    constructor(
        urlProfileImage: String,
        names: String,
        keyChat: String,
        uidReceived: String,
        idMessage: String,
        typeMessage: String,
        message: String,
        senderUid: String,
        receiverUid: String,
        time: Long
    ) {
        this.urlProfileImage = urlProfileImage
        this.names = names
        this.keyChat = keyChat
        this.uidReceived = uidReceived
        this.idMessage = idMessage
        this.typeMessage = typeMessage
        this.message = message
        this.senderUid = senderUid
        this.receiverUid = receiverUid
        this.time = time
    }

}