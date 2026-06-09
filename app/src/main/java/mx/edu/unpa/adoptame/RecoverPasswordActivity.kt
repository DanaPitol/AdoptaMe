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

class RecoverPasswordActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_recover_password)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        findViewById<Button>(R.id.btnUpdatePassword).setOnClickListener { updatePassword() }
    }

    private fun updatePassword() {
        val email = findViewById<EditText>(R.id.txtEmail).text.toString().trim().lowercase()
        val newPassword = findViewById<EditText>(R.id.txtNewPassword).text.toString()
        val confirm = findViewById<EditText>(R.id.txtConfirmPassword).text.toString()

        if (email.isEmpty() || newPassword.isEmpty() || confirm.isEmpty()) {
            Toast.makeText(this, R.string.msg_fill_fields, Toast.LENGTH_SHORT).show()
            return
        }
        if (!AuthUtils.isValidEmail(email)) {
            Toast.makeText(this, R.string.msg_invalid_email, Toast.LENGTH_SHORT).show()
            return
        }
        if (newPassword != confirm) {
            Toast.makeText(this, R.string.msg_password_mismatch, Toast.LENGTH_SHORT).show()
            return
        }

        RetrofitClient.instance.getUsuarioByEmail(email)
            .enqueue(object : Callback<Usuario> {
                override fun onResponse(call: Call<Usuario>, response: Response<Usuario>) {
                    if (!response.isSuccessful || response.body() == null) {
                        Toast.makeText(
                            this@RecoverPasswordActivity,
                            R.string.msg_user_not_found,
                            Toast.LENGTH_SHORT
                        ).show()
                        return
                    }

                    val usuario = response.body()!!
                    val userId = usuario.resolvedId()
                    if (userId == null) {
                        Toast.makeText(
                            this@RecoverPasswordActivity,
                            R.string.msg_user_not_found,
                            Toast.LENGTH_SHORT
                        ).show()
                        return
                    }

                    usuario.password = newPassword
                    RetrofitClient.instance.actualizarUsuario(userId, usuario)
                        .enqueue(object : Callback<Usuario> {
                            override fun onResponse(
                                call: Call<Usuario>,
                                updateResponse: Response<Usuario>
                            ) {
                                if (updateResponse.isSuccessful) {
                                    Toast.makeText(
                                        this@RecoverPasswordActivity,
                                        R.string.msg_password_updated,
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    startActivity(
                                        Intent(
                                            this@RecoverPasswordActivity,
                                            MainActivity::class.java
                                        )
                                    )
                                    finish()
                                } else {
                                    Toast.makeText(
                                        this@RecoverPasswordActivity,
                                        getString(
                                            R.string.msg_register_error,
                                            updateResponse.code()
                                        ),
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            }

                            override fun onFailure(call: Call<Usuario>, t: Throwable) {
                                Log.e(TAG, "Fallo actualizar: ${t.message}", t)
                                Toast.makeText(
                                    this@RecoverPasswordActivity,
                                    R.string.msg_connection_error,
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        })
                }

                override fun onFailure(call: Call<Usuario>, t: Throwable) {
                    Log.e(TAG, "Fallo buscar usuario: ${t.message}", t)
                    Toast.makeText(
                        this@RecoverPasswordActivity,
                        R.string.msg_connection_error,
                        Toast.LENGTH_LONG
                    ).show()
                }
            })
    }

    companion object {
        private const val TAG = "RECOVER"
    }
}
