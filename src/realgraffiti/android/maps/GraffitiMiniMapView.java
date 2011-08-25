/**
 * Code taken from:
 * http://developer.android.com/resources/samples/ApiDemos/src/com/example/android/apis/graphics/Xfermodes.html
 * http://code.google.com/p/mapsadroidproject/source/browse/trunk/MapsDemo/src/com/example/android/apis/view/MapViewCompassDemo.java?spec=svn3&r=3
 */

package realgraffiti.android.maps;


import org.osmdroid.views.MapView;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.location.LocationManager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import realgraffiti.android.R;
import realgraffiti.android.activities.GraffitisLocationsMap;
import realgraffiti.common.data.RealGraffitiData;

public class GraffitiMiniMapView extends ViewGroup{
	private static final float SQ2 = 1.414213562373095f;
	private static final int ZOOM_LEVEL = 16;
    private MapView _mapView;
    private RealGraffitiData _realGraffitiData;
    
    private CurrentLocationOverlay _currentLocationOverlay;
    private GraffitiesLocationsOverlay _graffitiesLocationOverlay;
	private Context _context;
    
	public GraffitiMiniMapView(Context context) {
		super(context);
		initView(context);	    
	}
	
    public GraffitiMiniMapView(Context context, AttributeSet attrs) {
		super(context, attrs);
		if (isInEditMode()) {return;}
		_context = context;
		initView(context);
	}  
    
    private void initView(final Context context){
    	_mapView = new MapView(context, 100);
	    _mapView.setEnabled(true);
	    _mapView.setClickable(true);
	    _mapView.setMultiTouchControls(false);
	    _mapView.getController().setZoom(ZOOM_LEVEL);
	    
	    _mapView.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.setClass(context, GraffitisLocationsMap.class);
				context.startActivity(intent);
			}
		});
	    
	    addView(_mapView);
	        
	    Drawable currentLocationMarker = this.getResources().getDrawable(R.drawable.current_location);
	    LocationManager locationManager = (LocationManager) context.getSystemService(context.LOCATION_SERVICE);
	    _currentLocationOverlay = new CurrentLocationOverlay(currentLocationMarker, _mapView,locationManager);
	    _mapView.getOverlays().add(_currentLocationOverlay);    
    }
   
    public void setRealGraffitiData(RealGraffitiData realGraffitiData){
    	_realGraffitiData = realGraffitiData;
    	
    	Drawable graffitiMarker = this.getResources().getDrawable(R.drawable.graffiti_mark);
    	_graffitiesLocationOverlay = new GraffitiesLocationsOverlay(_context, graffitiMarker, _mapView, _realGraffitiData);
	    _mapView.getOverlays().add(_graffitiesLocationOverlay);
    }
    
    public void startOverlays(){
    	_graffitiesLocationOverlay.startPollingForGraffities();
    	_currentLocationOverlay.startTrackingLocation();
    }
    
	public void stopOverlays() {
		_graffitiesLocationOverlay.stopPollingForGraffities();
		_currentLocationOverlay.stopTrackingLocation();
	}
    
    @Override
    protected void dispatchDraw(Canvas canvas) {   	
        canvas.save(Canvas.MATRIX_SAVE_FLAG);
        //canvas.rotate(-_heading, getWidth() * 0.5f, getHeight() * 0.5f);
        //_canvas.setDelegate(canvas);
        super.dispatchDraw(canvas);
        //_mapView.draw(canvas);
        canvas.restore();
        
        int W = getWidth();
        int H = getHeight();
        Bitmap mDstB = createCircleFrame(W, H);
        Paint paint = new Paint();
    	paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
    	canvas.drawBitmap(mDstB, 0, 0, paint);
    	
    	paint.setXfermode(null);
    }
    
    private Bitmap createCircleFrame(int w, int h) {
        Bitmap bm = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bm);
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);

        p.setColor(0xFFFFCC44);
        c.drawOval(new RectF(0, 0, w, h), p);
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
