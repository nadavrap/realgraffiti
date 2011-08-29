package realgraffiti.android.activities;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import realgraffiti.android.R;
import realgraffiti.android.camera.CameraLiveView;
import realgraffiti.android.data.GraffitiLocationParametersGenerator;
import realgraffiti.android.data.GraffitiLocationParametersGeneratorFactory;
import realgraffiti.android.data.GraffitiPoller;
import realgraffiti.android.data.RealGraffitiLocalData;
import realgraffiti.android.data.SensorsGraffitiLocationParametersGeneretor;
import realgraffiti.android.maps.GraffitiMiniMapView;
import realgraffiti.android.web.RealGraffitiDataBufferdProxy;
import realgraffiti.common.data.RealGraffitiData;
import realgraffiti.common.dataObjects.Graffiti;
import realgraffiti.common.dataObjects.GraffitiLocationParameters;
import realgraffiti.common.dataObjects.Orientation;
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
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

public class RealGraffiti extends Activity {

	// public static final int VIEW_MODE_RGBA = 0;
	// public static final int VIEW_MODE_MATCHING = 1;
	public static final int SOUND_ID_SHUTTER 	= 1;
	public static final int SOUND_ID_TING 		= 2;
	
	private static final int POLLING_INTERVAL = 5000;
	private static final int GRAFFITI_UPDATE_INTERVAL = 3000;
	private static final int ORIENTAION_MATCH_DIFFERENCE = 30;
	
	protected static final CharSequence NO_LOCATION_AVAILIBLE_MESSAGE = "Location not available";
	protected static final int FINGER_PAINT_ACTIVITY = 0;
	private static final int PROXIMITY_GRAFFITI_RANGE = 15;
	private static final int BUFFERED_GRAFFITI_RANGE = 15000;

	// public static int viewMode = VIEW_MODE_RGBA;
	public static boolean _resetRef = false;

	public static Bitmap spraycanBitmap;
	public static Bitmap rgLogoBitmap;

	private RealGraffitiData _graffitiData;
	private GraffitiPoller _graffitiPoller;
	private String _backgroundLocation = null;
	private CameraLiveView _cameraLiveView;
	private GraffitiMiniMapView _miniMapView;
	private SoundPool soundPool;
	private HashMap<Integer, Integer> soundPoolMap;
	private Graffiti _paintedGraffiti;
	private Timer _graffitiUpdateTimer;
	private long currentGraffitiKey = 0;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Log.d("RealGraffiti", "on craete");

