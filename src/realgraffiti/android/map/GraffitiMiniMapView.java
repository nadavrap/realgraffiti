/**
 * Code taken from:
 * http://developer.android.com/resources/samples/ApiDemos/src/com/example/android/apis/graphics/Xfermodes.html
 * http://code.google.com/p/mapsadroidproject/source/browse/trunk/MapsDemo/src/com/example/android/apis/view/MapViewCompassDemo.java?spec=svn3&r=3
 */

package realgraffiti.android.map;


import com.google.android.maps.MapView;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.MeasureSpec;
import realgraffiti.android.activities.SmoothCanvas;

public class GraffitiMiniMapView extends ViewGroup{
	private SensorManager _sensorManager;
    private Sensor _sensor;
	private static final float SQ2 = 1.414213562373095f;
	private static final String MAP_KEY = "0OUnpM96lLtw7orPft9tQGYGiIuhVDDEJmmQjHg";
    private final SmoothCanvas _canvas = new SmoothCanvas();
    private float _heading = 45;
    private MapView _mapView;
    private float[] mValues;
    
	public GraffitiMiniMapView(Context context) {
		super(context);
		initView(context);	    
	}
	
    public GraffitiMiniMapView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initView(context);
	}  
    
    private void initView(Context context){
    	_mapView = new MapView(context, MAP_KEY);
	    _mapView.setEnabled(true);
	    addView(_mapView);
	    
	    
    	
    }
   
    @Override
    protected void dispatchDraw(Canvas canvas) {
    	
        canvas.save(Canvas.MATRIX_SAVE_FLAG);
        
       // _heading = SensorManager.getOrientation(R, values);
        
        canvas.rotate(-_heading, getWidth() * 0.5f, getHeight() * 0.5f);
        //_canvas.setDelegate(canvas);
        super.dispatchDraw(canvas);
        canvas.restore();
        
        int W = getWidth();
        int H = getHeight();
        Bitmap mDstB = makeDst(W, H);
        Paint paint = new Paint();
    	paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
    	canvas.drawBitmap(mDstB, 0, 0, paint);
    	
    	paint.setXfermode(null);
    }
    
    private Bitmap makeDst(int w, int h) {
        Bitmap bm = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bm);
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);

        p.setColor(0xFFFFCC44);
        c.drawOval(new RectF(0, 0, w*3/4, h*3/4), p);
        return bm;
    }
    
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        final int width = getWidth();
        final int height = getHeight();
        final int count = getChildCount();
        for (int i = 0; i < count; i++) {
            final View view = getChildAt(i);
            final int childWidth = view.getMeasuredWidth();
            final int childHeight = view.getMeasuredHeight();
            final int childLeft = (width - childWidth) / 2;
            final int childTop = (height - childHeight) / 2;
            view.layout(childLeft, childTop, childLeft + childWidth, childTop + childHeight);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int w = getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec);
        int h = getDefaultSize(getSuggestedMinimumHeight(), heightMeasureSpec);
        int sizeSpec;
        if (w > h) {
            sizeSpec = MeasureSpec.makeMeasureSpec((int) (w * SQ2), MeasureSpec.EXACTLY);
        } else {
            sizeSpec = MeasureSpec.makeMeasureSpec((int) (h * SQ2), MeasureSpec.EXACTLY);
        }
        final int count = getChildCount();
        for (int i = 0; i < count; i++) {
            getChildAt(i).measure(sizeSpec, sizeSpec);
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        // TODO: rotate events too
        return super.dispatchTouchEvent(ev);
    }
}
