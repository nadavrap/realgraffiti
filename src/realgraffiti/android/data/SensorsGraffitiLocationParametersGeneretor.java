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
	private LocationManager _myLocationManager;
	private MyLocationListener _myLocationListener;
	private boolean isLocationParametersAvailable = false;
	public SensorsGraffitiLocationParametersGeneretor(Context context) {
		startListening(context);
	}

	@Override
	public boolean isLocationParametersAvailable() {
		return isLocationParametersAvailable;
	};
	
	public GraffitiLocationParameters generate(){
		return _graffitiLocationParameters;
	}

	public void startListening(Context context){
		_myLocationManager = (LocationManager)context.getSystemService(Context.LOCATION_SERVICE);
		_myLocationListener = new MyLocationListener();
		_myLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,_myLocationListener);
		
		// First, get an instance of the SensorManager
	    SensorManager sman = (SensorManager)context.getSystemService(Context.SENSOR_SERVICE);
	    // Second, get the sensor you're interested in
	    Sensor magnetfield = sman.getDefaultSensor(Sensor.TYPE_ORIENTATION);
	    // Third, implement a SensorEventListener class
	    SensorEventListener magnetlistener = new SensorEventListener() {
	        public void onAccuracyChanged(Sensor sensor, int accuracy) {
	            // do things if you're interested in accuracy changes
	        }
	        public void onSensorChanged(SensorEvent event) {
	        	//GraffitiLocationParametersGeneratorFactory.setGraffitiLocationOrientation(event.values[0]);
	        	_graffitiLocationParameters.setOrientation(new Orientation(event.values));
	        	Log.d("SensorGenerator", "Orientation changed: x:" + event.values[0] +
	        						", y:" + event.values[1] +
	        						", z:" + event.values[2]);
	        }
	    };
	    // Finally, register the listener
	    sman.registerListener(magnetlistener, magnetfield, orientationDelay);
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
	@Override
	public GraffitiLocationParameters getCurrentLocationParameters() {
		return _graffitiLocationParameters;
	}


}
