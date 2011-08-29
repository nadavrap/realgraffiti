package realgraffiti.android.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import android.location.Location;

import realgraffiti.common.data.RealGraffitiData;
import realgraffiti.common.dataObjects.*;

public class RealGraffitiLocalData implements RealGraffitiData {

	private List<Graffiti> _graffiteis;

	public RealGraffitiLocalData() {
		_graffiteis = new ArrayList<Graffiti>();
	}

	@Override
	public boolean addNewGraffiti(Graffiti graffiti) {
		graffiti.setKey(new Long(_graffiteis.size()));
		_graffiteis.add(graffiti);
		return true;
	}

	@Override
	public Collection<Graffiti> getNearByGraffiti(
			GraffitiLocationParameters graffitiLocationParameters, int rangeInMeters) {
		List<Graffiti> nearByGraffities = GraffitiUtils.filterGraffitiesByDistance(_graffiteis,graffitiLocationParameters.getCoordinates(), rangeInMeters);
		return new ArrayList<Graffiti>(_graffiteis);
	}

	@Override
	public byte[] getGraffitiImage(Long graffitiKey) {
		byte[] imageData = _graffiteis.get(graffitiKey.intValue()).getImageData();

		return imageData;
	}

	@Override
	public byte[] getGraffitiWallImage(Long graffitiKey) {
		byte[] allImageData = _graffiteis.get(graffitiKey.intValue()).getWallImageData();

		return allImageData;
	}
	


}