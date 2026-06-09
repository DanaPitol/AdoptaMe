package mx.edu.unpa.adoptame

import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.appbar.MaterialToolbar
import mx.edu.unpa.adoptame.client.RetrofitClient
import mx.edu.unpa.adoptame.model.Usuario
import mx.edu.unpa.adoptame.util.AuthUtils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SettingsActivity : AppCompatActivity() {
    private lateinit var prefs: SharedPreferences
    private var loadedUser: Usuario? = null

    private lateinit var txtEmail: EditText
    private lateinit var txtNombre: EditText
    private lateinit var txtApellidoPaterno: EditText
    private lateinit var txtApellidoMaterno: EditText
    private lateinit var txtTelefono: EditText
    private lateinit var btnSave: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_settings)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        prefs = getSharedPreferences(MainActivity.PREFS_NAME, MODE_PRIVATE)

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }

        txtEmail = findViewById(R.id.txtEmail)
        txtNombre = findViewById(R.id.txtNombre)
        txtApellidoPaterno = findViewById(R.id.txtApellidoPaterno)
        txtApellidoMaterno = findViewById(R.id.txtApellidoMaterno)
        txtTelefono = findViewById(R.id.txtTelefono)
        btnSave = findViewById(R.id.btnSave)

        val savedEmail = prefs.getString(MainActivity.KEY_EMAIL, "") ?: ""
        txtEmail.setText(savedEmail)

        btnSave.setOnClickListener { saveProfile() }
        loadProfile(savedEmail)
    }

    private fun loadProfile(email: String) {
        if (email.isBlank()) {
            Toast.makeText(this, R.string.msg_session_expired, Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        btnSave.isEnabled = false
        // Siempre por correo: evita 500/conflictos con GET /usuario/{id}
        fetchUserByEmail(email)
    }

    private fun fetchUserByEmail(email: String) {
        fetchUser(RetrofitClient.instance.getUsuarioByEmail(email), email, onNotFound = null)
    }

    private fun fetchUser(
        call: Call<Usuario>,
        email: String,
        onNotFound: (() -> Unit)?
    ) {
        call.enqueue(object : Callback<Usuario> {
            override fun onResponse(call: Call<Usuario>, response: Response<Usuario>) {
                btnSave.isEnabled = true
                when {
                    response.isSuccessful && response.body() != null -> bindUser(response.body()!!, email)
                    response.isSuccessful && response.body() == null -> {
                        Log.e(TAG, "Perfil recibido vacío (error al interpretar JSON)")
                        Toast.makeText(
                            this@SettingsActivity,
                            R.string.msg_profile_parse_error,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    (response.code() == 404 || response.code() >= 500) && onNotFound != null -> onNotFound()
                    else -> {
                        Log.e(TAG, "Error cargando perfil: ${response.code()}")
                        Toast.makeText(
                            this@SettingsActivity,
                            getString(R.string.msg_profile_load_error, response.code()),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }

            override fun onFailure(call: Call<Usuario>, t: Throwable) {
                btnSave.isEnabled = true
                Log.e(TAG, "Fallo cargando perfil: ${t.message}", t)
                Toast.makeText(this@SettingsActivity, R.string.msg_connection_error, Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun bindUser(usuario: Usuario, email: String) {
        loadedUser = usuario

        val resolvedId = usuario.resolvedId()
        if (resolvedId != null && resolvedId > 0) {
            prefs.edit().putInt(MainActivity.KEY_USER_ID, resolvedId).apply()
        } else {
            Log.w(TAG, "Usuario sin id en respuesta JSON")
        }

        val resolvedEmail = usuario.email ?: email
        txtEmail.setText(resolvedEmail)
        txtNombre.setText(AuthUtils.displayNombre(usuario.nombre, resolvedEmail))
        txtApellidoPaterno.setText(AuthUtils.displayApellidoPaterno(usuario.apellidoPaterno))
        txtApellidoMaterno.setText(usuario.apellidoMaterno.orEmpty())
        txtTelefono.setText(usuario.telefono.orEmpty())
    }

    private fun saveProfile() {
        val nombre = txtNombre.text.toString().trim()
        val apellidoPaterno = txtApellidoPaterno.text.toString().trim()
        val apellidoMaterno = txtApellidoMaterno.text.toString().trim()
        val telefono = txtTelefono.text.toString().trim()
        val email = txtEmail.text.toString().trim().lowercase()

        if (nombre.isEmpty() || apellidoPaterno.isEmpty()) {
            Toast.makeText(this, R.string.msg_fill_profile_required, Toast.LENGTH_SHORT).show()
            return
        }

        val userId = loadedUser?.resolvedId() ?: prefs.getInt(MainActivity.KEY_USER_ID, -1)
        if (userId > 0 && loadedUser != null) {
            performSave(userId, loadedUser!!, nombre, apellidoPaterno, apellidoMaterno, telefono, email)
            return
        }

        // Si no hay id guardado, buscar por correo antes de actualizar
        btnSave.isEnabled = false
        RetrofitClient.instance.getUsuarioByEmail(email).enqueue(object : Callback<Usuario> {
            override fun onResponse(call: Call<Usuario>, response: Response<Usuario>) {
                btnSave.isEnabled = true
                val usuario = response.body()
                val id = usuario?.resolvedId()
                if (response.isSuccessful && usuario != null && id != null && id > 0) {
                    loadedUser = usuario
                    prefs.edit().putInt(MainActivity.KEY_USER_ID, id).apply()
                    performSave(id, usuario, nombre, apellidoPaterno, apellidoMaterno, telefono, email)
                } else {
                    Toast.makeText(this@SettingsActivity, R.string.msg_session_expired, Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Usuario>, t: Throwable) {
                btnSave.isEnabled = true
                Toast.makeText(this@SettingsActivity, R.string.msg_connection_error, Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun performSave(
        userId: Int,
        user: Usuario,
        nombre: String,
        apellidoPaterno: String,
        apellidoMaterno: String,
        telefono: String,
        email: String
    ) {
        val updated = user.copy(
            idUsuario = userId,
            nombre = nombre,
            apellidoPaterno = apellidoPaterno,
            apellidoMaterno = apellidoMaterno.ifBlank { null },
            email = email,
            telefono = telefono.ifBlank { null },
            password = user.password,
            activo = user.activo,
            fechaRegistro = user.fechaRegistro
        )

        btnSave.isEnabled = false
        RetrofitClient.instance.actualizarUsuario(userId, updated)
            .enqueue(object : Callback<Usuario> {
                override fun onResponse(call: Call<Usuario>, response: Response<Usuario>) {
                    btnSave.isEnabled = true
                    if (response.isSuccessful) {
                        loadedUser = response.body() ?: updated
                        response.body()?.resolvedId()?.let { id ->
                            prefs.edit().putInt(MainActivity.KEY_USER_ID, id).apply()
                        }
                        Toast.makeText(
                            this@SettingsActivity,
                            R.string.msg_profile_saved,
                            Toast.LENGTH_SHORT
                        ).show()
                        finish()
                    } else {
                        Log.e(TAG, "Error guardando perfil: ${response.code()} - ${response.errorBody()?.string()}")
                        Toast.makeText(
                            this@SettingsActivity,
                            getString(R.string.msg_profile_save_error, response.code()),
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }

                override fun onFailure(call: Call<Usuario>, t: Throwable) {
                    btnSave.isEnabled = true
                    Log.e(TAG, "Fallo guardando perfil: ${t.message}", t)
                    Toast.makeText(this@SettingsActivity, R.string.msg_connection_error, Toast.LENGTH_LONG).show()
                }
            })
    }

    companion object {
        private const val TAG = "SETTINGS"
    }
}
