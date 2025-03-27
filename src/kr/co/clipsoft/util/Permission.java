package kr.co.clipsoft.util;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

import java.util.ArrayList;
import java.util.List;

import static android.content.pm.PackageManager.PERMISSION_DENIED;
import static android.Manifest.permission.INTERNET;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.Manifest.permission.READ_PHONE_STATE;
import static android.Manifest.permission.ACCESS_NETWORK_STATE;
import static android.Manifest.permission.ACCESS_WIFI_STATE;
import static android.Manifest.permission.CHANGE_WIFI_STATE;
import static android.Manifest.permission.BROADCAST_STICKY;
import static android.Manifest.permission.ACCESS_COARSE_LOCATION;

public enum Permission {
            PERMISSION_INTERNET(INTERNET,"기기 정보", PERMISSION_DENIED),
            PERMISSION_READ_EXTERNAL_STORAGE(READ_EXTERNAL_STORAGE,"저장공간", PERMISSION_DENIED),
            PERMISSION_WRITE_EXTERNAL_STORAGE(WRITE_EXTERNAL_STORAGE,"저장공간", PERMISSION_DENIED),
            PERMISSION_READ_PHONE_STATE(READ_PHONE_STATE,"기기 정보", PERMISSION_DENIED),
            PERMISSION_ACCESS_NETWORK_STATE(ACCESS_NETWORK_STATE,"기기 정보", PERMISSION_DENIED),
            PERMISSION_ACCESS_WIFI_STATE(ACCESS_WIFI_STATE,"기기 정보", PERMISSION_DENIED),
            PERMISSION_CHANGE_WIFI_STATE(CHANGE_WIFI_STATE,"기기 정보", PERMISSION_DENIED),
            PERMISSION_BROADCAST_STICKY(BROADCAST_STICKY,"기기 정보", PERMISSION_DENIED),
            PERMISSION_ACCESS_COARSE_LOCATION(ACCESS_COARSE_LOCATION,"위치", PERMISSION_DENIED),
            PERMISSION_POST_NOTIFICATIONS("android.permission.POST_NOTIFICATIONS","알림", PERMISSION_DENIED)
            ;

            private static final int ANDROID_TIRAMISU_SDK_VERSION = 33;
            private final String name;
            private final String displayName;
            private int permissionGrantCode;

            Permission(String name, String displayName, int permissionGrantCode) {
                this.name = name;
                this.displayName = displayName;
                this.permissionGrantCode = permissionGrantCode;
            }

            protected void initPermissionGrantCode(Context context) {
                    if (isNotSupportedPostNotification()) {
                        permissionGrantCode = PERMISSION_GRANTED;
                    } else {
                        permissionGrantCode = context.checkSelfPermission(name);
                    }
            }

            protected boolean isPermissionGranted() {
                    return permissionGrantCode == PERMISSION_GRANTED;                
            }

            protected boolean isPermissionDenied() {
                return permissionGrantCode == PERMISSION_DENIED;
            }

            protected int getPermissionGrantCode() {
                return permissionGrantCode;
            }

            public static boolean isPermissionAllGranted() {
                boolean isAllGranted = true;
                for (Permission permission : values()) {
                    if (permission.isPermissionDenied()) {
                        permission.logPermissionDenied();
                        isAllGranted = false;
                    }
                    permission.logPermissionGranted();
                }
                return isAllGranted;
            }

            public static String[] getDeniedPermissions() {
                List<String> deniedPermissions = new ArrayList<String>();
                for (Permission permission : values()) {
                    if (permission.isPermissionDenied()) {
                        deniedPermissions.add(permission.name);
                    }
                }
                return deniedPermissions.toArray(new String[0]);
            }

            public static boolean isOnlyNotificationPermissionDenied() {
               if(PERMiSSION_POST_NOTIFICATIONS.permissionGrantCode == PERMISSION_DENIED) {
                    for (Permission permission : values()) {
                        if (permission.name.equals(PERMiSSION_POST_NOTIFICATIONS.name)) {
                            continue;
                        }
                        if (permission.permissionGrantCode == PERMISSION_DENIED) {
                            return false;
                        }
                    }
                    return true;
               }
               return false;
            }
    

            private boolean isNotSupportedPostNotification() {
                return name.equals(PERMiSSION_POST_NOTIFICATIONS.name) && Build.VERSION.SDK_INT < ANDROID_TIRAMISU_SDK_VERSION;
            }

            private static final String TAG = "Permission"; 

            private void logPermissionDenied() {
                Log.i(TAG, String.format("[logPermissionDenied] %s 권한: %s",
                    displayName,
                    "PERMISSION_DENIED"));
            }

            private void logPermissionGranted() {
                Log.i(TAG, String.format("[logPermissionGranted] %s 권한: %s",
                    displayName,
                    "PERMISSION_GRANTED"));
            }
            
            
}
