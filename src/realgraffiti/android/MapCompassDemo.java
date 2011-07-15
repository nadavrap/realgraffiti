/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package realgraffiti.android;

import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.DrawFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Picture;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Region;
import android.hardware.SensorListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import javax.microedition.khronos.opengles.GL;

import realgraffiti.android.map.GraffitiMiniMapView;

/**
 * Example of how to use an {@link com.google.android.maps.MapView}
 * in conjunction with the {@link com.hardware.SensorManager}
 * <h3>MapViewCompassDemo</h3>

<p>This demonstrates creating a Map based Activity.</p>

<h4>Source files</h4>
 * <table class="LinkTable">
 *         <tr>
 *             <td >src/com.example.android.apis/view/MapViewCompassDemo.java</td>
 *             <td >The Alert Dialog Samples implementation</td>
 *         </tr>
 * </table>
 */
public class MapCompassDemo extends MapActivity {


	private static final String TAG = "MapViewCompassDemo";
    private GraffitiMiniMapView _miniMapView;
    private MapView mMapView;
    private MyLocationOverlay mMyLocationOverlay;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        _miniMapView = new GraffitiMiniMapView(this);
        mMapView = new MapView(this, "0OUnpM96lLtw7orPft9tQGYGiIuhVDDEJmmQjHg");
        
        _miniMapView.addView(mMapView);
        
        setContentView(_miniMapView);

        /*mMyLocationOverlay = new MyLocationOverlay(this, mMapView);
        mMyLocationOverlay.runOnFirstFix(new Runnable() { public void run() {
            mMapView.getController().animateTo(mMyLocationOverlay.getMyLocation());
        }});*/
        //mMapView.getOverlays().add(mMyLocationOverlay);
        mMapView.getController().setZoom(8);
        mMapView.setClickable(true);
        mMapView.setEnabled(true);
        mMapView.setBuiltInZoomControls(true);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStop() {
        mMyLocationOverlay.disableMyLocation();
        super.onStop();
    }

    @Override
    protected boolean isRouteDisplayed() {
        return false;
    }


   
}