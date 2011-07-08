package realgraffiti.android.map;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import realgraffiti.common.dataObjects.Graffiti;

import android.graphics.drawable.Drawable;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.OverlayItem;

public class GraffitiItemizedOverlay extends ItemizedOverlay<OverlayItem> {
	private List<OverlayItem> _overlays = new ArrayList<OverlayItem>();
	
	public GraffitiItemizedOverlay(Drawable defaultMarker) {
		  super(boundCenterBottom(defaultMarker));
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
	
	
}
