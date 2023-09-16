package com.eye.imagelaberlecropperkotlin

import android.content.ContentValues.TAG
import android.content.Intent
import android.graphics.Rect
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE
import java.io.File

class ShowLabel : AppCompatActivity() {
    private var imageUri: Uri? = null
    private var textView: TextView? = null
    private var labeledImage : ImageView? = null
    private var pathxml = ""
    private var xmlN = ""
    private var xmlP = ""
    lateinit var rect: Rect // Add this
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_show_label)
        //image of user labeled
        labeledImage = findViewById(R.id.imagelabled)
        textView = findViewById(R.id.textView)

        textView?.visibility = View.VISIBLE

        // Get the Intent that started this activity
        val intent = getIntent()

        //get xml string
        val xmlString = intent.getStringExtra("xmlString")
        val xmlName = intent.getStringExtra("xmlName")
        val xmlPath = intent.getStringExtra("xmlPath")
        xmlN = xmlName.toString()
        xmlP = xmlPath.toString()
        Log.d(TAG, "onCreate: $xmlString")
        // Regex to find text between <folder> and </folder> tags
        val folderRegex = "<folder>(.*?)</folder>".toRegex()
        val match = xmlString?.let { folderRegex.find(it) }
        val folder = match?.groupValues?.get(1)
        //filename
        val nameRegex = "<filename>(.*?)</filename>".toRegex()
        val namematch = xmlString?.let { nameRegex.find(it) }
        val name = namematch?.groupValues?.get(1)
        //filepath
        val pathRegex = "<path>(.*?)</path>".toRegex()
        val pathmatch = xmlString?.let { pathRegex.find(it) }
        var path = pathmatch?.groupValues?.get(1)
        path += "/"
        //xmin
        val xminRegex = "<xmin>(.*?)</xmin>".toRegex()
        val xminMatch = xmlString?.let { xminRegex.find(it) }
        val xmin = xminMatch?.groupValues?.get(1)
        //ymin
        val yminRegex = "<ymin>(.*?)</ymin>".toRegex()
        val yminMatch = xmlString?.let { yminRegex.find(it) }
        val ymin = yminMatch?.groupValues?.get(1)
        //xmax
        val xmaxRegex = "<xmax>(.*?)</xmax>".toRegex()
        val xmaxMatch = xmlString?.let { xmaxRegex.find(it) }
        val xmax = xmaxMatch?.groupValues?.get(1)
        //ymax
        val ymaxRegex = "<ymax>(.*?)</ymax>".toRegex()
        val ymaxMatch = xmlString?.let { ymaxRegex.find(it) }
        val ymax = ymaxMatch?.groupValues?.get(1)

        // Convert strings to integers
        val xminNum = xmin?.toInt()
        val yminNum = ymin?.toInt()
        val xmaxNum = xmax?.toInt()
        val ymaxNum = ymax?.toInt()

        //convert path to URI string
        imageUri = Uri.fromFile(File(path,name))
        Log.d(TAG, "image uri : $imageUri")
        Log.d(TAG, "address is: $path$name")


        // Create Rect object
        rect = Rect(xminNum ?: 0, yminNum ?: 0, xmaxNum ?: 0, ymaxNum ?: 0)
        startCropActivity()

        Log.d(TAG, "XML is : $folder $name $path $xmin $ymin $xmax $ymax")
        Log.d(TAG, "rect is : $rect")

    }
    private fun startCropActivity() {
        try {
            CropImage.activity(imageUri)
                .setCropMenuCropButtonTitle("CHANGE")
                .setActivityTitle("Image Labeled")
                .setAutoZoomEnabled(false)
                .setInitialCropWindowRectangle(rect)
                .start(this@ShowLabel)

        }catch(e: Exception){
            Log.d(TAG, "startCropActivity: $e")
        }
        
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK) {
            val result: CropImage.ActivityResult = CropImage.getActivityResult(data)
            val resultUri: Uri = result.getUri()
            labeledImage?.setImageURI(resultUri)
            textView?.visibility = View.GONE

            // Get the crop coordinates from the CropImage result
            val cropRect = result.getCropRect()
            val minX = cropRect.left
            val minY = cropRect.top
            val maxX = cropRect.right
            val maxY = cropRect.bottom

            Log.d(TAG, "onActivityResult: xmin: $minX ymin: $minY xmax: $maxX ymax: $maxY")

            // Open XML file for writing
            pathxml = xmlP + xmlN
            val file = File(pathxml)
            Log.d(TAG, "onActivityResult: the file path is $xmlP$xmlN")
            val bufferedReader = file.bufferedReader()
            val stringBuilder = StringBuilder()
            bufferedReader.forEachLine {
                stringBuilder.append(it).appendLine()
            }
            bufferedReader.close()

            val writer = file.bufferedWriter()

            // Find the bndbox tag
            val startIndex = stringBuilder.indexOf("<bndbox>")
            val endIndex = stringBuilder.indexOf("</bndbox>")

            // Replace the values of the bndbox tag
            val newBndbox =
                    "<bndbox>\n" +
                    "<xmin>$minX</xmin>\n" +
                    "<ymin>$minY</ymin>\n" +
                    "<xmax>$maxX</xmax>\n" +
                    "<ymax>$maxY</ymax>\n"
            stringBuilder.replace(startIndex, endIndex, newBndbox)

            // Write the updated contents to the file
            writer.append(stringBuilder)
            writer.close()
        }
    }
}
