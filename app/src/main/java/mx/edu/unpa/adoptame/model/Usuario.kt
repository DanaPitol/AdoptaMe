package mx.edu.unpa.adoptame.model

import com.google.gson.annotations.JsonAdapter
import com.google.gson.annotations.SerializedName
import mx.edu.unpa.adoptame.util.FlexibleDateTypeAdapter
import java.io.Serializable

data class Usuario(
    @SerializedName("idUsuario")
    var idUsuario: Int? = null,
    var nombre: String? = null,
    var apellidoPaterno: String? = null,
    var apellidoMaterno: String? = null,
    var email: String? = null,
    var telefono: String? = null,
    var password: String? = null,
    var activo: Boolean = true,
    @JsonAdapter(FlexibleDateTypeAdapter::class)
    @SerializedName("fechaRegistro")
    var fechaRegistro: String? = null
) : Serializable {
    fun resolvedId(): Int? = idUsuario
}

data class UsuarioDonador(
    @SerializedName("idUsuario")
    var idUsuario: Int
) : Serializable
