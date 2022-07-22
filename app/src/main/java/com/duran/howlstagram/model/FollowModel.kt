package com.duran.howlstagram.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class FollowModel(
    var followerCount: Int = 0, // 팔로우 수
    var followers: MutableMap<String, String> = hashMapOf(), // 중복 팔로우 방지
    var followingCount: Int = 0, // 팔로잉 수
    var followings: MutableMap<String, String> = hashMapOf() // 중복 팔로잉 방지
) : Parcelable