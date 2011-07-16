package realgraffiti.android.map;

import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;

public class CurrentLocationOverlay extends ItemizedOverlay<OverlayItem>{
	protected static final double E6 = 1000000;
	MapView _mapView;
	OverlayItem _currentLocationOverlayItem = null;
	LocationManager _locationManager;
	public CurrentLocationOverlay(Drawable defaultMarker, MapView mapView, LocationManager locationManager) {
		 super(boundCenterBottom(defaultMarker));
		 
		 _mapView = mapView;
		 _locationManager = locationManager;
		 
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
	
	public void startTrackingLocation(){
		craeteLocationListener();
	}
	
	public void stopTrackingLocation(){
		
	}
	
	public void craeteLocationListener(){
		// Define a listener that responds to location updates
		LocationListener locationListener = new LocationListener() {
		    public void onLocationChanged(Location location) {
		      int latitude = (int)(location.getLatitude() * E6);
		      int longitude = (int)(location.getLongitude() * E6);
		      
		      GeoPoint currentLocation = new GeoPoint(latitude, longitude);
		      Log.d("realgraffiti", "location recieved");
		      setCurrentLocation(currentLocation);
		      
		      MapController mc = _mapView.getController();
		      mc.animateTo(currentLocation);
		      //mc.setCenter(point)
		      mc.setZoom(10);
		    }

			@Override
			public void onProviderDisabled(String provider) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onProviderEnabled(String provider) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onStatusChanged(String provider, int status,
					Bundle extras) {
				// TODO Auto-generated method stub
				
			}

		  };

		// Register the listener with the Location Manager to receive location updates
		_locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
	}

}
