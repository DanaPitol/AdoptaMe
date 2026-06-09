package mx.edu.unpa.adoptame

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
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

class RegisterActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_register)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        findViewById<Button>(R.id.btnRegister).setOnClickListener { register() }
    }

    private fun register() {
        val email = findViewById<EditText>(R.id.txtEmail).text.toString().trim().lowercase()
        val password = findViewById<EditText>(R.id.txtPassword).text.toString()
        val confirm = findViewById<EditText>(R.id.txtConfirmPassword).text.toString()

        if (email.isEmpty() || password.isEmpty() || confirm.isEmpty()) {
            Toast.makeText(this, R.string.msg_fill_fields, Toast.LENGTH_SHORT).show()
            return
        }
        if (!AuthUtils.isValidEmail(email)) {
            Toast.makeText(this, R.string.msg_invalid_email, Toast.LENGTH_SHORT).show()
            return
        }
        if (password != confirm) {
            Toast.makeText(this, R.string.msg_password_mismatch, Toast.LENGTH_SHORT).show()
            return
        }

        val nuevo = Usuario(
            nombre = "",
            apellidoPaterno = "",
            email = email,
            password = password
        )

        RetrofitClient.instance.crearUsuario(nuevo).enqueue(object : Callback<Usuario> {
            override fun onResponse(call: Call<Usuario>, response: Response<Usuario>) {
                if (response.isSuccessful) {
                    Toast.makeText(
                        this@RegisterActivity,
                        R.string.msg_register_success,
                        Toast.LENGTH_SHORT
                    ).show()
                    startActivity(Intent(this@RegisterActivity, MainActivity::class.java))
                    finish()
                } else if (response.code() == 409 || response.code() == 400) {
                    Toast.makeText(
                        this@RegisterActivity,
                        R.string.msg_email_already_exists,
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e(TAG, "Error registro: ${response.code()} - $errorBody")
                    Toast.makeText(
                        this@RegisterActivity,
                        getString(R.string.msg_register_error, response.code()),
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

            override fun onFailure(call: Call<Usuario>, t: Throwable) {
                Log.e(TAG, "Fallo registro: ${t.message}", t)
                Toast.makeText(
                    this@RegisterActivity,
                    R.string.msg_connection_error,
                    Toast.LENGTH_LONG
                ).show()
            }
        })
    }

    companion object {
        private const val TAG = "REGISTER"
    }
}
