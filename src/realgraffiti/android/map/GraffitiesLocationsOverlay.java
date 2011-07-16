package realgraffiti.android.map;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import realgraffiti.android.R;
import realgraffiti.android.web.GraffitiPollListener;
import realgraffiti.android.web.GraffitiServerPoller;
import realgraffiti.common.data.RealGraffitiData;
import realgraffiti.common.dataObjects.Graffiti;

import android.graphics.drawable.Drawable;
import android.util.Log;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;

public class GraffitiesLocationsOverlay extends ItemizedOverlay<OverlayItem> {
	private List<OverlayItem> _overlays = new ArrayList<OverlayItem>();
	
	private RealGraffitiData _realGraffitiData;
	
	private GraffitiServerPoller _graffitiServerPoller = null;
	private MapView _mapView;

	private static final int DATA_POLLING_INTERVAL = 10000;
	
	public GraffitiesLocationsOverlay(Drawable defaultMarker, MapView mapView, RealGraffitiData realGraffitiData) {
		  super(boundCenterBottom(defaultMarker));
		  
		  _realGraffitiData = realGraffitiData;
		  _mapView = mapView;
	}
	
	public void addGraffiti(Graffiti graffiti){
		int latitude = graffiti.getLocationParameters().getCoordinates().getLatitude();
		int longitude = graffiti.getLocationParameters().getCoordinates().getLongitude();
		
		GeoPoint point = new GeoPoint(latitude, longitude);
		OverlayItem overlay = new OverlayItem(point, "graffiti", "");
		
		_overlays.add(overlay);
		
		
	}
	
	public void addGraffities(Collection<Graffiti> graffities){
		for(Graffiti graffiti: graffities){
			addGraffiti(graffiti);
		}
		populate();
	}
	
	public void setGraffitis(Collection<Graffiti> graffities){
		_overlays.clear();
		addGraffities(graffities);
		populate();
	}
	
	@Override
	protected OverlayItem createItem(int i) {
	  return _overlays.get(i);
	}
	
	@Override
	public int size() {
	  return _overlays.size();
	}
	
	public void startPollingForGraffities(){
		if(_graffitiServerPoller == null){
			initializeGraffitiServerPoller();
		}
		
		_graffitiServerPoller.beginPolling();
	}
	
	public void stopPollingForGraffities(){
		_graffitiServerPoller.stopPolling();
	}

	private void initializeGraffitiServerPoller() {
	    _graffitiServerPoller = new GraffitiServerPoller(_realGraffitiData, DATA_POLLING_INTERVAL);
	    
	    _graffitiServerPoller.setOnPoll(new GraffitiPollListener() {
			@Override
			public void onPollingData(Collection<Graffiti> graffities) {
				if(graffities != null && graffities.size() > 0){
					addGraffities(graffities);
				    _mapView.invalidate();
				}
				Log.d("realgraffiti", "poll data of size: " + graffities.size());
			}
		});
	}
	
	
}
