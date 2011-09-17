/*
IMPORTANT: READ BEFORE DOWNLOADING, COPYING, INSTALLING OR USING. 

 By downloading, copying, installing or using the software you agree to this license.
 If you do not agree to this license, do not download, install,
 copy or use the software.


                          License Agreement
               For Open Source Computer Vision Library

Copyright (C) 2000-2008, Intel Corporation, all rights reserved.
Copyright (C) 2008-2010, Willow Garage Inc., all rights reserved.
Third party copyrights are property of their respective owners.

Redistribution and use in source and binary forms, with or without modification,
are permitted provided that the following conditions are met:

  * Redistribution's of source code must retain the above copyright notice,
    this list of conditions and the following disclaimer.

  * Redistribution's in binary form must reproduce the above copyright notice,
    this list of conditions and the following disclaimer in the documentation
    and/or other materials provided with the distribution.

  * The name of the copyright holders may not be used to endorse or promote products
    derived from this software without specific prior written permission.

This software is provided by the copyright holders and contributors "as is" and
any express or implied warranties, including, but not limited to, the implied
warranties of merchantability and fitness for a particular purpose are disclaimed.
In no event shall the Intel Corporation or contributors be liable for any direct,
indirect, incidental, special, exemplary, or consequential damages
(including, but not limited to, procurement of substitute goods or services;
loss of use, data, or profits; or business interruption) however caused
and on any theory of liability, whether in contract, strict liability,
or tort (including negligence or otherwise) arising in any way out of
the use of this software, even if advised of the possibility of such damage.

*/
#define CV_NO_BACKWARD_COMPATIBILITY

#include "cv.h"
#include "highgui.h"
#include "swp_steuerung_HeadTracker.h"

#include <jni.h>
#include <iostream>
#include <cstdio>
#include <windows.h>

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <assert.h>
#include <math.h>
#include <float.h>
#include <limits.h>
#include <time.h>
#include <ctype.h>



#ifdef _EiC
#define WIN32
#endif

using namespace std;
using namespace cv;

void detectFace( Mat& img,
                   CascadeClassifier& cascade, CascadeClassifier& nestedCascade,
                   double scale);
int normalizePosition(int, int);

String cascadeName = "C:/filesforheadtracker/haarcascade_frontalface_alt.xml";

int radiusMax = 0;
int centerX = 0;
int centerY = 0;
int videoFrameSizeX = 1;
double detectionTime = 0;
int running = 0;

JNIEXPORT jint JNICALL Java_swp_steuerung_HeadTracker_detectFace (JNIEnv *env, jobject obj) {
    CvCapture* capture = 0;
    Mat frame, frameCopy, image;
    const String scaleOpt = "--scale=";
    size_t scaleOptLen = scaleOpt.length();
    const String cascadeOpt = "--cascade=";
    size_t cascadeOptLen = cascadeOpt.length();
    const String nestedCascadeOpt = "--nested-cascade";
    size_t nestedCascadeOptLen = nestedCascadeOpt.length();
    String inputName;
    
    if(running) { running = 0; return 0; }
    else { running = 1; }

    CascadeClassifier cascade, nestedCascade;
    double scale = 1;
    int maxCapturesPerSecond = 10;

	 try
	 {
		if( !cascade.load( cascadeName ) )
		{
			cerr << "ERROR: Could not load classifier cascade" << endl;
			return -1;
		}
	 }
	 catch(exception ex)
	 {}
    if( inputName.empty() || (isdigit(inputName.c_str()[0]) && inputName.c_str()[1] == '\0') )
        capture = cvCaptureFromCAM( inputName.empty() ? 0 : inputName.c_str()[0] - '0' );
    else
    {
        cerr << "ERROR: Could not read from camera" << endl;
        return -1;
    }

//    cvNamedWindow( "result", 1 );
    if( capture )
    {
        for(;;)
        {
            IplImage* iplImg = cvQueryFrame( capture );
            frame = iplImg;
            if( frame.empty() )
                break;
            
            if( iplImg->origin == IPL_ORIGIN_TL )
                frame.copyTo( frameCopy );
            else
                flip( frame, frameCopy, 0 );
            detectFace( frameCopy, cascade, nestedCascade, scale );
            //Das Ergebnis der Gesichtsekennung an das Java-Programm uebergeben:
            jclass javaClass = (env)->GetObjectClass(obj);
            jmethodID javaMethodSetFace = (env)->GetMethodID(javaClass, "setFace", "(III)V");
            jmethodID javaMethodSetFrameSize = (env)->GetMethodID(javaClass, "setVideoFrameSize", "(II)V");
            if(javaMethodSetFace == 0 || javaMethodSetFrameSize == 0) printf("Could not exec java method.");
            if(radiusMax != 0) {
              if(!running) return 0;
              //Die Eckdaten des groessten erkannten Gesichtes an das Java-Programm uebergeben.
              if(cvRound(frame.rows/scale) != videoFrameSizeX) {
                //Wenn sich die Videobreite geaendert hat, wird die neue an das Java-Programm uebergeben.
                videoFrameSizeX = cvRound(frame.rows/scale);
                (env)->CallVoidMethod(obj, javaMethodSetFrameSize, videoFrameSizeX, cvRound(frame.cols/scale));
              }
              (env)->CallVoidMethod(obj,
                                    javaMethodSetFace,
                                    normalizePosition(radiusMax, cvRound(frame.cols/scale)),
                                    normalizePosition(centerX, cvRound(frame.cols/scale)),
                                    normalizePosition(centerY, cvRound(frame.rows/scale)));
            }else{
              //Wenn kein Gesicht erkannt wurde:
              (env)->CallVoidMethod(obj, javaMethodSetFace, 0, 0, 0);
            }
            if(detectionTime < (1000/maxCapturesPerSecond)) {
              //Verhindert eine unnoetige Auslastung der CPU
              Sleep(((1000/maxCapturesPerSecond) - detectionTime)*1000);         
            }

        }

        waitKey(0);

_cleanup_:
        cvReleaseCapture( &capture );
    }
    else
    {
        if( !image.empty() )
        {
            detectFace( image, cascade, nestedCascade, scale );
            waitKey(0);
        }
        else if( !inputName.empty() )
        {
            // assume it is a text file containing the
            // list of the image filenames to be processed - one per line
            FILE* f = fopen( inputName.c_str(), "rt" );
            if( f )
            {
                char buf[1000+1];
                while( fgets( buf, 1000, f ) )
                {
                    int len = (int)strlen(buf), c;
                    while( len > 0 && isspace(buf[len-1]) )
                        len--;
                    buf[len] = '\0';
                    cout << "file " << buf << endl;
                    image = imread( buf, 1 );
                    if( !image.empty() )
                    {
                        detectFace( image, cascade, nestedCascade, scale );
                        c = waitKey(0);
                        if( c == 27 || c == 'q' || c == 'Q' )
                            break;
                    }
                }
                fclose(f);
            }
        }
    }

//    cvDestroyWindow("result");

    return 0;
}


