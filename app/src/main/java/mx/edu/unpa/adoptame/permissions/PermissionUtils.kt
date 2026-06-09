package mx.edu.unpa.adoptame.permissions

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.core.app.ActivityCompat

object PermissionUtils {
    fun shouldShowRationale(activity: Activity, permission: String): Boolean {
        return ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)
    }

    fun openAppSettings(activity: Activity) {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        intent.data = Uri.fromParts("package", activity.packageName, null)
        activity.startActivity(intent)
    }
}
