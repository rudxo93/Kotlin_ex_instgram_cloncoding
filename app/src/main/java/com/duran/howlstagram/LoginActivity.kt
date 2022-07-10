package com.duran.howlstagram

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import com.duran.howlstagram.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    private val btn_email_login: AppCompatButton by lazy {
        findViewById<AppCompatButton>(R.id.email_login_button)
    }
    private val et_email: EditText by lazy {
        findViewById<EditText>(R.id.email_edittext)
    }
    private val et_password: EditText by lazy {
        findViewById<EditText>(R.id.password_edittext)
    }

    var auth: FirebaseAuth? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()

        btn_email_login.setOnClickListener {
            signinAndsignup()
        }
    }

    fun signinAndsignup(){
        auth?.createUserWithEmailAndPassword(et_email.text.toString(), et_password.text.toString())
            ?.addOnCompleteListener {
                task ->
                    if(task.isSuccessful){
                        // Creating a user account
                        moveMainPage(task.result.user)
                    } else if(task.exception?.message.isNullOrEmpty()){
                        // Show the error message
                        Toast.makeText(this, task.exception?.message, Toast.LENGTH_LONG).show()
                    } else {
                        // Login if you have account
                        signinEmail()
                    }
            }
    }

    fun signinEmail(){
        auth?.signInWithEmailAndPassword(et_email.text.toString(), et_password.text.toString())
            ?.addOnCompleteListener {
                    task ->
                if(task.isSuccessful){
                    // Login
                    moveMainPage(task.result.user)
                } else {
                    // Show the error message
                    Toast.makeText(this, task.exception?.message, Toast.LENGTH_LONG).show()
                }
            }
    }

    fun moveMainPage(user: FirebaseUser?){
        if(user != null){
            startActivity(Intent(this, MainActivity::class.java))
        }
    }
}