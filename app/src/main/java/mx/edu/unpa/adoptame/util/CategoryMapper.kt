package mx.edu.unpa.adoptame.util

import android.content.Context
import mx.edu.unpa.adoptame.R
import mx.edu.unpa.adoptame.model.PetCategory

object CategoryMapper {

    /** Tarjetas del dashboard: filtran por Mascota.tipo (solo UI, no hay tabla Categoria). */
    fun dashboardCategories(context: Context): List<PetCategory> = listOf(
        PetCategory("Perro", null, R.drawable.imagen_perrito, true),
        PetCategory("Gato", null, R.drawable.imagen_gatito, true),
        PetCategory("Hamster", null, R.drawable.imagen_hasmter, true),
        PetCategory("Loro", null, R.drawable.imagen_loro, false)
    )
}
