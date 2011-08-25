package realgraffiti.android.maps;

//import org.osmdroid.util.BoundingBoxE6;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.*;
import org.osmdroid.views.overlay.*;
import org.osmdroid.views.overlay.OverlayItem.HotspotPlace;


import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

public class CurrentLocationOverlay extends ItemizedOverlay<OverlayItem>{
	public static final double E6 = 1000000;
	private MapView _mapView;
	private OverlayItem _currentLocationOverlayItem = null;
	private LocationManager _locationManager;
	private LocationListener _locationListener;
	
	public CurrentLocationOverlay(Drawable defaultMarker, MapView mapView, LocationManager locationManager) {
		 super(defaultMarker, mapView.getResourceProxy());
		 boundToHotspot(defaultMarker, HotspotPlace.CENTER);
		 _mapView = mapView;
		 _locationManager = locationManager;
		 
		 populate();
	}
	
	public void setCurrentLocation(GeoPoint currentLocation){
		_currentLocationOverlayItem = new OverlayItem("", "Current location", "",currentLocation);
		_currentLocationOverlayItem.setMarkerHotspot(HotspotPlace.CENTER);
		populate();
	}

	@Override
	protected OverlayItem createItem(int i) {
		
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
		_locationManager.removeUpdates(_locationListener);
	}
	
	public void craeteLocationListener(){
		// Define a listener that responds to location updates
		_locationListener = new LocationListener() {
		    public void onLocationChanged(Location location) {
		      int latitude = (int)(location.getLatitude() * E6);
		      int longitude = (int)(location.getLongitude() * E6);
		      			
		      GeoPoint currentLocation = new GeoPoint(latitude, longitude);
		      Log.d("realgraffiti", "location recieved");
		      setCurrentLocation(currentLocation);
		      
		      MapController mapController = _mapView.getController();
		      mapController.animateTo(currentLocation);
		    }

			@Override
			public void onProviderDisabled(String provider) {
				
			}

			@Override
			public void onProviderEnabled(String provider) {
				
			}

			@Override
			public void onStatusChanged(String provider, int status,
					Bundle extras) {
				
			}

		  };

		// Register the listener with the Location Manager to receive location updates
		_locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, _locationListener);
	}

	@Override
	public boolean onSnapToItem(int arg0, int arg1, Point arg2, MapView arg3) {
		// TODO Auto-generated method stub
		return false;
	}

}
