package com.example.practica17_canvasii

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import java.util.ArrayList

class DrawView @JvmOverloads constructor(context: Context?, attrs: AttributeSet? = null) :
    View(context, attrs) {
    private var mX = 0f
    private var mY = 0f
    private lateinit var mPath: Path
    /* la clase Paint encapsula el color e información de estilo sobre
        cómo dibujar las geometrías, el texto y los mapas de bits*/
    private val mPaint: Paint

    /*ArrayList para almacenar todos los trazos dibujado por el usuario en el lienzo*/
    private val paths = ArrayList<Stroke>()
    private var currentColor = 0
    private var strokeWidth = 0
    private lateinit var mBitmap: Bitmap
    private lateinit var mCanvas: Canvas
    private val mBitmapPaint = Paint(Paint.DITHER_FLAG)
    //este método instancia el mapa de bits y el objeto
    fun init(height: Int, width: Int) {
        mBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        mCanvas = Canvas(mBitmap)
        // establecer un color inicial del pincel
        currentColor = Color.GREEN
        // establecer un tamaño de pincel inicial
        strokeWidth = 20
    }
    // establece el color actual del trazo
    fun setColor(color: Int) {
        currentColor = color
    }
    // establece el ancho del trazo
    fun setStrokeWidth(width: Int) {
        strokeWidth = width
    }
    fun undo() {
        /* comprobar si la lista está vacía o no si está vacío, el
           método de eliminación devolverá un error*/
        if (paths.size != 0) {
            paths.removeAt(paths.size - 1)
            invalidate()
        }
    }
    // este método devuelve el mapa de bits actual
    fun save(): Bitmap {
        return mBitmap
    }
    /* este es el método principal donde el dibujo real tiene lugar*/
    override fun onDraw(canvas: Canvas) {
        /* guarda el estado actual del lienzo antes, para dibujar el fondo del lienzo*/
        canvas.save()
        // Color PREDETERMINADO del lienzo
        val backgroundColor = Color.WHITE
        mCanvas.drawColor(backgroundColor)
        /* ahora, iteramos sobre la lista de rutas
           y dibuja cada ruta en el lienzo*/
        for (fp in paths) {
            mPaint.color = fp.color
            mPaint.strokeWidth = fp.strokeWidth.toFloat()
            mCanvas.drawPath(fp.path!!, mPaint)
        }
        canvas.drawBitmap(mBitmap, 0f, 0f, mBitmapPaint)
        canvas.restore()
    }
    /* los siguientes métodos gestionan el toque respuesta del usuario en pantalla
       en primer lugar, creamos un nuevo trazo y agregarlo a la lista de rutas*/
    private fun touchStart(x: Float, y: Float) {
        mPath = Path()
        val fp = Stroke(currentColor, strokeWidth, mPath)
        paths.add(fp)

        /* finalmente elimina cualquier curva o línea de la ruta */
        mPath.reset()

        /* este método establece el inicio punto de la línea que se dibuja*/
        mPath.moveTo(x, y)
        /* guardamos la corriente coordenadas del dedo*/
        mX = x
        mY = y
    }
    /* en este método comprobamos
       si el movimiento del dedo en la
       la pantalla es mayor que la
       Tolerancia que hemos definido previamente,
       luego llamamos al método quadTo () que
       en realidad suaviza los giros que creamos,
       calculando la posición media entre
       la posición anterior y la posición actual*/
    private fun touchMove(x: Float, y: Float) {
        val dx = Math.abs(x - mX)
        val dy = Math.abs(y - mY)
        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
            mPath.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2)
            mX = x
            mY = y
        }
    }
    /* al final, llamamos al método lineTo
       que simplemente dibuja la línea hasta
       la posición final*/
    private fun touchUp() {
        mPath.lineTo(mX, mY)
    }

    /* el método onTouchEvent () nos proporciona
       la información sobre el tipo de movimiento
       que ha tenido lugar, y de acuerdo
       a eso llamamos a nuestros métodos deseados*/
    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                touchStart(x, y)
                invalidate()
            }
            MotionEvent.ACTION_MOVE -> {
                touchMove(x, y)
                invalidate()
            }
            MotionEvent.ACTION_UP -> {
                touchUp()
                invalidate()
            }
        }
        return true
    }

    companion object {
        private const val TOUCH_TOLERANCE = 4f
    }
    // Constructores para inicializar todos los atributos
    init {
        mPaint = Paint()
        /* los siguientes métodos suavizan
            los dibujos del usuario*/
        mPaint.isAntiAlias = true
        mPaint.isDither = true
        mPaint.color = Color.GREEN
        mPaint.style = Paint.Style.STROKE
        mPaint.strokeJoin = Paint.Join.ROUND
        mPaint.strokeCap = Paint.Cap.ROUND

        // 0xff = 255 en décimas
        mPaint.alpha = 0xff
    }
}