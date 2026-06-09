package mx.edu.unpa.adoptame.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class ImagenMascota(
    @SerializedName("idImagen")
    var idImagen: Int? = null,
    var urlImagen: String? = null,
    var imagenPrincipal: Boolean? = true
) : Serializable
