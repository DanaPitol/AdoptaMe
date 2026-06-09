package mx.edu.unpa.adoptame

import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import mx.edu.unpa.adoptame.adapter.RegisterPhotoAdapter
import mx.edu.unpa.adoptame.client.RetrofitClient
import mx.edu.unpa.adoptame.model.Mascota
import mx.edu.unpa.adoptame.model.UploadFile
import mx.edu.unpa.adoptame.model.UsuarioDonador
import mx.edu.unpa.adoptame.permissions.PermissionManager
import mx.edu.unpa.adoptame.permissions.PermissionState
import mx.edu.unpa.adoptame.permissions.PermissionType
import mx.edu.unpa.adoptame.util.GlideImageLoader
import mx.edu.unpa.adoptame.util.ImageUrlHelper
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream

class RegisterPetActivity : AppCompatActivity() {
    private lateinit var permissionManager: PermissionManager
    private lateinit var photoAdapter: RegisterPhotoAdapter
    private lateinit var imgPetPhoto: ImageView

    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { imageBitmap ->
        if (imageBitmap != null) {
            photoAdapter.addPhoto(imageBitmap)
            updatePrincipalPreview(photoAdapter.getPrincipalIndex())
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_register_pet)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        permissionManager = PermissionManager(this)
        imgPetPhoto = findViewById(R.id.imgPetPhoto)

        val tipoPrellenado = intent.getStringExtra(EXTRA_TIPO)?.trim().orEmpty().ifBlank { "Gato" }
        findViewById<EditText>(R.id.txtTipo).setText(tipoPrellenado)

        val spinnerEstado = findViewById<Spinner>(R.id.spinnerEstado)
        ArrayAdapter.createFromResource(
            this,
            R.array.pet_estado_options,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerEstado.adapter = adapter
        }

        val spinnerSexo = findViewById<Spinner>(R.id.spinnerSexo)
        ArrayAdapter.createFromResource(
            this,
            R.array.pet_sexo_options,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerSexo.adapter = adapter
        }

        photoAdapter = RegisterPhotoAdapter { index -> updatePrincipalPreview(index) }
        findViewById<RecyclerView>(R.id.recyclerPhotos).apply {
            layoutManager = LinearLayoutManager(this@RegisterPetActivity, LinearLayoutManager.HORIZONTAL, false)
            adapter = photoAdapter
        }

        findViewById<Button>(R.id.btnTakePhoto).setOnClickListener { tomarFoto() }
        findViewById<Button>(R.id.btnSavePet).setOnClickListener { savePet() }
    }

    private fun updatePrincipalPreview(index: Int) {
        val photos = photoAdapter.getPhotos()
        if (index in photos.indices) {
            GlideImageLoader.load(imgPetPhoto, photos[index])
        }
    }

