package mx.edu.unpa.adoptame.util

import mx.edu.unpa.adoptame.model.Mascota

/**
 * Catálogo fijo (siempre visible) + mascotas registradas solo del usuario en sesión.
 */
object PetListMerger {

    private val CATALOG_ORDER = listOf("Mishi", "Luna", "Simba", "Nina", "Oreo", "Kira")

    fun mergeForDisplay(
        apiPets: List<Mascota>,
        currentUserId: Int,
        tipoFiltro: String = "Gato"
    ): List<Mascota> {
        val gatos = apiPets.filter { it.tipo.equals(tipoFiltro, ignoreCase = true) }

        val catalog = CATALOG_ORDER.map { name ->
            resolveCatalogPet(name, gatos, apiPets, tipoFiltro)
        }

        val userPets = if (currentUserId > 0) {
            gatos.filter { pet ->
                !PetGalleryHelper.isCatalogCat(pet.nombre) && pet.matchesDonador(currentUserId)
            }.sortedBy { it.nombre.orEmpty().lowercase() }
        } else {
            emptyList()
        }

        val firstFive = catalog.take(5)
        val kira = catalog.last()
        return firstFive + listOf(kira) + userPets
    }

    fun findIdInApi(pet: Mascota, apiPets: List<Mascota>): Int? {
        pet.idMascota?.takeIf { it > 0 }?.let { return it }
        val nombre = pet.nombre?.trim().orEmpty()
        if (nombre.isEmpty()) return null
        return apiPets.find { it.nombre.equals(nombre, ignoreCase = true) }?.idMascota?.takeIf { it > 0 }
    }

    private fun resolveCatalogPet(
        name: String,
        gatos: List<Mascota>,
        apiPets: List<Mascota>,
        tipoFiltro: String
    ): Mascota {
        gatos.find { it.nombre.equals(name, ignoreCase = true) }?.let { return it }

        apiPets.find {
            it.nombre.equals(name, ignoreCase = true) &&
                it.tipo.equals(tipoFiltro, ignoreCase = true)
        }?.let { return it }

        return defaultCatalogPet(name)
    }

    fun defaultCatalogList(): List<Mascota> = CATALOG_ORDER.map { defaultCatalogPet(it) }

    private fun defaultCatalogPet(nombre: String): Mascota {
        return when (nombre) {
            "Mishi" -> Mascota(
                nombre = "Mishi", tipo = "Gato", raza = "Siamés", sexo = "Hembra",
                estadoAdopcion = "En proceso"
            )
            "Luna" -> Mascota(
                nombre = "Luna", tipo = "Gato", raza = "Persa", sexo = "Hembra",
                estadoAdopcion = "Disponible"
            )
            "Simba" -> Mascota(
                nombre = "Simba", tipo = "Gato", raza = "Maine Coon", sexo = "Macho",
                estadoAdopcion = "En proceso"
            )
            "Nina" -> Mascota(
                nombre = "Nina", tipo = "Gato", raza = "Bengalí", sexo = "Hembra",
                estadoAdopcion = "Disponible"
            )
            "Oreo" -> Mascota(
                nombre = "Oreo", tipo = "Gato", raza = "Doméstico", sexo = "Macho",
                estadoAdopcion = "En proceso"
            )
            "Kira" -> Mascota(
                nombre = "Kira", tipo = "Gato", raza = "Azul Ruso", sexo = "Hembra",
                estadoAdopcion = "En proceso"
            )
            else -> Mascota(nombre = nombre, tipo = "Gato", estadoAdopcion = "Disponible")
        }
    }

    private fun Mascota.matchesDonador(currentUserId: Int): Boolean {
        val donador = donadorId() ?: return false
        return donador == currentUserId
    }
}
