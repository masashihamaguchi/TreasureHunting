package com.picopico.treasurehunting

import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class DetailActivity : AppCompatActivity() {

    private var _imageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)

        val button = findViewById<Button>(R.id.button)
        button.setOnClickListener {
            val dateFormat = SimpleDateFormat("yyyyMMddHHmmss")
            val values = ContentValues()
            _imageUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)

            val now = Date()
            val nowStr = dateFormat.format(now)
            val fileName = "CameraIntentPhoto_${nowStr}.jpg"
            values.put(MediaStore.Images.Media.TITLE, fileName)
            values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

            intent.putExtra(MediaStore.EXTRA_OUTPUT, _imageUri)

            startActivityForResult(intent, 200)
        }
    }

    // startActivityForResult()による遷移先アクティビティでの処理終了後、
    // 遷移元アクティビティで呼び出される処理(=コールバック処理)
    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        val imageView = findViewById<ImageView>(R.id.imageView)

        // リクエストコードが一致し、かつ正常に処理が終了していた場合の処理
        if(requestCode == 200 && resultCode == AppCompatActivity.RESULT_OK) {
            imageView.setImageURI(_imageUri)
        }
        checkLabel()
    }

    private fun checkLabel() {
        val image: InputImage
        try {
            image = InputImage.fromFilePath(this, _imageUri)
            val labeler = ImageLabeling.getClient(ImageLabelerOptions.DEFAULT_OPTIONS)
            labeler.process(image)
                .addOnSuccessListener { labels ->
                    // Task completed successfully
                    for (label in labels) {
                        val text = label.text
                        val confidence = label.confidence
                        val index = label.index
                        Log.d("tagtag", "$text$confidence  $index")
                    }
                }
                .addOnFailureListener { e ->
                    // Task failed with an exception
                    Log.e("tagtag", "connection with ml kit failed")
                }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}