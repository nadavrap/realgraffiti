package realgraffiti.android.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import realgraffiti.common.data.RealGraffitiData;
import realgraffiti.common.dataObjects.*;

public class RealGraffitiLocalData implements RealGraffitiData {

	private List<Graffiti> _graffiteis;

	public RealGraffitiLocalData() {
		_graffiteis = new ArrayList<Graffiti>();
	}

	@Override
	public boolean addNewGraffiti(Graffiti GraffitiDto) {
		GraffitiDto.setKey(new Long(_graffiteis.size()));
		_graffiteis.add(GraffitiDto);
		return true;
	}

	@Override
	public Collection<Graffiti> getNearByGraffiti(
			GraffitiLocationParameters graffitiLocationParameters) {
		return new ArrayList<Graffiti>(_graffiteis);
	}

	@Override
	public byte[] getGraffitiImage(Long graffitiKey) {
		int n = 16;
		int colorNumber = 256;
		byte[] imageData = new byte[n];
		for (int i = 0; i < n; i++) {
			imageData[i] = (byte) Math.floor(Math.random() * colorNumber);
		}

		return imageData;
	}

}