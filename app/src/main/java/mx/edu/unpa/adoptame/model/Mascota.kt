package mx.edu.unpa.adoptame.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class Mascota(
    @SerializedName(value = "idMascota", alternate = ["id"])
    var idMascota: Int? = null,
    var nombre: String? = null,
    var tipo: String? = null,
    var raza: String? = null,
    var sexo: String? = null,
    var edadAproximada: String? = null,
    var descripcion: String? = null,
    var estadoAdopcion: String? = null,
    var activo: Boolean? = true,
    var fechaPublicacion: String? = null,
    var idusuarioDonador: UsuarioDonador? = null,
    @SerializedName("idUsuarioDonador")
    var idUsuarioDonador: Int? = null,
    var imagenes: List<ImagenMascota>? = null
) : java.io.Serializable {

    fun donadorId(): Int? = idUsuarioDonador ?: idusuarioDonador?.idUsuario

    fun mainImageUrl(): String? {
        val principal = imagenes?.firstOrNull { it.imagenPrincipal == true }?.urlImagen
        return principal ?: imagenes?.firstOrNull()?.urlImagen
    }
}
