package realgraffiti.android.camera;

import org.opencv.android;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import realgraffiti.android.activities.RealGraffiti;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;

public class CameraLiveView extends CameraLiveViewBase{

	public static final int     VIEW_MODE_IDLE     		= 0;
    public static final int     VIEW_MODE_SEARCHING 	= 1;
    public static final int     VIEW_MODE_VIEWING 		= 2;
    public static final int     VIEW_MODE_PAINTING 		= 3;
	
    public static final int     MAX_BAD_MATHES_ALLOWED	= 5;
    public static final long    LOGO_DISPLAY_PERIOD_MS	= 4000;
    
    private static int           mViewMode;
	
	private static long prevFTime = 0, startTime;
	private Mat mYuv;
    private Mat mRgba;
    private Mat mGraySubmat;
    private Mat mTrackOffset;
    private Mat mWarpImg;
    private Mat mWarpedImg;
    private boolean mMatchNWarpRunning=false;
    private Thread mThread;
    private int mMatchret=9;
    private int mNumMatches=0;
    private float offsetX, offsetY;
    private float offsetMatchX, offsetMatchY;
    private Bitmap mWarpedGraffiti;
    private boolean mGoodTracking = false;
    private boolean mGoodMatch = false;
    private int	mNumBadMatches = 0;

    
	public CameraLiveView(Context context, AttributeSet attrs) {
		super(context, attrs);
        mViewMode = VIEW_MODE_IDLE;
		Log.d("CameraLiveView", "on craete");
	}  
    

    @Override
    public void surfaceChanged(SurfaceHolder _holder, int format, int width, int height) {
        super.surfaceChanged(_holder, format, width, height);
		Log.d("CameraLiveView", "surface changed");

        // Copy the resource bitmap and convert to ARGB_8888 format, required by BitmapToMat
        Bitmap tmpBitmap = RealGraffiti.graffitiBitMap.copy(Bitmap.Config.ARGB_8888, true);
        // Convert to OpenCV Mat object and resize
        mWarpImg=android.BitmapToMat(tmpBitmap);
        if(!mWarpImg.empty())
        {
        	Imgproc.resize(mWarpImg, mWarpImg, new Size(getFrameWidth(), getFrameHeight()));
//            Imgproc.cvtColor(mWarpImg, mWarpImg, Imgproc.COLOR_RGBA2RGB, 0);
        }
        tmpBitmap.recycle();
        
        mWarpedGraffiti=Bitmap.createBitmap(getFrameWidth(), getFrameHeight(), Bitmap.Config.ARGB_8888);
        startTime=SystemClock.uptimeMillis();
        
		Log.d("CameraLiveView", "surface changed before synch");
        
        synchronized (this) {
            // initialize Mats before usage
            mYuv = new Mat(getFrameHeight() + getFrameHeight() / 2, getFrameWidth(), CvType.CV_8UC1);
            mGraySubmat = mYuv.submat(0, getFrameHeight(), 0, getFrameWidth());

            mRgba = new Mat();
            mWarpedImg = new Mat();
            mTrackOffset = new Mat(1,2,CvType.CV_32FC1);
        }
		Log.d("CameraLiveView", "surface changed done");
    }

    @Override
    protected Bitmap processFrame(byte[] data) {
    	int ret;
    	mYuv.put(0, 0, data);
    	
        switch (mViewMode) {
        case VIEW_MODE_IDLE:
            Imgproc.cvtColor(mYuv, mRgba, Imgproc.COLOR_YUV420i2RGB, 4);
            break;
        case VIEW_MODE_PAINTING:
             break;
        case VIEW_MODE_SEARCHING:
            Imgproc.cvtColor(mYuv, mRgba, Imgproc.COLOR_YUV420i2RGB, 4);
            
            // Run the interest point detection, matching, homography calculation and Graffiti warping as a background task
            mMatchret=MatchAndWarp(mGraySubmat.getNativeObjAddr(), mWarpImg.getNativeObjAddr(), mWarpedImg.getNativeObjAddr());
            // Match found - switch to VIEWING mode and reset the offset matrix
        	mNumMatches++;
            if(mMatchret==0)
            {
            	android.MatToBitmap(mWarpedImg, mWarpedGraffiti);
            	mNumMatches++;
               	offsetX = 0;
            	offsetY = 0;
            	offsetMatchX = 0;
            	offsetMatchY = 0;
            	mViewMode = VIEW_MODE_VIEWING;
            	mGoodTracking = true;
            	mTrackOffset = new Mat(1,2,CvType.CV_32FC1);
            }
            break;
        case VIEW_MODE_VIEWING:
        	if(mGoodTracking && mGoodMatch)
        		Imgproc.cvtColor(mYuv, mRgba, Imgproc.COLOR_YUV420i2RGB, 4);
        	else
        		Imgproc.cvtColor(mGraySubmat, mRgba, Imgproc.COLOR_GRAY2RGB, 4);
        		
        	// Perform Lucas-Kanade optical flow tracking to update offset of warped graffiti image
            ret=TrackPoints(mGraySubmat.getNativeObjAddr(), mTrackOffset.getNativeObjAddr());
            if(ret == 0)
            {
	            float[] offset = new float[2];
	           	mTrackOffset.get(0, 0, offset);
	
	           	// Update the accumulated offset
	           	offsetX+=offset[0];
	           	offsetY+=offset[1];
            }
            else if (ret == -3)
            {
               	mGoodTracking = false;
            	offsetX = 0;
            	offsetY = 0;
            }

            // Run the interest point detection, matching, homography calculation and Graffiti warping as a background thread
            if(!mMatchNWarpRunning)
            	RunMatchThread();
	 
            break;
        }
    	 
//        if (mViewMode != VIEW_MODE_PAINTING)
//        	Core.putText(mRgba, String.format("%02.1f FPS, Mod %d, Match %d, Mret %d",
//        			1000.0/(double)(SystemClock.uptimeMillis()-prevFTime),
//        			mViewMode, mNumMatches, mMatchret), new Point(10, getFrameHeight()-30), 3, 1, new Scalar(255, 0, 0, 255), 2);
        
        prevFTime=SystemClock.uptimeMillis();
        Bitmap bmp = Bitmap.createBitmap(getFrameWidth(), getFrameHeight(), Bitmap.Config.ARGB_8888);
        
        if (android.MatToBitmap(mRgba, bmp))
        {
          if((mViewMode == VIEW_MODE_IDLE) && (SystemClock.uptimeMillis()-startTime < LOGO_DISPLAY_PERIOD_MS) )
          {
           	Canvas canvas = new Canvas(bmp);
           	Paint paint = new Paint(Paint.FILTER_BITMAP_FLAG);
           	canvas.drawBitmap(RealGraffiti.rgLogoBitmap, (getFrameWidth()-RealGraffiti.rgLogoBitmap.getWidth())/2
          			, (getFrameHeight()-RealGraffiti.rgLogoBitmap.getHeight())/2, paint);
          }
          else if( (mViewMode == VIEW_MODE_VIEWING) && mGoodTracking && mGoodMatch)
          {
           	Canvas canvas = new Canvas(bmp);
          	Paint paint = new Paint(Paint.FILTER_BITMAP_FLAG);
          	canvas.drawBitmap(mWarpedGraffiti, offsetX, offsetY, paint);
          }
          else if(mViewMode == VIEW_MODE_PAINTING)
          {
          	Canvas canvas = new Canvas(bmp);
          	Paint paint = new Paint(Paint.FILTER_BITMAP_FLAG);
          	canvas.drawBitmap(RealGraffiti.spraycanBitmap, getFrameWidth()-RealGraffiti.spraycanBitmap.getWidth()
          			, (getFrameHeight()-RealGraffiti.spraycanBitmap.getHeight())/2, paint);
          }
          return bmp;
        }

        bmp.recycle();
        return null;
    }

