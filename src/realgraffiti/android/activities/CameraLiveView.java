package realgraffiti.android.activities;

import org.opencv.android;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.View.OnTouchListener;

public class CameraLiveView extends CameraLiveViewBase implements OnTouchListener{
    private Mat mYuv;
    private Mat mRgba;
    private Mat mRgb;
    private Mat mRgb2;
    private Mat mGraySubmat;
    private Mat mH;
    private Mat mWarpImg;
    private boolean mReref = true;

    public CameraLiveView(Context context) {
        super(context);
        setOnTouchListener(this);
    }

    @Override
    public void surfaceChanged(SurfaceHolder _holder, int format, int width, int height) {
        super.surfaceChanged(_holder, format, width, height);

        synchronized (this) {
            // initialize Mats before usage
            mYuv = new Mat(getFrameHeight() + getFrameHeight() / 2, getFrameWidth(), CvType.CV_8UC1);
            mGraySubmat = mYuv.submat(0, getFrameHeight(), 0, getFrameWidth());

            mRgba = new Mat();
            mRgb = new Mat();
            mRgb2 = new Mat();
            mH = new Mat();

            // Read the warp image and resize it to the frame size
            mWarpImg=Highgui.imread("/mnt/sdcard/download/imgtrans.jpg");
            Imgproc.resize(mWarpImg, mWarpImg, new Size(getFrameWidth(), getFrameHeight()));
        }
    }

    @Override
    protected Bitmap processFrame(byte[] data) {
        mYuv.put(0, 0, data);

        switch (ApplicationDemo.viewMode) {
         case ApplicationDemo.VIEW_MODE_RGBA:
            Imgproc.cvtColor(mYuv, mRgba, Imgproc.COLOR_YUV420i2RGB, 4);
            Core.putText(mRgba, "RealGraffitiAR1. Press Menu.", new Point(10, 100), 3/* CV_FONT_HERSHEY_COMPLEX */, 1, new Scalar(255, 0, 0, 255), 2);
            break;
        case ApplicationDemo.VIEW_MODE_MATCHING:
            // Reference reset or first time run
            if(mReref == true || ApplicationDemo.mResetRef == true)
        	{
            	mReref = false;
            	ApplicationDemo.mResetRef = false;

                ProcessRefFrame(mGraySubmat.getNativeObjAddr());
        	}
            Imgproc.cvtColor(mYuv, mRgb, Imgproc.COLOR_YUV420i2RGB, 3);

            // Match features from ref image to current image, calculate homography and warp the warp-image
            MatchAndWarp(mRgb.getNativeObjAddr(), mGraySubmat.getNativeObjAddr(), mWarpImg.getNativeObjAddr(), mRgb2.getNativeObjAddr());
            if(!mRgb2.empty())
                Imgproc.cvtColor(mRgb2, mRgba, Imgproc.COLOR_RGB2RGBA, 4);

            break;
        }
    	 
        Bitmap bmp = Bitmap.createBitmap(getFrameWidth(), getFrameHeight(), Bitmap.Config.ARGB_8888);
        
        if (android.MatToBitmap(mRgba, bmp))
            return bmp;

        bmp.recycle();
        return null;
    }

    public boolean onTouch(View v, MotionEvent event) {
    	mReref = true;
    	ApplicationDemo.viewMode = ApplicationDemo.VIEW_MODE_MATCHING;
        return false;
    }    
    
    
    @Override
    public void run() {
        super.run();

        synchronized (this) {
            // Explicitly deallocate Mats
            if (mYuv != null)
                mYuv.dispose();
            if (mRgba != null)
                mRgba.dispose();
            if (mRgb != null)
                mRgb2.dispose();
            if (mGraySubmat != null)
                mGraySubmat.dispose();
  
            mYuv = null;
            mRgba = null;
            mRgb = null;
            mRgb2 = null;
            mGraySubmat = null;
        }
    }
    
    
    public native void ProcessRefFrame(long matAddrSrc); 
    public native void MatchAndWarp(long matAddrImg, long matAddrImgBW, long matAddrWarpImg, long matAddrDst);

    static {
        System.loadLibrary("mixed_sample");
    }
}
