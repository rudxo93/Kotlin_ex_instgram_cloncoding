package com.duran.howlstagram.model

data class AlarmModel (
    var destinationUid: String? = null,
    var userId: String? = null,
    var uid: String? = null,
    var kind: Int? = null, // 0: like alarm , 1: comment alarm, 2: follow alarm
    var message: String? = null,
    var timestamp: Long? = null
)