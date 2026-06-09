package mx.edu.unpa.adoptame.util

import mx.edu.unpa.adoptame.R
import mx.edu.unpa.adoptame.model.GalleryImageItem
import mx.edu.unpa.adoptame.model.ImagenMascota
import mx.edu.unpa.adoptame.model.Mascota

object PetGalleryHelper {

    const val CATALOG_GALLERY_SIZE = 16

    private val CATALOG_CAT_NAMES = setOf(
        "Mishi", "Luna", "Simba", "Nina", "Oreo", "Kira"
    )

    fun buildGalleryItems(pet: Mascota): List<GalleryImageItem> {
        val fromDb = pet.imagenes
            ?.mapNotNull { toGalleryItem(it) }
            ?.sortedByDescending { it.isPrincipal }
            .orEmpty()

        if (isCatalogCat(pet.nombre)) {
            return buildCatalogGallery(fromDb)
        }

        return fromDb.ifEmpty {
            listOf(GalleryImageItem(drawableRes = R.drawable.imagen_gatito))
        }
    }

    fun isCatalogCat(nombre: String?): Boolean {
        return CATALOG_CAT_NAMES.any { it.equals(nombre, ignoreCase = true) }
    }

    private fun buildCatalogGallery(fromDb: List<GalleryImageItem>): List<GalleryImageItem> {
        if (fromDb.size >= CATALOG_GALLERY_SIZE) {
            return fromDb.take(CATALOG_GALLERY_SIZE)
        }

        val template = fromDb.firstOrNull()
            ?: GalleryImageItem(drawableRes = R.drawable.imagen_gatito, isPrincipal = true)

        return List(CATALOG_GALLERY_SIZE) { index ->
            template.copy(isPrincipal = index == 0)
        }
    }

    private fun toGalleryItem(img: ImagenMascota): GalleryImageItem? {
        val path = img.urlImagen?.takeIf { it.isNotBlank() } ?: return null
        return GalleryImageItem(
            url = path,
            isPrincipal = img.imagenPrincipal == true
        )
    }
}
