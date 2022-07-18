package com.duran.howlstagram.model

data class FollowModel(
    var followerCount: Int = 0, // 팔로우 수
    var followers: MutableMap<String, Boolean> = HashMap(), // 중복 팔로우 방지
    var followingCount: Int = 0, // 팔로잉 수
    var followings: MutableMap<String, Boolean> = HashMap() // 중복 팔로잉 방지
)