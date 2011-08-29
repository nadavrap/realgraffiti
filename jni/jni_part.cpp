#include <jni.h>
#include <opencv2/core/core.hpp>
#include <opencv2/imgproc/imgproc.hpp>
#include <opencv2/features2d/features2d.hpp>
#include <opencv2/calib3d/calib3d.hpp>
#include "opencv2/video/tracking.hpp"
#include <vector>

using namespace std;
using namespace cv;

#define RANSAC_REPROJ_DIST                  7   /* Allowed projection error distance in pixels */
#define USE_FAST_DETECTOR                   0
#define USE_GFTT_DETECTOR                   1	/* Use OpenCV GoodFeaturesToTrack detector wrapper */
#define MAX_INTEREST_POINTS					100				
#define USE_CORNERS_SUBPIXELS				1
#define FAST_DETECTOR_THRESHOLD             20
#define	MAX_ALLOWED_BRIEF_MATCH_DISTANCE	70	/* Threshold for filtering bad BRIEF Hamming matches */
#define USE_SURF_DESCRIPTOR                 0
#define USE_BRIEF_DESCRIPTOR                1
#define USE_FLANN_MATCHER                   0
#define USE_BRUTE_MATCHER                   1
#define CROSS_CHECK_FILTER                  1
#define CROSS_CHECK_MATCH_KNN_NUM			2	/* Cross-check the two nearest neighbors */
#define MIN_MATCH_NUM_POINTS                10
#define MIN_MATCH_RATIO                     0.25
#define MATCH_BACK_PROJ_MAX_DIST			25
#define NUM_FRAMES_BETWEEN_REMATCH          40
#define MAX_POINTS_TO_TRACK					30
#define MIN_TRACK_POINTS					6
#define MAX_TRACK_POINTS					30

static vector<KeyPoint> keypoints1;
static Mat descriptors1;
static int FrameCount = 0;
static int start_tracking = 0;
static Mat prevImgBW;
static vector<Point2f> trackPrevPoints, trackNewPoints;

//Takes a descriptor and turns it into an xy point
void keypoints2points(const vector<KeyPoint>& in, vector<Point2f>& out)
{
    out.clear();
    out.reserve(in.size());
    for (size_t i = 0; i < in.size(); ++i)
    {
        out.push_back(in[i].pt);
    }
}

//Takes an xy point and appends that to a keypoint structure
void points2keypoints(const vector<Point2f>& in, vector<KeyPoint>& out)
{
    out.clear();
    out.reserve(in.size());
    for (size_t i = 0; i < in.size(); ++i)
    {
        out.push_back(KeyPoint(in[i], 1));
    }
}


//Copy (x,y) location of descriptor matches found from KeyPoint data structures into Point2f vectors
void matches2points(const vector<DMatch>& matches, const vector<KeyPoint>& kpts_train,
                    const vector<KeyPoint>& kpts_query, vector<Point2f>& pts_train, vector<Point2f>& pts_query)
{
  pts_train.clear();
  pts_query.clear();
  pts_train.reserve(matches.size());
  pts_query.reserve(matches.size());
  for (size_t i = 0; i < matches.size(); i++)
  {
    const DMatch& match = matches[i];
    pts_query.push_back(kpts_query[match.queryIdx].pt);
    pts_train.push_back(kpts_train[match.trainIdx].pt);
  }

}

void CreateMaskImage(const Mat& srcImg, Mat& maskImg)
{
    // Create a binary mask for the nonzero pixels in the warpimg
	Mat srcImgbw;
	cvtColor(srcImg, srcImgbw, COLOR_RGB2GRAY);
	IplImage *cvmask = cvCreateImage(cvSize(srcImg.cols,srcImg.rows),  IPL_DEPTH_8U, 1);
	IplImage cvimg2 = srcImgbw;
	cvInRangeS(&cvimg2, cvScalar(10), cvScalar(255), cvmask);
	Mat mask(cvmask);
	mask.copyTo(maskImg);
	mask.release();
	srcImgbw.release();
}


void simpleMatching( const Mat& descriptors1, const Mat& descriptors2,
                     vector<DMatch>& matches12)
{
#if USE_FLANN_MATCHER
    FlannBasedMatcher matcher;
    matcher.match( descriptors1, descriptors2, matches12);
#elif USE_BRUTE_MATCHER
#if USE_BRIEF_DESCRIPTOR
	BruteForceMatcher<HammingLUT> matcher;  // BRIEF binary descriptor requires hamming distance and not L2

    vector<DMatch> matches;
    matcher.match( descriptors1, descriptors2, matches);

	matches12.clear();
	// Filter out matches with high match distance (in the feature space)
    for( size_t i = 0; i < matches.size(); i++ )
		if(matches[i].distance < MAX_ALLOWED_BRIEF_MATCH_DISTANCE)
			matches12.push_back(matches[i]);
#else
	BruteForceMatcher<L2<float> > matcher;
    matcher.match( descriptors1, descriptors2, matches12);
#endif
#endif
}

