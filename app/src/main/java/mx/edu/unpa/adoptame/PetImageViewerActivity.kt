package mx.edu.unpa.adoptame

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.appbar.MaterialToolbar
import android.widget.ImageView
import mx.edu.unpa.adoptame.util.GlideImageLoader

class PetImageViewerActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_pet_image_viewer)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }

        val url = intent.getStringExtra(EXTRA_IMAGE_URL)
        val drawableRes = intent.getIntExtra(EXTRA_DRAWABLE_RES, R.drawable.imagen_gatito)
        val imgFull = findViewById<ImageView>(R.id.imgFull)
        GlideImageLoader.load(imgFull, url, drawableRes)
    }

    companion object {
        const val EXTRA_IMAGE_URL = "image_url"
        const val EXTRA_DRAWABLE_RES = "drawable_res"
    }
}
