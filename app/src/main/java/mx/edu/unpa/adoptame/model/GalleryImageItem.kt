package mx.edu.unpa.adoptame.model

import java.io.Serializable

data class GalleryImageItem(
    val url: String? = null,
    val drawableRes: Int? = null,
    val isPrincipal: Boolean = false
) : Serializable
