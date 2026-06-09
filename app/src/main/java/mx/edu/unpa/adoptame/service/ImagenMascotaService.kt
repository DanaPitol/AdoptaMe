package mx.edu.unpa.adoptame.service

import mx.edu.unpa.adoptame.model.ImagenMascota
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ImagenMascotaService {
    @GET("imagenMascota/mascota/{idMascota}")
    fun getImagenesPorMascota(@Path("idMascota") idMascota: Int): Call<List<ImagenMascota>>

    @POST("imagenMascota")
    fun crearImagen(@Body body: Map<String, @JvmSuppressWildcards Any>): Call<ImagenMascota>
}
