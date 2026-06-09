package mx.edu.unpa.adoptame.model

data class UploadFile(
    val nombre: String,
    val ruta: String,
    val tipo: String,
    val size: Long
)
