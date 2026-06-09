package mx.edu.unpa.adoptame.service

import mx.edu.unpa.adoptame.model.UploadFile
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query

interface UploadService {
    @Multipart
    @POST("api/upload")
    fun uploadImage(
        @Part file: MultipartBody.Part,
        @Query("carpeta") carpeta: String = "mascotas"
    ): Call<UploadFile>
}
