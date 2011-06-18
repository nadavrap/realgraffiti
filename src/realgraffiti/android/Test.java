package realgraffiti.android;

import realgraffiti.android.data.RealGraffitiDataProxy;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
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
		        graffitiData.addNewGraffiti(null);
			}
		});

    }
}