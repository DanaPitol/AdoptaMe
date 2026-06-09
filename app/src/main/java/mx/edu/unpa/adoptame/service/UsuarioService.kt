package mx.edu.unpa.adoptame.service

import mx.edu.unpa.adoptame.model.Usuario
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface UsuarioService {
    @GET("usuario")
    fun getUsuarios(): Call<List<Usuario>>

    @GET("usuario/{idUsuario}")
    fun getUsuarioById(@Path("idUsuario") idUsuario: Int): Call<Usuario>

    @GET("usuario/email")
    fun getUsuarioByEmail(@Query("email") email: String): Call<Usuario>

    @POST("usuario")
    fun crearUsuario(@Body usuario: Usuario): Call<Usuario>

    @PUT("usuario/{idUsuario}")
    fun actualizarUsuario(@Path("idUsuario") idUsuario: Int, @Body usuario: Usuario): Call<Usuario>
}
