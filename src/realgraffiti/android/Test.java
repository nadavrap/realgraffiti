package realgraffiti.android;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import realgraffiti.android.data.RealGraffitiDataProxy;
import realgraffiti.android.data.RealGraffitiLocalData;
import realgraffiti.common.data.RealGraffitiData;
import realgraffiti.common.dataObjects.*;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

public class Test extends Activity {
	private RealGraffitiData _graffitiData;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        _graffitiData = new RealGraffitiDataProxy(getApplicationContext());
        setContentView(R.layout.main);
        
        Button button = (Button)findViewById(R.id.button1);
        button.setOnClickListener(new OnClickListener() {
		@Override
			public void onClick(View v) {

				Coordinates coordinates = new Coordinates(123, 5433);
				double angle = 30;
				List<Double> siftDisc = new ArrayList<Double>();
				siftDisc.add(1.1);
				siftDisc.add(3.1);
				GraffitiLocationParameters glp = new GraffitiLocationParameters(coordinates, angle, siftDisc);
				
				byte[] imageData = new byte[]{1,2,3,4};
				Graffiti g = new Graffiti(glp);
				g.set_imageData(imageData);
				
				_graffitiData.addNewGraffiti(g);
			}
		});
        
        
        
        button = (Button)findViewById(R.id.getButton);
        button.setOnClickListener(new OnClickListener() {
		
			public void onClick(View v) {
		        GraffitiLocationParameters graffitiLocationParameters = new GraffitiLocationParameters(new Coordinates(123, 5433), 34, null);
				Collection<Graffiti> graffiities = _graffitiData.getNearByGraffiti(graffitiLocationParameters);
				ListView listView = (ListView)findViewById(R.id.listView1);
				listView.setAdapter(new ArrayAdapter<Graffiti>(Test.this, android.R.layout.test_list_item, (List<Graffiti>)graffiities ));
			}
		});
        
        ListView listView = (ListView)findViewById(R.id.listView1);
        listView.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                    int position, long id) {           	
        		ArrayAdapter<Graffiti> adapter = (ArrayAdapter<Graffiti>)parent.getAdapter();
        		Graffiti clickedGraffiti = adapter.getItem(position);
        		
        		byte[] imageData = _graffitiData.getGraffitiImage(clickedGraffiti.getKey());
        		Log.d("realgraffiti", "clicked graffiti: " + imageData.toString());
            }
        });

    }
}