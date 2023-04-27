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
    int c = 0;
    private static final String TAG = "MyAccessibilityService";
    @SuppressLint({"SuspiciousIndentation", "SwitchIntDef"})
    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
         //Log.e(TAG, "onAccessibilityEvent: ");t

        // ? Get the text of the event
        String s = String.valueOf(event.getText());

        // Check if the event text is empty
        boolean check_empty = s.contains("[]");
        // Check if the event text is a notification
        boolean check_notification = s.contains("Notification");
        // Check if the event text is a quick setting
        boolean check_quick_settings = s.contains("Quick setting");

        if(!check_empty && !check_notification)
            Log.i(TAG, s); // Print the text of the event (EG: [Home 1 of 1])

        if(check_notification || check_quick_settings)
            Log.e(TAG,"Notification shade");

        // ? Get the event type and X,Y coordinates
        final int eventType = event.getEventType();
        String eventTypeStr = "";
        
        int touchX = -1;
        int touchY = -1;
        switch (eventType) {

            // * Click event
            case AccessibilityEvent.TYPE_VIEW_CLICKED:
                eventTypeStr = "Click";
                Log.i(TAG, "Event type: " + eventTypeStr);

                AccessibilityNodeInfo sourceNode = event.getSource();

                // ? Get the location of the click
                if (sourceNode != null) {
                    Rect bounds = new Rect();
                    sourceNode.getBoundsInScreen(bounds);
                    touchX = bounds.centerX();
                    touchY = bounds.centerY();
                    Log.i(TAG, "Touch Location is X=" + touchX + " Y=" + touchY);
                }
                break;

            // * Long click event
            case AccessibilityEvent.TYPE_VIEW_LONG_CLICKED:

                eventTypeStr = "Long Click";
                Log.i(TAG, "Event type: " + eventTypeStr);

                AccessibilityNodeInfo sourceNode2 = event.getSource();

                // ? Get the location of the long click
                if (sourceNode2 != null) {
                    Rect bounds = new Rect();
                    sourceNode2.getBoundsInScreen(bounds);
                    touchX = bounds.centerX();
                    touchY = bounds.centerY();
                    Log.i(TAG, "Touch Location is X=" + touchX + " Y=" + touchY);
                }
                break;

            // * Window state changed event
            case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED:

                eventTypeStr = "Window State Changed";
                Log.i(TAG, "Event type: " + eventTypeStr);

                // Detect if the app has crashed
                AccessibilityNodeInfo rootNodeInfo = getRootInActiveWindow();
                String packageName = event.getPackageName().toString();
                PackageManager packageManager = this.getPackageManager();
                if (packageName.equals("android")) {
                    Log.e(TAG, "The app has crashed");
                }
                break;

            default:
                eventTypeStr = "Other";
                Log.i(TAG, "Event type: " + eventTypeStr);

                break;
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
        Log.i(TAG, "onServiceConnected: ");
    }

    // ! Currently, this function is not used
    private static void findPopups (AccessibilityNodeInfo nodeInfo) {
        // If the node is a leaf node, return
        if (nodeInfo.getChildCount() == 0) {
            return;
        }

        // ? Check if the node is a popup
        for (int i = 0; i < nodeInfo.getChildCount(); i++) {
            AccessibilityNodeInfo childNodeInfo = nodeInfo.getChild(i);

            boolean check_popup = childNodeInfo.getClassName().equals("PopupWindow");
            if (check_popup) {
                Log.i(TAG,"Popup detected");
            }
            else {
                Log.e(TAG, childNodeInfo.getClassName().toString());
                findPopups(childNodeInfo); // Recursively check the children of the node
            }
        }
    }
}
