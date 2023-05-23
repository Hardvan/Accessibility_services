package com.example.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

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
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;


// ? Press Ctrl+Alt+L to format the code

public class MyAccessibilityService extends AccessibilityService {
    int c = 0;
    private static final String TAG = "MyAccessibilityService";

    @SuppressLint({"SuspiciousIndentation", "SwitchIntDef"})
    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        // Log.e(TAG, "onAccessibilityEvent: ");t

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

                // ? CPU temperature, CPU utilization, Memory utilization, App utilization
                thermal();
                cpu_util();
                getCPUMemoryUtilization();
                getapputil();

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

        // Display the details of the event only if the event is not of type "Other"
        boolean is_other = Objects.equals(eventDetailsMap.get("eventTypeStr"), "Other");
        if (!is_other) {
            displayEventsMap(eventDetailsMap);
//            saveEventsMap(eventDetailsMap);
        }
    }

    public void displayEventsMap(HashMap<String, String> eventDetailsMap) {
        // Iterate through the HashMap and print the details of the event
        Log.i(TAG, "------------------");
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
    private static void findChildren(AccessibilityNodeInfo nodeInfo) {
        // If the node is a leaf node, return
        if (nodeInfo.getChildCount() == 0) {
            return;
        }

        // ? Check if the node is a popup
        for (int i = 0; i < nodeInfo.getChildCount(); i++) {
            AccessibilityNodeInfo childNodeInfo = nodeInfo.getChild(i);
            boolean check_popup = childNodeInfo.getClassName().equals("PopupWindow");
            if (childNodeInfo.getClassName() != null) {
                Log.e(TAG, (String) childNodeInfo.getClassName());
            }
            if (check_popup) {
                Log.i(TAG, "Popup detected");
            } else {
                Log.e(TAG, childNodeInfo.getClassName().toString());
                findChildren(childNodeInfo); // Recursively check the children of the node
            }
        }
    }

    public static int thermal() {
        Log.d(TAG, "CPU TEMPERATURE\n");
        try {
            // Running a shell command to extract data in temperature file
            Process process = Runtime.getRuntime().exec("cat /sys/devices/virtual/thermal/thermal_zone0/temp");
            // Waiting for the command to complete
            process.waitFor();

            // Reading the output of the command
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line = reader.readLine();
            float temp;
            if (line != null) {
                temp = Float.parseFloat(line); // Converting the string to float
                String s = thermalType(0); // Getting the type of temperature
                float temp_value = temp / 1000.0f; // Dividing by 1000 because of the way data is stored in files

                Log.i(TAG, "The " + s + " temperature is:" + temp_value);

                return 1; // Success
            }
        } catch (Exception e) {
            e.printStackTrace();
            return 0; // Failure
        }

        return 1; // Success
    }

    public static String thermalType(int i) {
        String type = null;
        try {
            // Getting the type of temperature
            Process process = Runtime.getRuntime().exec("cat sys/class/thermal/thermal_zone" + i + "/type");
            // Waiting for the command to complete
            process.waitFor();

            // Reading the output of the command
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line = reader.readLine();
            if (line != null) {
                type = line; // Storing the type of temperature
            }

            // Closing the reader and destroying the process
            reader.close();
            process.destroy();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return type;
    }

    public static void cpu_util() {
        Log.d(TAG, "CPU UTILIZATION\n");
        int total_cores = getNumCores(); // Getting the number of cores
        for (int i = 0; i < total_cores; i++) {
            try {
                // ? Getting CPU utilization
                // Running a shell command to extract data
                Process process = Runtime.getRuntime().exec("cat /sys/devices/system/cpu/cpu" + i + "/cpufreq/scaling_cur_freq");
                // Waiting for the command to complete
                process.waitFor();

                // Reading the output of the command
                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String line = reader.readLine();
                if (line != null) {
                    int core_num = i + 1; // Getting the core number
                    float cpu_util = Float.parseFloat(line) / 1000000; // Calculating the CPU utilization
                    Log.i(TAG, "Core " + (core_num) + ":" + cpu_util + " GHz");
                }

                reader.close();
                process.destroy();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static int getNumCores() {
        // Returns total number of cores on device
        class CpuFilter implements FileFilter {
            @Override
            public boolean accept(File pathname) {
                // Check if filename is "cpu", followed by a single digit number
                return Pattern.matches("cpu[0-9]+", pathname.getName());
            }
        }

        try {
            // Get directory containing CPU info
            File dir = new File("/sys/devices/system/cpu/");
            // Filter to only list the devices we care about
            File[] files = dir.listFiles(new CpuFilter());
            // Return the number of cores (virtual CPU devices)
            return files.length;
        } catch (Exception e) {
            // Default to return 1 core
            return 1;
        }
    }

    public static float getCPUMemoryUtilization() {
        Log.d(TAG, "CPU MEMORY UTILIZATION\n");
        // HashMap to store the memory info
        HashMap<String, String> memInfoMap = new HashMap<>();
        try {
            // Getting CPU Memory Utilization
            Process process = Runtime.getRuntime().exec("cat /proc/meminfo");
            // Waiting for the command to complete
            process.waitFor();

            // Reading the output of the command
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                // Storing the output in the HashMap
                String[] parts = line.split(":");
                if (parts.length == 2) {
                    String key = parts[0].trim();
                    String value = parts[1].trim();
                    memInfoMap.put(key, value);
                }
            }

            // Displaying the Memory Info
            for (Map.Entry<String, String> entry : memInfoMap.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                Log.i(TAG, key + ": " + value);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        return 0.0f; // Default value in case of an error
    }

    public static float getapputil() {
        Log.d(TAG, "APP UTILIZATION\n");
        try {
            // Getting App Utilization
            Process process = Runtime.getRuntime().exec("top -n 1");
            // Waiting for the command to complete
            process.waitFor();

            // Reading the output of the command
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                Log.i(TAG, line); // Printing the output of the command
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        return 0.0f; // Default value in case of an error
    }

}

