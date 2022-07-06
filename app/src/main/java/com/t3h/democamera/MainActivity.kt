package com.t3h.democamera

import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.FrameLayout

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        supportFragmentManager.beginTransaction()
            .add(R.id.content, CameraFragment(), CameraFragment::class.java.name)
            .commit()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 101) {
            for (grantResult in grantResults) {
                if (grantResult == PackageManager.PERMISSION_DENIED) {
                    return
                }
            }
            (supportFragmentManager.findFragmentByTag(CameraFragment::class.java.name)
                    as CameraFragment).acceptPermissionCamera()
        }
    }
}