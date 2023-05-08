package com.example.test;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Objects;
import java.util.regex.Pattern;


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

        // ? Get the package name of the event
        String eventPackageName = String.valueOf(event.getPackageName());
        eventDetailsMap.put("eventPackageName", eventPackageName);

        // ? Get event index
        int eventIndex = event.getFromIndex();
        eventDetailsMap.put("eventIndex", String.valueOf(eventIndex));

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

            case AccessibilityEvent.TYPE_VIEW_FOCUSED:
                eventTypeStr = "Focused";
                eventDetailsMap.put("eventTypeStr", eventTypeStr);
                break;

            case AccessibilityEvent.TYPE_VIEW_SELECTED:
                eventTypeStr = "Selected";
                eventDetailsMap.put("eventTypeStr", eventTypeStr);
                break;

            case AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED:
                eventTypeStr = "Text Changed";
                eventDetailsMap.put("eventTypeStr", eventTypeStr);
                break;

            case AccessibilityEvent.TYPE_VIEW_TEXT_SELECTION_CHANGED:
                eventTypeStr = "Text Selection Changed";
                eventDetailsMap.put("eventTypeStr", eventTypeStr);
                break;

            // * Window state changed event
            case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED:
                eventTypeStr = "Window State Changed";
                eventDetailsMap.put("eventTypeStr", eventTypeStr);

                //CPU utilization and temperature
                thermal();
                cpu_util();

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
//            AccessibilityNodeInfo.CollectionItemInfo collectionItemInfo = sourceNode.getCollectionItemInfo();
//            if (collectionItemInfo != null) {
//                int rowIndex = collectionItemInfo.getRowIndex();
//                eventDetailsMap.put("rowIndex", String.valueOf(rowIndex));
//
//                int columnIndex = collectionItemInfo.getColumnIndex();
//                eventDetailsMap.put("columnIndex", String.valueOf(columnIndex));
//            }

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

    public static float thermal()
    {
        Process process;
        try {
            //running a shell command to extract data in temperature file
            process = Runtime.getRuntime().exec("cat /sys/devices/virtual/thermal/thermal_zone0/temp");
            process.waitFor();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line = reader.readLine();
            float temp;
            if (line != null) {
                temp = Float.parseFloat(line);
                //getting the type of temperature
                String s=thermalType(0);
                //dividing by 1000 because of the way data is stored in files
                Log.e(TAG, "The "+s+" temperature is:"+String.valueOf(temp/1000.0f));
                return 1;

            }
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
        return 1;

    }
    public static String thermalType(int i) {
        Process process;
        BufferedReader reader;
        String line, type = null;
        try {
            //getting the type of temperature
            process = Runtime.getRuntime().exec("cat sys/class/thermal/thermal_zone" + i + "/type");
            process.waitFor();
            reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            line = reader.readLine();
            if (line != null) {
                type=line;
            }
            reader.close();
            process.destroy();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return type;
    }
    public static void cpu_util() {
        Process process;
        BufferedReader reader;
        String line, type = null;
        for(int i=0;i<getNumCores();i++) {

            try {
                //getting CPU utilization
                process = Runtime.getRuntime().exec("cat /sys/devices/system/cpu/cpu"+String.valueOf(i)+"/cpufreq/scaling_cur_freq");
                process.waitFor();
                reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                line = reader.readLine();
                if (line != null) {
                    Log.e(TAG, "Core "+(i+1)+":"+(Float.parseFloat(line)/1000000));
                }
                reader.close();
                process.destroy();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }
    public static int getNumCores() {
        //Private Class to display only CPU devices in the directory listing
        class CpuFilter implements FileFilter {
            @Override
            public boolean accept(File pathname) {
                //Check if filename is "cpu", followed by a single digit number
                if(Pattern.matches("cpu[0-9]+", pathname.getName())) {
                    return true;
                }
                return false;
            }
        }

        try {
            //Get directory containing CPU info
            File dir = new File("/sys/devices/system/cpu/");
            //Filter to only list the devices we care about
            File[] files = dir.listFiles(new CpuFilter());
            //Return the number of cores (virtual CPU devices)
            return files.length;
        } catch(Exception e) {
            //Default to return 1 core
            return 1;
        }
    }
}

