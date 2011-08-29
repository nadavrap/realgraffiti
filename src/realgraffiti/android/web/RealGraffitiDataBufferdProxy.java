package realgraffiti.android.web;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.OverlayItem;

import android.content.Context;
import android.util.Log;

import realgraffiti.android.data.GraffitiPoller;
import realgraffiti.android.data.GraffitiUtils;
import realgraffiti.common.data.RealGraffitiData;
import realgraffiti.common.dataObjects.Coordinates;
import realgraffiti.common.dataObjects.Graffiti;
import realgraffiti.common.dataObjects.GraffitiLocationParameters;

public class RealGraffitiDataBufferdProxy implements RealGraffitiData {
	private List<Graffiti> _buffredGraffiti;
	private RealGraffitiData _realGraffitiData;
	private GraffitiPoller _graffitiPoller;
	
	public RealGraffitiDataBufferdProxy(Context context, GraffitiPoller graffitiPoller){
		_buffredGraffiti = new ArrayList<Graffiti>();
		_realGraffitiData = graffitiPoller.getPolledRealGraffitiData();
		_graffitiPoller = graffitiPoller;
		
		_graffitiPoller.setOnPoll(new GraffitiPollListener() {
			@Override
			public void onPollingData(Collection<Graffiti> graffities) {
				if(graffities != null && graffities.size() > 0){
					_buffredGraffiti = new ArrayList<Graffiti>(graffities);
					
					
				}
				Log.d("realgraffiti", "poll data of size: " + graffities.size());
			}});
	}
	
	@Override
	public boolean addNewGraffiti(Graffiti graffiti) {
		return _realGraffitiData.addNewGraffiti(graffiti);
	}

	@Override
	public Collection<Graffiti> getNearByGraffiti(
			GraffitiLocationParameters graffitiLocationParameters, int rangeInMeters) {
		// TODO Auto-generated method stub
		return GraffitiUtils.filterGraffitiesByDistance(_buffredGraffiti, graffitiLocationParameters.getCoordinates(), rangeInMeters);
	}

	@Override
	public byte[] getGraffitiImage(Long graffitiKey) {
		// TODO Auto-generated method stub
		return _realGraffitiData.getGraffitiImage(graffitiKey);
	}

	@Override
	public byte[] getGraffitiWallImage(Long graffitiKey) {
		return _realGraffitiData.getGraffitiWallImage(graffitiKey);
	}
}
