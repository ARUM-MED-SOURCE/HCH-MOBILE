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

import static android.support.v4.content.ContextCompat.checkSelfPermission;

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
            private static final String TAG = Permission.class.getSimpleName();
            
            private final String name;
            private final String displayName;
            private int permissionGrantCode;

            Permission(String name, String displayName, int permissionGrantCode) {
                this.name = name;
                this.displayName = displayName;
                this.permissionGrantCode = permissionGrantCode;
            }

            public static void initPermissionGrantCode(Context context) {
                for (Permission permission : values()) {
                    if (isNotSupportedPostNotification()) {
                        permissionGrantCode = PERMISSION_GRANTED;
                        logPermissionGrantCode();
                    } else {
                        permissionGrantCode = checkSelfPermission(context, name);
                        logPermissionGrantCode();
                    }
                }
            }
           

            public boolean isPermissionGranted() {
                    return permissionGrantCode == PERMISSION_GRANTED;                
            }

            public boolean isPermissionDenied() {
                return permissionGrantCode == PERMISSION_DENIED;
            }

            public int getPermissionGrantCode() {
                return permissionGrantCode;
            }

            public static boolean isPermissionAllGranted() {
                boolean isAllGranted = true;
                for (Permission permission : values()) {
                    if (permission.isPermissionDenied()) {
                        isAllGranted = false;
                    }
                }
                return isAllGranted;
            }

            public static void logAllPermissionStatus() {
                for (Permission permission : values()) {
                    if (permission.isPermissionDenied()) {
                        permission.logPermissionDenied();
                    } else {
                        permission.logPermissionGranted();
                    }
                }
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
               if(PERMISSION_POST_NOTIFICATIONS.isPermissionDenied()) {
                    for (Permission permission : values()) {
                        if (permission.isPermissionPostNotification()) {
                            continue;
                        }
                        if (permission.isPermissionDenied()) {
                            return false;
                        }
                    }
                    return true;
               }
               return false;
            }
    

            private boolean isNotSupportedPostNotification() {
                return isPermissionPostNotification() && isSdkVersionUnderTiramisu();
            }

            private boolean isPermissionPostNotification() {
                return name.equals(PERMISSION_POST_NOTIFICATIONS.name);
            }

            private boolean isSdkVersionUnderTiramisu() {
                return Build.VERSION.SDK_INT < ANDROID_TIRAMISU_SDK_VERSION;
            }

            private void logPermissionStatus(String status) {
                Log.i(TAG, 
                      String.format("[logPermission%s] %s 권한: %s",
                                   status,
                                   displayName,
                                   status));
            }

            private void logPermissionDenied() {
                logPermissionStatus("DENIED");
            }

            private void logPermissionGranted() {
                logPermissionStatus("GRANTED");
            }

            private void logPermissionGrantCode() {
                Log.i(TAG, String.format("[logPermissionGrantCode] %s 권한: %s", displayName, permissionGrantCode));
            }
}
