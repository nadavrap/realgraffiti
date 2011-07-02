package realgraffiti.android.map;

import java.util.Collection;
import java.util.List;

import realgraffiti.android.R;
import realgraffiti.android.R.id;
import realgraffiti.android.R.layout;
import realgraffiti.android.data.GraffitiLocationParametersGeneratorFactory;
import realgraffiti.android.data.RealGraffitiDataProxy;
import realgraffiti.common.data.RealGraffitiData;
import realgraffiti.common.dataObjects.Graffiti;
import realgraffiti.common.dataObjects.GraffitiLocationParameters;
import realgraffiti.android.data.*;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

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
	    
	    List<Overlay> mapOverlays = mapView.getOverlays();
	    Drawable drawable = this.getResources().getDrawable(R.drawable.spraycan);
	    GraffitiItemizedOverlay itemizedoverlay = new GraffitiItemizedOverlay(drawable);
	    
	    RealGraffitiData graffitiData =  new RealGraffitiLocalData();
	    
	    GraffitiLocationParametesrGenerator graffitiLocationParametesrGenerator = GraffitiLocationParametersGeneratorFactory.getGaffitiLocationParametersGenerator();
	    
	    graffitiData.addNewGraffiti(new Graffiti(graffitiLocationParametesrGenerator.getCurrentLocationParameters()));
	    graffitiData.addNewGraffiti(new Graffiti(graffitiLocationParametesrGenerator.getCurrentLocationParameters()));
	    graffitiData.addNewGraffiti(new Graffiti(graffitiLocationParametesrGenerator.getCurrentLocationParameters()));
	    graffitiData.addNewGraffiti(new Graffiti(graffitiLocationParametesrGenerator.getCurrentLocationParameters()));
	    
	    GraffitiLocationParameters glp = graffitiLocationParametesrGenerator.getCurrentLocationParameters();
	    Collection<Graffiti> graffities = graffitiData.getNearByGraffiti(glp);

	    itemizedoverlay.setGraffitis(graffities);
	    mapOverlays.add(itemizedoverlay);
	}
	
	@Override
	protected boolean isRouteDisplayed() {
		// TODO Auto-generated method stub
		return false;
	}
	
	
}