void crossCheckMatching( const Mat& descriptors1, const Mat& descriptors2,
                         vector<DMatch>& filteredMatches12, int knn=1 )
{
#if USE_FLANN_MATCHER
    FlannBasedMatcher matcher;
#elif USE_BRUTE_MATCHER
#if USE_BRIEF_DESCRIPTOR
	BruteForceMatcher<HammingLUT> matcher;  // BRIEF binary descriptor requires hamming distance and not L2
#endif
#endif
    filteredMatches12.clear();
    vector<vector<DMatch> > matches12, matches21;
    matcher.knnMatch( descriptors1, descriptors2, matches12, knn );
    matcher.knnMatch( descriptors2, descriptors1, matches21, knn );
    for( size_t m = 0; m < matches12.size(); m++ )
    {
        bool findCrossCheck = false;
        for( size_t fk = 0; fk < matches12[m].size(); fk++ )
        {
            DMatch forward = matches12[m][fk];

            for( size_t bk = 0; bk < matches21[forward.trainIdx].size(); bk++ )
            {
                DMatch backward = matches21[forward.trainIdx][bk];
                if( backward.trainIdx == forward.queryIdx )
                {
                    filteredMatches12.push_back(forward);
                    findCrossCheck = true;
                    break;
                }
            }
            if( findCrossCheck ) break;
        }
    }
}




void DetectPoints(const Mat& img, vector<KeyPoint>& keypoints, int MaxPoints)
{
#if USE_FAST_DETECTOR
	FastFeatureDetector detector(FAST_DETECTOR_THRESHOLD);
#elif USE_GFTT_DETECTOR
	GoodFeaturesToTrackDetector detector(MaxPoints, 0.1, 10);
#endif

    detector.detect( img, keypoints );
    if(keypoints.empty())
        return;

#if USE_CORNERS_SUBPIXELS
    TermCriteria termcrit(CV_TERMCRIT_ITER|CV_TERMCRIT_EPS,20,0.03);
    Size winSize(10,10);

    vector<Point2f> points;
    keypoints2points(keypoints, points);
    cornerSubPix(img, points, winSize, Size(-1,-1), termcrit);
    points2keypoints(points, keypoints);
#endif
}


void DescribePoints(const Mat& img, vector<KeyPoint>& keypoints, Mat& descriptors)
{
    if(keypoints.empty())
        return;

#if USE_SURF_DESCRIPTOR
   	SurfDescriptorExtractor extractor;
#elif USE_BRIEF_DESCRIPTOR
    BriefDescriptorExtractor extractor(32);
#endif
    extractor.compute( img, keypoints, descriptors );
}


// Perform interest point detection on the reference image
extern "C" {
JNIEXPORT void JNICALL Java_realgraffiti_android_camera_CameraLiveView_ProcessRefFrame(JNIEnv* env, jobject thiz, jlong addrSrc)
{
	Mat* srcimg = (Mat*)addrSrc;
	DetectPoints(*srcimg, keypoints1,MAX_INTEREST_POINTS);
	DescribePoints(*srcimg, keypoints1, descriptors1);
}
}


