package realgraffiti.android.activities;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;

import realgraffiti.android.R;
import realgraffiti.android.paint.ColorPickerDialog;
import realgraffiti.android.paint.GraphicsActivity;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.EmbossMaskFilter;
import android.graphics.MaskFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;

public class FingerPaintActivity extends GraphicsActivity
implements ColorPickerDialog.OnColorChangedListener {
	protected static final String WALL_IMAGE_LOC = "Location of wall image";
	protected static final String PAINTING_LOC = "tmp_paint";

	private String bg_loc;
	/** Called when the activity is first created. */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		bg_loc = getIntent().getStringExtra(WALL_IMAGE_LOC);
		myView = new MyView(this);
		Object retained = this.getLastNonConfigurationInstance();
		setContentView(myView);
		if(retained != null) {
			myView.mBitmap = ((MyDataObject)retained)._bitmap;
			myView.mCanvas = ((MyDataObject)retained)._canvas;
			myView.mPath = ((MyDataObject)retained)._path;
			myView.mBitmapPaint = ((MyDataObject)retained)._paint;
		}
		mPaint = new Paint();
		mPaint.setAntiAlias(true);
		mPaint.setDither(true);
		mPaint.setColor(0xFFFF0000);
		mPaint.setStyle(Paint.Style.STROKE);
		mPaint.setStrokeJoin(Paint.Join.ROUND);
		mPaint.setStrokeCap(Paint.Cap.ROUND);
		mPaint.setStrokeWidth(12);

		mEmboss = new EmbossMaskFilter(new float[] { 1, 1, 1 },
				0.4f, 6, 3.5f);

		mBlur = new BlurMaskFilter(8, BlurMaskFilter.Blur.NORMAL);

	}
	private MyView myView;
	private Paint       mPaint;
	private MaskFilter  mEmboss;
	private MaskFilter  mBlur;

	public void colorChanged(int color) {
		mPaint.setColor(color);
	}
	@Override
	public Object onRetainNonConfigurationInstance() {
		return new MyDataObject(myView.mBitmap, myView.mCanvas, myView.mPath, myView.mBitmapPaint);
	}

	public class MyView extends View {

		private Bitmap  mBitmap;
		private Canvas  mCanvas;
		private Path    mPath;
		private Paint   mBitmapPaint;

		public MyView(Context c) {
			super(c);
			Bitmap tmpBitmap;
			Display display = getWindowManager().getDefaultDisplay();

			//Lock screen orientation on landscape
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
			int display_width = display.getWidth();
			int display_height = display.getHeight();
			Log.d("MyView", "Display width: " + display_width + ", height: " + display_height);        
			//mBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

			
			String filename = getIntent().getStringExtra(WALL_IMAGE_LOC);
			if (filename != null) {//Background should be taken from given file
				tmpBitmap = BitmapFactory.decodeFile(filename).copy(Bitmap.Config.ARGB_8888, true);
				Log.d("FingerPaint", "filename is not null: " + filename);
			}
			else {//Background will be taken from a default file
				tmpBitmap = BitmapFactory.decodeResource(getResources(),R.drawable.wall);
			}
			
			int bitmap_height = tmpBitmap.getHeight();
			int bitmap_width= tmpBitmap.getWidth();
			Log.d("MyView", "Bitmap width: " + bitmap_width + ", height: " + bitmap_height);
			//mBitmap = Bitmap.createBitmap(tmpBitmap, 0, 0, tmpBitmap.getWidth(), tmpBitmap.getHeight(), aMatrix, false);
			Matrix aMatrix = new Matrix();
			//aMatrix.postRotate(90.f);
			float scaleW = (float)display_width/bitmap_width;
			float scaleH = (float)display_height/bitmap_height;
			float scale = Math.max(scaleW, scaleH);
			//Log.d("MyView", "Swidth: " + scaleW + ", Sheight: " + scaleH + ", scale: " + scale);
			aMatrix.postScale(scale, scale);
			mBitmap = Bitmap.createBitmap(tmpBitmap, 0, 0, tmpBitmap.getWidth(), tmpBitmap.getHeight(), aMatrix, false);
			mCanvas = new Canvas(mBitmap);
			mPath = new Path();
			mBitmapPaint = new Paint(Paint.DITHER_FLAG);
		}

		@Override
		protected void onSizeChanged(int w, int h, int oldw, int oldh) {
			super.onSizeChanged(w, h, oldw, oldh);
		}

		@Override
		protected void onDraw(Canvas canvas) {
			//Log.d("MyView", "onDraw");
			canvas.drawColor(0xFF0AA0AA);//Background color of the Canvas
			//Bitmap icon = BitmapFactory.decodeResource(getResources(),R.drawable.wall);
			canvas.drawBitmap(mBitmap, 0, 0, mBitmapPaint);
			//canvas.drawBitmap(icon, 0, 0, mBitmapPaint);

			canvas.drawPath(mPath, mPaint);
			//this.setBackgroundColor(0);
		}

		private float mX, mY;
		private static final float TOUCH_TOLERANCE = 4;

		private void touch_start(float x, float y) {
			mPath.reset();
			mPath.moveTo(x, y);
			mX = x;
			mY = y;
		}
		private void touch_move(float x, float y) {
			float dx = Math.abs(x - mX);
			float dy = Math.abs(y - mY);
			if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
				mPath.quadTo(mX, mY, (x + mX)/2, (y + mY)/2);
				mX = x;
				mY = y;
			}
		}
		private void touch_up() {
			mPath.lineTo(mX, mY);
			// commit the path to our offscreen
			mCanvas.drawPath(mPath, mPaint);
			// kill this so we don't double draw
			mPath.reset();
			//finishPaint();//Not belong to here, for development only
		}

		@Override
		public boolean onTouchEvent(MotionEvent event) {
			float x = event.getX();
			float y = event.getY();

			switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				touch_start(x, y);
				invalidate();
				break;
			case MotionEvent.ACTION_MOVE:
				touch_move(x, y);
				invalidate();
				break;
			case MotionEvent.ACTION_UP:
				touch_up();
				invalidate();
				break;
			}
			return true;
		}
	}

	private static final int COLOR_MENU_ID = Menu.FIRST;
	private static final int EMBOSS_MENU_ID = Menu.FIRST + 1;
	private static final int BLUR_MENU_ID = Menu.FIRST + 2;
	private static final int ERASE_MENU_ID = Menu.FIRST + 3;
	private static final int SRCATOP_MENU_ID = Menu.FIRST + 4;
	private static final int SAVE_MENU_ID = Menu.FIRST + 5;

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);

		menu.add(0, COLOR_MENU_ID, 0, "Color").setShortcut('3', 'c');
		menu.add(0, EMBOSS_MENU_ID, 0, "Emboss").setShortcut('4', 's');
		menu.add(0, BLUR_MENU_ID, 0, "Blur").setShortcut('5', 'z');
		menu.add(0, ERASE_MENU_ID, 0, "Erase").setShortcut('5', 'z');
		menu.add(0, SRCATOP_MENU_ID, 0, "SrcATop").setShortcut('5', 'z');
		menu.add(0, SAVE_MENU_ID, 0, "Save").setShortcut('5', 'z');

		/****   Is this the mechanism to extend with filter effects?
        Intent intent = new Intent(null, getIntent().getData());
        intent.addCategory(Intent.CATEGORY_ALTERNATIVE);
        menu.addIntentOptions(
                              Menu.ALTERNATIVE, 0,
                              new ComponentName(this, NotesList.class),
                              null, intent, 0, null);
		 *****/
		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		mPaint.setXfermode(null);
		mPaint.setAlpha(0xFF);

		switch (item.getItemId()) {
		case COLOR_MENU_ID:
			new ColorPickerDialog(this, this, mPaint.getColor()).show();
			return true;
		case EMBOSS_MENU_ID:
			if (mPaint.getMaskFilter() != mEmboss) {
				mPaint.setMaskFilter(mEmboss);
			} else {
				mPaint.setMaskFilter(null);
			}
			return true;
		case BLUR_MENU_ID:
			if (mPaint.getMaskFilter() != mBlur) {
				mPaint.setMaskFilter(mBlur);
			} else {
				mPaint.setMaskFilter(null);
			}
			return true;
		case ERASE_MENU_ID:
			mPaint.setXfermode(new PorterDuffXfermode(
					PorterDuff.Mode.CLEAR));
			return true;
		case SRCATOP_MENU_ID:
			mPaint.setXfermode(new PorterDuffXfermode(
					PorterDuff.Mode.SRC_ATOP));
			mPaint.setAlpha(0x80);
			return true;
		case SAVE_MENU_ID:
			finishPaint();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	public void finishPaint(){
		//First save the image somewhere
		String filename = savePainting();
		//Then finish, and set the image location
		Intent resultIntent = new Intent();
		resultIntent.putExtra(PAINTING_LOC, filename);
		setResult(Activity.RESULT_OK, resultIntent);
		finish();
	}
	private String savePainting() {
		//FileOutputStream fos;
		Long timestamp = System.currentTimeMillis();
		String filename =getFilesDir()+ "paint" + timestamp.toString();
		Log.d("FingerPaint", "filename: " + filename);
		try {
			/*fos = openFileOutput(filename, MODE_PRIVATE);
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			oos.writeObject(myView.mBitmap); 
			oos.close();
			fos.close();*/
			FileOutputStream fos = new FileOutputStream(filename);
			myView.mBitmap.compress(Bitmap.CompressFormat.PNG, 90, fos);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			Log.d("FingerPaint", "file not found exception, "+ e.getMessage() + ", " + e.toString());
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			Log.d("FingerPaint", "Exception " + e.getMessage() + ", " + e.toString());
			return null;
		}
		Log.d("FingerPaint", "Filename: " + filename);
		return filename;
	}
	private Object readBack(String filename){
		Bitmap f = null;
		FileInputStream fis;
		try {
			//fis = new FileInputStream(filename);
			//BitmapFactory.decodeFile(filename);
			fis = openFileInput(filename);
			ObjectInputStream ois = new ObjectInputStream(fis);
			f = (Bitmap)ois.readObject();
			ois.close();
			fis.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (StreamCorruptedException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return f;
	}
	public class MyDataObject {
		private Bitmap _bitmap;
		private Canvas _canvas;
		private Path _path;
		private Paint _paint;
		public MyDataObject(Bitmap bitmap, Canvas canvas, Path path, Paint paint) {
			_bitmap = bitmap;
			_canvas = canvas;
			_path = path;
			_paint = paint;
		}
	}
}
