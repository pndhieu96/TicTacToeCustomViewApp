package com.example.tictactoecustomviewapp.CustomView

import android.animation.ValueAnimator
import android.content.Context
import android.content.res.TypedArray
import android.graphics.*
import android.text.TextPaint
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import androidx.constraintlayout.widget.ConstraintSet.Motion
import androidx.core.content.ContextCompat
import com.example.tictactoecustomviewapp.Object.Player
import com.example.tictactoecustomviewapp.Object.TicTacToeItem
import com.example.tictactoecustomviewapp.R
import kotlin.math.log

class TicTacToeView : View {
    private val density = resources.displayMetrics.density

    private val rows = 3
    private val cols = 3
    private val maxIndex = 2
    private var matrix = Array(rows) { Array(cols) { TicTacToeItem(Rect(), Player.NONE) } }
    private var floatWith = width.toFloat()
    private var floatHeight = floatWith
    private var floatWithOfAItem = 1f/3 * floatWith

    private val textPaint = TextPaint()
    private val paint: Paint = Paint()
    private val highlightPaint = Paint()
    private val winLinetPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private var isFirst = true
    private var firstPlayer = Player.X

    private val path = Path()
    private var shouldAnimate = false

    constructor(context: Context) : super(context) {
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.TicTacToeView)
        init(typedArray)
    }

    //AttributeSet: defining specific properties of a custom view in resource file (attrs.xml)
    //defStyleAttr: applying default attributes of view's style from XML in resource file (attrs.xml)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.TicTacToeView)
        init(typedArray)
    }

    private fun init(typedArray: TypedArray) {
        // Initialize any setup here
        val strokeColor = typedArray.getColor(R.styleable.TicTacToeView_TicTacToeView_stroke_color, Color.BLACK)
        typedArray.recycle()

        paint.color = strokeColor
        paint.strokeWidth = 5f
        paint.strokeWidth = density * 3

        highlightPaint.color = ContextCompat.getColor(context, R.color.highlight_color)
        highlightPaint.style = Paint.Style.FILL
        highlightPaint.isAntiAlias = true

        winLinetPaint.style = Paint.Style.STROKE;
        winLinetPaint.color = Color.RED;
        winLinetPaint.strokeWidth = 5f;
    }

    override fun onDraw(canvas: Canvas) {
        Log.d("TicTacToeView","onDraw")
        if(isFirst) {
            floatWith = width.toFloat()
            floatHeight = floatWith
            floatWithOfAItem = 1f/3*floatWith
            saveItems()
            isFirst = false
        }

        if(touching) {
            drawHighLightRectangle(canvas)
        }

        if(shouldAnimate) {
            canvas.drawPath(path, winLinetPaint)
        }

        drawVerticalLines(canvas)
        drawHorizontalLines(canvas)
        drawMatrix(canvas)
    }

    private fun drawVerticalLines(canvas: Canvas) {
        canvas.drawLine(0f,0f,0f, floatHeight, paint)
        canvas.drawLine((1f/3)*floatWith,0f,(1f/3)*floatWith, floatHeight, paint)
        canvas.drawLine((2f/3)*floatWith,0f,(2f/3)*floatWith, floatHeight, paint)
        canvas.drawLine(floatWith,0f,floatWith, floatHeight, paint)
    }

    private fun drawHorizontalLines(canvas: Canvas) {
        canvas.drawLine(0f,0f,floatWith, 0f, paint)
        canvas.drawLine(0f,(1f/3)*floatHeight,floatWith, (1f/3)*floatHeight, paint)
        canvas.drawLine(0f,(2f/3)*floatHeight,floatWith, (2f/3)*floatHeight, paint)
        canvas.drawLine(0f,floatHeight,floatWith, floatHeight, paint)
    }

    private fun saveItems() {
        for (i in 0 until rows) {
            for (j in 0 until cols) {
                matrix[i][j] = calculateCoordinatorOfItem(i,j)
            }
        }
    }

    private fun calculateCoordinatorOfItem(positionRow: Int, positionCol: Int): TicTacToeItem {
        val coordinatorLeftX = positionCol * floatWithOfAItem
        val coordinatorTopY = positionRow * floatWithOfAItem

        val coordinatorRightX = coordinatorLeftX + floatWithOfAItem
        val coordinatorBottomY = coordinatorTopY + floatWithOfAItem

        val rect = Rect(
            coordinatorLeftX.toInt(),
            coordinatorTopY.toInt(),
            coordinatorRightX.toInt(),
            coordinatorBottomY.toInt()
        )

        return TicTacToeItem(rect, Player.NONE)
    }

    /*
    * textPaint.measureText: calculate text with
    * textPaint.fontMetrics.ascent: calculate text height base on baseline
    * */
    private fun drawTextInsideRectangle(canvas: Canvas, positionRow: Int, positionCol: Int, player: Player) {
        val item = matrix[positionRow][positionCol]
        val textSizeInPixels = resources.getDimension(R.dimen.text_size_player)
        item.player = player
        textPaint.textSize = textSizeInPixels
        val xOffset = textPaint.measureText(item.player.symbol) * 0.5f
        val yOffset = textPaint.fontMetrics.ascent * (-0.5f)
        val textX = (item.rect.exactCenterX()) - xOffset
        val textY = (item.rect.exactCenterY()) + yOffset
        canvas.drawText(item.player.symbol, textX, textY, textPaint)
    }

    private fun updateMatrix(index: Pair<Int,Int>) {
        val item = matrix[index.first][index.second]
        if(item.player == Player.NONE) {
            item.player = firstPlayer

            firstPlayer = if(firstPlayer == Player.O)
            {
                Player.X
            } else {
                Player.O
            }
        }

        val winList = checkForWin()
        if(winList[0] != -1) {
            animateWin(winList[0],winList[1],winList[2],winList[3])
        }
    }

    private fun drawMatrix(canvas: Canvas){
        for (i in 0 until rows) {
            for (j in 0 until cols) {
                drawTextInsideRectangle(canvas,i,j, matrix[i][j].player)
            }
        }
    }

    private fun drawHighLightRectangle(canvas: Canvas) {
        canvas.drawRect(matrix[touchedRectPair.first][touchedRectPair.second].rect, highlightPaint)
    }

    private fun getIndexFor(x: Float, y:Float) : Pair<Int, Int> {
        for (i in 0 until rows) {
            for (j in 0 until cols) {
                val rect = matrix[i][j].rect
                if(rect.contains(x.toInt(), y.toInt())) {
                    return Pair(i,j)
                }
            }
        }
        return Pair(-1,-1)
    }

    fun animateWin(x1: Int, y1: Int, x3: Int, y3: Int) {
        val valueAnimator = ValueAnimator.ofFloat(1f, 0f)
        val winCoordinates = arrayOf(x1, y1, x3, y3)

        if (winCoordinates[0] < 0) return

        val centerX = matrix[winCoordinates[0]][winCoordinates[1]].rect.exactCenterX()
        val centerY = matrix[winCoordinates[0]][winCoordinates[1]].rect.exactCenterY()
        val centerX2 = matrix[winCoordinates[2]][winCoordinates[3]].rect.exactCenterX()
        val centerY2 = matrix[winCoordinates[2]][winCoordinates[3]].rect.exactCenterY()
        path.reset()
        path.moveTo(centerX, centerY) // moving to centre of first square
        path.lineTo(centerX2, centerY2) // creating a line till centre of last square
        val measure = PathMeasure(path, false)
        shouldAnimate = true

        valueAnimator.duration = 600
        valueAnimator.addUpdateListener {
            val offset = measure.length * it.animatedValue as Float
            this.winLinetPaint.pathEffect = createPathEffect(measure.length, offset)
            invalidate()
        }
        valueAnimator.start()
    }

    private fun createPathEffect(pathLength: Float, offset: Float): PathEffect {
        return DashPathEffect(floatArrayOf(pathLength, pathLength),
            offset)
    }

    private fun reset() {
        touching = false
        isFirst = true
        shouldAnimate = false
        invalidate()
    }

    private fun checkForWin() : Array<Int> {
        val result = arrayOf(-1,-1,-1,-1)
        // Duyệt từng hàng
        for (i in 0 until rows) {
            var cell1 = matrix[i][0]
            var cell2 = matrix[i][1]
            var cell3 = matrix[i][2]

            if (cell1.player != Player.NONE && cell1.player == cell2.player && cell2.player == cell3.player) {
                result[0] = i
                result[1] = 0
                result[2] = i
                result[3] = 2
                return result
            }
        }

        // Duyệt từng cột
        for (i in 0 until cols) {
            var cell1 = matrix[0][i]
            var cell2 = matrix[1][i]
            var cell3 = matrix[2][i]

            if (cell1.player != Player.NONE && cell1.player == cell2.player && cell2.player == cell3.player) {
                result[0] = 0
                result[1] = i
                result[2] = 2
                result[3] = i
                return result
            }
        }

        // Duyệt Đường chéo 1
        var cell1 = matrix[0][maxIndex]
        var cell2 = matrix[1][maxIndex-1]
        var cell3 = matrix[2][maxIndex-2]

        if (cell1.player != Player.NONE && cell1.player == cell2.player && cell2.player == cell3.player) {
            result[0] = 0
            result[1] = maxIndex
            result[2] = 2
            result[3] = maxIndex-2
            return result
        }

        // Duyệt Đường chéo 2
        cell1 = matrix[0][0]
        cell2 = matrix[1][1]
        cell3 = matrix[2][2]

        if (cell1.player != Player.NONE && cell1.player == cell2.player && cell2.player == cell3.player) {
            result[0] = 0
            result[1] = 0
            result[2] = 2
            result[3] = 2
            return result
        }

        return result
    }

    var touchedRectPair = Pair(-1,-1)
    var touching = false
    var lastTouchX = -1
    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y
        when(event.action) {
            MotionEvent.ACTION_DOWN -> {
                touching = true
                touchedRectPair = getIndexFor(x,y)
                updateMatrix(touchedRectPair)
                invalidate()
            }
            MotionEvent.ACTION_MOVE -> {
                if(x - lastTouchX >= 20f) {
                    reset()
                }
                lastTouchX = x.toInt();
            }
            MotionEvent.ACTION_UP -> {
                touching = false
                invalidate()
            }
            MotionEvent.ACTION_CANCEL -> {

            }
        }
        return true
    }


}