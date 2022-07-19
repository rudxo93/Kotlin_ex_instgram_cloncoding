package com.duran.howlstagram

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.databinding.DataBindingUtil
import com.duran.howlstagram.databinding.ActivityAddPhotoBinding
import com.duran.howlstagram.model.ContentModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.text.SimpleDateFormat
import java.util.*

class AddPhotoActivity : AppCompatActivity() {

    lateinit var binding: ActivityAddPhotoBinding
    lateinit var auth: FirebaseAuth
    lateinit var storage: FirebaseStorage
    lateinit var firestore: FirebaseFirestore
    lateinit var photoUri: Uri

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_add_photo)
        auth = FirebaseAuth.getInstance()
        storage = FirebaseStorage.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // 엑티비티를 실행하자마자 화면이 열릴수 있도록 open code
        var intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        photoResult.launch(intent)

        // addPhoto버튼 클릭 시
        binding.addphotoUploadBtn.setOnClickListener {
            contentUpload()
        }
    }

    fun contentUpload() {
        // 이미지 저장 날싸와 시간(파일명이 중복되지 않도록 날짜와 시간으로)
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        // 파일 저장 이름 만들기 -> IMAGE_저장날짜.png
        val imageFileName = "IMAGE_" + timeStamp + ".png"

        // Firebase Storage에 images라는 폴더를 만들어서 사진을 업로드한다.
        var storagePath = storage.reference.child("images").child(imageFileName)

        // 파일 업로드
        // Promise method
        storagePath.putFile(photoUri).continueWithTask {
            return@continueWithTask storagePath.downloadUrl
        }.addOnCompleteListener {
            downloadUrl ->

            val contentModel = ContentModel()

            // 다운로드 url
            contentModel.imageUrl = downloadUrl.result.toString()
            // content의 내용
            contentModel.explain = binding.addphotoEditEdittext.text.toString()
            // user의 uid
            contentModel.uid = auth.uid
            // user의 id
            contentModel.userId = auth.currentUser?.email
            // 시간
            contentModel.timestamp = System.currentTimeMillis()

            // contentModel을 images 컬렉션 안에 데이터를 넣어준다.
            firestore.collection("images").document().set(contentModel)

            // 업로드 성공 메세지
            Toast.makeText(this, "업로드에 성공했습니다.", Toast.LENGTH_SHORT).show()

            // 업로드가 완료되었으면 창을 닫아준다.
            finish()
        }

        /*// Callback method
        storagePath.putFile(photoUri).addOnSuccessListener {
            storagePath.downloadUrl.addOnSuccessListener { uri ->
                val contentModel = ContentModel()

                contentModel.imageUrl = uri.toString()
                contentModel.uid = auth.currentUser?.uid
                contentModel.userId = auth.currentUser?.email
                contentModel.explain = binding.addphotoEditEdittext.text.toString()
                contentModel.timestamp = System.currentTimeMillis()

                firestore.collection("image").document().set(contentModel)

                setResult(Activity.RESULT_OK)

                finish()
            }
        }*/
    }

    // 선택한 이미지를 받는 부분
    var photoResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        result ->
        // 사진 받는 부분
        photoUri = result.data?.data!! // 이미지의 경로가 넘어온다. photoUri에 경로를 담아준다.
        binding.uploadImageview.setImageURI(photoUri) // imageview에 선택한 이미지를 넣어준다.
    }
}