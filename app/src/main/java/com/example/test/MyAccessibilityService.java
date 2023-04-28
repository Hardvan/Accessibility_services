package com.example.test;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.HashMap;
import java.util.Objects;

// ? Press Ctrl+Alt+L to format the code

public class MyAccessibilityService extends AccessibilityService {
    int c = 0;
    private static final String TAG = "MyAccessibilityService";

    @SuppressLint({"SuspiciousIndentation", "SwitchIntDef"})
    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        //Log.e(TAG, "onAccessibilityEvent: ");t

        // ? HashMap to store the details of the event
        HashMap<String, String> eventDetailsMap = new HashMap<>();

        // ? Get the text of the event
        String eventText = String.valueOf(event.getText());
        eventDetailsMap.put("eventText", eventText);

        String eventPackageName = String.valueOf(event.getPackageName());
        eventDetailsMap.put("eventPackageName", eventPackageName);

        boolean check_empty = eventText.contains("[]");
        boolean check_notification = eventText.contains("Notification");
        boolean check_quick_settings = eventText.contains("Quick setting");
        if (!check_empty && !check_notification) {
            // Log.i(TAG, eventText); // Print the text of the event (EG: [Home 1 of 1])
        }
        if (check_notification || check_quick_settings) {
            Log.e(TAG, "Notification shade");
        }

        // ? Get the event type
        final int eventType = event.getEventType();
        String eventTypeStr = "";
        switch (eventType) {
            // * Click event
            case AccessibilityEvent.TYPE_VIEW_CLICKED:
                eventTypeStr = "Click";
                eventDetailsMap.put("eventTypeStr", eventTypeStr);
                break;

            // * Long click event
            case AccessibilityEvent.TYPE_VIEW_LONG_CLICKED:
                eventTypeStr = "Long Click";
                eventDetailsMap.put("eventTypeStr", eventTypeStr);
                break;

            // * Window state changed event
            case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED:
                eventTypeStr = "Window State Changed";
                eventDetailsMap.put("eventTypeStr", eventTypeStr);

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
                eventDetailsMap.put("eventTypeStr", eventTypeStr);
                break;
        }

        // Get the event's resource-id, text, content-desc, index, touchX, touchY, time
        AccessibilityNodeInfo sourceNode = event.getSource();
        if (sourceNode != null) {
            // Resource-id
            String resourceId = String.valueOf(sourceNode.getViewIdResourceName());
            eventDetailsMap.put("resourceId", resourceId);

            // content-desc
            CharSequence contentDesc = sourceNode.getContentDescription();
            eventDetailsMap.put("contentDesc", String.valueOf(contentDesc));

            // Row, Column index
            AccessibilityNodeInfo.CollectionItemInfo collectionItemInfo = sourceNode.getCollectionItemInfo();
            if (collectionItemInfo != null) {
                int rowIndex = collectionItemInfo.getRowIndex();
                eventDetailsMap.put("rowIndex", String.valueOf(rowIndex));

                int columnIndex = collectionItemInfo.getColumnIndex();
                eventDetailsMap.put("columnIndex", String.valueOf(columnIndex));
            }

            // For touchX and touchY
            Rect bounds = new Rect();
            sourceNode.getBoundsInScreen(bounds);
            int touchX = bounds.centerX();
            int touchY = bounds.centerY();
            eventDetailsMap.put("touchX", String.valueOf(touchX));
            eventDetailsMap.put("touchY", String.valueOf(touchY));
        }

        // Iterate through the HashMap and print the details of the event
        boolean is_other = Objects.equals(eventDetailsMap.get("eventTypeStr"), "Other");
        if (!is_other) {
            displayEventsMap(eventDetailsMap);
//            saveEventsMap(eventDetailsMap);
        }
    }

    public void displayEventsMap(HashMap<String, String> eventDetailsMap) {
        for (String key : eventDetailsMap.keySet()) {
            String value = eventDetailsMap.get(key);
            Log.i(TAG, key + " : " + value);
        }
        Log.i(TAG, "------------------");
    }

    @Override
    public void onInterrupt() {
        Log.e(TAG, "onInterrupt: unexpected error");
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        AccessibilityServiceInfo info = new AccessibilityServiceInfo();
        info.eventTypes = AccessibilityEvent.TYPES_ALL_MASK;

        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_SPOKEN;

        info.notificationTimeout = 10;

        this.setServiceInfo(info);
        Log.i(TAG, "onServiceConnected: ");
    }

    // ! Currently, this function is not used
    private static void findPopups(AccessibilityNodeInfo nodeInfo) {
        // If the node is a leaf node, return
        if (nodeInfo.getChildCount() == 0) {
            return;
        }

        // ? Check if the node is a popup
        for (int i = 0; i < nodeInfo.getChildCount(); i++) {
            AccessibilityNodeInfo childNodeInfo = nodeInfo.getChild(i);

            boolean check_popup = childNodeInfo.getClassName().equals("PopupWindow");
            if (check_popup) {
                Log.i(TAG, "Popup detected");
            } else {
                Log.e(TAG, childNodeInfo.getClassName().toString());
                findPopups(childNodeInfo); // Recursively check the children of the node
            }
        }
    }
}
