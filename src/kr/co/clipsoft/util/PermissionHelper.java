package kr.co.clipsoft.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.provider.Settings;
import static android.Manifest.permission.INTERNET;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.Manifest.permission.READ_PHONE_STATE;
import static android.Manifest.permission.ACCESS_NETWORK_STATE;
import static android.Manifest.permission.ACCESS_WIFI_STATE;
import static android.Manifest.permission.CHANGE_WIFI_STATE;
import static android.Manifest.permission.BROADCAST_STICKY;
import static android.Manifest.permission.ACCESS_COARSE_LOCATION;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static android.content.pm.PackageManager.PERMISSION_DENIED;

import android.util.Log;

public class PermissionHelper {

	
	private static final int PERMISSION_REQUEST_CODE = 0;
	private static final int ANDROID_TIRAMISU_SDK_VERSION = 33;
	private static final String POST_NOTIFICATIONS = "android.permission.POST_NOTIFICATIONS"; // 구버전 호환성을 위한 직접 문자열 정의
	private static final String TAG = "PermissionHelper";
	private static final String[] REQUIRED_PERMISSIONS = initRequiredPermissions();

	// 권한 상수
	private static String[] initRequiredPermissions() {
		List<String> permissions = new ArrayList<>(Arrays.asList(
			INTERNET,
			READ_EXTERNAL_STORAGE,
			WRITE_EXTERNAL_STORAGE,
			READ_PHONE_STATE,
			ACCESS_NETWORK_STATE,
			ACCESS_WIFI_STATE,
			CHANGE_WIFI_STATE,
			BROADCAST_STICKY,
			ACCESS_COARSE_LOCATION
		));
		
		// Android 13(API 33) 이상에서만 알림 권한 추가
		if (Build.VERSION.SDK_INT >= ANDROID_TIRAMISU_SDK_VERSION) {
			permissions.add(POST_NOTIFICATIONS); // 구버전 라이브러리 호환성을 위해 String 상수 사용
		}
		
		return permissions.toArray(new String[0]);
	}

	
	private final Context context;
	private static boolean isShowingPermissionDialog = false;

	
	public PermissionHelper(Context context) {
		if (context == null) {
			throw new IllegalArgumentException("Context 가 null 입니다.");
		}

		this.context = context;
		isShowingPermissionDialog = false;
	}

	//  권한 체크 관련 메소드
	private boolean checkPermission(String permission) {
		return ContextCompat.checkSelfPermission(context, permission) == PERMISSION_GRANTED;
	}

	public boolean currentAllPermisionCheck() {
		for (String permission : REQUIRED_PERMISSIONS) {
			if (!checkPermission(permission)) {
				logPermissionStatus(permission);
				return false;
			}
		}
		Log.i(TAG, "[currentAllPermisionCheck] 모든 권한 허용됨");
		return true;
	}

	public boolean hasAllPermissionsGranted(int[] grantResults) {
		boolean result = true;
		for (int grantResult : grantResults) {
			if (grantResult == PERMISSION_DENIED) {
				result = false;
			}
		}
		Log.i(TAG, "[hasAllPermissionsGranted ] 모든 권한 확인 : " + result);
		return result;
	}

	//  권한 요청 관련 메소드
	public void showRequestPermissionsDialog() {
		String isUsed = CommonUtil.getInstance(context).getSharedPreferences("PERMISSION", "IS_USED", "FALSE");
		Log.i(TAG, "[isUsedSystemPermissionsDialog] 시스템 권한 다이얼로그 사용 여부 : " + isUsed);
		
		if (isUsed.equals("FALSE")) {
			CommonUtil.getInstance(context).setSharedPreferences("PERMISSION", "IS_USED", "TRUE");
			showSystemPermissionsDialog();
			return;
		}

		if (currentAllPermisionCheck() || isShowingPermissionDialog) {
			return;
		}

		showCustomPermissionsDialog();
	}

	public void showSystemPermissionsDialog() {
		List<String> deniedPermissions = new ArrayList<>();
		
		for (String permission : REQUIRED_PERMISSIONS) {
			if (!checkPermission(permission)) {
				deniedPermissions.add(permission);
				Log.i(TAG, String.format("[Permission Request] %s 권한 추가", getPermissionDisplayName(permission)));
			}
		}

		if (!deniedPermissions.isEmpty()) {
			String[] permissions = deniedPermissions.toArray(new String[0]);
			ActivityCompat.requestPermissions((Activity) context, permissions, PERMISSION_REQUEST_CODE);
		}
	}

