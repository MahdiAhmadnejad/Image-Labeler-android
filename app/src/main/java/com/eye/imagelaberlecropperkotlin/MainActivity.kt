package com.eye.imagelaberlecropperkotlin

import android.content.ContentValues.TAG
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.theartofdev.edmodo.cropper.CropImage
import org.w3c.dom.Node
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory

class MainActivity : AppCompatActivity() {
    private var userpic :ImageView? = null
    private val STORAGE_REQUEST = 200
    var PICK_XML_REQUEST_CODE = 2
    private var storagePermission:Array<String>? = null
    private var click:TextView? = null
    private var importXML:TextView? = null
    var xmlString = "empty"

    //xml parser
    private fun parseXmlToString(uri: Uri) {
        val inputStream = contentResolver.openInputStream(uri)
        val builderFactory = DocumentBuilderFactory.newInstance()
        val documentBuilder = builderFactory.newDocumentBuilder()
        val document = documentBuilder.parse(inputStream)

        val output = StringBuilder()
        val rootNode = document.documentElement
        parseNode(rootNode, output)

        // Put the image Uri as an extra data
        xmlString = output.toString()
        Log.d(TAG, "parseXmlToString: $xmlString")
        // Use the xmlString as required

    }
    //parse node
    private fun parseNode(node: Node, output: StringBuilder) {
        when (node.nodeType) {
            Node.ELEMENT_NODE -> {
                output.append("<${node.nodeName}>")
                val childNodes = node.childNodes
                for (i in 0 until childNodes.length) {
                    parseNode(childNodes.item(i), output)
                }
                output.append("</${node.nodeName}>")
            }
            Node.TEXT_NODE -> {
                output.append(node.nodeValue)
            }
        }
    }
    //parse node

    //xml parser
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        click = findViewById(R.id.click)
        importXML = findViewById(R.id.importxml)
        userpic = findViewById(R.id.set_profile_image)

        /**ok run it*/
        storagePermission = arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
        click!!.setOnClickListener {
            showImagePicDialog()
        }

