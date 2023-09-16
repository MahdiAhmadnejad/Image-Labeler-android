package com.eye.imagelaberlecropperkotlin

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.squareup.picasso.Picasso


class CompleteActivity : AppCompatActivity() {
    private var dataString: String = ""
    private var userImage : ImageView? = null
    private var exportBtn : ConstraintLayout? = null
    private lateinit var autoCompleteTextView: AutoCompleteTextView
    private lateinit var autoCompleteName: AutoCompleteTextView
    private val SAVE_FILE_REQUEST_CODE = 42
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_complete)
        userImage = findViewById(R.id.image_final)
        exportBtn = findViewById(R.id.export_btn)




        //get intent data
        // Get the Intent that started this activity
        var intent = intent

        // Get the image Uri from the extra data
        val imageUri = intent.getStringExtra("imageUri")

        // Load the image from the Uri using Picasso library
        Picasso.with(this).load(imageUri).into(userImage)


        // Get the crop coordinates from the extra data
        val minX = intent.getIntExtra("minX", 0)
        val minY = intent.getIntExtra("minY", 0)
        val maxX = intent.getIntExtra("maxX", 0)
        val maxY = intent.getIntExtra("maxY", 0)
        val height = intent.getIntExtra("height", 0)
        val width = intent.getIntExtra("width", 0)
        val origWidth = intent.getIntExtra("orgWidth", 0)
        val origHeight = intent.getIntExtra("orgHeight", 0)
        val fileParentPath  = intent.getStringExtra("folder")
        val filePath  = intent.getStringExtra("path")
        val fileName  = intent.getStringExtra("fileName")
        //get intent data

        //textInput layout
        autoCompleteTextView = findViewById(R.id.autoComplete)
        autoCompleteName = findViewById(R.id.autoCompleteName)

        val utils = Utils()
        autoCompleteTextView.onItemClickListener =
            AdapterView.OnItemClickListener { parent, view, position, id ->

                //get text from user
                val objectName = autoCompleteName.text
                // Get selected item
                val selectedItem = parent.adapter.getItem(position) as String
                Log.d(TAG, "onCreate: Object Name Is $objectName")

                // Handle selection
                Toast.makeText(this, "Selected $selectedItem", Toast.LENGTH_SHORT).show()

                //Handle Export BTN
                exportBtn!!.setOnClickListener {
                    when (position) {
                        0 -> {
                            dataString = utils.generateXml(
                                folder = fileParentPath.toString(),
                                filename = fileName.toString(),
                                path = filePath.toString(),
                                database = "Unknown",
                                width = origWidth,
                                height = origHeight,
                                name = objectName.toString(),
                                xmin = minX,
                                ymin = minY,
                                xmax = maxX,
                                ymax = maxY
                            )
                            Log.d(TAG, "onCreate: xmlfile $dataString")
                            intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                                addCategory(Intent.CATEGORY_OPENABLE)
                                type = "text/xml" // mime type
                                putExtra(Intent.EXTRA_TITLE, "labels.xml") // filename
                            }
                            startActivityForResult(intent, SAVE_FILE_REQUEST_CODE)

                        }
                        1 -> {
                            dataString = utils.generateYoloFormat(
                                className = objectName.toString(),
                                minX = minX,
                                minY = minY,
                                maxX = maxX,
                                maxY = maxY,
                                imgWidth = origWidth,
                                imgHeight = origHeight)
                            Log.d(TAG, "onCreate: yoloFile is $dataString")
                            intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                                addCategory(Intent.CATEGORY_OPENABLE)
                                type = "text/plain" // YOLO export file type
                                putExtra(Intent.EXTRA_TITLE, "labels.txt") // YOLO export file name
                            }
                            startActivityForResult(intent, SAVE_FILE_REQUEST_CODE)
                        }
                        else -> {
                            Toast.makeText(this,"No Option is Selected!",Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }

        //textInput layout

        // Find the TextView in the layout
        val cordText = findViewById<TextView>(R.id.coordText)

        // Set the text to show the crop coordinates
        """
                Crop Cords: 
                (minX : $minX, minY : $minY)
                (maxX : $maxX, maxY : $maxY)
                Bounding Box Size:
                (width : $width, height : $height)
            """.trimIndent().also { cordText.text = it }

    }

    override fun onResume() {
        super.onResume()

        val options = resources.getStringArray(R.array.exportFormat)
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, options)

        autoCompleteTextView.setAdapter(adapter)
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        Log.d("TAG", "onActivityResult called")
        if(requestCode == SAVE_FILE_REQUEST_CODE && resultCode == RESULT_OK) {
            val uri = data?.data
            val outputStream = contentResolver.openOutputStream(uri!!)

            Log.d(TAG, "every thing is done $outputStream")

            outputStream?.write(dataString.toByteArray())
            outputStream?.close()
        }
        super.onActivityResult(requestCode, resultCode, data)
    }
}
