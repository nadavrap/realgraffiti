#include <jni.h>
#include <opencv2/core/core.hpp>
#include <opencv2/imgproc/imgproc.hpp>
#include <opencv2/features2d/features2d.hpp>
#include <opencv2/calib3d/calib3d.hpp>
#include <vector>

using namespace std;
using namespace cv;

#define RANSAC_REPROJ_DIST                  5   /* Allowed projection error distance in pixels */
#define USE_FAST_DETECTOR                   0
#define USE_GFTT_DETECTOR                   1	/* Use OpenCV GoodFeaturesToTrack detector wrapper */
#define GFTT_MAX_CORNERS					100				
#define FAST_DETECTOR_THRESHOLD             20
#define	MAX_ALLOWED_BRIEF_MATCH_DISTANCE	30	/* Threshold for filtering bad BRIEF Hamming matches */
#define USE_SURF_DESCRIPTOR                 0
#define USE_BRIEF_DESCRIPTOR                1
#define USE_FLANN_MATCHER                   0
#define USE_BRUTE_MATCHER                   1
#define CROSS_CHECK_FILTER                  0
#define DRAW_WARPED_POINTS_MODE             1
#define MIN_MATCH_NUM_POINTS                10
#define MIN_MATCH_RATIO                     0.25
#define MATCH_BACK_PROJ_MAX_DIST			25

static vector<KeyPoint> keypoints1;
static Mat descriptors1;
//static Mat PrevH12;


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

//Uses computed homography H to warp original input points to new planar position
void warpKeypoints(const Mat& H, const vector<KeyPoint>& in, vector<KeyPoint>& out)
{
    vector<Point2f> pts;
    keypoints2points(in, pts);
    vector<Point2f> pts_w(pts.size());
    Mat m_pts_w(pts_w);
    perspectiveTransform(Mat(pts), m_pts_w, H);
    points2keypoints(pts_w, out);
}

void resetH(Mat&H)
{
    H = Mat::eye(3, 3, CV_32FC1);
}


