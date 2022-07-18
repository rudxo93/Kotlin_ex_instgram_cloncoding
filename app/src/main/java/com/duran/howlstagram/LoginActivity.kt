package com.duran.howlstagram

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.duran.howlstagram.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class LoginActivity : AppCompatActivity() {

    lateinit var binding: ActivityLoginBinding
    lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_login)

        auth = FirebaseAuth.getInstance()

        binding.emailLoginButton.setOnClickListener {
            signinAndsignup()
        }
    }

    private fun signinAndsignup() {
        val id = binding.edittextId.text.toString()
        val password = binding.edittextPassword.text.toString()

        auth.createUserWithEmailAndPassword(id, password).addOnCompleteListener {
            task ->
            if(task.isSuccessful){
                // 로그인
                moveMain(task.result?.user)
            } else {
                // 에러 메세지
                Toast.makeText(this, task.exception?.message, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun moveMain(user: FirebaseUser?) {
        if(user != null){
            startActivity(Intent(this, MainActivity::class.java))
        }
    }
}