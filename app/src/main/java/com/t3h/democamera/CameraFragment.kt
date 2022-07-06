package com.t3h.democamera

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.SurfaceTexture
import android.hardware.Camera
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.view.LayoutInflater
import android.view.TextureView
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import java.io.FileOutputStream
import java.util.*

class CameraFragment : Fragment(), TextureView.SurfaceTextureListener, View.OnClickListener {
    private var camera: Camera? = null
    private var sf: SurfaceTexture? = null
    private lateinit var ttCamera: TextureView
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val content = inflater.inflate(R.layout.fragment_camera, container, false)
        ttCamera = content.findViewById(R.id.tt_camera)

        //bắt sự kiện để textureview sẵn sàng nhận dữ liệu từ camera
        ttCamera.surfaceTextureListener = this

        content.findViewById<View>(R.id.btn_camera).setOnClickListener(this)
        return content
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }

    private fun checkCamerePermission(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true
        }

        val permissions = mutableListOf<String>()
        val permissionWrite = ActivityCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        if (permissionWrite == PackageManager.PERMISSION_DENIED) {
            permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
        val permissionRead = ActivityCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.READ_EXTERNAL_STORAGE
        )
        if (permissionRead == PackageManager.PERMISSION_DENIED) {
            permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }

        val permissionCamera =
            ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
        if (permissionCamera == PackageManager.PERMISSION_DENIED) {
            permissions.add(Manifest.permission.CAMERA)
        }
        if (permissions.size == 0) {
            return true
        }
        ActivityCompat.requestPermissions(requireActivity(), permissions.toTypedArray(), 101)
        return false
    }

    fun acceptPermissionCamera() {
        openCamera()
    }


    private fun startCamera() {
        if (camera == null || sf == null) {
            return
        }
        val cameraInfo = Camera.CameraInfo()
        Camera.getCameraInfo(Camera.CameraInfo.CAMERA_FACING_BACK, cameraInfo)

        val ps = camera!!.parameters
        val pr = ps.supportedPreviewSizes
        val pp = ps.supportedPictureSizes


        val ratio = pr[pr.size - 1].width.toFloat() / pr[pr.size - 1].height
        val heiView = ratio * ttCamera.width
        val weiView = ttCamera.width
        ttCamera.layoutParams.height = heiView.toInt() - 130
        ttCamera.layoutParams = ttCamera.layoutParams
        ttCamera.requestLayout()

        ps.setPreviewSize(pr[pr.size - 1].width, pr[pr.size - 1].height)
        ps.setPictureSize(pp[pp.size - 1].width, pp[pp.size - 1].height)
//        camera?.getParameters()?.getSupportedPictureFormats()
        camera?.parameters = ps

        camera?.setPreviewTexture(sf)
        camera?.startPreview()

    }

    override fun onSurfaceTextureAvailable(sf: SurfaceTexture, w: Int, h: Int) {
        //sãn sàng nhận dữ liệu từ camera đổ lên
        this.sf = sf
        startCamera()
    }

    override fun onSurfaceTextureSizeChanged(sf: SurfaceTexture, w: Int, h: Int) {
        this.sf = sf
        startCamera()
    }

    override fun onSurfaceTextureDestroyed(p0: SurfaceTexture): Boolean {
        camera = null
        return true
    }

    override fun onSurfaceTextureUpdated(sf: SurfaceTexture) {

    }


    override fun onResume() {
        super.onResume()
        if (checkCamerePermission()) {
            openCamera()
        }
    }

    private fun openCamera() {
        if (camera != null) {
            return
        }
        camera = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK)
        startCamera()
    }

    override fun onPause() {
        super.onPause()
        if (camera != null) {
            camera?.stopPreview()
            camera?.release()
            camera = null
        }
    }

    override fun onClick(view: View) {
        if (camera == null) {
            return
        }
//        data: mang buy chua anh
        camera?.takePicture(null, null, null, object : Camera.PictureCallback{
            override fun onPictureTaken(data: ByteArray?, c: Camera?) {
                val pathFile = Environment.getExternalStorageDirectory().path + "/" +
                        Environment.DIRECTORY_PICTURES + "/" + Date().time.toString() + ".jpg"
                val bm = BitmapFactory.decodeByteArray(data, 0, data!!.size)
                val out = FileOutputStream(pathFile)
                bm.compress(Bitmap.CompressFormat.JPEG, 100, out)
                out.close()
                camera?.stopPreview()
                camera?.release()
                camera = null
                openCamera()
                Toast.makeText(requireContext(), "Finish save", Toast.LENGTH_LONG).show()
            }
        })
    }


}