    private fun tomarFoto() {
        permissionManager.requestPermission(PermissionType.CAMERA) { state ->
            when (state) {
                PermissionState.GRANTED -> cameraLauncher.launch(null)
                PermissionState.DENIED -> Toast.makeText(this, "Permiso de cámara denegado", Toast.LENGTH_SHORT).show()
                PermissionState.PERMANENTLY_DENIED -> Toast.makeText(this, "Habilita el permiso en configuración", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun savePet() {
        val nombre = findViewById<EditText>(R.id.txtNombre).text.toString().trim()
        val tipo = findViewById<EditText>(R.id.txtTipo).text.toString().trim()
        val raza = findViewById<EditText>(R.id.txtRaza).text.toString().trim()
        val sexo = findViewById<Spinner>(R.id.spinnerSexo).selectedItem.toString()
        val estado = findViewById<Spinner>(R.id.spinnerEstado).selectedItem.toString()

        if (nombre.isEmpty() || tipo.isEmpty() || raza.isEmpty()) {
            Toast.makeText(this, R.string.msg_fill_fields, Toast.LENGTH_SHORT).show()
            return
        }

        if (photoAdapter.getPhotos().isEmpty()) {
            Toast.makeText(this, R.string.msg_photo_required, Toast.LENGTH_SHORT).show()
            return
        }

        val userId = getSharedPreferences(MainActivity.PREFS_NAME, MODE_PRIVATE)
            .getInt(MainActivity.KEY_USER_ID, -1)
        if (userId <= 0) {
            Toast.makeText(this, R.string.msg_connection_error, Toast.LENGTH_SHORT).show()
            return
        }

        setLoading(true)
        crearMascota(nombre, tipo, raza, sexo, estado, userId)
    }

    private fun crearMascota(
        nombre: String,
        tipo: String,
        raza: String,
        sexo: String,
        estado: String,
        userId: Int
    ) {
        val mascota = Mascota(
            nombre = nombre,
            tipo = tipo,
            raza = raza,
            sexo = sexo,
            estadoAdopcion = estado,
            activo = true,
            idusuarioDonador = UsuarioDonador(userId)
        )

        RetrofitClient.mascotaService.crearMascota(mascota).enqueue(object : Callback<Mascota> {
            override fun onResponse(call: Call<Mascota>, response: Response<Mascota>) {
                if (response.isSuccessful) {
                    val idMascota = response.body()?.idMascota
                    if (idMascota != null) {
                        uploadPhotosForPet(idMascota, 0)
                    } else {
                        setLoading(false)
                        Toast.makeText(this@RegisterPetActivity, R.string.msg_pet_saved, Toast.LENGTH_SHORT).show()
                        finish()
                    }
                } else {
                    setLoading(false)
                    val text = when (response.code()) {
                        400 -> getString(R.string.msg_session_expired)
                        else -> getString(R.string.msg_pet_error, response.code())
                    }
                    Toast.makeText(this@RegisterPetActivity, text, Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<Mascota>, t: Throwable) {
                setLoading(false)
                Log.e(TAG, "Fallo guardar mascota: ${t.message}", t)
                Toast.makeText(this@RegisterPetActivity, R.string.msg_connection_error, Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun uploadPhotosForPet(idMascota: Int, index: Int) {
        val photos = photoAdapter.getPhotos()
        if (index >= photos.size) {
            setLoading(false)
            Toast.makeText(this, R.string.msg_pet_saved, Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        subirImagen(photos[index]) { imagePath ->
            val esPrincipal = photoAdapter.isPrincipal(index)
            guardarImagenMascota(idMascota, imagePath, esPrincipal) {
                uploadPhotosForPet(idMascota, index + 1)
            }
        }
    }

    private fun subirImagen(bitmap: Bitmap, onSuccess: (String) -> Unit) {
        val file = File(cacheDir, "mascota_${System.currentTimeMillis()}.jpg")
        FileOutputStream(file).use { fos ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos)
        }

        val body = MultipartBody.Part.createFormData(
            "file",
            file.name,
            file.asRequestBody("image/*".toMediaType())
        )

        RetrofitClient.uploadService.uploadImage(body, "mascotas").enqueue(object : Callback<UploadFile> {
            override fun onResponse(call: Call<UploadFile>, response: Response<UploadFile>) {
                if (response.isSuccessful && response.body() != null) {
                    onSuccess(ImageUrlHelper.uploadPath(response.body()!!.ruta))
                } else {
                    setLoading(false)
                    Toast.makeText(
                        this@RegisterPetActivity,
                        getString(R.string.msg_pet_error, response.code()),
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

            override fun onFailure(call: Call<UploadFile>, t: Throwable) {
                setLoading(false)
                Log.e(TAG, "Fallo subir imagen: ${t.message}", t)
                Toast.makeText(this@RegisterPetActivity, R.string.msg_connection_error, Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun guardarImagenMascota(
        idMascota: Int,
        imagePath: String,
        imagenPrincipal: Boolean,
        onSuccess: () -> Unit
    ) {
        val body = mapOf(
            "idMascota" to idMascota,
            "urlImagen" to imagePath,
            "imagenPrincipal" to imagenPrincipal
        )

        RetrofitClient.imagenMascotaService.crearImagen(body).enqueue(object : Callback<mx.edu.unpa.adoptame.model.ImagenMascota> {
            override fun onResponse(
                call: Call<mx.edu.unpa.adoptame.model.ImagenMascota>,
                response: Response<mx.edu.unpa.adoptame.model.ImagenMascota>
            ) {
                if (response.isSuccessful) {
                    onSuccess()
                } else {
                    setLoading(false)
                    Toast.makeText(
                        this@RegisterPetActivity,
                        getString(R.string.msg_pet_error, response.code()),
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

            override fun onFailure(call: Call<mx.edu.unpa.adoptame.model.ImagenMascota>, t: Throwable) {
                setLoading(false)
                Log.e(TAG, "Fallo guardar imagen mascota: ${t.message}", t)
                Toast.makeText(this@RegisterPetActivity, R.string.msg_connection_error, Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun setLoading(loading: Boolean) {
        findViewById<Button>(R.id.btnSavePet).isEnabled = !loading
        findViewById<Button>(R.id.btnTakePhoto).isEnabled = !loading
    }

    companion object {
        const val EXTRA_TIPO = "tipo"
        private const val TAG = "REGISTER_PET"
    }
}
