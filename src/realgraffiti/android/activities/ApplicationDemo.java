package realgraffiti.android.activities;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import realgraffiti.android.R;
import realgraffiti.android.camera.CameraLiveView;
import realgraffiti.android.data.GraffitiLocationParametersGenerator;
import realgraffiti.android.data.GraffitiLocationParametersGeneratorFactory;
import realgraffiti.android.data.RealGraffitiLocalData;
import realgraffiti.android.maps.GraffitiMiniMapView;
import realgraffiti.common.data.RealGraffitiData;
import realgraffiti.common.dataObjects.Graffiti;
import realgraffiti.common.dataObjects.GraffitiLocationParameters;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.maps.MapActivity;


public class ApplicationDemo extends MapActivity {

	public static final int     VIEW_MODE_RGBA     = 0;
    public static final int     VIEW_MODE_MATCHING = 1;

    public static int           viewMode        = VIEW_MODE_RGBA;
    public static boolean		mResetRef		= false;
    
    public static Bitmap		graffitiBitMap;

	
	protected static final CharSequence NO_LOCATION_AVAILIBLE_MESSAGE = "Location not avilible";
	protected static final int FINGER_PAINT_ACTIVITY = 0;
	private RealGraffitiData _graffitiData;
	private String backgroundLocation = null;
	private CameraLiveView _cameraLiveView;


	/**
	 * Avoid that the screen get's turned off by the system.
	 */
	public void disableScreenTurnOff() {
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
				WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
	}

	/**
	 * Set's the orientation to landscape, as this is needed by AndAR.
	 */
	public void setOrientation() {
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
	}

	/**
	 * Maximize the application.
	 */
	public void setFullscreen() {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
	}

	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d("realgraffiti life cycle", "on craete");
		
		// Load the default graffiti image
		graffitiBitMap = BitmapFactory.decodeResource(getResources(),R.drawable.graffiti);
		
		setOrientation();
		setFullscreen();
		disableScreenTurnOff();
		
		LayoutInflater inflater = getLayoutInflater();
		View tmpView;
		tmpView = inflater.inflate(R.layout.application_demo, null);
//		_cameraLiveView = new CameraLiveView(this);
		_cameraLiveView = (CameraLiveView) findViewById(R.id.cameraLiveView);
		
