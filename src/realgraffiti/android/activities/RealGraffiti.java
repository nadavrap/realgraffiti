package realgraffiti.android.activities;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.HashMap;

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
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;


public class RealGraffiti extends Activity {

//	public static final int VIEW_MODE_RGBA = 0;
//    public static final int VIEW_MODE_MATCHING = 1;
    public static final int		SOUND_ID_SHUTTER	= 1;

    protected static final CharSequence NO_LOCATION_AVAILIBLE_MESSAGE = "Location not available";
	protected static final int FINGER_PAINT_ACTIVITY = 0;
    
//    public static int viewMode = VIEW_MODE_RGBA;
    public static boolean _resetRef	= false;
    
    public static Bitmap graffitiBitMap;
    public static Bitmap spraycanBitmap;
    public static Bitmap rgLogoBitmap;

	private RealGraffitiData _graffitiData;
	private String _backgroundLocation = null;
	private GraffitiLocationParameters _paintedGraffitiLocationParameters;
	private CameraLiveView _cameraLiveView;
	private GraffitiMiniMapView _miniMapView;
	private SoundPool soundPool;
	private HashMap<Integer, Integer> soundPoolMap;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//Log.d("RealGraffiti", "on craete");
		
		// Load the default graffiti image
		graffitiBitMap = BitmapFactory.decodeResource(getResources(),R.drawable.graffiti);
		spraycanBitmap = BitmapFactory.decodeResource(getResources(),R.drawable.spraycanlarge);
		rgLogoBitmap = BitmapFactory.decodeResource(getResources(),R.drawable.rglogo);
		
		// Prepare some sound effects
		soundPool = new SoundPool(4,AudioManager.STREAM_MUSIC,100);
	    soundPoolMap = new HashMap<Integer, Integer>();
	    soundPoolMap.put(SOUND_ID_SHUTTER, soundPool.load(this, R.raw.shutter, 1));
	    
		setOrientation();
		setFullscreen();
		disableScreenTurnOff();
		
		setContentView(R.layout.realgraffiti);
			
		_cameraLiveView = (CameraLiveView) findViewById(R.id.cameraLiveView);
		
		//_graffitiData = new RealGraffitiDataProxy(this);
		_graffitiData = new RealGraffitiLocalData(this);

		_miniMapView = (GraffitiMiniMapView)findViewById(R.id.miniMap);
		_miniMapView.setRealGraffitiData(_graffitiData);

		setAddNewGraffitiButton();
		
		GraffitiLocationParametersGenerator graffitiLocationParametersGenerator = 
			GraffitiLocationParametersGeneratorFactory.getGaffitiLocationParametersGenerator(RealGraffiti.this);
		Log.d("RealGraffiti", "on create done");
	}

	/**
	 * Avoid that the screen get's turned off by the system.
	 */
	private void disableScreenTurnOff() {
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
				WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
	}

	/**
	 * Set's the orientation to landscape, as this is needed by AndAR.
	 */
	private void setOrientation() {
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
	}

	/**
	 * Maximize the application.
	 */
	private void setFullscreen() {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
	}

	private void playSound(int soundID)
	{
	    AudioManager mgr = (AudioManager)this.getSystemService(Context.AUDIO_SERVICE);
	    float streamVolumeCurrent = mgr.getStreamVolume(AudioManager.STREAM_MUSIC);
	    float streamVolumeMax = mgr.getStreamMaxVolume(AudioManager.STREAM_MUSIC);    
	    float volume = streamVolumeCurrent / streamVolumeMax;
		soundPool.play(soundID, volume, volume, 1, 0, 1f);
	}
	
	protected void onStart(){
		super.onStart();
		Log.d("RealGraffiti", "on start");
		
		_miniMapView.startOverlays();
	}

	protected void onRestart(){
		super.onRestart(); Log.d("RealGraffiti", "on restart");
	}

	protected void onStop(){
		super.onStop(); 
		Log.d("RealGraffiti", "on stop");
	}

	protected void onDestroy(){
		super.onDestroy(); Log.d("RealGraffiti", "on destroy");
	}

	@Override
	protected void onPause() {
		super.onPause();
		Log.d("RealGraffiti", "on pouse");
		_miniMapView.stopOverlays();
	}

	@Override
	protected void onResume() {
		super.onResume();
		Log.d("RealGraffiti", "on resume");
		// The activity has become visible (it is now "resumed").
	}

	private void setAddNewGraffitiButton() {
		Button button = (Button)findViewById(R.id.addNewGraffitiButton);
		button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				_cameraLiveView.setViewMode(CameraLiveView.VIEW_MODE_PAINTING);
				playSound(SOUND_ID_SHUTTER);
				GraffitiLocationParametersGenerator graffitiLocationParametersGenerator = 
					GraffitiLocationParametersGeneratorFactory.getGaffitiLocationParametersGenerator(RealGraffiti.this);

				if(false){
// EITAN - DEBUG	if(graffitiLocationParametersGenerator.isLocationParametersAvailable() == false){
					Toast noLocationAvailibleToast = Toast.makeText(getApplicationContext(), NO_LOCATION_AVAILIBLE_MESSAGE, 1000);
					noLocationAvailibleToast.show();
				} else{
					_paintedGraffitiLocationParameters = graffitiLocationParametersGenerator.getCurrentLocationParameters();
					
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
					
					Intent myIntent = new Intent(RealGraffiti.this, FingerPaintActivity.class);
					//TODO
					//Need to change the 'Location' to the real location of the image
					myIntent.putExtra(FingerPaintActivity.WALL_IMAGE_LOC, filename);
					startActivityForResult(myIntent, FINGER_PAINT_ACTIVITY);
					
					Log.d("RealGraffiti","New Button pressed");
				}
			}
		});
	}
	
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch(requestCode) { 
			case (FINGER_PAINT_ACTIVITY) : {
				if (resultCode == RESULT_OK) {
					String newText = data.getStringExtra(FingerPaintActivity.PAINTING_LOC);
					Log.d("RealGraffiti", "Returned from FingerPaint: Result OK, " + newText);
					
					_backgroundLocation = newText;
					
					if (_backgroundLocation != null) //Background should be taken from given file
					{
						graffitiBitMap = BitmapFactory.decodeFile(_backgroundLocation).copy(Bitmap.Config.ARGB_8888, true);
						_cameraLiveView.setViewMode(CameraLiveView.VIEW_MODE_VIEWING);
					}
					else
						_cameraLiveView.setViewMode(CameraLiveView.VIEW_MODE_IDLE);
					
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
					_cameraLiveView.setViewMode(CameraLiveView.VIEW_MODE_IDLE);
					Log.d("RealGraffiti", "Returned from FingerPaint: Result Cancled");
				}break;
			}
		}	
	}

	private class AddNewGraffitiTask extends AsyncTask<Graffiti, Integer, Boolean> {
		private ProgressDialog _progressDialog; 
		protected Boolean doInBackground(Graffiti... graffiti) {
			Log.d("RealGraffiti","newTask");	
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
