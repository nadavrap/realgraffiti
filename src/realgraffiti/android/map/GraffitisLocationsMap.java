package realgraffiti.android.map;

import java.util.Collection;
import java.util.List;

import realgraffiti.android.R;
import realgraffiti.android.data.GraffitiLocationParametersGeneratorFactory;
import realgraffiti.common.data.RealGraffitiData;
import realgraffiti.common.dataObjects.Graffiti;
import realgraffiti.common.dataObjects.GraffitiLocationParameters;

import realgraffiti.android.data.*;
import realgraffiti.android.web.GraffitiServerPoller;
import realgraffiti.android.web.GraffitiPollListener;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;


import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;
import com.google.android.maps.Overlay;


public class GraffitisLocationsMap extends MapActivity {
	private final int DATA_POLLING_INTERVAL = 10000;
	private GraffitiServerPoller _graffitiServerPoller = null;
	private static final String MAP_KEY = "0OUnpM96lLtw7orPft9tQGYGiIuhVDDEJmmQjHg";
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.mapview);
	    
	    RealGraffitiData realGraffitiData = new RealGraffitiLocalData();
	    MapView mapView = new MapView(this, MAP_KEY);
	    mapView.setEnabled(true);
	    mapView.setBuiltInZoomControls(true);
	    mapView.setClickable(true);
	    mapView.setSatellite(true);
	    mapView.setTraffic(true);
	    mapView.setStreetView(true);   
	    
	    
	    GraffitiMapView graffitiMapView = new GraffitiMapView(this, mapView, realGraffitiData);
	    
	    graffitiMapView.startPollingForGraffities();
	    LinearLayout layout = (LinearLayout) findViewById(R.id.graffitiMapViewLayout);
	    
	    layout.addView(graffitiMapView);
	    
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