		addContentView(_cameraLiveView, new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.FILL_PARENT));
		addContentView(tmpView, new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT));

		//_graffitiData = new RealGraffitiDataProxy(this);
		_graffitiData = new RealGraffitiLocalData(this);

		GraffitiMiniMapView miniMapView = (GraffitiMiniMapView)findViewById(R.id.demo_mini_map);
		miniMapView.setRealGraffitiData(_graffitiData);

		setAddNewGraffitiButton();       
		setGetNearByGraffitiButton();

		Log.d("ApplicationDemo","onCreate");
		GraffitiLocationParametersGenerator graffitiLocationParametersGenerator = 
			GraffitiLocationParametersGeneratorFactory.getGaffitiLocationParametersGenerator(ApplicationDemo.this);
	}

	protected void onStart(){
		super.onStart();
		Log.d("realgraffiti life cycle", "on start");
		GraffitiMiniMapView miniMapView = (GraffitiMiniMapView)findViewById(R.id.demo_mini_map);
		miniMapView.startOverlays();
	}

	protected void onRestart(){super.onRestart(); Log.d("realgraffiti life cycle", "on restart");}

	protected void onStop(){
		super.onStop(); 
		Log.d("realgraffiti life cycle", "on stop");
		viewMode = VIEW_MODE_RGBA;
		}

	protected void onDestroy(){super.onDestroy(); Log.d("realgraffiti life cycle", "on distroy");}

	@Override
	protected void onPause() {
		super.onPause();
		Log.d("realgraffiti life cycle", "on pouse");
		GraffitiMiniMapView miniMapView = (GraffitiMiniMapView)findViewById(R.id.demo_mini_map);
		miniMapView.stopOverlays();
	}

	@Override
	protected void onResume() {
		super.onResume();
		Log.d("realgraffiti life cycle", "on resume");
		// The activity has become visible (it is now "resumed").
	}

	private void setGetNearByGraffitiButton() {
/*		Button button = (Button)findViewById(R.id.getButton);
		button.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				GraffitiLocationParameters graffitiLocationParameters = new GraffitiLocationParameters(new Coordinates(123, 5433), 34, null);
				Collection<Graffiti> graffiities = _graffitiData.getNearByGraffiti(graffitiLocationParameters);
				ListView listView = (ListView)findViewById(R.id.listView1);
				listView.setAdapter(new ArrayAdapter<Graffiti>(ApplicationDemo.this, android.R.layout.test_list_item, (List<Graffiti>)graffiities ));
			}
		}); */
	}

	private void setAddNewGraffitiButton() {
		Button button = (Button)findViewById(R.id.demo_add_new_graffiti_button);
		button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				AddNewGraffitiTask addGraffitiTask = new AddNewGraffitiTask();

				GraffitiLocationParametersGenerator graffitiLocationParametersGenerator = GraffitiLocationParametersGeneratorFactory.getGaffitiLocationParametersGenerator(ApplicationDemo.this);


				if(false){
// EITAN - DEBUG	if(graffitiLocationParametersGenerator.isLocationParametersAvailable() == false){
					Toast noLocationAvailibleToast = Toast.makeText(getApplicationContext(), NO_LOCATION_AVAILIBLE_MESSAGE, 1000);
					noLocationAvailibleToast.show();
				} else{
					
					Bitmap graffitiWallImg = _cameraLiveView.getBackgroundImage();
					String filename =getFilesDir()+ "/wall";
					FileOutputStream fos = null;
					try {
						fos = new FileOutputStream(filename);
					} catch (FileNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					graffitiWallImg.compress(Bitmap.CompressFormat.PNG, 90, fos);
					
					
					Intent myIntent = new Intent(ApplicationDemo.this, FingerPaintActivity.class);
					//TODO
					//Need to change the 'Location' to the real location of the image
					myIntent.putExtra(FingerPaintActivity.WALL_IMAGE_LOC, filename);
					startActivityForResult(myIntent, FINGER_PAINT_ACTIVITY);

					GraffitiLocationParameters	glp = graffitiLocationParametersGenerator.getCurrentLocationParameters();

					Log.d("ApplicationDemo","newButton");
					byte [] imageData = createRandomImage();
					Graffiti graffiti = new Graffiti(glp, imageData);
					addGraffitiTask.execute(graffiti);
				}
			}
		});
	}
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch(requestCode) { 
			case (FINGER_PAINT_ACTIVITY) : {
				if (resultCode == RESULT_OK) {
					String newText = data.getStringExtra(FingerPaintActivity.PAINTING_LOC);
					Log.d("Add new Button", "Returned from FingerPaint: Result OK, " + newText);
					backgroundLocation = newText;
					if (backgroundLocation != null) //Background should be taken from given file
					{
						graffitiBitMap = BitmapFactory.decodeFile(backgroundLocation).copy(Bitmap.Config.ARGB_8888, true);
						viewMode = VIEW_MODE_MATCHING;
						mResetRef = true;
					}

					//ApplicationDemo.this.getCurrentFocus().setBackgroundColor(111111);
					//setContentView();
					//setBackgroundColor(111111);
				}
				else {
					//When the user pressed 'Back', or other error
					Log.d("Add new Button", "Returned from FingerPaint: Result Cancled");
				}break;
			}
		}	
	}
@Override
	protected boolean isRouteDisplayed() {
		// TODO Auto-generated method stub
		return false;
	}

	private byte[] createRandomImage(){
		int n = 50*50;
		byte[] imageData = new byte[n];

		for(int i=0;i<n;i++)
			imageData[i] = (byte) (Math.floor(Math.random()*255));

		return imageData;
	}

	private class AddNewGraffitiTask extends AsyncTask<Graffiti, Integer, Boolean> {
		private ProgressDialog _progressDialog; 
		protected Boolean doInBackground(Graffiti... graffiti) {

			//	    	 GraffitiLocationParameters glp = GraffitiLocationParametersGeneratorFactory.
			//				getGaffitiLocationParametersGenerator().
			//				getCurrentLocationParameters();
			GraffitiLocationParameters glp = GraffitiLocationParametersGeneratorFactory.getGaffitiLocationParametersGenerator(getApplicationContext()).getCurrentLocationParameters();
			//	    	 GraffitiLocationParametersGenerator glp = new SensorsGraffitiLocationParametersGeneretor(getApplicationContext());
			//				if (glp instanceof SensorsGraffitiLocationParametersGeneretor)
			//					((SensorsGraffitiLocationParametersGeneretor)glp).startListening(getApplicationContext());
			Log.d("ApplicationDemo","newTask");	

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