extern "C" {
JNIEXPORT void JNICALL Java_realgraffiti_android_activities_CameraLiveView_ProcessRefFrame(JNIEnv* env, jobject thiz, jlong addrSrc)
{
	Mat* srcimg = (Mat*)addrSrc;

#if USE_FAST_DETECTOR
	FastFeatureDetector detector(FAST_DETECTOR_THRESHOLD);
#elif USE_GFTT_DETECTOR
	GoodFeaturesToTrackDetector detector(GFTT_MAX_CORNERS, 0.1, 10);
#endif
    detector.detect( *srcimg, keypoints1 );
    if(keypoints1.empty())
        return;

#if USE_SURF_DESCRIPTOR
   	SurfDescriptorExtractor extractor;
#elif USE_BRIEF_DESCRIPTOR
    BriefDescriptorExtractor extractor(32);
#endif
    extractor.compute( *srcimg, keypoints1, descriptors1 );
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
                     vector<DMatch>& matches12 /*, Mat& mask */)
{
#if USE_FLANN_MATCHER
    FlannBasedMatcher matcher;
#elif USE_BRUTE_MATCHER
#if USE_BRIEF_DESCRIPTOR
	BruteForceMatcher<HammingLUT> matcher;  // BRIEF binary descriptor requires hamming distance and not L2
#else
	BruteForceMatcher<L2<float> > matcher;
#endif
#endif
    vector<DMatch> matches;
    matcher.match( descriptors1, descriptors2, matches /*, mask*/ );

	matches12.clear();
	// Filter out matches with high match distance (in the feature space)
    for( size_t i = 0; i < matches.size(); i++ )
		if(matches[i].distance < MAX_ALLOWED_BRIEF_MATCH_DISTANCE)
			matches12.push_back(matches[i]);

}

void crossCheckMatching( const Mat& descriptors1, const Mat& descriptors2,
                         vector<DMatch>& filteredMatches12, int knn=1 )
{
    FlannBasedMatcher matcher;
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



extern "C" {
JNIEXPORT void JNICALL Java_realgraffiti_android_activities_CameraLiveView_MatchAndWarp(JNIEnv* env, jobject thiz, jlong matAddrImg, jlong matAddrImgBW, jlong matAddrWarpImg, jlong matAddrDst)
{
    Mat* pMatImg=(Mat*)matAddrImg;
    Mat* pMatImgBW=(Mat*)matAddrImgBW;
    Mat* pMatWarpImg=(Mat*)matAddrWarpImg;
    Mat* pMatDst=(Mat*)matAddrDst;

    Mat H12, matchmask;
    vector<unsigned char> homography_matches;

	if(descriptors1.empty())
	{
		(*pMatWarpImg).copyTo(*pMatDst);
		putText(*pMatDst,"descriptors1 empty",cvPoint(30,100),FONT_HERSHEY_COMPLEX,2,cvScalar(255,0,0));
		return;
	}


    vector<KeyPoint> keypoints2;
#if USE_FAST_DETECTOR
	FastFeatureDetector detector(FAST_DETECTOR_THRESHOLD);
#elif USE_GFTT_DETECTOR
	GoodFeaturesToTrackDetector detector(GFTT_MAX_CORNERS, 0.1, 10);
#endif
    detector.detect( *pMatImgBW, keypoints2 );
    if(keypoints2.empty())
	{
		(*pMatImg).copyTo(*pMatDst);
		putText(*pMatDst,"No interest points detected",cvPoint(30,100),FONT_HERSHEY_COMPLEX,1,cvScalar(255,0,0),2);
//		resetH(PrevH12);
        return;
	}

	// Use the previous homography matrix to filter the new keypoints
//	if(!PrevH12.empty())
//	{
//    	vector<KeyPoint> test_kpts;
//        warpKeypoints(PrevH12.inv(), keypoints2, test_kpts);
//
//        matchmask = windowedMatchingMask(test_kpts, keypoints1, MATCH_BACK_PROJ_MAX_DIST, MATCH_BACK_PROJ_MAX_DIST);
//	}

#if USE_SURF_DESCRIPTOR
   	SurfDescriptorExtractor extractor;
#elif USE_BRIEF_DESCRIPTOR
    BriefDescriptorExtractor extractor(32);
#endif
    Mat descriptors2;
    extractor.compute( *pMatImgBW, keypoints2, descriptors2 );

    vector<DMatch> filteredMatches;
#if CROSS_CHECK_FILTER
        crossCheckMatching( descriptors1, descriptors2, filteredMatches, 1 );
#else
        simpleMatching( descriptors1, descriptors2, filteredMatches /*, matchmask */);
#endif

	if(filteredMatches.size() < MIN_MATCH_NUM_POINTS)
	{
		(*pMatImg).copyTo(*pMatDst);
		putText(*pMatDst,"Not enough good matching points",cvPoint(30,100),FONT_HERSHEY_COMPLEX,1,cvScalar(255,0,0),2);
//		resetH(PrevH12);
		return;
	}

	float mean_dist = 0;
    vector<int> queryIdxs( filteredMatches.size() ), trainIdxs( filteredMatches.size() );
    for( size_t i = 0; i < filteredMatches.size(); i++ )
    {
		mean_dist = mean_dist + filteredMatches[i].distance;
        queryIdxs[i] = filteredMatches[i].queryIdx;
        trainIdxs[i] = filteredMatches[i].trainIdx;
    }
	mean_dist = mean_dist / filteredMatches.size();

    vector<Point2f> points1; KeyPoint::convert(keypoints1, points1, queryIdxs);
    vector<Point2f> points2; KeyPoint::convert(keypoints2, points2, trainIdxs);

	// Calculate a homography transformation matrix using RANSAC for filtering outliers
    H12 = findHomography( Mat(points1), Mat(points2), CV_RANSAC, RANSAC_REPROJ_DIST, homography_matches );

    if( !H12.empty() ) // filter outliers
    {
        vector<Point2f> points1; KeyPoint::convert(keypoints1, points1, queryIdxs);
        vector<Point2f> points2; KeyPoint::convert(keypoints2, points2, trainIdxs);
        Mat points1t; perspectiveTransform(Mat(points1), points1t, H12);

		double num_matched = (double)countNonZero(Mat(homography_matches));

//        double num_matched=0;
//        for( size_t i1 = 0; i1 < points1.size(); i1++ )
//        {
//            if( norm(points2[i1] - points1t.at<Point2f>((int)i1,0)) <= RANSAC_REPROJ_DIST )
//                num_matched++;
//        }

		char stat[70];
		sprintf(stat,"%d / %d / %d / %d / %2.3f",keypoints1.size(),keypoints2.size(), filteredMatches.size(), (int)num_matched, mean_dist);

        if( (num_matched>MIN_MATCH_NUM_POINTS) && (num_matched/points2.size() > MIN_MATCH_RATIO))
        {
            Mat warpedImg, maskImg;
			// Warp the input Warp image according to the calculated homography
            warpPerspective(*pMatWarpImg, warpedImg, H12, (*pMatWarpImg).size() );
			// Create a binary mask for non-black pixels in the warped image
			CreateMaskImage(warpedImg, maskImg);

			// Overlay the warped image on top of the input frame
            (*pMatImg).copyTo(*pMatDst);
            warpedImg.copyTo(*pMatDst,maskImg);

			putText(*pMatDst,stat,cvPoint(30,100),FONT_HERSHEY_COMPLEX,1,cvScalar(0,128,64),2);

//			H12.copyTo(PrevH12); // Save the calculated homography

			warpedImg.release();
			maskImg.release();
        }
		else
		{
//			resetH(PrevH12);
			(*pMatImg).copyTo(*pMatDst);
			putText(*pMatDst,stat,cvPoint(30,100),FONT_HERSHEY_COMPLEX,1,cvScalar(255,0,0),2);
//			putText(*pMatDst,"Match Failed",cvPoint(30,100),FONT_HERSHEY_COMPLEX,2,cvScalar(255,0,0));
		}

    }

}
}


