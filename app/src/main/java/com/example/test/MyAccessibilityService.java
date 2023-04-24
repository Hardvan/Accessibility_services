package com.example.test;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

public class MyAccessibilityService extends AccessibilityService {
    int c=0;
    private static final String TAG = "MyAccessibilityService";
    @SuppressLint("SuspiciousIndentation")
    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        //Log.e(TAG, "onAccessibilityEvent: ");t
        String s=String.valueOf(event.getText());
        if(!(s.contains("[]")) & !String.valueOf(event.getText()).contains("Notification"))
            Log.e(TAG,s);
            if(String.valueOf(event.getText()).contains("Notification")||String.valueOf(event.getText()).contains("Quick setting"))
                Log.e(TAG,"Notification shade");
        final int eventType = event.getEventType();
        switch (eventType) {
            case AccessibilityEvent.TYPE_VIEW_CLICKED:
                Log.e(TAG, "Click");

                AccessibilityNodeInfo sourceNode = event.getSource();

                if (sourceNode != null) {
                    Rect bounds = new Rect();
                    sourceNode.getBoundsInScreen(bounds);
                    int touchX = bounds.centerX();
                    int touchY = bounds.centerY();
                    Log.e(TAG, "Touch Location is X=" + touchX + " Y=" + touchY);
                }
                break;

            case AccessibilityEvent.TYPE_VIEW_LONG_CLICKED:

                Log.e(TAG, "Long Click");
                AccessibilityNodeInfo sourceNode2 = event.getSource();
                if (sourceNode2 != null) {
                    Rect bounds = new Rect();
                    sourceNode2.getBoundsInScreen(bounds);
                    int touchX = bounds.centerX();
                    int touchY = bounds.centerY();
                    Log.e(TAG, "Touch Location is X=" + touchX + " Y=" + touchY);
                }
                break;


        }
        if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {

            AccessibilityNodeInfo rootNodeInfo = getRootInActiveWindow();
            String packageName = event.getPackageName().toString();
            PackageManager packageManager = this.getPackageManager();

            if (packageName.equals("android")) {
                Log.e(TAG, "The app has crashed");
            }
        }

    }

        @Override
        public void onInterrupt () {
            Log.e(TAG, "onInterrupt: unexpected error");
        }

        @Override
        protected void onServiceConnected () {
            super.onServiceConnected();
            AccessibilityServiceInfo info = new AccessibilityServiceInfo();
            info.eventTypes = AccessibilityEvent.TYPES_ALL_MASK;


            info.feedbackType = AccessibilityServiceInfo.FEEDBACK_SPOKEN;


            info.notificationTimeout = 10;

            this.setServiceInfo(info);
            Log.e(TAG, "onServiceConnected: ");
        }

        private static void findPopups (AccessibilityNodeInfo nodeInfo){
            if (nodeInfo.getChildCount() == 0) {
                return;
            }
            for (int i = 0; i < nodeInfo.getChildCount(); i++) {
                AccessibilityNodeInfo childNodeInfo = nodeInfo.getChild(i);
                if (childNodeInfo.getClassName().equals("PopupWindow")){
                    Log.e(TAG,"Popup detected");}

                else {
                    Log.e(TAG,childNodeInfo.getClassName().toString());
                    findPopups(childNodeInfo);
                }
            }
        }

    }