// Perform Lucas-Kanade Optical Flow tracking of the previosly matched points
extern "C" {
JNIEXPORT jint JNICALL Java_realgraffiti_android_camera_CameraLiveView_TrackPoints(JNIEnv* env, jobject thiz, jlong matAddrImgBW, jlong matAddrOffsetDst)
{
    Mat* pMatImgBW=(Mat*)matAddrImgBW;
	Mat* offsetMat = (Mat*)matAddrOffsetDst;

	if((*pMatImgBW).empty())
		return -1;

	// If no or too few tracked points, run interest point detector
	if( trackPrevPoints.empty() || trackPrevPoints.size()<MIN_TRACK_POINTS )
	{
		vector<KeyPoint> keypoints;
		DetectPoints(*pMatImgBW, keypoints,MAX_TRACK_POINTS);
    	keypoints2points(keypoints, trackPrevPoints);
    	(*pMatImgBW).copyTo(prevImgBW);
		return -2;
	}

    TermCriteria termcrit(CV_TERMCRIT_ITER|CV_TERMCRIT_EPS,10,0.1);
//  TermCriteria termcrit(CV_TERMCRIT_ITER|CV_TERMCRIT_EPS,20,0.03);
    Size winSize(20,20);
    vector<uchar> status;
    vector<float> err;
	Point2f newCenter(0,0), prevCenter(0,0), currShift;

    if(prevImgBW.empty())
    	(*pMatImgBW).copyTo(prevImgBW);

    // Do Locas-Kanade Pyramide Optical Flow
    calcOpticalFlowPyrLK(prevImgBW, *pMatImgBW, trackPrevPoints, trackNewPoints, status, err, winSize, 3, termcrit, 0);

    // Update points
    size_t i, k;
    for( i = k = 0; i < trackNewPoints.size(); i++ )
    {
        if( !status[i] )
        continue;

		prevCenter+=trackPrevPoints[i];
		newCenter+=trackNewPoints[i];
        trackNewPoints[k] = trackNewPoints[i];
        k++;
    }
    trackNewPoints.resize(k);

	// Move the current data to the previous data for the next track step
    std::swap(trackNewPoints, trackPrevPoints);
    (*pMatImgBW).copyTo(prevImgBW);

	if(trackPrevPoints.size()<MIN_TRACK_POINTS)
		return -3;

	prevCenter*=1.0/(float)k;
	newCenter*=1.0/(float)k;
	currShift=newCenter-prevCenter;

	// Return the shift in the center of gravity of the tracked points - the mean optical flow
	*offsetMat = Mat(currShift);

	return 0;

}
}
 


// Perform interest point detection, matching to referece points, homography transformation calculation and warping of the graffiti image
extern "C" {
JNIEXPORT jint JNICALL Java_realgraffiti_android_camera_CameraLiveView_MatchAndWarp(JNIEnv* env, jobject thiz, jlong matAddrImgBW, jlong matAddrWarpImg, jlong matAddrWarpedDst)
{
    Mat* pMatImgBW=(Mat*)matAddrImgBW;
    Mat* pMatWarpImg=(Mat*)matAddrWarpImg;
    Mat* pMatWarpedDst=(Mat*)matAddrWarpedDst;

    Mat H12, matchmask;
    vector<unsigned char> homography_matches;
    double num_points,num_matched=0;
	char stat[70];

	if(descriptors1.empty())
		return -1;

   	vector<KeyPoint> keypoints2;
    Mat descriptors2;

	// Detect interest points and calculate interest point descriptions
	DetectPoints(*pMatImgBW, keypoints2,MAX_INTEREST_POINTS);
	DescribePoints(*pMatImgBW, keypoints2, descriptors2);

	if(keypoints2.empty())
	    return -2;

	vector<DMatch> filteredMatches;
#if CROSS_CHECK_FILTER
	crossCheckMatching( descriptors1, descriptors2, filteredMatches, CROSS_CHECK_MATCH_KNN_NUM );
#else
	simpleMatching( descriptors1, descriptors2, filteredMatches);
#endif

	if(filteredMatches.size() < MIN_MATCH_NUM_POINTS)
		return -3;

	vector<int> queryIdxs( filteredMatches.size() ), trainIdxs( filteredMatches.size() );
	for( size_t i = 0; i < filteredMatches.size(); i++ )
	{
	    queryIdxs[i] = filteredMatches[i].queryIdx;
	    trainIdxs[i] = filteredMatches[i].trainIdx;
	}

	vector<Point2f> points1; KeyPoint::convert(keypoints1, points1, queryIdxs);
	vector<Point2f> points2; KeyPoint::convert(keypoints2, points2, trainIdxs);

	// Calculate a homography transformation matrix using RANSAC for filtering outliers
	H12 = findHomography( Mat(points1), Mat(points2), CV_RANSAC, RANSAC_REPROJ_DIST, homography_matches );

	num_matched = countNonZero(Mat(homography_matches));
	num_points = points2.size();
    if( H12.empty() || (num_matched<MIN_MATCH_NUM_POINTS) || (num_matched/num_points < MIN_MATCH_RATIO) )
		return num_points*100+num_matched;

	// Warp the input Warp image according to the calculated homography
    warpPerspective(*pMatWarpImg, *pMatWarpedDst, H12, (*pMatWarpImg).size() );
	// Create a binary mask for non-black pixels in the warped image
//	CreateMaskImage(*pMatWarpedDst, *pMatWarpedMaskDst);

	return 0;
}
}


