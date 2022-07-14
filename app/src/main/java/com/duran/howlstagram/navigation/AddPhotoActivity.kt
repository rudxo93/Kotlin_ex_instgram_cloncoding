package com.duran.howlstagram.navigation

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import com.duran.howlstagram.R
import com.duran.howlstagram.navigation.model.ContentDTO
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import java.text.SimpleDateFormat
import java.util.*

class AddPhotoActivity : AppCompatActivity() {

    private val addPhotoUploadButton: Button by lazy {
        findViewById(R.id.addphoto_btn_upload)
    }
    private val addPhotoImage: ImageView by lazy {
        findViewById(R.id.addphoto_image)
    }
    private val addPhotoEtExplain: EditText by lazy {
        findViewById(R.id.addphoto_edit_explain)
    }

    private var PICK_IMAGE_FROM_ALBUM = 0
    lateinit var storage: FirebaseStorage
    lateinit var photoUri: Uri
    lateinit var auth: FirebaseAuth
    lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_photo)

        // Initiate
        storage = FirebaseStorage.getInstance()
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // Open the album
        val photoPickerIntent = Intent(Intent.ACTION_PICK)
        photoPickerIntent.type = "image/*"
        startActivityForResult(photoPickerIntent, PICK_IMAGE_FROM_ALBUM)

        // add image upload event
        addPhotoUploadButton.setOnClickListener {
            contentUpload()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == PICK_IMAGE_FROM_ALBUM){
            if(resultCode == Activity.RESULT_OK){
                // This is path to the selected image
                photoUri = data?.data!!
                addPhotoImage.setImageURI(photoUri)
            } else {
                // Exit the addPhotoActivity if you leave the album with selecting it
                finish()
            }
        }
    }

    @SuppressLint("SimpleDateFormat")
    fun contentUpload() {
        // Make filename
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val imageFileName = "IMAGE_" + timestamp + "_.png"

        val storageRef = storage.reference.child("images").child(imageFileName)

        // Promise method
        storageRef.putFile(photoUri).continueWithTask { task: Task<UploadTask.TaskSnapshot> ->
            return@continueWithTask storageRef.downloadUrl
        }.addOnSuccessListener { uri ->
            var contentDTO = ContentDTO()

            // Insert downloadUrl of image
            contentDTO.imageUrl = uri.toString()

            // Insert uid of user
            contentDTO.uid = auth.currentUser?.uid

            // Insert userId
            contentDTO.userId = auth.currentUser?.email

            // Insert explain of content
            contentDTO.explain = addPhotoEtExplain.text.toString()

            // Insert timestamp
            contentDTO.timestamp = System.currentTimeMillis()

            firestore.collection("image").document().set(contentDTO)

            setResult(Activity.RESULT_OK)

            finish()
        }

        // Callback method
        /*storageRef?.putFile(photoUri!!)?.addOnSuccessListener {
            storageRef.downloadUrl.addOnSuccessListener { uri ->
                var contentDTO = ContentDTO()

                // Insert downloadUrl of image
                contentDTO.imageUrl = uri.toString()

                // Insert uid of user
                contentDTO.uid = auth?.currentUser?.uid

                // Insert userId
                contentDTO.userId = auth?.currentUser?.email

                // Insert explain of content
                contentDTO.explain = addPhotoEtExplain.text.toString()

                // Insert timestamp
                contentDTO.timestamp = System.currentTimeMillis()

                firestore?.collection("image")?.document()?.set(contentDTO)

                setResult(Activity.RESULT_OK)

                finish()
            }
        }*/
    }
}