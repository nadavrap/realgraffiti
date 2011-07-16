package realgraffiti.android.map;

import android.graphics.drawable.Drawable;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;

public class CurrentLocationOverlay extends ItemizedOverlay<OverlayItem>{
	MapView _mapView;
	OverlayItem _currentLocationOverlayItem;
	
	public CurrentLocationOverlay(Drawable defaultMarker, MapView mapView) {
		 super(boundCenterBottom(defaultMarker));
		 
		 _mapView = mapView;
		 setCurrentLocation(new GeoPoint(30000000, 30000000));
	}
	
	public void setCurrentLocation(GeoPoint currentLocation){
		_currentLocationOverlayItem = new OverlayItem(currentLocation, "Current location", "");
		populate();
	}

	@Override
	protected OverlayItem createItem(int i) {
		// TODO Auto-generated method stub
		return _currentLocationOverlayItem;
	}

	@Override
	public int size() {
		if(_currentLocationOverlayItem == null)
			return 0;
		
		return 1;
	}

}
