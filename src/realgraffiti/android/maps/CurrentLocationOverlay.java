package realgraffiti.android.maps;

//import org.osmdroid.util.BoundingBoxE6;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.*;
import org.osmdroid.views.overlay.*;
import org.osmdroid.views.overlay.OverlayItem.HotspotPlace;

import realgraffiti.common.dataObjects.Coordinates;


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
	private static final int TWO_MINUTES = 1000 * 60 * 2;

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
		Log.d("RealGraffiti", "CurrentLocationOverlay - stop Listening");
		_locationManager.removeUpdates(_locationListener);
	}
	
	public void craeteLocationListener(){
		Log.d("RealGraffiti", "CurrentLocationOverlay - start Listening");
		// Define a listener that responds to location updates
		_locationListener = new LocationListener() {
		    public void onLocationChanged(Location location) {
		      int latitude = (int)(location.getLatitude() * E6);
		      int longitude = (int)(location.getLongitude() * E6);
		      			
		      GeoPoint currentLocation = new GeoPoint(latitude, longitude);
		      Log.d("realgraffiti", "location recieved");
		      setCurrentLocation(currentLocation);
		      
		      MapController mapController = _mapView.getController();
		      mapController.setCenter(currentLocation);
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
		
		//Eitan - Get last known GPS and Network locations and use the better one
		Location gpsloc = _locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
  	  	Location netloc = _locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
  	  	
  	  	if(netloc != null){
	  	  	Location loc;
	
	  	  	if (isBetterLocation(netloc, gpsloc))
	  	  		loc = netloc;
	  	  	else
	  	  		loc = gpsloc;
	  	  	
			if(loc != null) {
				
			      GeoPoint currentLocation = new GeoPoint((int)(loc.getLatitude() * E6), (int)(loc.getLongitude() * E6) );
			      Log.d("realgraffiti", "location recieved");
			      setCurrentLocation(currentLocation);
			      
			      MapController mapController = _mapView.getController();
			      mapController.setCenter(currentLocation);
			}
  	  	}
		
		
		
	}

	@Override
	public boolean onSnapToItem(int arg0, int arg1, Point arg2, MapView arg3) {
		// TODO Auto-generated method stub
		return false;
	}

    /** Determines whether one Location reading is better than the current Location fix
     * @param location  The new Location that you want to evaluate
     * @param currentBestLocation  The current Location fix, to which you want to compare the new one
     */
   protected boolean isBetterLocation(Location location, Location currentBestLocation) {
       if (currentBestLocation == null) {
           // A new location is always better than no location
           return true;
       }

       // Check whether the new location fix is newer or older
       long timeDelta = location.getTime() - currentBestLocation.getTime();
       boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
       boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
       boolean isNewer = timeDelta > 0;

       // If it's been more than two minutes since the current location, use the new location
       // because the user has likely moved
       if (isSignificantlyNewer) {
           return true;
       // If the new location is more than two minutes older, it must be worse
       } else if (isSignificantlyOlder) {
           return false;
       }

       // Check whether the new location fix is more or less accurate
       int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
       boolean isLessAccurate = accuracyDelta > 0;
       boolean isMoreAccurate = accuracyDelta < 0;
       boolean isSignificantlyLessAccurate = accuracyDelta > 200;

       // Check if the old and new location are from the same provider
       boolean isFromSameProvider = isSameProvider(location.getProvider(),
               currentBestLocation.getProvider());

       // Determine location quality using a combination of timeliness and accuracy
       if (isMoreAccurate) {
           return true;
       } else if (isNewer && !isLessAccurate) {
           return true;
       } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
           return true;
       }
       return false;
   }
   /** Checks whether two providers are the same */
   private boolean isSameProvider(String provider1, String provider2) {
       if (provider1 == null) {
         return provider2 == null;
       }
       return provider1.equals(provider2);
   }
	
	
}
