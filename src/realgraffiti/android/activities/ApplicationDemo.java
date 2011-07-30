package realgraffiti.android.activities;

import java.util.Collection;
import java.util.List;

import com.google.android.maps.MapActivity;
import realgraffiti.android.R;
import realgraffiti.android.data.GraffitiLocationParametersGeneratorFactory;
import realgraffiti.android.data.RealGraffitiLocalData;
import realgraffiti.android.data.SensorsService;
import realgraffiti.android.maps.GraffitiMiniMapView;
import realgraffiti.android.web.RealGraffitiDataProxy;
import realgraffiti.common.data.RealGraffitiData;
import realgraffiti.common.dataObjects.*;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
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
       // _graffitiData = new RealGraffitiDataProxy(this);
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
				AddNewGraffitiTask addGraffitiTask = new AddNewGraffitiTask();
				GraffitiLocationParameters glp = GraffitiLocationParametersGeneratorFactory.
					getGaffitiLocationParametersGenerator().getCurrentLocationParameters();
				
				byte [] imageData = new byte[]{1,2,3,4};
				Graffiti graffiti = new Graffiti(glp, imageData);
				addGraffitiTask.execute(graffiti);
			}
		});
	}

	@Override
	protected boolean isRouteDisplayed() {
		// TODO Auto-generated method stub
		return false;
	}
	
	private byte[] createRandomImage(){
		int n = 10000;
		byte[] imageData = new byte[n];
		
		for(int i=0;i<n;i++)
			imageData[i] = (byte) (Math.floor(Math.random()*255));
		
		return imageData;
	}
	
	private class AddNewGraffitiTask extends AsyncTask<Graffiti, Integer, Boolean> {
		 private ProgressDialog _progressDialog; 
	     protected Boolean doInBackground(Graffiti... graffiti) {
	    	 
	    	 GraffitiLocationParameters glp = GraffitiLocationParametersGeneratorFactory.
				getGaffitiLocationParametersGenerator().
				getCurrentLocationParameters();
			
			return _graffitiData.addNewGraffiti(graffiti[0]);
	     }

	     protected void onPreExecute() {
	    	 _progressDialog = ProgressDialog.show(ApplicationDemo.this, "", 
                     "Saving... Please wait.", true);
	     }
	     
	     protected void onPostExecute(Boolean result) {
	    	 _progressDialog.dismiss();
	     }
	 }
}