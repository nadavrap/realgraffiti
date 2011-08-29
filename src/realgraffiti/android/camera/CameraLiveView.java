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
    public static final int     VIEW_MODE_VIEWING 		= 1;
    public static final int     VIEW_MODE_PAINTING 		= 2;
	
    public static final int     MAX_BAD_MATHES_ALLOWED	= 5;
    public static final long    LOGO_DISPLAY_PERIOD_MS	= 4000;

    private static final int    TRACKING_IDLE		= 1;
    private static final int    TRACKING_STARTED	= 2;
    private static final int    TRACKING_GOOD		= 3;
    private static final int    TRACKING_LOST		= 4;

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
    private boolean mGoodMatch = false;
    private int	mNumBadMatches = 0;
    private int mTrackingStatus = TRACKING_IDLE;

    
	public CameraLiveView(Context context, AttributeSet attrs) {
		super(context, attrs);
		if (isInEditMode()) {return;}
		mViewMode = VIEW_MODE_IDLE;
        startTime=SystemClock.uptimeMillis();
		Log.d("RealGraffiti", "on craete");
	}  
    

    @Override
    public void surfaceChanged(SurfaceHolder _holder, int format, int width, int height) {
        super.surfaceChanged(_holder, format, width, height);
		Log.d("RealGraffiti", "surface changed");
        
        mWarpedGraffiti=Bitmap.createBitmap(getFrameWidth(), getFrameHeight(), Bitmap.Config.ARGB_8888);
        
		Log.d("RealGraffiti", "surface changed before synch");
        
        synchronized (this) {
            // initialize Mats before usage
            mYuv = new Mat(getFrameHeight() + getFrameHeight() / 2, getFrameWidth(), CvType.CV_8UC1);
            mGraySubmat = mYuv.submat(0, getFrameHeight(), 0, getFrameWidth());

            mRgba = new Mat();
            mWarpedImg = new Mat();
            mTrackOffset = new Mat(1,2,CvType.CV_32FC1);
        }
		Log.d("RealGraffiti", "surface changed done");
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
        case VIEW_MODE_VIEWING:
        	if( (mTrackingStatus == TRACKING_GOOD) && mGoodMatch)
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
	           	if(mTrackingStatus == TRACKING_STARTED)
	           		mTrackingStatus = TRACKING_GOOD;
            }
            else if ( ((mTrackingStatus == TRACKING_STARTED) || (mTrackingStatus == TRACKING_GOOD) )&& (ret == -3))
            {
        		Log.d("RealGraffiti", "bad tracking");
        		mTrackingStatus = TRACKING_LOST;
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
          else if( (mViewMode == VIEW_MODE_VIEWING) && (mTrackingStatus == TRACKING_GOOD) && mGoodMatch)
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
		Log.d("RealGraffiti", "run");

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
    
    // Set the working mode to Idle mode - no graffiti to view
    public void setModeToIdle()
    {
		Log.d("RealGraffiti", "setModeToIdle");
    	mViewMode = VIEW_MODE_IDLE;
    }

    // Set the working mode to Viewing mode - view a single live graffiti
    public void setModeToViewing(Bitmap wallImage, Bitmap graffitiImage)
    {
		Log.d("RealGraffiti", "setModeToViewing");
        Bitmap tmpBitmap;

        // Save graffitiImage as a OpenCV Mat object
        tmpBitmap = graffitiImage.copy(Bitmap.Config.ARGB_8888, true);
        mWarpImg=android.BitmapToMat(tmpBitmap);
        if(!mWarpImg.empty())
        	Imgproc.resize(mWarpImg, mWarpImg, new Size(getFrameWidth(), getFrameHeight()));
        
        // Get wallImage, convert to gray-scale and process for interest points
        tmpBitmap = wallImage.copy(Bitmap.Config.ARGB_8888, true);
        Mat wallImg = android.BitmapToMat(tmpBitmap);
        if(wallImg.empty())
    		Log.d("RealGraffiti", "Error - empty wall image");
        else
        {
        	Imgproc.cvtColor(wallImg, wallImg, Imgproc.COLOR_BGRA2GRAY, 1);
        	ProcessRefFrame(wallImg.getNativeObjAddr());
        }
        tmpBitmap.recycle();
        wallImg.dispose();
		
        mTrackingStatus = TRACKING_IDLE;
    	mGoodMatch = false;
		offsetX = 0;
    	offsetY = 0;
    	offsetMatchX = 0;
    	offsetMatchY = 0;
		
    	mViewMode = VIEW_MODE_VIEWING;
    }
    
    
    // Set the working mode to Painting mode - get last frame and stop camera preview
    public Bitmap setModeToPainting()
    {
		Log.d("RealGraffiti", "setModeToPainting");
    	mViewMode = VIEW_MODE_PAINTING;
        Bitmap bmp = Bitmap.createBitmap(getFrameWidth(), getFrameHeight(), Bitmap.Config.ARGB_8888);
        
        if (android.MatToBitmap(mRgba, bmp))
            return bmp;
        else
        	return null;
    }
    
    public int getMode()
    {
    	return mViewMode;
    }
    
    public native void ProcessRefFrame(long matAddrSrc); 
    public native int MatchAndWarp(long matAddrImgBW, long matAddrWarpImg, long matAddrWarpedDst);
    public native int TrackPoints(long matAddrImgBW, long matAddrOffset);

    static {
        System.loadLibrary("mixed_sample");
    }
    
    private void RunMatchThread()
    {
		Log.d("RealGraffiti", "RunMatchThread");
       	mMatchNWarpRunning = true;
    	offsetMatchX=offsetX;
    	offsetMatchY=offsetY;
    	if( (mTrackingStatus == TRACKING_IDLE) || (mTrackingStatus == TRACKING_LOST) )
    	{
    		mTrackingStatus = TRACKING_STARTED;
    		mGoodMatch = false;
    	}
        
        Thread myThread = new Thread(new Runnable() {
        	public void run() {
                mMatchret=MatchAndWarp(mGraySubmat.getNativeObjAddr(), mWarpImg.getNativeObjAddr(), mWarpedImg.getNativeObjAddr());
                if(mMatchret==0)
                {
            		Log.d("RealGraffiti", "Good match found");
                	mNumBadMatches=0;
                	offsetX=offsetX-offsetMatchX;
                	offsetY=offsetY-offsetMatchY;
                	if(mTrackingStatus == TRACKING_GOOD)
                		mGoodMatch = true;	// If tracking was good, indicate that we now have a good match as well
                	android.MatToBitmap(mWarpedImg, mWarpedGraffiti);
                }
                else
                {
            		Log.d("RealGraffiti", "Bad match");
                	if(mGoodMatch && (mNumBadMatches++ > MAX_BAD_MATHES_ALLOWED) )
                       	mGoodMatch = false;
                }
            	mNumMatches++;
                mMatchNWarpRunning = false;
        	}
        });
        if(mGoodMatch && (mTrackingStatus == TRACKING_GOOD))
        	myThread.setPriority(Thread.NORM_PRIORITY);
        else
        	myThread.setPriority(Thread.NORM_PRIORITY+1);
        // Start the matching thread
        myThread.start();
    }
    
}
