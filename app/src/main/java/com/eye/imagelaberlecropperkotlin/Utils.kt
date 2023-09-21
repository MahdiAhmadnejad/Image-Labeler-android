package com.eye.imagelaberlecropperkotlin

import android.util.Log

class Utils {
    //method for xml generation
    fun generateXml(

        folder: String,
        filename: String,
        path: String,
        database: String,
        width: Int,
        height: Int,
        name: String,
        xmin: Float,
        ymin: Float,
        xmax: Float,
        ymax: Float
    ): String {
        //xml codes detail
        val xmlfile = """
    <annotation>
      <folder>$folder</folder>
      <filename>$filename</filename>
      <path>$path</path>
      <source>
        <database>$database</database>
      </source>
      <size>
        <width>$width</width>  
        <height>$height</height>
        <depth>3</depth>
      </size>
      <segmented>0</segmented>
      <object>
        <name>$name</name>
        <pose>Unspecified</pose> 
        <truncated>0</truncated>
        <difficult>0</difficult>
        <bndbox>
           <xmin>$xmin</xmin>
           <ymin>$ymin</ymin>
           <xmax>$xmax</xmax>
           <ymax>$ymax</ymax>
        </bndbox>
      </object>
    </annotation> 
  """.trimIndent()
// Log the xml string with debug level
        Log.d("Utils", "Generated xml: $xmlfile")

        return xmlfile
    }
    fun generateYoloFormat(
        className: String,
        minX: Float,
        minY: Float,
        maxX: Float,
        maxY: Float,
        imgWidth: Int,
        imgHeight: Int
    ): String {

        // Calculate center x, y, width, height
        val x = (minX + maxX) / (2.0 * imgWidth)
        val y = (minY + maxY) / (2.0 * imgHeight)

        val width = (maxX - minX) / imgWidth
        val height = (maxY - minY) / imgHeight

        // Generate string
        return "$className $x $y $width $height"
    }
}