        importXML!!.setOnClickListener {
            if (!checkStoragePermission()){
                requestStoragePermission()
            } else{
                showFileXmlDialog()
            }
        }
    }
    private fun showFileXmlDialog() {
        // Intent to pick XML file
        PICK_XML_REQUEST_CODE = 23
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            setTitle("Pick XML file")
            type = "text/xml"
        }
        startActivityForResult(intent, PICK_XML_REQUEST_CODE)
    }


    private fun showImagePicDialog() {
        val builder = AlertDialog.Builder(this)
            .setTitle("Pick Image From")
            .setPositiveButton("Gallery"){dialog,_->
                if (!checkStoragePermission()){
                    requestStoragePermission()
                }else{
                    pickFromGallery()
                }
            }
        builder.create().show()
    }

    private fun checkStoragePermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,android.Manifest.permission.WRITE_EXTERNAL_STORAGE
        )== PackageManager.PERMISSION_GRANTED

    }

    private fun pickFromGallery() {
        CropImage.activity()
            .setCropMenuCropButtonTitle("Label")
            .start(this@MainActivity)
    }

    private fun requestStoragePermission() {
        requestPermissions(storagePermission!!,STORAGE_REQUEST)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array < String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == STORAGE_REQUEST){
            if (grantResults.size> 0) {
                val writeStoregeaccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED
                if (writeStoregeaccepted){
                    pickFromGallery()
                }else{
                    Toast.makeText(this,
                        "Please Enable Storage Permission",
                        Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    //test
    private fun getFileFromUri(uri: Uri): String? {
        var filePath: String? = null
        val projection = arrayOf(MediaStore.Images.Media.DATA)
        val cursor = contentResolver.query(uri, projection, null, null, null)
        cursor?.let {
            if (it.moveToFirst()) {
                val columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                filePath = cursor.getString(columnIndex)
            }
        }
        cursor?.close()
        return filePath
    }
    //test
    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        Log.d(TAG, "File data is: $data")
        Log.d(TAG, "resultCode is: $resultCode")
        Log.d(TAG, "requestCode is: $requestCode")
        //test

        fun getFilePathWithoutName(filePath: String): String {
            val fileNameIndex = filePath.lastIndexOf("/")
            return if (fileNameIndex >= 0) {
                filePath.substring(0, fileNameIndex) + "/0/Download/"
            } else {
                filePath + "/"
            }
        }

        //test

        //on xml file selected
        if (PICK_XML_REQUEST_CODE == 23 && resultCode == RESULT_OK) {
            data?.data?.let { uri ->
                parseXmlToString(uri)
                Log.d(TAG, "onActivityResult: xml uri address: $uri")
//                val uri = "content://com.android.providers.downloads.documents/document/39"
                val context = this
                val contentResolver = context.contentResolver
                val cursor = contentResolver.query(uri, arrayOf(MediaStore.Downloads.DISPLAY_NAME, MediaStore.Downloads.DATA), null, null, null)
                var fileName: String? = null
                var pathWithoutName: String? = null
                if (cursor != null && cursor.moveToFirst()) {
                    val nameColumnIndex = cursor.getColumnIndex(MediaStore.Downloads.DISPLAY_NAME)
                    val pathColumnIndex = cursor.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME)
                    Log.d(TAG, "onActivityResult: column path is: $pathColumnIndex  column name is: $nameColumnIndex")
                    if (nameColumnIndex >= 0) {
                        fileName = cursor.getString(nameColumnIndex)
                    }
                    if (pathColumnIndex >= 0) {
                        val filePath = cursor.getString(pathColumnIndex)
                        val fullPath = Environment.getExternalStorageDirectory().toString() + filePath!!
                        pathWithoutName = getFilePathWithoutName(fullPath)
                        Log.d(TAG, "onActivityResult: full path is: $fullPath and path without name is: $pathWithoutName")
                    }
                    cursor.close()
                }
                Log.d(TAG, "onActivityResult: fileName: $fileName filePath: $pathWithoutName")
                Toast.makeText(this,"xml file is successfully parsed",Toast.LENGTH_LONG).show()
                val intent = Intent(this, ShowLabel::class.java)
                //send to ShowLabel
                intent.putExtra("xmlString", xmlString)
                intent.putExtra("xmlName", fileName)
                intent.putExtra("xmlPath", pathWithoutName)
                startActivity(intent)
            }
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE  && data!=null) {
            val result: CropImage.ActivityResult = CropImage.getActivityResult(data)
            val originalUri = result.originalUri
            Log.d(TAG, "Original Image URI: $originalUri")
            // Decode original image
            val origOpts = BitmapFactory.Options()
            Log.d(TAG, "origOpts URI: $origOpts")
            val originalStream = originalUri?.let { contentResolver.openInputStream(it) }
            Log.d(TAG, "Original stream URI: $originalStream")

            BitmapFactory.decodeStream(originalStream, null, origOpts)
            val origWidth = origOpts.outWidth
            val origHeight = origOpts.outHeight
            if (resultCode == RESULT_OK) {

                val imageUri = result.uri // Get the image URI
                val cropname = imageUri.lastPathSegment
                Log.d(TAG, "Image URI: $imageUri")
                Log.d(TAG, "cropname: $cropname")

                val opts = BitmapFactory.Options()
                opts.inJustDecodeBounds = true
                BitmapFactory.decodeFile(imageUri.path, opts)
                val width = opts.outWidth
                val height = opts.outHeight



                Log.d(TAG, "File original uri is: $originalUri")


                val fileName: String?
                var fileParentPath: String? = "null"
                var filePath: String? = "null"
                var fileNameOnly: String? = "null"
                val file: File

                if (originalUri != null) {
                    //Get file name
                    fileName = getFileFromUri(originalUri)
                    Log.d(TAG, "File result is: $fileName")
                    if(fileName != null) {
                        file = File(fileName)
                        Log.d(TAG, "File is: $file")
                    }else {
                        file = File("It is Not Saved So... No Address Available \n Just Rect Address is there")
                        Toast.makeText(this, "$file",Toast.LENGTH_LONG).show()
                        Log.d(TAG, "onActivityResult: File name is null")
                    }
                    fileParentPath = file.parentFile?.name
                    filePath = file.parent
                    fileNameOnly = file.name
                    Log.d(TAG, "Filename is: $fileNameOnly")
                    Log.d(TAG, "File Parent Path is: $fileParentPath")
                    Log.d(TAG, "File Path is: $filePath")
                } else {
                    Log.e(TAG, "Original URI is null")
                    Toast.makeText(this,
                        "For Camera Images Don't Forget to Save",
                        Toast.LENGTH_SHORT).show()
                }

                val resultUri: Uri = result.getUri()

                // Create an Intent to start the second activity
                val intent = Intent(this, CompleteActivity::class.java)

                // Put the image Uri as an extra data
                intent.putExtra("imageUri", resultUri.toString())


                // Get the crop coordinates from the CropImage result
                val cropRect = result.getCropRect()
                val minX = cropRect.left
                val minY = cropRect.top
                val maxX = cropRect.right
                val maxY = cropRect.bottom



//                // Start the second activity
//                val xml = utils.generateXml(
//                    folder = fileParentPath.toString(),
//                    filename = fileNameOnly.toString(),
//                    path = filePath.toString(),
//                    database = "Unknown",
//                    width = origWidth,
//                    height = origHeight,
//                    name = cropname.toString(),
//                    xmin = minX,
//                    ymin = minY,
//                    xmax = maxX,
//                    ymax = maxY)
//                Log.d("XML", xml)
                // Put the crop coordinates as extra data
                Log.d(TAG, "onCreate: path $filePath parentpath $fileParentPath filename $fileNameOnly")

                intent.putExtra("minX", minX)
                intent.putExtra("minY", minY)
                intent.putExtra("maxX", maxX)
                intent.putExtra("maxY", maxY)
                intent.putExtra("height", height)
                intent.putExtra("orgHeight", origHeight)
                intent.putExtra("width", width)
                intent.putExtra("orgWidth", origWidth)
                intent.putExtra("folder", fileParentPath)
                intent.putExtra("path", filePath)
                intent.putExtra("fileName", fileNameOnly)

                try {
                    startActivity(intent)
                }catch (e: Exception){
                    Log.d(TAG, "onActivityResult: $e")
                }
            }
        }
    }
}

