package realgraffiti.android.map;

import java.util.Collection;
import java.util.List;

import realgraffiti.android.R;
import realgraffiti.android.web.GraffitiPollListener;
import realgraffiti.android.web.GraffitiServerPoller;
import realgraffiti.common.data.RealGraffitiData;
import realgraffiti.common.dataObjects.Graffiti;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.widget.LinearLayout;

import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;

public class GraffitiMapView extends LinearLayout  {
	private MapView _mapView;
	private RealGraffitiData _realGraffitiData;
	
	private GraffitiServerPoller _graffitiServerPoller = null;
	

	private static final int DATA_POLLING_INTERVAL = 10000;
	
	public GraffitiMapView(Context context, MapView mapView, RealGraffitiData realGraffitiData){
		super(context);
		
		_mapView = mapView;
		
		addView(_mapView);
		    
	    _realGraffitiData = realGraffitiData;
	}
	
	public void startPollingForGraffities(){
		if(_graffitiServerPoller == null){
			initializeGraffitiServerPoller();
		}
		
		_graffitiServerPoller.beginPolling();
	}
	
	public void stopPollingForGraffities(){
		_graffitiServerPoller.stopPolling();
	}

	private void initializeGraffitiServerPoller() {
	    _graffitiServerPoller = new GraffitiServerPoller(_realGraffitiData, DATA_POLLING_INTERVAL);
	    
	    Drawable drawable = this.getResources().getDrawable(R.drawable.spraycan);
	    final GraffitiItemizedOverlay itemizedoverlay = new GraffitiItemizedOverlay(drawable);
	    
	    _graffitiServerPoller.setOnPoll(new GraffitiPollListener() {
			@Override
			public void onPollingData(Collection<Graffiti> graffities) {
				if(graffities != null && graffities.size() > 0){
					List<Overlay> mapOverlays = _mapView.getOverlays();
					itemizedoverlay.addGraffities(graffities);
				    mapOverlays.add(itemizedoverlay);
				    _mapView.invalidate();
				}
				Log.d("realgraffiti", "poll data of size: " + graffities.size());
			}
		});
	}
}
