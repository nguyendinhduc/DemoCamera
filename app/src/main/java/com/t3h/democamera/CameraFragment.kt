package com.t3h.democamera

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.graphics.SurfaceTexture
import android.hardware.Camera
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.view.*
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
        setOrientationCamera(cameraInfo)
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

    private fun setOrientationCamera(cameraInfo: Camera.CameraInfo) {
        //huong giao dien
        val rotation: Int = requireActivity().getWindowManager().getDefaultDisplay()
            .getRotation()
        var degrees = 0
        when (rotation) {
            Surface.ROTATION_0 -> degrees = 0
            Surface.ROTATION_90 -> degrees = 90
            Surface.ROTATION_180 -> degrees = 180
            Surface.ROTATION_270 -> degrees = 270
        }
        var result: Int
        if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (cameraInfo.orientation + degrees) % 360
            result = (360 - result) % 360 // compensate the mirror
        } else {  // back-facing
            result = (cameraInfo.orientation - degrees + 360) % 360
        }
        //set huong cho camera
        camera?.setDisplayOrientation(result)
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
        camera?.takePicture(null, object : Camera.PictureCallback{
            override fun onPictureTaken(data: ByteArray?, p1: Camera?) {
                if (data == null){
                    return
                }
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
        }, object : Camera.PictureCallback{
            override fun onPictureTaken(data: ByteArray?, p1: Camera?) {
                val pathFile = Environment.getExternalStorageDirectory().path + "/" +
                        Environment.DIRECTORY_PICTURES + "/" + Date().time.toString() + ".jpg"
                val bm = BitmapFactory.decodeByteArray(data, 0, data!!.size)
//                quay anh
                val matrix = Matrix()
                matrix.postRotate(90.toFloat())
                val bmRotate = Bitmap.createBitmap(bm, 0, 0, bm.width, bm.height, matrix, true)
                bm.recycle()

                val out = FileOutputStream(pathFile)
                bmRotate.compress(Bitmap.CompressFormat.JPEG, 100, out)
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