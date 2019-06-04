/*
 * Copyright (C) 2011 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.ros.android.android_tutorial_teleop;

import com.google.common.collect.Lists;
import com.google.common.base.Preconditions;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ToggleButton;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import org.ros.address.InetAddressFactory;
import org.ros.android.RosActivity;
import org.ros.android.view.VirtualJoystickView;
import org.ros.android.view.visualization.VisualizationView;
import org.ros.android.view.visualization.layer.CameraControlLayer;
import org.ros.android.view.visualization.layer.CameraControlListener;
import org.ros.android.view.visualization.layer.LaserScanLayer;
import org.ros.android.view.visualization.layer.Layer;
import org.ros.android.view.visualization.layer.OccupancyGridLayer;
import org.ros.android.view.visualization.layer.PathLayer;
import org.ros.android.view.visualization.layer.PosePublisherLayer;
import org.ros.android.view.visualization.layer.PoseSubscriberLayer;
import org.ros.android.view.visualization.layer.RobotLayer;
import org.ros.node.NodeConfiguration;
import org.ros.node.NodeMainExecutor;
import org.ros.time.NtpTimeProvider;
import android.util.Log;
import org.ros.concurrent.CancellableLoop;
import org.ros.rosjava_geometry.Vector3;

import java.util.concurrent.TimeUnit;

/**
 * An app that can be used to control a remote robot. This app also demonstrates
 * how to use some of views from the rosjava android library.
 * 
 * @author munjaldesai@google.com (Munjal Desai)
 * @author moesenle@google.com (Lorenz Moesenlechner)
 */
public class MainActivity extends RosActivity {


  private static final String MAP_FRAME = "map";
  private VirtualJoystickView virtualJoystickView;
  private VisualizationView visualizationView;

  private CameraControlLayer cameraControlLayer;
  private static final String ROBOT_FRAME = "base_link";

  public MainActivity() {
    super("Smart-Wheelchair", "Smart-Wheelchair");
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.settings_menu, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case R.id.virtual_joystick_snap:
        if (!item.isChecked()) {
          item.setChecked(true);
          virtualJoystickView.EnableSnapping();
        } else {
          item.setChecked(false);

          virtualJoystickView.DisableSnapping();
        }
        return true;
      default:
        return super.onOptionsItemSelected(item);
    }
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main);
    virtualJoystickView = (VirtualJoystickView) findViewById(R.id.virtual_joystick);
    visualizationView = (VisualizationView) findViewById(R.id.visualization);
    visualizationView.getCamera().jumpToFrame("map");
    cameraControlLayer = new CameraControlLayer();
    visualizationView.onCreate(Lists.<Layer>newArrayList(cameraControlLayer,
            new OccupancyGridLayer("map"), new PathLayer("move_base/NavfnROS/plan"),
            new LaserScanLayer("scan"), new PosePublisherLayer("simple_waypoints_server/goal_pose"), new RobotLayer("base_footprint")));
    //enableFollowMe();
  }

  @Override
  protected void init(NodeMainExecutor nodeMainExecutor) {
    visualizationView.init(nodeMainExecutor);
    cameraControlLayer.addListener(new CameraControlListener() {
      @Override
      public void onZoom(float focusX, float focusY, float factor) {
        //disableFollowMe();
      }
      @Override
      public void onTranslate(float distanceX, float distanceY) {
        //disableFollowMe();
      }
      @Override
      public void onRotate(float focusX, float focusY, double deltaAngle) {
        //disableFollowMe();
      }
      @Override
      public void onDoubleTap(float x, float y) {
        Vector3 touchVector = visualizationView.getCamera().toCameraFrame((int) x, (int) y);
        double touchX = touchVector.getX();
        double touchY = touchVector.getY();//LaserScanLayer
        String text = "x:" + touchX +" y: " + touchY;
        Log.d("tag", text);
        toast(text);
      }
    });
    NodeConfiguration nodeConfiguration =
            NodeConfiguration.newPublic(InetAddressFactory.newNonLoopback().getHostAddress(),
                    getMasterUri());
    NtpTimeProvider ntpTimeProvider =
            new NtpTimeProvider(InetAddressFactory.newFromHostString("192.168.0.1"),
                    nodeMainExecutor.getScheduledExecutorService());
    ntpTimeProvider.startPeriodicUpdates(1, TimeUnit.MINUTES);
    nodeConfiguration.setTimeProvider(ntpTimeProvider);
    nodeMainExecutor.execute(virtualJoystickView, nodeConfiguration.setNodeName("virtual_joystick"));
    nodeMainExecutor.execute(visualizationView, nodeConfiguration.setNodeName("android/map_view"));
  }

  private void toast(final String text) {
    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        Toast toast = Toast.makeText(MainActivity.this, text, Toast.LENGTH_SHORT);
        toast.show();
      }
    });

  }

//  private void enableFollowMe() {
//    Preconditions.checkNotNull(visualizationView);
//
//    runOnUiThread(new Runnable() {
//      @Override
//      public void run() {
//        visualizationView.getCamera().jumpToFrame(ROBOT_FRAME);
//      }
//    });
//  }
//
//  //zoom, translate, rotate
//  private void disableFollowMe() {
//    Preconditions.checkNotNull(visualizationView);
//    runOnUiThread(new Runnable() {
//      @Override
//      public void run() {
//        visualizationView.getCamera().setFrame(MAP_FRAME);
//      }
//    });
//  }

}