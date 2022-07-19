package com.duran.howlstagram

import android.annotation.SuppressLint
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.duran.howlstagram.databinding.ActivityLoginBinding
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.*

class LoginActivity : AppCompatActivity() {

    lateinit var binding: ActivityLoginBinding
    // 계정 인증
    lateinit var auth: FirebaseAuth
    //
    lateinit var googleSignInClient: GoogleSignInClient
    //
    lateinit var callbackManager: CallbackManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_login)

        // auth를 쓰기전에싱글톤 패턴으로 인스턴스를 받아와야한다.
        auth = FirebaseAuth.getInstance()

        // 이메일 로그인 버튼 클릭 시
        binding.emailLoginButton.setOnClickListener {
            // 계정 생성 함수
            signinAndsignup()
        }

        // 구글 로그인 버튼 클릭 시
        binding.googleLoginButton.setOnClickListener {
            // 구글 로그인
            googleLogin()
        }

        callbackManager = CallbackManager.Factory.create()
        // 페이스북 로그인 버튼 클릭 시
        binding.facebookLoginButton.setOnClickListener {
            facebookLogin()
        }

        // 구글 로그인 클라이언트 설정 및 생성
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id)) // 구글API키 -> ID Token을 가져와야 한다.
            .requestEmail()
            .build()

        // 옵션값을 구글로그인 클라이언트에 세팅
        googleSignInClient = GoogleSignIn.getClient(this, gso)
        // 페이스북 로그인에 필요한 hashkey값 구하기
        //printHashKey(this)
    }

    // 페이스북 로그인
    private fun facebookLogin() {
        val loginManager = LoginManager.getInstance()
        // 로그인 요청
        loginManager.logInWithReadPermissions(this, Arrays.asList("public_profile", "email"))
        loginManager.registerCallback(callbackManager, object : FacebookCallback<LoginResult>{
            override fun onSuccess(result: LoginResult?) {
                val token = result?.accessToken
                firebaseAuthWithFacebook(token)
            }

            override fun onCancel() {

            }

            override fun onError(error: FacebookException?) {

            }
        })
    }

    //Firebase에 사용자 인증 정보를 넘겨주는 부분(페이스북 계정을 저장)
    fun firebaseAuthWithFacebook(idToken: AccessToken?) {
        val credential = FacebookAuthProvider.getCredential(idToken!!.token)
        auth.signInWithCredential(credential).addOnCompleteListener {
            task ->
            if(task.isSuccessful){
                if(auth.currentUser!!.isEmailVerified){
                    // 이메일 인증이 되었을때
                    moveMain(auth.currentUser)
                } else {
                    // 이메일 인증이 실패했을때
                }
            }
        }
    }

    // 구글 로그인
    private fun googleLogin() {
        val intent = googleSignInClient.signInIntent
        googleLoginResult.launch(intent)
    }

    // 구글 로그인 결과
    // startActivityForResult(), onActivityResult() 가 deprecated가 되고 registerForActivityResult()를 사용한다.
    // registerForActivityResult() 안에는 ActivityResultContracts.StartActivityForResult(), ActivityResultCallback<ActivityResult>() 두가지 인자가 들어있다.
    // ActivityResultContracts.StartActivityForResult()인자의 타입으로 엑티비티가 호출되고 결과값을 이전 엑티비티로 전달할 수 있는
    // 엑티비티가 호출될 것이다. -> startActivityForResult() 와 같다.
    // 그리고 onActivityResult 에서는 매개변서 3개가 있었는데 지금은 result매개변수 하나만 있다.
    var googleLoginResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        result ->
        // result.data는, 현재 A 액티비티이고 호출한 B 액티비티가 있다면 B 액티비티에서
        // setResult() 매개변수의 두번째 인자로 넣어주는 intent 값을 받아온다.
        val data = result.data // ActivityResult객체 result로 data를 받아온다.
        val task = GoogleSignIn.getSignedInAccountFromIntent(data)
        val account = task.getResult(ApiException::class.java)

        firebaseAuthWithGoogle(account.idToken)
    }

    // Firebase에 사용자 인증 정보를 넘겨주는 부분(구글 계정을 저장)
    private fun firebaseAuthWithGoogle(idToken: String?) {
        val credential = GoogleAuthProvider.getCredential(idToken,null)
        auth.signInWithCredential(credential).addOnCompleteListener {
            task->
            if(task.isSuccessful){
                if(auth.currentUser!!.isEmailVerified){
                    // 구글 로그인 인증되었을때
                    moveMain(auth.currentUser)
                } else {
                    // 구글 로그인 인증 실패시
                    Toast.makeText(this, task.exception?.message, Toast.LENGTH_SHORT).show()
                }
            }

        }
    }

    // hash key값 출력
    @SuppressLint("PackageManagerGetSignatures")
    private fun printHashKey(context: Context) {
        try {
            val info: PackageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), PackageManager.GET_SIGNATURES)
            for (signature in info.signatures) {
                val md: MessageDigest = MessageDigest.getInstance("SHA")
                md.update(signature.toByteArray())
                val hashKey: String = String(Base64.encode(md.digest(), 0))
                Log.i(TAG, "printHashKey() Hash Key: $hashKey")
            }
        }  catch (e: NoSuchAlgorithmException) {
            Log.e(TAG, "printHashKey()", e)
        } catch (e: Exception) {
            Log.e(TAG, "printHashKey()", e)
        }
    }

    // 계정 생성
    private fun signinAndsignup() {
        val id = binding.edittextId.text.toString()
        val password = binding.edittextPassword.text.toString()

        // 이메일 주소와 비밀번호를 createUserWithEmailAndPassword에 전달하여 신규 계정을 생성한다.
        auth.createUserWithEmailAndPassword(id, password).addOnCompleteListener { // addOnCompleteListener 리스너로 성공 유무의 값을 확인
            task ->
            if(task.isSuccessful){
                // 신규계정 생성이 성공했다면 ?
                moveMain(task.result?.user)
            } else {
                // 이미 계정이 존재한다면?
                signinEmail()
            }
        }
    }

    // 이미 계정이 존재한다면? -> 로그인
    private fun signinEmail() {
        val id = binding.edittextId.text.toString()
        val password = binding.edittextPassword.text.toString()

        // 앱에 로그인하게되면 주소와 비밀번호를 signInWithEmailAndPassword에 전달
        auth.signInWithEmailAndPassword(id, password).addOnCompleteListener {
                task ->
            if(task.isSuccessful) {
                moveMain(task.result?.user)
            }
        }
    }

    // 로그인 성공했다면(user가 null이 아닐경우) 메인 엑티비티로 이동
    private fun moveMain(user: FirebaseUser?) {
        if(user != null){
            startActivity(Intent(this, MainActivity::class.java))
        }
    }

}