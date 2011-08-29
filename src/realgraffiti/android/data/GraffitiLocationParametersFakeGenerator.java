package realgraffiti.android.data;

import java.util.ArrayList;
import java.util.List;

import android.util.Log;

import realgraffiti.common.dataObjects.Coordinates;
import realgraffiti.common.dataObjects.GraffitiLocationParameters;
import realgraffiti.common.dataObjects.Orientation;

public class GraffitiLocationParametersFakeGenerator implements
		GraffitiLocationParametersGenerator {
	@Override
	public GraffitiLocationParameters getCurrentLocationParameters() {	
		double latitudeFrom = 48.645;
		double latitudeTo = 48.655;
		
		double longtitudeFrom = 2.345;
		double longtitudeTo = 2.355;
		
		int latitude = (int) ((Math.random()*(latitudeTo - latitudeFrom) + latitudeFrom)*1000000);
		int longtitude = (int) ((Math.random()*(longtitudeTo - longtitudeFrom) + longtitudeFrom)*1000000);
		
		Log.d("realgraffiti", "lat:" + latitude + " long: " + longtitude);
		GraffitiLocationParameters locationParameter = new GraffitiLocationParameters(
				new Coordinates(latitude, longtitude),
				new Orientation(new float[]{1,2,3})
			);
		
		return locationParameter;
	}

	@Override
	public boolean isLocationParametersAvailable() {
		// TODO Auto-generated method stub
		return true;
	}
}