int normalizePosition(int value, int maxValue) {
  // drueckt die Relation von Value zu maxValue auf einer Skala zwischen 1 und 100 aus
  int normValue = 1;
  if(value > maxValue) return normValue;
  normValue = cvRound((1000 * value)/(maxValue));
  return normValue;
}

void detectFace( Mat& img,
                   CascadeClassifier& cascade, CascadeClassifier& nestedCascade,
                   double scale)
{
    int i = 0;
    double t = 0;
    vector<Rect> faces;
    const static Scalar colors[] =  { CV_RGB(0,0,255),
        CV_RGB(0,128,255),
        CV_RGB(0,255,255),
        CV_RGB(0,255,0),
        CV_RGB(255,128,0),
        CV_RGB(255,255,0),
        CV_RGB(255,0,0),
        CV_RGB(255,0,255)} ;
    Mat gray, smallImg( cvRound (img.rows/scale), cvRound(img.cols/scale), CV_8UC1 );

    radiusMax = 0;

    cvtColor( img, gray, CV_BGR2GRAY );
    resize( gray, smallImg, smallImg.size(), 0, 0, INTER_LINEAR );
    equalizeHist( smallImg, smallImg );

    t = (double)cvGetTickCount();
    cascade.detectMultiScale( smallImg, faces,
        1.1, 2, 0
        |CV_HAAR_FIND_BIGGEST_OBJECT
        //|CV_HAAR_DO_ROUGH_SEARCH
        //|CV_HAAR_SCALE_IMAGE
        ,
        Size(cvRound (img.rows/scale/7), cvRound (img.rows/scale/7)) );
    t = (double)cvGetTickCount() - t;
    detectionTime = t/((double)cvGetTickFrequency()*1000.);
    //printf( "detection time = %g ms\n", detectionTime );

    //Ohne die Option CV_HAAR_FIND_BIGGEST_OBJECT kann faces mehrere Gesichter enthalten, aus denen das groesste ermittelt wird:
    for( vector<Rect>::const_iterator r = faces.begin(); r != faces.end(); r++, i++ )
    {
        Mat smallImgROI;
        vector<Rect> nestedObjects;
        Point center;
        int radius;
        center.x = cvRound((r->x + r->width*0.5)*scale);
        center.y = cvRound((r->y + r->height*0.5)*scale);
        radius = cvRound((r->width + r->height)*0.25*scale);
        if(radius > radiusMax) {
          //Das erkannte Gesicht ist groesser als das bisherige groesste.
          radiusMax = radius;
          centerX = center.x;
          centerY = center.y;
        }
    }

}

