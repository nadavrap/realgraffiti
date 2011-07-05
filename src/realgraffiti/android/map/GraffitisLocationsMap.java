package realgraffiti.android.map;

import java.util.Collection;
import java.util.List;

import realgraffiti.android.R;
import realgraffiti.android.R.id;
import realgraffiti.android.R.layout;
import realgraffiti.android.data.GraffitiLocationParametersGeneratorFactory;
import realgraffiti.common.data.RealGraffitiData;
import realgraffiti.common.dataObjects.Graffiti;
import realgraffiti.common.dataObjects.GraffitiLocationParameters;
import realgraffiti.android.data.*;
import realgraffiti.android.web.GraffitiServerPoller;
import realgraffiti.android.web.GraffitiPollListener;
import realgraffiti.android.web.RealGraffitiDataProxy;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;

public class GraffitisLocationsMap extends MapActivity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.mapview);
	    
	    MapView mapView = (MapView) findViewById(R.id.mapview);
	    mapView.setBuiltInZoomControls(true);
	    
	    final List<Overlay> mapOverlays = mapView.getOverlays();
	    Drawable drawable = this.getResources().getDrawable(R.drawable.spraycan);
	    final GraffitiItemizedOverlay itemizedoverlay = new GraffitiItemizedOverlay(drawable);
	    
	    final RealGraffitiData graffitiData =  new RealGraffitiLocalData();
	    //RealGraffitiData graffitiData = new RealGraffitiDataProxy(getApplicationContext());
	    
	    final GraffitiLocationParametesrGenerator graffitiLocationParametesrGenerator = GraffitiLocationParametersGeneratorFactory.getGaffitiLocationParametersGenerator();
	    
	    //graffitiData.addNewGraffiti(new Graffiti(graffitiLocationParametesrGenerator.getCurrentLocationParameters(),"",new byte[] {1,2,3,4}));
	    graffitiData.addNewGraffiti(new Graffiti(graffitiLocationParametesrGenerator.getCurrentLocationParameters()));
	    graffitiData.addNewGraffiti(new Graffiti(graffitiLocationParametesrGenerator.getCurrentLocationParameters()));
	    graffitiData.addNewGraffiti(new Graffiti(graffitiLocationParametesrGenerator.getCurrentLocationParameters()));
	    
	        
	    GraffitiServerPoller serverPoller = new GraffitiServerPoller(graffitiData, 10000);
	    serverPoller.setOnPoll(new GraffitiPollListener() {
			@Override
			public void onPollingData(Collection<Graffiti> graffities) {
				if(graffities != null){
					itemizedoverlay.addGraffities(graffities);
				    mapOverlays.add(itemizedoverlay);
				}
				
				Log.d("realgraffiti", "poll data of size: " + graffities.size());
			}
		});
	    
	    
	    serverPoller.beginPolling();
	    
	    Button button = (Button)findViewById(R.id.button1);
	    button.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				graffitiData.addNewGraffiti(new Graffiti(graffitiLocationParametesrGenerator.getCurrentLocationParameters()));
			}
		});
	}
	
	@Override
	protected boolean isRouteDisplayed() {
		// TODO Auto-generated method stub
		return false;
	}
	
	
}
