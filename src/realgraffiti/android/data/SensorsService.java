package realgraffiti.android.data;

import realgraffiti.common.dataObjects.Coordinates;
import realgraffiti.common.dataObjects.GraffitiLocationParameters;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

public class SensorsService extends Service {
	private final int orientationDelay = SensorManager.SENSOR_DELAY_NORMAL;
	private LocationManager myLocationManager;
	private MyLocationListener myLocationListener;
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public void onCreate() {
		super.onCreate();
		myLocationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
		myLocationListener = new MyLocationListener();
		myLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,myLocationListener);
		
		// First, get an instance of the SensorManager
	    SensorManager sman = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
	    // Second, get the sensor you're interested in
	    Sensor magnetfield = sman.getDefaultSensor(Sensor.TYPE_ORIENTATION);
	    // Third, implement a SensorEventListener class
	    SensorEventListener magnetlistener = new SensorEventListener() {
	        public void onAccuracyChanged(Sensor sensor, int accuracy) {
	            // do things if you're interested in accuracy changes
	        }
	        public void onSensorChanged(SensorEvent event) {
	        	GraffitiLocationParametersGeneratorFactory.setGraffitiLocationOrientation(event.values[0]);
//	        	_locParam.setAngle(event.values[0]);
	        	Log.d("orientaion", "changed: " + event.values[0]);
	        }
	    };
	    // Finally, register the listener
	    sman.registerListener(magnetlistener, magnetfield, orientationDelay);
	    
		//LocationManager myLocationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
	}
	private class MyLocationListener implements LocationListener{
		public void onLocationChanged(Location loc) {
			int myLatitude = (int)(loc.getLatitude() * realgraffiti.android.maps.CurrentLocationOverlay.E6);
			int myLongitude = (int)(loc.getLongitude() * realgraffiti.android.maps.CurrentLocationOverlay.E6);
			//_locParam.setCoordinates(new Coordinates(myLongitude, myLongitude));
			GraffitiLocationParametersGeneratorFactory.setGraffitiLocationCoordinates(new Coordinates(myLatitude, myLongitude));
			Log.d("Location" , "Changed: " + myLatitude + ", " + myLongitude);
		}
		public void onProviderDisabled(String provider) {}
		public void onProviderEnabled(String provider) {}
		public void onStatusChanged(String provider, int status, Bundle extras) {}
	};
}
