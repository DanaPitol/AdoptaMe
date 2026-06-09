package mx.edu.unpa.adoptame.util

import mx.edu.unpa.adoptame.client.RetrofitClient

object ImageUrlHelper {

    fun buildUrl(path: String?): String? {
        if (path.isNullOrBlank()) return null
        if (path.startsWith("http://") || path.startsWith("https://")) return path
        val base = RetrofitClient.BASE_URL.trimEnd('/')
        val normalized = if (path.startsWith("/")) path else "/$path"
        return base + normalized
    }

    fun uploadPath(ruta: String?): String {
        if (ruta.isNullOrBlank()) return ""
        return if (ruta.startsWith("/")) ruta else "/$ruta"
    }
}
