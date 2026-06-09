package mx.edu.unpa.adoptame.util

import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.TextView
import android.widget.Toast
import mx.edu.unpa.adoptame.R

object PetTipoToast {

    fun show(context: Context, message: String) {
        val view = LayoutInflater.from(context).inflate(R.layout.toast_pet_tipo, null)
        view.findViewById<TextView>(R.id.txtToastTipo).text = message

        Toast(context).apply {
            duration = Toast.LENGTH_SHORT
            @Suppress("DEPRECATION")
            this.view = view
            setGravity(Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL, 0, 120)
        }.show()
    }
}
