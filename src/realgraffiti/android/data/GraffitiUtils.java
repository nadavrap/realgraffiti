package realgraffiti.android.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import realgraffiti.common.dataObjects.Coordinates;
import realgraffiti.common.dataObjects.Graffiti;
import android.location.Location;

public class GraffitiUtils {
	public static List<Graffiti> filterGraffitiesByDistance(
			Collection<Graffiti> graffities,Coordinates currentCoordinates, int distanceInMeters) {
		
		List<Graffiti> nearByGraffiti = new ArrayList<Graffiti>();
		Location currentLocation = coordinatesToLocation(currentCoordinates);
		
		for(Graffiti graffiti : graffities){
			Location graffitiLocation = coordinatesToLocation(graffiti.getLocationParameters().getCoordinates());
			if(currentLocation.distanceTo(graffitiLocation) < distanceInMeters){
				nearByGraffiti.add(graffiti);
			}
		}
		
		return nearByGraffiti;
	}

	public static Location coordinatesToLocation(Coordinates coordinates) {
		Location location = new Location("My provider");
		location.setLatitude(coordinates.getLatitude());
		location.setLongitude(coordinates.getLongitude());
		return location;
	}
}
