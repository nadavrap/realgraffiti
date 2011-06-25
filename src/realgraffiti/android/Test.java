package realgraffiti.android;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import realgraffiti.android.data.RealGraffitiDataProxy;
import realgraffiti.android.data.RealGraffitiLocalData;
import realgraffiti.common.data.RealGraffitiData;
import realgraffiti.common.dto.GraffitiDto;
import realgraffiti.common.dto.GraffitiLocationParametersDto;
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
        _graffitiData = new RealGraffitiLocalData();
        setContentView(R.layout.main);
        
        Button button = (Button)findViewById(R.id.button1);
        button.setOnClickListener(new OnClickListener() {
		@Override
			public void onClick(View v) {

				String coordinates = "N234, E2345";
				double angle = 30;
				List<Double> siftDisc = new ArrayList<Double>();
				siftDisc.add(1.1);
				siftDisc.add(3.1);
				GraffitiLocationParametersDto glp = new GraffitiLocationParametersDto(coordinates, angle, siftDisc);
				
				byte[] imageData = new byte[]{1,2,3,4};
				GraffitiDto g = new GraffitiDto(glp);
				g.set_imageData(imageData);
				
				_graffitiData.addNewGraffiti(g);
			}
		});
        
        
        
        button = (Button)findViewById(R.id.getButton);
        button.setOnClickListener(new OnClickListener() {
		
			public void onClick(View v) {
		        GraffitiLocationParametersDto graffitiLocationParameters = new GraffitiLocationParametersDto("asdf", 34, null);
				Collection<GraffitiDto> graffiities = _graffitiData.getNearByGraffiti(graffitiLocationParameters);
				ListView listView = (ListView)findViewById(R.id.listView1);
				listView.setAdapter(new ArrayAdapter<GraffitiDto>(Test.this, android.R.layout.test_list_item, (List<GraffitiDto>)graffiities ));
			}
		});
        
        ListView listView = (ListView)findViewById(R.id.listView1);
        listView.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                    int position, long id) {           	
        		ArrayAdapter<GraffitiDto> adapter = (ArrayAdapter<GraffitiDto>)parent.getAdapter();
        		GraffitiDto clickedGraffiti = adapter.getItem(position);
        		
        		byte[] imageData = _graffitiData.getGraffitiImage(clickedGraffiti.getKey());
        		Log.d("realgraffiti", "clicked graffiti: " + imageData.toString());
            }
        });

    }
}