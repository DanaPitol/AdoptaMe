package mx.edu.unpa.adoptame.permissions

import android.content.pm.PackageManager
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat

class PermissionManager(private val activity: ComponentActivity) {

    private var callback: ((PermissionState) -> Unit)? = null
    private var currentPermission: String? = null

    private val launcher = activity.registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            callback?.invoke(PermissionState.GRANTED)
        } else {
            currentPermission?.let { permission ->
                if (PermissionUtils.shouldShowRationale(activity, permission)) {
                    callback?.invoke(PermissionState.DENIED)
                } else {
                    callback?.invoke(PermissionState.PERMANENTLY_DENIED)
                }
            }
        }
    }

    fun requestPermission(permissionType: PermissionType, result: (PermissionState) -> Unit) {
        callback = result
        currentPermission = permissionType.permission
        when {
            ContextCompat.checkSelfPermission(activity, permissionType.permission) ==
                PackageManager.PERMISSION_GRANTED -> result(PermissionState.GRANTED)
            else -> launcher.launch(permissionType.permission)
        }
    }
}
