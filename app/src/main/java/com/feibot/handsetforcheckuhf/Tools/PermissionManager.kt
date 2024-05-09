package com.feibot.handsetforcheckuhf.Tools
import android.Manifest
import android.app.Activity
import android.widget.Toast
import net.zy13.library.OmgPermission

object PermissionManager {
    fun askPermissionManager(activity:Activity){
        OmgPermission.with(activity)
            .addRequestCode(100)
            .permissions(
                Manifest.permission.INTERNET,
                Manifest.permission.ACCESS_NETWORK_STATE,
                Manifest.permission.ACCESS_WIFI_STATE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.SET_TIME
            )
            .request(object :OmgPermission.PermissionCallback{
                override fun permissionSuccess(requsetCode: Int) {
                    Toast.makeText(activity, "成功授予权限！", Toast.LENGTH_SHORT).show()
                }

                override fun permissionFail(requestCode: Int) {
                    Toast.makeText(activity, "获取权限失败，请允许权限！", Toast.LENGTH_SHORT).show()
                }
            })
    }
}