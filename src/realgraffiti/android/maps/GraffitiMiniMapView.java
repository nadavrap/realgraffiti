/**
 * Code taken from:
 * http://developer.android.com/resources/samples/ApiDemos/src/com/example/android/apis/graphics/Xfermodes.html
 * http://code.google.com/p/mapsadroidproject/source/browse/trunk/MapsDemo/src/com/example/android/apis/view/MapViewCompassDemo.java?spec=svn3&r=3
 */
package realgraffiti.android.maps;

import org.osmdroid.views.MapView;
import realgraffiti.android.R;
import realgraffiti.android.activities.GraffitisLocationsMap;
import realgraffiti.common.data.RealGraffitiData;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.LocationManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

public class GraffitiMiniMapView extends ViewGroup{
	private static final int MAP_RANGE_IN_METERS = 1000;
	private static final float SQ2 = 1.414213562373095f;
	private static final int ZOOM_LEVEL = 18;
	
	private MapView _mapView;
	private RealGraffitiData _realGraffitiData;
	private CurrentLocationOverlay _currentLocationOverlay;
	private GraffitiesLocationsOverlay _graffitiesLocationOverlay;
	private Context _context;
	private float _heading;
	//private OrientationM _orientationListener;

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
		//_graffitiLocationParametersGenerator = 
		//		GraffitiLocationParametersGeneratorFactory.getGaffitiLocationParametersGenerator(_context);
		new OrientationM(); 
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
		LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
		_currentLocationOverlay = new CurrentLocationOverlay(currentLocationMarker, _mapView,locationManager);
		_mapView.getOverlays().add(_currentLocationOverlay);    
	}

	public void setRealGraffitiData(RealGraffitiData realGraffitiData){
		_realGraffitiData = realGraffitiData;

		Drawable graffitiMarker = this.getResources().getDrawable(R.drawable.graffiti_mark);
		_graffitiesLocationOverlay = new GraffitiesLocationsOverlay(_context, graffitiMarker,MAP_RANGE_IN_METERS,  _mapView, _realGraffitiData);
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
		//_heading = _graffitiLocationParametersGenerator.getCurrentLocationParameters().getOrientation().getXorientation();
		//Log.d("GraffitiMiniMap","dispatchDraw, heading: " + _heading);
		canvas.rotate(-(_heading+90), getWidth() * 0.5f, getHeight() * 0.5f);
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

	private class OrientationM implements SensorEventListener {
		private SensorManager sensorManager = null;

		public OrientationM() {
			// Get a reference to a SensorManager
			sensorManager = (SensorManager)_context.getSystemService(Context.SENSOR_SERVICE);
			sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION), SensorManager.SENSOR_DELAY_NORMAL);
		}

		// This method will update the UI on new sensor events
		public void onSensorChanged(SensorEvent sensorEvent) {
			synchronized (this) {
				if (sensorEvent.sensor.getType() == Sensor.TYPE_ORIENTATION) {
					_heading = sensorEvent.values[0];
					//Log.d("OrientationM","onSensorChanged, heading: " + _heading);
					//Redraw the miniMao
					invalidate();
				}
			}
		}

		// I've chosen to not implement this method
		public void onAccuracyChanged(Sensor arg0, int arg1) {
			// TODO Auto-generated method stub
		}
	}
}
