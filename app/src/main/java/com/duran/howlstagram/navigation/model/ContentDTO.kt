package com.duran.howlstagram.navigation.model

data class ContentDTO(var explain: String? = null, // 컨텐츠의 설명을 관리
                      var imageUrl: String? = null, // 이미지 주소 관리
                      var uid: String? = null, // 어느 유저가 올렸는지 관리
                      var userId: String? = null, // 올린 유저의 이미지를 관리해주는 유저id
                      var timestamp: Long? = null, // 컨텐츠 올린 날짜
                      var favoriteCount: Int = 0, // 좋아요를 몇개 눌렀는지 관리
                      var favorites: MutableMap<String, Boolean> = HashMap()){ // 중복 좋아요를 방지하기 위해 좋아요를 누른 유저를 관리
    // 댓글을 관리해주는 데이터 클래스
    data class comment(var uid: String? = null, // uid관리
                       var userId: String? = null, // 이메일을 관리
                       var comment: String? = null, // 댓글 관리
                       var timestamp: Long? = null) // 댓글 시간 관리
}