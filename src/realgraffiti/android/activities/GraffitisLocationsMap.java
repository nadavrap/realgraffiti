package realgraffiti.android.activities;

import org.osmdroid.views.MapView;

import realgraffiti.android.R;
import realgraffiti.android.data.GraffitiLocationParametersGeneratorFactory;
import realgraffiti.common.data.RealGraffitiData;
import realgraffiti.common.dataObjects.Graffiti;
import realgraffiti.common.dataObjects.GraffitiLocationParameters;
import realgraffiti.android.data.*;
import realgraffiti.android.maps.CurrentLocationOverlay;
import realgraffiti.android.maps.GraffitiesLocationsOverlay;
import realgraffiti.android.web.GraffitiServerPoller;
import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.LinearLayout;

public class GraffitisLocationsMap extends Activity {
	private GraffitiesLocationsOverlay _graffitiLocationsOverlay;
	private CurrentLocationOverlay _currentLocationOverlay;
	
	private final int ZOOM_LEVEL = 13;
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.mapview);
	    
	    MapView mapView = (MapView)findViewById(R.id.mapview);
	    mapView.setBuiltInZoomControls(true);
	    //mapView.setSatellite(true);
	    mapView.getController().setZoom(ZOOM_LEVEL);
	    
	    Drawable graffitiMarker = this.getResources().getDrawable(R.drawable.spraycan);
	    RealGraffitiData realGraffitiData = new RealGraffitiLocalData();
	    _graffitiLocationsOverlay = new GraffitiesLocationsOverlay(this, graffitiMarker, mapView, realGraffitiData);
	    mapView.getOverlays().add(_graffitiLocationsOverlay);
	    
	    Drawable currentLocationMarker = this.getResources().getDrawable(R.drawable.current_location);
	    LocationManager locationManager = (LocationManager) this.getSystemService(this.LOCATION_SERVICE);
	    _currentLocationOverlay = new CurrentLocationOverlay(currentLocationMarker, mapView,locationManager);
	    mapView.getOverlays().add(_currentLocationOverlay);
	}
	
	protected boolean isRouteDisplayed() {
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	protected void onStart(){
		super.onStart();
		_graffitiLocationsOverlay.startPollingForGraffities();
		_currentLocationOverlay.startTrackingLocation();
	}
	
	@Override
	protected void onPause(){
		super.onPause();
		_graffitiLocationsOverlay.stopPollingForGraffities();
		_currentLocationOverlay.stopTrackingLocation();
	}
	
	@Override
	protected void onResume(){
		super.onResume();
	}
	
	
}