		// Load some images
		spraycanBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.spraycanlarge);
		rgLogoBitmap = BitmapFactory.decodeResource(getResources(),	R.drawable.rglogo);

		// Prepare some sound effects
		soundPool = new SoundPool(4, AudioManager.STREAM_MUSIC, 100);
		soundPoolMap = new HashMap<Integer, Integer>();
		soundPoolMap.put(SOUND_ID_SHUTTER, soundPool.load(this, R.raw.shutter, 1));
		soundPoolMap.put(SOUND_ID_TING, soundPool.load(this, R.raw.ting, 1));

		setOrientation();
		setFullscreen();
		disableScreenTurnOff();

		setContentView(R.layout.realgraffiti);

		_cameraLiveView = (CameraLiveView) findViewById(R.id.cameraLiveView);

		// change the comment between the following lines to switch between
		// server and local storage:

		// RealGraffitiData innerGraffitiData = new RealGraffitiDataProxy(getApplicationContext()); // server storage
		RealGraffitiData innerGraffitiData = new RealGraffitiLocalData(); // local storage

		_graffitiPoller = new GraffitiPoller(getApplicationContext(), innerGraffitiData,BUFFERED_GRAFFITI_RANGE,  POLLING_INTERVAL);
		_graffitiData = new RealGraffitiDataBufferdProxy(getApplicationContext(), _graffitiPoller);

		_miniMapView = (GraffitiMiniMapView) findViewById(R.id.miniMap);
		_miniMapView.setRealGraffitiData(_graffitiData);

		setAddNewGraffitiButton();

		//GraffitiLocationParametersGenerator graffitiLocationParametersGenerator = GraffitiLocationParametersGeneratorFactory
		//		.getGaffitiLocationParametersGenerator(RealGraffiti.this);
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
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
	}

	private void playSound(int soundID) {
		AudioManager mgr = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
		float streamVolumeCurrent = mgr.getStreamVolume(AudioManager.STREAM_RING);
		float streamVolumeMax = mgr.getStreamMaxVolume(AudioManager.STREAM_RING);
		float volume = streamVolumeCurrent / streamVolumeMax;
		soundPool.play(soundID, volume, volume, 1, 0, 1f);
	}

	private void graffitiUpdateTimerTick() {
		 Collection<Graffiti> graffities = null;
		 GraffitiLocationParametersGenerator graffitiLocationParametersGenerator = GraffitiLocationParametersGeneratorFactory
			.getGaffitiLocationParametersGenerator(getApplicationContext());

		 // Get the current orientation
		 GraffitiLocationParameters currentLocationParameters = graffitiLocationParametersGenerator.getCurrentLocationParameters();
		 
		 if(!graffitiLocationParametersGenerator.isLocationParametersAvailable())
			 return;
		 
		 graffities = _graffitiData.getNearByGraffiti(graffitiLocationParametersGenerator.getCurrentLocationParameters(), PROXIMITY_GRAFFITI_RANGE);
		 
		 if(!graffities.isEmpty())
		 {
			 Orientation currentOrientation = currentLocationParameters.getOrientation();
//			 Toast.makeText(getApplicationContext(), "Orientation: "+currentOrientation.toString(), Toast.LENGTH_SHORT).show();

			 // Loop through the available graffities and find one that matches the current orientation
			 for(Graffiti graffiti : graffities)
			 {
				 GraffitiLocationParameters graffitiLocation = graffiti.getLocationParameters();
				 Orientation graffitiOrientation = graffitiLocation.getOrientation();
				 
				 if(graffitiOrientation == null || currentOrientation == null)
					 break;
				 
				 float orientationDiff = Math.abs(graffitiOrientation.getXorientation()-currentOrientation.getXorientation());
				 if(orientationDiff>180)
					 orientationDiff = 360 - orientationDiff;
				 
				 if(_cameraLiveView.getMode() == _cameraLiveView.VIEW_MODE_VIEWING)
				 {
					 if(graffiti.getKey() == currentGraffitiKey)
					 {
						 if(orientationDiff<=ORIENTAION_MATCH_DIFFERENCE)
							 break; // Do nothing - continue viewing current graffiti
						 else
						 {
							 Log.d("RealGraffiti", "Graffiti out of orientation");
							 _cameraLiveView.setModeToIdle(); // Out of orientation - go back to idle
//							 Toast.makeText(getApplicationContext(), "Graffiti Out of Orientation", Toast.LENGTH_LONG).show();
							 currentGraffitiKey = 0;
						 }
					 }
				 }
				 else if(_cameraLiveView.getMode() == _cameraLiveView.VIEW_MODE_IDLE)
				 {
					 if(orientationDiff<=ORIENTAION_MATCH_DIFFERENCE)
					 {
						 byte[] graffitiImgBytes = graffiti.getImageData();
						 byte[] wallImgBytes = graffiti.getWallImageData();
						 Bitmap graffitiImage = BitmapFactory.decodeByteArray(graffitiImgBytes, 0, graffitiImgBytes.length);
						 Bitmap wallImage = BitmapFactory.decodeByteArray(wallImgBytes, 0, wallImgBytes.length);
		
						 if( (graffitiImage != null) && (wallImage != null) )
						 {
							Log.d("RealGraffiti", "Start viewing graffiti");
//							 Toast.makeText(getApplicationContext(), "Found Nearby Graffiti", Toast.LENGTH_LONG).show();
							 currentGraffitiKey = graffiti.getKey();
							_cameraLiveView.setModeToViewing(wallImage, graffitiImage);
							playSound(SOUND_ID_TING);
						 }
						 else
							Log.d("RealGraffiti", "graffitiUpdateTimerTick - failed retrieving bitmaps");
					 
						 break;
					 }
				 }
				 
			 }
		 }
	}
	
	private void scheduleGraffitiUpdateTimer()
	{
		_graffitiUpdateTimer = new Timer(); // Timer to update nearby graffiti to view
		_graffitiUpdateTimer.scheduleAtFixedRate(new TimerTask() {
			private Handler updateUI = new Handler(){
				@Override
				public void dispatchMessage(Message msg) {
				    super.dispatchMessage(msg);
				    graffitiUpdateTimerTick();
				}
				};
			@Override
			public void run() {
				updateUI.sendEmptyMessage(0);
			}
		}, 5000, GRAFFITI_UPDATE_INTERVAL);
	}
	
	protected void onStart() {
		super.onStart();
		Log.d("RealGraffiti", "on start");

		GraffitiLocationParametersGenerator graffitiLocationParametersGenerator = 
			  GraffitiLocationParametersGeneratorFactory.getGaffitiLocationParametersGenerator(RealGraffiti.this);
		
		graffitiLocationParametersGenerator.startTracking();
		
		_miniMapView.startOverlays();
		_graffitiPoller.beginPolling();
		scheduleGraffitiUpdateTimer();
		
	}

	protected void onRestart() {
		super.onRestart();
		Log.d("RealGraffiti", "on restart");
	}

	protected void onStop() {
		super.onStop();
		Log.d("RealGraffiti", "on stop");
		_graffitiUpdateTimer.cancel();
	}

	protected void onDestroy() {
		super.onDestroy();
		Log.d("RealGraffiti", "on destroy");
		GraffitiLocationParametersGenerator graffitiLocationParametersGenerator = 
				  GraffitiLocationParametersGeneratorFactory.getGaffitiLocationParametersGenerator(RealGraffiti.this);
		graffitiLocationParametersGenerator.stopTracking();
	}

	@Override
	protected void onPause() {
		super.onPause();
		Log.d("RealGraffiti", "on pause");
		_miniMapView.stopOverlays();
		_graffitiPoller.stopPolling();
	}

	@Override
	protected void onResume() {
		super.onResume();
		Log.d("RealGraffiti", "on resume");
		// The activity has become visible (it is now "resumed").
	}

	private void setAddNewGraffitiButton() {
		Button button = (Button) findViewById(R.id.addNewGraffitiButton);
		button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				GraffitiLocationParametersGenerator graffitiLocationParametersGenerator = GraffitiLocationParametersGeneratorFactory
						.getGaffitiLocationParametersGenerator(RealGraffiti.this);

				// if(graffitiLocationParametersGenerator.isLocationParametersAvailable()
				// == false){
				if (false) { // DEBUG
					Toast noLocationAvailibleToast = Toast.makeText(
							getApplicationContext(),
							NO_LOCATION_AVAILIBLE_MESSAGE, 1000);
					noLocationAvailibleToast.show();
				} else {
					Bitmap graffitiWallImg = _cameraLiveView.setModeToPainting();
					playSound(SOUND_ID_SHUTTER);

					_paintedGraffiti = new Graffiti();
					GraffitiLocationParameters currentLocationParameters = graffitiLocationParametersGenerator.getCurrentLocationParameters();
					_paintedGraffiti.setLocationParameters(currentLocationParameters);

					_paintedGraffiti.setWallImageData(bitmapToByteArray(graffitiWallImg));

					String filename = getFilesDir() + "/wall";
					FileOutputStream fos = null;
					try {
						fos = new FileOutputStream(filename);
					} catch (FileNotFoundException e) {
						e.printStackTrace();
					}
					graffitiWallImg.compress(Bitmap.CompressFormat.PNG, 90, fos);

					Intent myIntent = new Intent(RealGraffiti.this, FingerPaintActivity.class);

					// Need to change the 'Location' to the real location of the
					// image
					myIntent.putExtra(FingerPaintActivity.WALL_IMAGE_LOC,filename);
					startActivityForResult(myIntent, FINGER_PAINT_ACTIVITY);

					Log.d("RealGraffiti", "New Button pressed");
				}
			}
		});
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case (FINGER_PAINT_ACTIVITY): {
			if (resultCode == RESULT_OK) {
				String newText = data.getStringExtra(FingerPaintActivity.PAINTING_LOC);
				Log.d("RealGraffiti", "Returned from FingerPaint: Result OK, "+ newText);

				_backgroundLocation = newText;

				if (_backgroundLocation != null) { 
					// Get painted graffiti and update the database
					Bitmap graffitiBitMap = BitmapFactory.decodeFile(_backgroundLocation).copy(Bitmap.Config.ARGB_8888,true);
					byte[] imageData = bitmapToByteArray(graffitiBitMap);
					_paintedGraffiti.setImageData(imageData);

					AddNewGraffitiTask addGraffitiTask = new AddNewGraffitiTask();
					addGraffitiTask.execute(_paintedGraffiti);
					_paintedGraffiti = null;
				}
			}
			else
				Log.d("RealGraffiti","Returned from FingerPaint: Result Cancled");
			
			_cameraLiveView.setModeToIdle(); // Return to idle mode until nearby graffiti is found

			break;
		}
		}
	}

	private byte[] bitmapToByteArray(Bitmap bitmap) {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		bitmap.compress(CompressFormat.PNG, 10, os);

		byte[] imageData = os.toByteArray();
		return imageData;
	}

	private class AddNewGraffitiTask extends
			AsyncTask<Graffiti, Integer, Boolean> {
		private ProgressDialog _progressDialog;

		protected Boolean doInBackground(Graffiti... graffiti) {
			Log.d("RealGraffiti", "newTask");
			return _graffitiData.addNewGraffiti(graffiti[0]);
		}

		protected void onPreExecute() {
			_progressDialog = ProgressDialog.show(RealGraffiti.this, "","Saving... Please wait.", true);
		}

		protected void onPostExecute(Boolean result) {
			_progressDialog.dismiss();
		}
	}

}