	private void logPermissionStatus(String permission) {
		Log.i(TAG, String.format("[currentAllPermisionCheck] %s 권한: %s",
			getPermissionDisplayName(permission),
			checkPermission(permission) ? "PERMISSION_GRANTED" : "PERMISSION_DENIED"));
	}

	private String getPermissionDisplayName(String permission) {
		switch (permission) {
			case WRITE_EXTERNAL_STORAGE:
			case READ_EXTERNAL_STORAGE:
				return "저장공간";
			case ACCESS_COARSE_LOCATION:
				return "위치";
			default:
				return "기기 정보";
		}
	}

	

	// 권한 요청 커스텀 팝업
	public void showCustomPermissionsDialog() {
		Log.i(TAG, "[showCustomPermissionDialog]");

		final boolean isNotificationPermission = isNotificationPermission();
		String message = "전자동의서를 사용하기 위해서는 해당 권한들이 필요합니다.\n[설정] -> [권한]으로 이동 후 허용해주시기 바랍니다.\n거부를 선택하시면 앱이 종료됩니다.";
		if (isNotificationPermission) {
			message = "신규 기기는 알림 권한이 필요합니다.\n[설정] -> [알림]으로 이동 후 허용해주시기 바랍니다.\n거부를 선택하시면 앱이 종료됩니다.";
		}

		try {
			AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
			dialogBuilder.setTitle("권한 요청"); // 팝업 창 타이틀
			// 팝업 안내 메시지 부분으로 string.xml에서 설정한 메시지를 노출합니다.

			dialogBuilder.setMessage(message);
			dialogBuilder.setCancelable(false);

			// 거부 클릭 이벤트
			dialogBuilder.setNegativeButton("거부", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					Log.i(TAG, "[showRequestPermissionDialog] 거부 클릭 ");
					isShowingPermissionDialog = false;
					dialog.cancel();
					((Activity) context).finish();
				}
			});

			// 설정 클릭 이벤트
			dialogBuilder.setPositiveButton("설정", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					Log.i(TAG, "[showRequestPermissionDialog] 설정 클릭 ");
					isShowingPermissionDialog = false;
					dialog.cancel();
					
					if (isNotificationPermission) {	
						moveNotificationSetting();
					} else {
						moveSetting();
					}
				}
			});
			
			AlertDialog alertDialog = dialogBuilder.create();
			alertDialog.show();
			isShowingPermissionDialog = true;
		} catch (Exception e) {
			e.toString();
			Log.e(TAG, "[showCustomPermissionsDialog] Exception : " + e.toString());
		}
	}

	// 알림 권한만 거부된 상태인지 확인
	private boolean isNotificationPermission() {
    	// 알림 권한 상태 확인
    	int notificationPermissionCheck = (Build.VERSION.SDK_INT >= ANDROID_TIRAMISU_SDK_VERSION)
            	? ContextCompat.checkSelfPermission(context, POST_NOTIFICATIONS)
            	: PERMISSION_GRANTED;
            
    	// 알림 권한이 허용된 경우 false 반환
    	if (notificationPermissionCheck == PERMISSION_GRANTED) {
        	return false;
    	}
    
    	// 기존 REQUIRED_PERMISSIONS에서 POST_NOTIFICATIONS를 제외한 권한들 체크
    	for (String permission : REQUIRED_PERMISSIONS) {
        	if (permission.equals(POST_NOTIFICATIONS)) {
            	continue;
        	}
        	if (ContextCompat.checkSelfPermission(context, permission) != PERMISSION_GRANTED) {
            	return false;
        	}
    	}
    
   		return true;
	}

	// 설정화면으로 이동
	private void moveSetting() {
		Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
		intent.setData(Uri.parse("package:" + context.getPackageName()));
		((Activity) context).startActivity(intent);
	}

	/**
	 * by sangu02
	 * 2024/10/23
	 * 알림 설정 화면으로 이동하게 되어있음
	 * 해당 함수를 탄다는 건 안드로이드 13이상이라는 것
	 */
	private void moveNotificationSetting() {
		Intent intent = new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
		intent.putExtra(Settings.EXTRA_APP_PACKAGE, context.getPackageName());
		((Activity) context).startActivity(intent);
	}
}
