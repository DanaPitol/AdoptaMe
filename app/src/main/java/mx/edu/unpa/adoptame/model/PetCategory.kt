package mx.edu.unpa.adoptame.model

data class PetCategory(
    val name: String,
    val imageUrl: String? = null,
    val imageRes: Int,
    val available: Boolean
)
