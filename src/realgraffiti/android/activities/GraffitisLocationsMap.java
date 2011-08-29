package realgraffiti.android.activities;

import org.osmdroid.views.MapView;

import realgraffiti.android.R;
import realgraffiti.android.data.GraffitiLocationParametersGeneratorFactory;
import realgraffiti.android.data.GraffitiPoller;
import realgraffiti.common.data.RealGraffitiData;
import realgraffiti.common.dataObjects.Graffiti;
import realgraffiti.common.dataObjects.GraffitiLocationParameters;
import realgraffiti.android.data.*;
import realgraffiti.android.maps.CurrentLocationOverlay;
import realgraffiti.android.maps.GraffitiesLocationsOverlay;
import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.LinearLayout;

public class GraffitisLocationsMap extends Activity {
	private static final int MAP_RANGE_IN_METERS = 5000;
	private GraffitiesLocationsOverlay _graffitiLocationsOverlay;
	private CurrentLocationOverlay _currentLocationOverlay;
	
	private final int ZOOM_LEVEL = 16;
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
		Log.d("GraffitisLocationsMap", "on craete");

	    setContentView(R.layout.mapview);
	    
	    MapView mapView = (MapView)findViewById(R.id.mapview);
	    mapView.setBuiltInZoomControls(true);
	    //mapView.setSatellite(true);
	    mapView.getController().setZoom(ZOOM_LEVEL);
	    
	    Drawable graffitiMarker = this.getResources().getDrawable(R.drawable.spraycan);
	    RealGraffitiData realGraffitiData = new RealGraffitiLocalData();
	    _graffitiLocationsOverlay = new GraffitiesLocationsOverlay(this, graffitiMarker,MAP_RANGE_IN_METERS, mapView, realGraffitiData);
	    mapView.getOverlays().add(_graffitiLocationsOverlay);
	    
	    Drawable currentLocationMarker = this.getResources().getDrawable(R.drawable.current_location);
	    LocationManager locationManager = (LocationManager) this.getSystemService(this.LOCATION_SERVICE);
	    _currentLocationOverlay = new CurrentLocationOverlay(currentLocationMarker, mapView,locationManager);
	    mapView.getOverlays().add(_currentLocationOverlay);
		Log.d("GraffitisLocationsMap", "on craete done");
	}
	
	protected boolean isRouteDisplayed() {
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	protected void onStart(){
		super.onStart();
		Log.d("GraffitisLocationsMap", "on start");
		_graffitiLocationsOverlay.startPollingForGraffities();
		_currentLocationOverlay.startTrackingLocation();
	}
	
	@Override
	protected void onPause(){
		super.onPause();
		Log.d("GraffitisLocationsMap", "on pause");
		_graffitiLocationsOverlay.stopPollingForGraffities();
		_currentLocationOverlay.stopTrackingLocation();
	}
	
	@Override
	protected void onResume(){
		super.onResume();
		Log.d("GraffitisLocationsMap", "on resume");
	}
	
	
}
