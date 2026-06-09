package mx.edu.unpa.adoptame

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import mx.edu.unpa.adoptame.client.RetrofitClient
import mx.edu.unpa.adoptame.model.Usuario
import mx.edu.unpa.adoptame.util.AuthUtils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {
    private lateinit var prefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val savedEmail = prefs.getString(KEY_EMAIL, null)
        if (savedEmail != null) {
            startActivity(Intent(this, DashboardActivity::class.java))
            finish()
            return
        }

        enableEdgeToEdge()
        setContentView(R.layout.activity_login)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        findViewById<Button>(R.id.btnLogin).setOnClickListener { login() }
        findViewById<TextView>(R.id.txtRegister).setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
        findViewById<TextView>(R.id.txtForgotPassword).setOnClickListener {
            startActivity(Intent(this, RecoverPasswordActivity::class.java))
        }
    }

    private fun login() {
        val email = findViewById<EditText>(R.id.txtEmail).text.toString().trim().lowercase()
        val password = findViewById<EditText>(R.id.txtPassword).text.toString()

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, R.string.msg_fill_fields, Toast.LENGTH_SHORT).show()
            return
        }
        if (!AuthUtils.isValidEmail(email)) {
            Toast.makeText(this, R.string.msg_invalid_email, Toast.LENGTH_SHORT).show()
            return
        }

        RetrofitClient.instance.getUsuarioByEmail(email)
            .enqueue(object : Callback<Usuario> {
                override fun onResponse(call: Call<Usuario>, response: Response<Usuario>) {
                    if (response.isSuccessful) {
                        val usuario = response.body()
                        if (usuario?.password == password) {
                            val userId = usuario.resolvedId() ?: -1
                            prefs.edit()
                                .putString(KEY_EMAIL, email)
                                .putInt(KEY_USER_ID, userId)
                                .apply()
                            if (userId <= 0) {
                                Log.w(TAG, "Login OK pero el API no devolvió id de usuario")
                            }

                            Toast.makeText(
                                this@MainActivity,
                                R.string.msg_login_success,
                                Toast.LENGTH_SHORT
                            ).show()

                            startActivity(Intent(this@MainActivity, DashboardActivity::class.java))
                            finish()
                        } else {
                            Toast.makeText(
                                this@MainActivity,
                                R.string.msg_wrong_password,
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } else if (response.code() == 404) {
                        Toast.makeText(
                            this@MainActivity,
                            R.string.msg_user_not_found,
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        Toast.makeText(
                            this@MainActivity,
                            getString(R.string.msg_register_error, response.code()),
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }

                override fun onFailure(call: Call<Usuario>, t: Throwable) {
                    Log.e(TAG, "Fallo login: ${t.message}", t)
                    Toast.makeText(
                        this@MainActivity,
                        R.string.msg_connection_error,
                        Toast.LENGTH_LONG
                    ).show()
                }
            })
    }

    companion object {
        const val PREFS_NAME = "sesion"
        const val KEY_EMAIL = "usuario"
        const val KEY_USER_ID = "idUsuario"
        private const val TAG = "LOGIN"
    }
}
