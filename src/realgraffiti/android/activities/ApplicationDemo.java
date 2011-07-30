package realgraffiti.android.activities;

import java.util.Collection;
import java.util.List;

import com.google.android.maps.MapActivity;
import realgraffiti.android.R;
import realgraffiti.android.data.GraffitiLocationParametersGeneratorFactory;
import realgraffiti.android.data.RealGraffitiLocalData;
import realgraffiti.android.data.SensorsService;
import realgraffiti.android.maps.GraffitiMiniMapView;
import realgraffiti.common.data.RealGraffitiData;
import realgraffiti.common.dataObjects.*;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;

import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;


public class ApplicationDemo extends MapActivity {
	private RealGraffitiData _graffitiData;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        _graffitiData = new RealGraffitiLocalData();
        setContentView(R.layout.application_demo);       
        
        GraffitiMiniMapView miniMapView = (GraffitiMiniMapView)findViewById(R.id.demo_mini_map);
        miniMapView.setRealGraffitiData(_graffitiData);
        
		setAddNewGraffitiButton();       
        setGetNearByGraffitiButton();
        //Start sensors service
        startService(new Intent(ApplicationDemo.this,SensorsService.class));
    }

    @Override
    protected void onPause() {
        super.onPause();

        GraffitiMiniMapView miniMapView = (GraffitiMiniMapView)findViewById(R.id.demo_mini_map);
        miniMapView.stopOverlays();
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        // The activity has become visible (it is now "resumed").
    }
    
	private void setGetNearByGraffitiButton() {
		Button button = (Button)findViewById(R.id.getButton);
        button.setOnClickListener(new OnClickListener() {
		
			public void onClick(View v) {
		        GraffitiLocationParameters graffitiLocationParameters = new GraffitiLocationParameters(new Coordinates(123, 5433), 34, null);
				Collection<Graffiti> graffiities = _graffitiData.getNearByGraffiti(graffitiLocationParameters);
				ListView listView = (ListView)findViewById(R.id.listView1);
				listView.setAdapter(new ArrayAdapter<Graffiti>(ApplicationDemo.this, android.R.layout.test_list_item, (List<Graffiti>)graffiities ));
			}
		});
	}

	private void setAddNewGraffitiButton() {
		Button button = (Button)findViewById(R.id.add_new_graffiti_button);
        button.setOnClickListener(new OnClickListener() {
		@Override
			public void onClick(View v) {
				GraffitiLocationParameters glp = GraffitiLocationParametersGeneratorFactory.
					getGaffitiLocationParametersGenerator().
					getCurrentLocationParameters();
				
				byte[] imageData = new byte[]{1,2,3,4};
				Graffiti g = new Graffiti(glp);
				g.setImageData(imageData);
				
				_graffitiData.addNewGraffiti(g);
			}
		});
	}

	@Override
	protected boolean isRouteDisplayed() {
		// TODO Auto-generated method stub
		return false;
	}
}