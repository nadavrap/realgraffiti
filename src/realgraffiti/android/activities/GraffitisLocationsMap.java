package realgraffiti.android.activities;

import realgraffiti.android.R;
import realgraffiti.android.data.GraffitiLocationParametersGeneratorFactory;
import realgraffiti.common.data.RealGraffitiData;
import realgraffiti.common.dataObjects.Graffiti;
import realgraffiti.common.dataObjects.GraffitiLocationParameters;
import realgraffiti.android.data.*;
import realgraffiti.android.map.GraffitiesLocationsOverlay;
import realgraffiti.android.map.GraffitiMapView;
import realgraffiti.android.map.CurrentLocationOverlay;
import realgraffiti.android.web.GraffitiServerPoller;
import android.graphics.drawable.Drawable;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.LinearLayout;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;



public class GraffitisLocationsMap extends MapActivity {

	private GraffitiServerPoller _graffitiServerPoller = null;
	private static final String MAP_KEY = "0OUnpM96lLtw7orPft9tQGYGiIuhVDDEJmmQjHg";
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.mapview);
	    
	    MapView mapView = (MapView)findViewById(R.id.mapview);
	    mapView.setBuiltInZoomControls(true);
	    mapView.setSatellite(true);
	    Drawable graffitiMarker = this.getResources().getDrawable(R.drawable.spraycan);
	    RealGraffitiData realGraffitiData = new RealGraffitiLocalData();
	    GraffitiesLocationsOverlay graffitiOverlay = new GraffitiesLocationsOverlay(graffitiMarker, mapView, realGraffitiData);
	    mapView.getOverlays().add(graffitiOverlay);
	    
	    graffitiOverlay.startPollingForGraffities();
	    
	    Drawable currentLocationMarker = this.getResources().getDrawable(R.drawable.current_location);
	    LocationManager locationManager = (LocationManager) this.getSystemService(this.LOCATION_SERVICE);
	    CurrentLocationOverlay currentLocationOverLay = new CurrentLocationOverlay(currentLocationMarker, mapView,locationManager);
	    mapView.getOverlays().add(currentLocationOverLay);
	    
	    currentLocationOverLay.startTrackingLocation();
		
	    GraffitiLocationParameters glp = GraffitiLocationParametersGeneratorFactory.getGaffitiLocationParametersGenerator().getCurrentLocationParameters();
	    Graffiti graffiti = new Graffiti(glp);
	    realGraffitiData.addNewGraffiti(graffiti);
	    
	    glp = GraffitiLocationParametersGeneratorFactory.getGaffitiLocationParametersGenerator().getCurrentLocationParameters();
	    graffiti = new Graffiti(glp);
	    realGraffitiData.addNewGraffiti(graffiti);
	    
	    glp = GraffitiLocationParametersGeneratorFactory.getGaffitiLocationParametersGenerator().getCurrentLocationParameters();
	    graffiti = new Graffiti(glp);
	    realGraffitiData.addNewGraffiti(graffiti);
	    
	    glp = GraffitiLocationParametersGeneratorFactory.getGaffitiLocationParametersGenerator().getCurrentLocationParameters();
	    graffiti = new Graffiti(glp);
	    realGraffitiData.addNewGraffiti(graffiti);
	}
	
	@Override
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
