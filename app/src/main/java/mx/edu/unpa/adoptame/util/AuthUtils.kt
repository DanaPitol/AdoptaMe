package mx.edu.unpa.adoptame.util

import android.util.Patterns

object AuthUtils {
    const val PLACEHOLDER_APELLIDO_PATERNO = "Pendiente"
    private const val GMAIL_DOMAIN = "@gmail.com"

    fun isValidEmail(email: String): Boolean {
        val normalized = email.trim().lowercase()
        if (!Patterns.EMAIL_ADDRESS.matcher(normalized).matches()) return false
        if (!normalized.endsWith(GMAIL_DOMAIN)) return false
        val localPart = normalized.removeSuffix(GMAIL_DOMAIN)
        return localPart.isNotBlank() && !localPart.contains("@")
    }

    fun placeholderNombreFromEmail(email: String): String {
        val localPart = email.substringBefore("@").ifBlank { "Usuario" }
        return localPart.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
    }

    /** Muestra vacío si el perfil aún no fue completado por el usuario. */
    fun displayNombre(nombre: String?, email: String): String {
        if (nombre.isNullOrBlank()) return ""
        if (nombre.equals(PLACEHOLDER_APELLIDO_PATERNO, ignoreCase = true)) return ""
        val placeholder = placeholderNombreFromEmail(email)
        return if (nombre.equals(placeholder, ignoreCase = true)) "" else nombre.trim()
    }

    /** Muestra vacío si el apellido paterno es el valor temporal del registro. */
    fun displayApellidoPaterno(apellidoPaterno: String?): String {
        if (apellidoPaterno.isNullOrBlank()) return ""
        return if (apellidoPaterno.equals(PLACEHOLDER_APELLIDO_PATERNO, ignoreCase = true)) {
            ""
        } else {
            apellidoPaterno
        }
    }
}
