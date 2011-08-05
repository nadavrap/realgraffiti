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

/*
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;
*/

public class GraffitisLocationsMap extends Activity {

	private GraffitiServerPoller _graffitiServerPoller = null;
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
	    GraffitiesLocationsOverlay graffitiOverlay = new GraffitiesLocationsOverlay(this, graffitiMarker, mapView, realGraffitiData);
	    mapView.getOverlays().add(graffitiOverlay);
	    
	    graffitiOverlay.startPollingForGraffities();
	    
	    Drawable currentLocationMarker = this.getResources().getDrawable(R.drawable.current_location);
	    LocationManager locationManager = (LocationManager) this.getSystemService(this.LOCATION_SERVICE);
	    CurrentLocationOverlay currentLocationOverLay = new CurrentLocationOverlay(currentLocationMarker, mapView,locationManager);
	    mapView.getOverlays().add(currentLocationOverLay);
	    
	    currentLocationOverLay.startTrackingLocation();
	}
	
	protected boolean isRouteDisplayed() {
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	protected void onPause(){
		super.onPause();
		if(_graffitiServerPoller != null)
			_graffitiServerPoller.stopPolling();
	}
	
	@Override
	protected void onResume(){
		super.onResume();
	}
	
	
}
