package com.duran.howlstagram.navigation

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.duran.howlstagram.LoginActivity
import com.duran.howlstagram.MainActivity
import com.duran.howlstagram.R
import com.duran.howlstagram.navigation.model.ContentDTO
import com.duran.howlstagram.navigation.model.FollowDTO
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class UserFragment: Fragment() {

    lateinit var fragmentView: View
    lateinit var firestore: FirebaseFirestore
    lateinit var uid: String
    lateinit var auth: FirebaseAuth
    lateinit var currentUserUid: String
    companion object{
        var PICK_PROFILE_FROM_ALBUM = 10
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        fragmentView = LayoutInflater.from(activity).inflate(R.layout.fragment_user, container, false)
        uid = arguments?.getString("destinationUid")!!
        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        currentUserUid = auth.currentUser!!.uid

        if(uid == currentUserUid){
            // MyPage
            fragmentView.findViewById<Button>(R.id.account_btn_follow_signout).text = getString(R.string.signout)
            fragmentView.findViewById<Button>(R.id.account_btn_follow_signout).setOnClickListener {
                activity?.finish()
                startActivity(Intent(activity, LoginActivity::class.java))
                auth.signOut()
            }
        } else {
            // OtherUserPage
            fragmentView.findViewById<Button>(R.id.account_btn_follow_signout).text = getString(R.string.follow)
            val mainactivity = (activity as MainActivity)
            mainactivity.findViewById<TextView>(R.id.toolbar_username).text = arguments?.getString("userId")
            mainactivity.findViewById<Button>(R.id.toolbar_btn_back).setOnClickListener {
                mainactivity.findViewById<BottomNavigationView>(R.id.bottom_navigation).selectedItemId = R.id.action_home
            }
            mainactivity.findViewById<ImageView>(R.id.toolbar_title_image).visibility = View.GONE
            mainactivity.findViewById<TextView>(R.id.toolbar_username).visibility = View.VISIBLE
            mainactivity.findViewById<Button>(R.id.toolbar_btn_back).visibility = View.VISIBLE
            fragmentView.findViewById<Button>(R.id.account_btn_follow_signout).setOnClickListener {
                requestFollow()
            }
        }
        fragmentView.findViewById<RecyclerView>(R.id.account_recycleriew).adapter = UserFragmentRecyclerViewAdapter()
        fragmentView.findViewById<RecyclerView>(R.id.account_recycleriew).layoutManager = GridLayoutManager(activity, 3)

        fragmentView.findViewById<ImageView>(R.id.account_iv_profile).setOnClickListener {
            val photoPickerIntent = Intent(Intent.ACTION_PICK)
            photoPickerIntent.type = "image/*"
            activity?.startActivityForResult(photoPickerIntent, PICK_PROFILE_FROM_ALBUM)
        }

        getProfileImage()
        getFollowerAndFollowing()
        return fragmentView
    }

    @SuppressLint("UseRequireInsteadOfGet")
    fun getFollowerAndFollowing(){
        firestore.collection("users").document(uid).addSnapshotListener { documentSnapshot, firebaseFirestoreException ->
            if(documentSnapshot == null) return@addSnapshotListener
            var followDTO = documentSnapshot.toObject(FollowDTO::class.java)
            if(followDTO?.followingCount != null){
                fragmentView.findViewById<TextView>(R.id.account_tv_following_count).text = followDTO.followingCount.toString()
            }
            if(followDTO?.followerCount != null) {
                fragmentView.findViewById<TextView>(R.id.account_tv_follower_count).text = followDTO.followerCount.toString()
                if(followDTO.followers.containsKey(currentUserUid)){
                    fragmentView.findViewById<Button>(R.id.account_btn_follow_signout).text = getString(R.string.follow_cancel)
                    fragmentView.findViewById<Button>(R.id.account_btn_follow_signout).background.setColorFilter(ContextCompat.getColor(activity!!, R.color.colorLightGray), PorterDuff.Mode.MULTIPLY)
                } else {
                    if(uid != currentUserUid){
                        fragmentView.findViewById<Button>(R.id.account_btn_follow_signout).text = getString(R.string.follow)
                        fragmentView.findViewById<Button>(R.id.account_btn_follow_signout).background.colorFilter = null
                    }
                }
            }
        }
    }

    fun requestFollow(){
        // Save data to my account
        var tsDocFollowing = firestore.collection("users").document(currentUserUid)
        firestore.runTransaction { transaction ->
            var followDTO = transaction.get(tsDocFollowing).toObject(FollowDTO::class.java)
            if(followDTO == null){
                followDTO = FollowDTO()
                followDTO!!.followerCount = 1
                followDTO!!.followers[uid] = true

                transaction.set(tsDocFollowing, followDTO) // 데이터가 DB에 담기게 된다.
                return@runTransaction
            }

            if(followDTO.followings.containsKey(uid)){
                // It remove following third person when a third person follow me
                followDTO.followingCount = followDTO.followingCount - 1
                followDTO.followers.remove(uid)
            } else {
                // It add following third person when a third person do not follow me
                followDTO.followingCount = followDTO.followingCount + 1
                followDTO.followers[uid] = true
            }
            transaction.set(tsDocFollowing, followDTO)
            return@runTransaction
        }

        // Save data to third person
        var tsDocFollower = firestore.collection("users").document(uid)
        firestore.runTransaction { transaction ->
            var followDTO = transaction.get(tsDocFollower).toObject(FollowDTO::class.java)
            if(followDTO == null) {
                followDTO = FollowDTO()
                followDTO!!.followerCount = 1
                followDTO!!.followers[currentUserUid] = true

                transaction.set(tsDocFollower, followDTO!!)
                return@runTransaction
            }

            if(followDTO!!.followers.containsKey(currentUserUid)) {
                // It cancel my follower when I follow a third person
                followDTO!!.followerCount = followDTO!!.followerCount - 1
                followDTO!!.followers.remove(currentUserUid)
            } else {
                // It add my follower when I don't follow a third person
                followDTO!!.followerCount = followDTO!!.followerCount + 1
                followDTO!!.followers[currentUserUid] = true
            }
            transaction.set(tsDocFollower, followDTO!!)
            return@runTransaction
        }
    }

    @SuppressLint("UseRequireInsteadOfGet")
    fun getProfileImage(){
        firestore.collection("profileImages").document(uid).addSnapshotListener { documentSnapshot, firebaseFirestoreException ->
            if(documentSnapshot == null) return@addSnapshotListener
            if(documentSnapshot.data != null) {
                var url = documentSnapshot.data!!["image"]
                Glide.with(activity!!).load(url).apply(RequestOptions().circleCrop()).into(fragmentView.findViewById(R.id.account_iv_profile))
            }

        }
    }

    inner class UserFragmentRecyclerViewAdapter: RecyclerView.Adapter<RecyclerView.ViewHolder>(){

        val contentDTOs: ArrayList<ContentDTO> = arrayListOf()
        init {
            firestore.collection("images").whereEqualTo("uid", uid).addSnapshotListener { querySnapshot, FirebaseFirestoreException ->
                // Sometimes, This code return null of querySnapshot when it signout
                if(querySnapshot == null) return@addSnapshotListener

                // Get data
                for(snapshot in querySnapshot.documents) {
                    contentDTOs.add(snapshot.toObject(ContentDTO::class.java)!!)
                }
                fragmentView.findViewById<TextView>(R.id.account_tv_post_count).text = contentDTOs.size.toString()
                notifyDataSetChanged()
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val width = resources.displayMetrics.widthPixels / 3
            val imageview = ImageView(parent.context)
            imageview.layoutParams = LinearLayoutCompat.LayoutParams(width, width)
            return CustomViewHolder(imageview)
        }

        inner class CustomViewHolder(var imageview: ImageView) : RecyclerView.ViewHolder(imageview) {

        }

        override fun getItemCount(): Int {
            return contentDTOs.size
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            var imageview = (holder as CustomViewHolder).imageview
            Glide.with(holder.itemView.context).load(contentDTOs[position].imageUrl).apply(RequestOptions().centerCrop()).into(imageview)
        }
    }
}