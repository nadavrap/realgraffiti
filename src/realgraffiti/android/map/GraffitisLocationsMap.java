package realgraffiti.android.map;

import java.util.Collection;
import java.util.List;

import realgraffiti.android.R;
import realgraffiti.android.data.GraffitiLocationParametersGeneratorFactory;
import realgraffiti.common.data.RealGraffitiData;
import realgraffiti.common.dataObjects.Graffiti;

import realgraffiti.android.data.*;
import realgraffiti.android.web.GraffitiServerPoller;
import realgraffiti.android.web.GraffitiPollListener;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;


import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;


public class GraffitisLocationsMap extends MapActivity {
	private final int DATA_POLLING_INTERVAL = 10000;
	private GraffitiServerPoller _graffitiServerPoller = null;
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.mapview);
	    
	    final MapView mapView = (MapView) findViewById(R.id.mapview);
	    mapView.setBuiltInZoomControls(true);
	    
	    Drawable drawable = this.getResources().getDrawable(R.drawable.spraycan);
	    final GraffitiItemizedOverlay itemizedoverlay = new GraffitiItemizedOverlay(drawable);
	    
	    final RealGraffitiData graffitiData =  new RealGraffitiLocalData();
	       
	    _graffitiServerPoller = new GraffitiServerPoller(graffitiData, DATA_POLLING_INTERVAL);
	    _graffitiServerPoller.setOnPoll(new GraffitiPollListener() {
			@Override
			public void onPollingData(Collection<Graffiti> graffities) {
				if(graffities != null && graffities.size() > 0){
					List<Overlay> mapOverlays = mapView.getOverlays();
					itemizedoverlay.addGraffities(graffities);
				    mapOverlays.add(itemizedoverlay);
				    mapView.invalidate();
				}			
				Log.d("realgraffiti", "poll data of size: " + graffities.size());
			}
		});
	    
	    _graffitiServerPoller.beginPolling();
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
