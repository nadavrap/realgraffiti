package realgraffiti.android.maps;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedOverlay;
import org.osmdroid.views.overlay.OverlayItem;
//import org.osmdroid.views.overlay.OverlayItem.HotspotPlace;
//import realgraffiti.android.R;
import realgraffiti.android.activities.GraffitisLocationsMap;
import realgraffiti.android.data.GraffitiPoller;
import realgraffiti.android.web.GraffitiPollListener;
import realgraffiti.common.data.RealGraffitiData;
import realgraffiti.common.dataObjects.Coordinates;
import realgraffiti.common.dataObjects.Graffiti;
import realgraffiti.common.dataObjects.GraffitiLocationParameters;

import android.content.Context;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.util.Log;


public class GraffitiesLocationsOverlay extends ItemizedOverlay<OverlayItem> {
	private List<OverlayItem> _overlays = new ArrayList<OverlayItem>();
	private RealGraffitiData _realGraffitiData;
	private GraffitiPoller _graffitiServerPoller = null;
	private MapView _mapView;
	private Context _context;
	private int _graffitiesDistance;
	
	private static final int DATA_POLLING_INTERVAL = 1000;
	
	public GraffitiesLocationsOverlay(Context context, Drawable defaultMarker, int graffitiesDistance, MapView mapView, RealGraffitiData realGraffitiData) {
		  super(defaultMarker, mapView.getResourceProxy());
		  
		  _realGraffitiData = realGraffitiData;
		  _mapView = mapView;
		  _context = context;
		  _graffitiesDistance = graffitiesDistance;
		  
		  populate();
	}
	
	public void addGraffiti(Graffiti graffiti){
		int latitude = graffiti.getLocationParameters().getCoordinates().getLatitude();
		int longitude = graffiti.getLocationParameters().getCoordinates().getLongitude();
		
		GeoPoint point = new GeoPoint(latitude, longitude);
		OverlayItem overlay = new OverlayItem("","graffiti", "", point);
		
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
	    _graffitiServerPoller = new GraffitiPoller(_context, _realGraffitiData, _graffitiesDistance, DATA_POLLING_INTERVAL);
	    
	    _graffitiServerPoller.setOnPoll(new GraffitiPollListener() {
			@Override
			public void onPollingData(Collection<Graffiti> graffities) {
				if(graffities != null && graffities.size() > 0){
					graffities = filterExistingGraffities(graffities);
					addGraffities(graffities);
				    _mapView.invalidate();
				}
				Log.d("realgraffiti", "poll data of size: " + graffities.size());
			}


		});

	    Log.d("GraffitiesLocationOverlay", "Start Polling for Graffites");
	}

	@Override
	public boolean onSnapToItem(int arg0, int arg1, Point arg2, MapView arg3) {
		// TODO Auto-generated method stub
		return false;
	}
	
	private Collection<Graffiti> filterExistingGraffities( Collection<Graffiti> graffities) {
		Collection<Graffiti> filteredGraffitie = new ArrayList<Graffiti>();
		filteredGraffitie.addAll(graffities);
		
		for(Graffiti graffiti : graffities){
			for(OverlayItem overlayItem: _overlays){
				Coordinates graffitiCoordinates = graffiti.getLocationParameters().getCoordinates();
				GeoPoint graffitiGeoPoint = new GeoPoint(graffitiCoordinates.getLatitude(), graffitiCoordinates.getLongitude());
				if(overlayItem.getPoint().equals(graffitiGeoPoint))
					filteredGraffitie.remove(graffiti);
			}
		}
		
		return filteredGraffitie;
	}
	
	
	
}
