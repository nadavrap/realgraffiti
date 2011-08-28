package realgraffiti.android.data;

import realgraffiti.common.dataObjects.Coordinates;
import realgraffiti.common.dataObjects.GraffitiLocationParameters;
import realgraffiti.common.dataObjects.Orientation;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

public class SensorsGraffitiLocationParametersGeneretor implements GraffitiLocationParametersGenerator {
	private static GraffitiLocationParameters _graffitiLocationParameters = new GraffitiLocationParameters();
	private final int orientationDelay = SensorManager.SENSOR_DELAY_NORMAL;
	private static final double E6 = 1000000;
	private LocationManager _myLocationManager;
	private MyLocationListener _myLocationListener;
	private boolean isLocationParametersAvailable = false;
	private SensorEventListener _magnetlistener;
	private SensorManager _mySensorManager;
	private static final int TWO_MINUTES = 1000 * 60 * 2;

	@Override
	public GraffitiLocationParameters getCurrentLocationParameters() {
		GraffitiLocationParameters glp = new GraffitiLocationParameters(_graffitiLocationParameters);
		return glp;

	}
	public void stopListening(){
		Log.d("RealGraffiti", "SensorsGraffitiLocationParametersGeneretor - stop Listening");
		_myLocationManager.removeUpdates(_myLocationListener);
		_mySensorManager.unregisterListener(_magnetlistener);
	}
	
	public SensorsGraffitiLocationParametersGeneretor(Context context) {
		startListening(context);
	}

	@Override
	public boolean isLocationParametersAvailable() {
		return isLocationParametersAvailable;
	};
	
	public void startListening(Context context){
		Log.d("RealGraffiti", "SensorsGraffitiLocationParametersGeneretor - start Listening");
		_myLocationManager = (LocationManager)context.getSystemService(Context.LOCATION_SERVICE);
		_myLocationListener = new MyLocationListener();
		_myLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,_myLocationListener);
		
		//Eitan - Get last known GPS and Network locations and use the better one
		Location gpsloc = _myLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
  	  	Location netloc = _myLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
  	  	
  	  	if(netloc != null){
	  	  	Location loc;
	
	  	  	if (isBetterLocation(netloc, gpsloc))
	  	  		loc = netloc;
	  	  	else
	  	  		loc = gpsloc;
	  	  	
			if(loc != null) {
				_graffitiLocationParameters.setCoordinates(new Coordinates((int)(loc.getLatitude() * E6), (int)(loc.getLongitude() * E6) ));
				isLocationParametersAvailable = true;
			}
  	  	}
		
		// First, get an instance of the SensorManager
	    _mySensorManager = (SensorManager)context.getSystemService(Context.SENSOR_SERVICE);
	    // Second, get the sensor you're interested in
	    Sensor magnetfield = _mySensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
	    // Third, implement a SensorEventListener class
	    _magnetlistener = new SensorEventListener() {
	        public void onAccuracyChanged(Sensor sensor, int accuracy) {
	            // do things if you're interested in accuracy changes
	        }
	        public void onSensorChanged(SensorEvent event) {
	        	//GraffitiLocationParametersGeneratorFactory.setGraffitiLocationOrientation(event.values[0]);
	        	_graffitiLocationParameters.setOrientation(new Orientation(event.values));
	        	/*Log.d("SensorGenerator", "Orientation changed: x:" + event.values[0] +
	        						", y:" + event.values[1] +
	        						", z:" + event.values[2]);*/
	        }
	    };
	    // Finally, register the listener
	    _mySensorManager.registerListener(_magnetlistener, magnetfield, orientationDelay);
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
	
	
	
	private class MyLocationListener implements LocationListener{
		public void onLocationChanged(Location loc) {
			isLocationParametersAvailable = true;
			
			int myLatitude = (int)(loc.getLatitude() * realgraffiti.android.maps.CurrentLocationOverlay.E6);
			int myLongitude = (int)(loc.getLongitude() * realgraffiti.android.maps.CurrentLocationOverlay.E6);
			//GraffitiLocationParametersGeneratorFactory.setGraffitiLocationCoordinates(new Coordinates(myLatitude, myLongitude));
			_graffitiLocationParameters.setCoordinates(new Coordinates(myLatitude, myLongitude));
			Log.d("SensorsGenerator" , "Location changed: " + myLatitude + ", " + myLongitude);
		}
		public void onProviderDisabled(String provider) {}
		public void onProviderEnabled(String provider) {}
		public void onStatusChanged(String provider, int status, Bundle extras) {}
	}

}
