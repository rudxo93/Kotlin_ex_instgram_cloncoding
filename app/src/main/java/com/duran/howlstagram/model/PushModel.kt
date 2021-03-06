package com.duran.howlstagram.model

data class PushModel (
    var to: String? = null,
    var notification: Notification = Notification()
){
    data class Notification(
        var body: String? = null,
        var title: String? = null
    )
}