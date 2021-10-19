package com.example.practica17_canvasii

import android.Manifest
import android.content.ContentValues;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.widget.ImageButton;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.slider.RangeSlider;
import java.io.OutputStream;
import petrov.kristiyan.colorpicker.ColorPicker;
import petrov.kristiyan.colorpicker.ColorPicker.OnFastChooseColorListener
import java.lang.Exception
import android.app.Activity
import android.app.AlertDialog
import android.content.Context

import androidx.core.app.ActivityCompat

import android.content.pm.PackageManager

import androidx.core.content.ContextCompat

import android.os.Build
import android.content.DialogInterface
import android.widget.Toast


class MainActivity : AppCompatActivity() {
    /*creando el objeto de tipo DrawView
      para obtener la referencia de la Vista*/
    private lateinit var paint: DrawView

    // creando objetos de tipo botón
    private lateinit var save: ImageButton
    private lateinit var color: ImageButton
    private lateinit var stroke: ImageButton
    private lateinit var undo: ImageButton

    /* creando un objeto RangeSlider, que
       ayuda en la selección del ancho del trazo*/
    private lateinit var rangeSlider: RangeSlider

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // obteniendo la referencia de las vistas a partir de sus identificadores
        paint = findViewById<DrawView>(R.id.draw_view)
        rangeSlider = findViewById<RangeSlider>(R.id.rangebar)
        undo = findViewById<ImageButton>(R.id.btn_undo)
        save = findViewById<ImageButton>(R.id.btn_save)
        color = findViewById<ImageButton>(R.id.btn_color)
        stroke = findViewById<ImageButton>(R.id.btn_stroke)

        /* creando un OnClickListener para cada botón,
           para realizar determinadas acciones

           el botón deshacer eliminará la mayoría
           trazo reciente del lienzo*/
        undo.setOnClickListener { paint.undo() }
        if (checkPermissionREAD_EXTERNAL_STORAGE(this)) {
            /* el botón guardar guardará el actual
           lienzo que en realidad es un mapa de bits
           en forma de PNG, en el almacenamiento*/
            save.setOnClickListener {
                // obteniendo el mapa de bits de la clase DrawView
                val bmp = paint.save()

                // abriendo un OutputStream para escribir en el archivo
                var imageOutStream: OutputStream? = null
                val cv = ContentValues()

                // nombre del archivo
                cv.put(MediaStore.Images.Media.DISPLAY_NAME, "drawing.png")

                // tipo de archivo
                cv.put(MediaStore.Images.Media.MIME_TYPE, "image/png")

                // ubicación del archivo a guardar
                cv.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)

                // obtener el Uri del archivo que se va a crear en el almacenamiento
                val uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, cv)
                try {
                    /// abre el flujo de salida con el uri anterior
                    imageOutStream = contentResolver.openOutputStream(uri!!)

                    // este método escribe los archivos almacenados
                    bmp.compress(Bitmap.CompressFormat.PNG, 100, imageOutStream)

                    // cierra el flujo de salida después de su uso
                    imageOutStream!!.close()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        /* el botón de color permitirá al usuario
            para seleccionar el color de su pincel*/
        color.setOnClickListener {
            val colorPicker = ColorPicker(this@MainActivity)
            colorPicker.setOnFastChooseColorListener(object : OnFastChooseColorListener {
                override fun setOnFastChooseColorListener(
                    position: Int,
                    color: Int
                ) {
                    /* obtiene el valor entero de color
                       seleccionado del cuadro de diálogo y
                       establecerlo como color de trazo*/
                    paint.setColor(color)
                }

                override fun onCancel() {
                    colorPicker.dismissDialog()
                }
            })
                /* establecer el número de columnas de color
                   desea mostrar en el diálogo.*/
                .setColumns(5)
                /* establecer un color predeterminado seleccionado
                   en el diálogo*/
                .setDefaultColorButton(Color.parseColor("#000000"))
                .show()
        }
        // el botón alternará la visibilidad de RangeBar / RangeSlider
        stroke.setOnClickListener {
            if (rangeSlider.visibility == View.VISIBLE)
                rangeSlider.visibility = View.GONE
            else
                rangeSlider.visibility =
                View.VISIBLE
        }
        // establece el rango del RangeSlide
        rangeSlider.setValueFrom(0.0f);
        rangeSlider.setValueTo(100.0f);

        /* agregando un OnChangeListener que
           cambia el ancho del trazo
           tan pronto como el usuario desliza el control deslizante*/
        rangeSlider.addOnChangeListener(RangeSlider.OnChangeListener { slider, value, fromUser ->
            paint.setStrokeWidth(value.toInt())
        })
        /* pasar la altura y el ancho de la vista personalizada
           al método init del objeto DrawView*/
        val vto = paint.viewTreeObserver
        vto.addOnGlobalLayoutListener(object : OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                paint.viewTreeObserver.removeOnGlobalLayoutListener(this)
                val width = paint.measuredWidth
                val height = paint.measuredHeight
                paint.init(height, width)
            }
        })
    }
    val MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 123

    fun checkPermissionREAD_EXTERNAL_STORAGE(
        context: Context?
    ): Boolean {
        val currentAPIVersion = Build.VERSION.SDK_INT
        return if (currentAPIVersion >= Build.VERSION_CODES.M) {
            if (context?.let {
                    ContextCompat.checkSelfPermission(
                        it,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    )
                } != PackageManager.PERMISSION_GRANTED
            ) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(
                        (context as Activity?)!!,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    )
                ) {
                    showDialog(
                        "External storage", context,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    )
                } else {
                    ActivityCompat
                        .requestPermissions(
                            (context as Activity?)!!,
                            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                            MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE
                        )
                }
                false
            } else {
                true
            }
        } else {
            true
        }
    }
    fun showDialog(
        msg: String, context: Context?,
        permission: String
    ) {
        val alertBuilder: AlertDialog.Builder = AlertDialog.Builder(context)
        alertBuilder.setCancelable(true)
        alertBuilder.setTitle("Permission necessary")
        alertBuilder.setMessage("$msg permission is necessary")
        alertBuilder.setPositiveButton(android.R.string.yes,
            DialogInterface.OnClickListener { dialog, which ->
                ActivityCompat.requestPermissions(
                    (context as Activity?)!!, arrayOf(permission),
                    MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE
                )
            })
        val alert: AlertDialog = alertBuilder.create()
        alert.show()
    }



    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE -> if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // do your stuff
            } else {
                Toast.makeText(
                    this@MainActivity, "GET_ACCOUNTS Denied",
                    Toast.LENGTH_SHORT
                ).show()
            }
            else -> super.onRequestPermissionsResult(
                requestCode, permissions!!,
                grantResults
            )
        }
    }
}

