package realgraffiti.android;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import realgraffiti.android.data.RealGraffitiDataProxy;
import realgraffiti.common.dto.GraffitiDto;
import realgraffiti.common.dto.GraffitiLocationParametersDto;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

public class Test extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        Button button = (Button)findViewById(R.id.button1);
        button.setOnClickListener(new OnClickListener() {
		@Override
			public void onClick(View v) {
				RealGraffitiDataProxy graffitiData = new RealGraffitiDataProxy(getApplicationContext());
				
				String coordinates = "N234, E2345";
				double angle = 30;
				List<Double> siftDisc = new ArrayList<Double>();
				siftDisc.add(1.1);
				siftDisc.add(3.1);
				GraffitiLocationParametersDto glp = new GraffitiLocationParametersDto(coordinates, angle, siftDisc);
				
				byte[] imageData = new byte[]{1,2,3,4};
				GraffitiDto g = new GraffitiDto(glp);
				g.set_imageData(imageData);
				
		        graffitiData.addNewGraffiti(g);
			}
		});
        
        
        
        button = (Button)findViewById(R.id.getButton);
        button.setOnClickListener(new OnClickListener() {
		@Override
			public void onClick(View v) {
				RealGraffitiDataProxy graffitiData = new RealGraffitiDataProxy(getApplicationContext());
				
		        GraffitiLocationParametersDto graffitiLocationParameters = new GraffitiLocationParametersDto("asdf", 34, null);
				Collection<GraffitiDto> graffiities = graffitiData.getNearByGraffiti(graffitiLocationParameters);
				ListView listView = (ListView)findViewById(R.id.listView1);
				listView.setAdapter(new ArrayAdapter<GraffitiDto>(Test.this, android.R.layout.test_list_item, (List<GraffitiDto>)graffiities ));
			}
		});

    }
}