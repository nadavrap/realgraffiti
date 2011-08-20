package realgraffiti.android.activities;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
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
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
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
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.maps.MapActivity;


public class RealGraffiti extends Activity {

	public static final int VIEW_MODE_RGBA = 0;
    public static final int VIEW_MODE_MATCHING = 1;
    protected static final CharSequence NO_LOCATION_AVAILIBLE_MESSAGE = "Location not avilible";
	protected static final int FINGER_PAINT_ACTIVITY = 0;
    
    public static int viewMode = VIEW_MODE_RGBA;
    public static boolean _resetRef	= false;
    
    public static Bitmap graffitiBitMap;

	private RealGraffitiData _graffitiData;
	private String _backgroundLocation = null;
	private GraffitiLocationParameters _paintedGraffitiLocationParameters;
	private CameraLiveView _cameraLiveView;
	private GraffitiMiniMapView _miniMapView;

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
		
		setContentView(R.layout.realgraffiti);
			
		_cameraLiveView = (CameraLiveView) findViewById(R.id.cameraLiveView);
		
		//_graffitiData = new RealGraffitiDataProxy(this);
		_graffitiData = new RealGraffitiLocalData();

		_miniMapView = (GraffitiMiniMapView)findViewById(R.id.miniMap);
		_miniMapView.setRealGraffitiData(_graffitiData);

		setAddNewGraffitiButton();
		
		GraffitiLocationParametersGenerator graffitiLocationParametersGenerator = GraffitiLocationParametersGeneratorFactory.getGaffitiLocationParametersGenerator(RealGraffiti.this);
	}

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
	
	protected void onStart(){
		super.onStart();
		Log.d("realgraffiti life cycle", "on start");
		
		_miniMapView.startOverlays();
	}

	protected void onRestart(){
		super.onRestart(); Log.d("realgraffiti life cycle", "on restart");
	}

	protected void onStop(){
		super.onStop(); 
		Log.d("realgraffiti life cycle", "on stop");
		viewMode = VIEW_MODE_RGBA;
	}

	protected void onDestroy(){
		super.onDestroy(); Log.d("realgraffiti life cycle", "on distroy");
	}

	@Override
	protected void onPause() {
		super.onPause();
		Log.d("realgraffiti life cycle", "on pouse");
		_miniMapView.stopOverlays();
	}

	@Override
	protected void onResume() {
		super.onResume();
		Log.d("realgraffiti life cycle", "on resume");
		// The activity has become visible (it is now "resumed").
	}

	private void setAddNewGraffitiButton() {
		Button button = (Button)findViewById(R.id.addNewGraffitiButton);
		button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				GraffitiLocationParametersGenerator graffitiLocationParametersGenerator = 
					GraffitiLocationParametersGeneratorFactory.getGaffitiLocationParametersGenerator(RealGraffiti.this);

				if(false){
// EITAN - DEBUG	if(graffitiLocationParametersGenerator.isLocationParametersAvailable() == false){
					Toast noLocationAvailibleToast = Toast.makeText(getApplicationContext(), NO_LOCATION_AVAILIBLE_MESSAGE, 1000);
					noLocationAvailibleToast.show();
				} else{
					_paintedGraffitiLocationParameters = graffitiLocationParametersGenerator.getCurrentLocationParameters();
					
					Bitmap graffitiWallImg = _cameraLiveView.getLastCameraFrame();
					String filename =getFilesDir()+ "/wall";
					FileOutputStream fos = null;
					try {
						fos = new FileOutputStream(filename);
					} catch (FileNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					graffitiWallImg.compress(Bitmap.CompressFormat.PNG, 90, fos);
					
					Intent myIntent = new Intent(RealGraffiti.this, FingerPaintActivity.class);
					//TODO
					//Need to change the 'Location' to the real location of the image
					myIntent.putExtra(FingerPaintActivity.WALL_IMAGE_LOC, filename);
					startActivityForResult(myIntent, FINGER_PAINT_ACTIVITY);
					
					Log.d("realgraffiti","New Button pressed");
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
					
					_backgroundLocation = newText;
					
					if (_backgroundLocation != null) //Background should be taken from given file
					{
						graffitiBitMap = BitmapFactory.decodeFile(_backgroundLocation).copy(Bitmap.Config.ARGB_8888, true);
						viewMode = VIEW_MODE_MATCHING;
						_resetRef = true;
					}
					
					ByteArrayOutputStream os = new ByteArrayOutputStream();
					graffitiBitMap.compress(CompressFormat.PNG, 10, os);
					//ByteArrayInputStream byteInputStream = new ByteArrayInputStream(imageData);
					byte[] imageData = os.toByteArray();
					
					
					AddNewGraffitiTask addGraffitiTask = new AddNewGraffitiTask();
					Graffiti graffiti = new Graffiti(_paintedGraffitiLocationParameters, imageData);
					_paintedGraffitiLocationParameters = null;
					addGraffitiTask.execute(graffiti);
				}
				else {
					//When the user pressed 'Back', or other error
					Log.d("Add new Button", "Returned from FingerPaint: Result Cancled");
				}break;
			}
		}	
	}

	private class AddNewGraffitiTask extends AsyncTask<Graffiti, Integer, Boolean> {
		private ProgressDialog _progressDialog; 
		protected Boolean doInBackground(Graffiti... graffiti) {
			Log.d("ApplicationDemo","newTask");	
			return _graffitiData.addNewGraffiti(graffiti[0]);
		}

		protected void onPreExecute() {
			_progressDialog = ProgressDialog.show(RealGraffiti.this, "", 
					"Saving... Please wait.", true);
		}

		protected void onPostExecute(Boolean result) {
			_progressDialog.dismiss();
		}
	}
}
