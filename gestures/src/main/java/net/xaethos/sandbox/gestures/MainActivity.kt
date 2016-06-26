package net.xaethos.sandbox.gestures

import android.content.Context
import android.os.Bundle
import android.support.v4.view.MotionEventCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import android.util.Log
import android.view.*
import android.widget.LinearLayout
import android.widget.Toast

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val recyclerView = findViewById(R.id.pager) as RecyclerView
        recyclerView.adapter = Adapter()
    }
}

class Adapter : RecyclerView.Adapter<ViewHolder>() {
    override fun getItemCount() = 5

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ViewHolder? {
        if (parent == null) return null
        val layoutInflater = LayoutInflater.from(parent.context)
        return ViewHolder(layoutInflater.inflate(R.layout.page_item, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder?, position: Int) {
    }
}

class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
    init {
        val ctx = itemView.context

        itemView.findViewById(R.id.column)?.setOnTouchListener(
                TouchListener(ctx, ColumnGestureListener(ctx)))

        val touchListener = TouchListener(ctx, BoxGestureListener(ctx))
        itemView.findViewById(R.id.box_top)?.setOnTouchListener(touchListener)
        itemView.findViewById(R.id.box_center)?.setOnTouchListener(touchListener)
        itemView.findViewById(R.id.box_bottom)?.setOnTouchListener(touchListener)
    }

    override fun onClick(v: View?) {
        val message = when (v!!.id) {
            R.id.column -> "column $adapterPosition container"
            R.id.box_top -> "column $adapterPosition top"
            R.id.box_center -> "column $adapterPosition center"
            R.id.box_bottom -> "column $adapterPosition bottom"
            else -> "something got clicked: $v"
        }
        Toast.makeText(v.context, message, Toast.LENGTH_SHORT).show()
    }
}

class ColumnView(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs) {

    data class Touch(val x: Float, val y: Float, val pointerId: Int)

    private val touchSlop: Int

    private var touch: Touch? = null

    init {
        val vc = ViewConfiguration.get(context);
        touchSlop = vc.scaledTouchSlop
    }

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        return false
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event == null) return false

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                if (touch == null) {
                    val pointerIndex = MotionEventCompat.getActionIndex(event);
                    touch = Touch(MotionEventCompat.getX(event, pointerIndex),
                            MotionEventCompat.getY(event, pointerIndex),
                            MotionEventCompat.getPointerId(event, pointerIndex))
                    startCharge()
                }
            }
            MotionEvent.ACTION_UP,
            MotionEvent.ACTION_CANCEL -> {
                if (touch != null) {
                    touch = null
                    cancelCharge()
                }
            }
            MotionEvent.ACTION_MOVE -> {
                if (touch != null && calculateDistance(event) > touchSlop) {
                    touch = null
                    cancelCharge()
                }
            }
        }

        return true
    }

    private fun calculateDistance(event: MotionEvent): Double {
        val startTouch = touch ?: return 0.0

        val pointerIndex = MotionEventCompat.getActionIndex(event);
        val id = MotionEventCompat.getPointerId(event, pointerIndex)
        if (id != startTouch.pointerId) return 0.0

        val deltaX = MotionEventCompat.getX(event, pointerIndex) - startTouch.x
        val deltaY = MotionEventCompat.getY(event, pointerIndex) - startTouch.y

        return Math.sqrt(Math.pow(deltaX.toDouble(), 2.0) + Math.pow(deltaY.toDouble(), 2.0))
    }

    fun startCharge() {
        Log.d("XAE", "startCharge")
    }

    fun cancelCharge() {
        Log.d("XAE", "cancelCharge")
    }
}

class TouchListener(context: Context, gestureListener: GestureDetector.OnGestureListener) : View.OnTouchListener {
    val gestureDetector = GestureDetector(context, gestureListener)

    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        Log.d("XAE", "onTouch $v [$event]")
        return gestureDetector.onTouchEvent(event)
    }
}

class ColumnGestureListener(val context: Context) : GestureDetector.SimpleOnGestureListener() {
    override fun onShowPress(e: MotionEvent?) {
        Toast.makeText(context, "I'm being depressed!", Toast.LENGTH_SHORT).show()
    }

    override fun onLongPress(e: MotionEvent?) {
        super.onLongPress(e)
    }
}

class BoxGestureListener(val context: Context) : GestureDetector.SimpleOnGestureListener() {
    override fun onDown(e: MotionEvent?) = true

    override fun onSingleTapConfirmed(e: MotionEvent?): Boolean {
        Toast.makeText(context, "Just one tap", Toast.LENGTH_SHORT).show()
        return true
    }

    override fun onDoubleTap(e: MotionEvent?): Boolean {
        Toast.makeText(context, "Double tap", Toast.LENGTH_SHORT).show()
        return true
    }
}
