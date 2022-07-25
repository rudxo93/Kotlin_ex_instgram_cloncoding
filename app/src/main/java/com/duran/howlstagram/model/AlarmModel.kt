package com.duran.howlstagram.model

data class AlarmModel (
    var destinationUid: String? = null, // 상대방 uid
    var userId: String? = null, // 나의 id
    var uid: String? = null, // 나의 uid
    var kind: Int? = null, // 0: 좋아요, 1: 코멘트, 2: 팔로우
    var message: String? = null, // 코멘트 내용
    var timestamp: Long? = null // 이벤트 발생시간
)