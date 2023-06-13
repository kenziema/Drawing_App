package com.ken.drawingapp

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View

class DrawingView(context : Context, attrs : AttributeSet) : View(context, attrs) {


    private var mDrawPath : CustomPath? = null // A variable of CustomPath inner class to use it further
    private var mCanvasBitmap : Bitmap? = null // An instance of the bitmap
    private var mDrawPaint : Paint? = null // The paint class holds the style and color information about how to draw
    private var mCanvasPaint : Paint? = null // An instance of canvas paint view
    private var mBrushSize : Float = 0.toFloat() // A variable for stroke/brush size to draw on the canvas
    private var color = Color.BLACK // A variable to hold a color of the stroke
    /*
        A variable for canvas which will be initialized later and used.

        The canvas class holds the "draw" calls. To draw something, we need 4 basic components
        the draw calls (writing into the bitmap), a drawing primitive (e.g. Rectangle, Path,
        text, bitmap), and a paint (to describe the colors and styles for the drawing)
     */
    private var canvas : Canvas? = null

    private val mPaths = ArrayList<CustomPath>() // Arraylist for paths
    private val mUndoPaths = ArrayList<CustomPath>() // Arraylist for undo-ing paths
    private val mRedoPaths = ArrayList<CustomPath>()

    init{
        setupDrawing()
    }

    fun onClickUndo(){
        if(mPaths.isEmpty()) return
            mUndoPaths.add(mPaths.removeLast())
            invalidate()

    }

    fun onClickRedo(){
        if(mUndoPaths.isEmpty()) return
            mPaths.add(mUndoPaths.removeLast())
            invalidate()

    }


    /*
        This method initializes the attributes of the ViewForDrawing class
     */

    private fun setupDrawing(){
        mDrawPaint = Paint()
        mDrawPath = CustomPath(color, mBrushSize)
        // this color needs a non asserted call or (!!)
        mDrawPaint!!.color = color
        mDrawPaint!!.style = Paint.Style.STROKE
        mDrawPaint!!.strokeJoin = Paint.Join.ROUND
        mDrawPaint!!.strokeCap = Paint.Cap.ROUND
        mCanvasPaint = Paint(Paint.DITHER_FLAG)
        
//        mBrushSize = 20.toFloat()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w,h,oldw,oldh)
        mCanvasBitmap  = Bitmap.createBitmap(w,h, Bitmap.Config.ARGB_8888)
        canvas = Canvas(mCanvasBitmap!!)
        // use '!!' when a variable has no type and it's a null
    }

    // Change canvas to Canvas? if fails

    /*
        This method is called when a stroke is drawn on the canvas as a part of the painting
     */
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        /*
            Draw the specified bitmap, with its top/left corner at (x,y), using the specified
            transformed by the current matrix

            if the bitmap and canvas have different densities, this function will take care of it
            automatically by scaling the bitmap to draw at the same density as the canvas

            @param bitmap The bitmap to be drawn
            @param left The position of the left side of the bitmap being drawn
            @param right The position of the right side of the bitmap being drawn
            @param paint The paint used to draw the bitmap (which may be null)
         */
        canvas.drawBitmap(mCanvasBitmap!!, 0f,0f, mCanvasPaint)

        for(path in mPaths){
            mDrawPaint!!.strokeWidth = path.brushThickness
            mDrawPaint!!.color = path.color
            canvas.drawPath(path, mDrawPaint!!)
        }

        if(!mDrawPath!!.isEmpty){
            mDrawPaint!!.strokeWidth = mDrawPath!!.brushThickness
            mDrawPaint!!.color = mDrawPath!!.color
            canvas.drawPath(mDrawPath!!, mDrawPaint!!)
        }
    }

    /*
        This method acts as an event listener when a touch
        event is detected on the device
     */
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        val touchX = event?.x
        val touchY = event?.y

        when(event?.action){
            MotionEvent.ACTION_DOWN ->{
                /*
                    this LoC means that when we press on the screen
                    we want to set the mirror path color to be the
                    correct color, indoor path thickness
                 */
                mDrawPath!!.color = color
                mDrawPath!!.brushThickness = mBrushSize


                /*
                    The LoC below is an alternative method to
                    set the touchX and touchY which it's a nullable
                 */
                mDrawPath!!.reset()
                if (touchX != null) {
                    if (touchY != null) {
                        mDrawPath!!.moveTo(touchX, touchY)
                    }
                }
            }
            MotionEvent.ACTION_MOVE ->{
                mDrawPath!!.lineTo(touchX!!, touchY!!)
            }

            MotionEvent.ACTION_UP ->{
                mPaths.add(mDrawPath!!)
                mDrawPath = CustomPath(color, mBrushSize)
            }
            else -> return false
        }
        invalidate()

        return true
    }

    /*
        The reason why it uses data type Float instead of Integer is because
        values on the screen are using floats
    */
    fun setSizeForBrush(newSize : Float){
        /*
            Why we uses COMPLEX_UNIT_DIP is because we wanted to scale the brush size,
            instead using SP which only scales the textSize and not any other
            UI properties.

            COMPLEX_UNIT_DIP = scale any UI properties, such as buttons, images and layouts.
         */
        mBrushSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                                                newSize,
                                                resources.displayMetrics)
        mDrawPaint!!.strokeWidth = mBrushSize
    }

    fun setColor(newColor: String){
        color = Color.parseColor(newColor)
        mDrawPaint!!.color = color
    }

    internal inner class CustomPath(var color: Int,
                                    var brushThickness : Float) : Path() {

    }

}