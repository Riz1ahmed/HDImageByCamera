package com.example.getfullsizeimagefromcamera

import android.Manifest
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.nabinbhandari.android.permissions.PermissionHandler
import com.nabinbhandari.android.permissions.Permissions
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


class MainActivity : AppCompatActivity() {

    val CAMERA_REQUEST=123
    val REQUEST_TAKE_PHOTO = 1
    private lateinit var cameraUri:Uri
    lateinit var openCamera: Button
    lateinit var preview: ImageView
    lateinit var size: TextView

    lateinit var currentPhotoPath: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        openCamera=findViewById(R.id.openCamera)
        preview=findViewById(R.id.preview)
        size=findViewById(R.id.size)

        openCamera.setOnClickListener {
            val perms=arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            Permissions.check(this, perms, null, null, object : PermissionHandler() {
                override fun onGranted() {
                    dispatchTakePictureIntent()
                    Log.d("Riz1", currentPhotoPath)
                    setImage(currentPhotoPath)
                }
            })
        }
    }

    private fun dispatchTakePictureIntent() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            // Ensure that there's a camera activity to handle the intent
            takePictureIntent.resolveActivity(packageManager)?.also {
                // Create the File where the photo should go
                val photoFile: File? = try {
                    createImageFile()
                } catch (ex: IOException) {
                    // Error occurred while creating the File
                    null
                }
                // Continue only if the File was successfully created
                photoFile?.also {
                    val photoURI: Uri = FileProvider.getUriForFile(this,
                        "${BuildConfig.APPLICATION_ID}.fileprovider", it)
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO)
                }
            }
        }
    }
    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile("JPEG_${timeStamp}_", ".jpg", storageDir)
            .apply {
            // Save a file: path for use with ACTION_VIEW intents
            currentPhotoPath = absolutePath
        }
    }

    private fun setImage(path: String) {
        findViewById<TextView>(R.id.link).text=path
        Glide.with(this).load(path).into(preview)

        Glide.with(this).asBitmap().load(path).into(object: CustomTarget<Bitmap>(){
            override fun onResourceReady(bitmap: Bitmap, transition: Transition<in Bitmap>?) {
                val height=bitmap.height
                val wight=bitmap.width
                findViewById<TextView>(R.id.size).text="$height x $wight"
            }
            override fun onLoadCleared(placeholder: Drawable?) {}
        })
    }
}