    @Override
    public void run() {
        super.run();
		Log.d("CameraLiveView", "run");

        synchronized (this) {
            // Explicitly deallocate Mats
            if (mYuv != null)
                mYuv.dispose();
            if (mRgba != null)
                mRgba.dispose();
            if (mGraySubmat != null)
                mGraySubmat.dispose();
  
            mYuv = null;
            mRgba = null;
            mGraySubmat = null;
            
        }
    }
    
    public void setViewMode(int viewMode)
    {
    	if(mViewMode == VIEW_MODE_VIEWING)
    	{
        	offsetX = 0;
        	offsetY = 0;
        	offsetMatchX = 0;
        	offsetMatchY = 0;
        	mGoodTracking = true;
    	}
    	mViewMode = viewMode;
		Log.d("CameraLiveView", "set view mode");
    }
    
    public Bitmap getBackgroundImage()
    {
    	// Process the reference frame and save the detected features internally
        ProcessRefFrame(mGraySubmat.getNativeObjAddr());
        Bitmap bmp = Bitmap.createBitmap(getFrameWidth(), getFrameHeight(), Bitmap.Config.ARGB_8888);
        
        if (android.MatToBitmap(mRgba, bmp))
            return bmp;
        else
        	return null;
    }
    
    public native void ProcessRefFrame(long matAddrSrc); 
    public native int MatchAndWarp(long matAddrImgBW, long matAddrWarpImg, long matAddrWarpedDst);
    public native int TrackPoints(long matAddrImgBW, long matAddrOffset);

    static {
        System.loadLibrary("mixed_sample");
    }
    
    private void RunMatchThread()
    {
       	mMatchNWarpRunning = true;
    	offsetMatchX=offsetX;
    	offsetMatchY=offsetY;
        
        Thread myThread = new Thread(new Runnable() {
        	public void run() {
                mMatchret=MatchAndWarp(mGraySubmat.getNativeObjAddr(), mWarpImg.getNativeObjAddr(), mWarpedImg.getNativeObjAddr());
                if(mMatchret==0)
                {
                	mNumBadMatches=0;
                	offsetX=offsetX-offsetMatchX;
                	offsetY=offsetY-offsetMatchY;
                	if(mGoodTracking)
                		mGoodMatch = true;	// If tracking was good, indicate that we now have a good match as well
                	else
                		mGoodTracking = true; // Turn on good tracking, since we have a new warp data
                	android.MatToBitmap(mWarpedImg, mWarpedGraffiti);
                }
                else
                {
                	if(mGoodMatch && (mNumBadMatches++ > MAX_BAD_MATHES_ALLOWED) )
                       	mGoodMatch = false;
                }
            	mNumMatches++;
                mMatchNWarpRunning = false;
        	}
        });
        myThread.setPriority(Thread.NORM_PRIORITY);
        myThread.start();
    }
    
    // Below not used - can be deleted
	private class MatchAndWarpTask extends AsyncTask<Void, Void, Integer> {
		protected Integer doInBackground(Void... voids) {
            // Match features from ref image to current image, calculate homography and warp the warp-image
            int ret=MatchAndWarp(mGraySubmat.getNativeObjAddr(), mWarpImg.getNativeObjAddr(), mWarpedImg.getNativeObjAddr());
            if(ret==0)
            {
            	offsetX=offsetX-offsetMatchX;
            	offsetY=offsetY-offsetMatchY;
            	android.MatToBitmap(mWarpedImg, mWarpedGraffiti);
            }
        	mNumMatches++;
            mMatchret = ret;
            mMatchNWarpRunning = false;
			return ret;
		}
	}
   
    
}
