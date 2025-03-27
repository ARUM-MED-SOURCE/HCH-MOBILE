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

import kr.co.clipsoft.util.Permission;
import static android.Manifest.permission.INTERNET;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.Manifest.permission.READ_PHONE_STATE;
import static android.Manifest.permission.ACCESS_NETWORK_STATE;
import static android.Manifest.permission.ACCESS_WIFI_STATE;
import static android.Manifest.permission.CHANGE_WIFI_STATE;
import static android.Manifest.permission.BROADCAST_STICKY;
import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static kr.co.clipsoft.util.Permission.isOnlyNotificationPermissionDenied;
import static kr.co.clipsoft.util.Permission.isPermissionAllGranted;
import static kr.co.clipsoft.util.Permission.getDeniedPermissions;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static android.content.pm.PackageManager.PERMISSION_DENIED;

import android.util.Log;

public class PermissionHelper {

	
	private static final int PERMISSION_REQUEST_CODE = 0;
	private static final String TAG = "PermissionHelper";
	private static final String BASIC_PERMISSION_MESSAGE = "전자동의서를 사용하기 위해서는 해당 권한들이 필요합니다.\n[설정] -> [권한]으로 이동 후 허용해주시기 바랍니다.\n거부를 선택하시면 앱이 종료됩니다.";
	private static final String NOTIFICATION_PERMISSION_MESSAGE = "신규 기기는 알림 권한이 필요합니다.\n[설정] -> [알림]으로 이동 후 허용해주시기 바랍니다.\n거부를 선택하시면 앱이 종료됩니다.";
		
	private final Context context;
	private static boolean isShowingPermissionDialog = false;


	public PermissionHelper(Context context) {
		if (context == null) {
			throw new IllegalArgumentException("Context 가 null 입니다.");
		}

		this.context = context;
		isShowingPermissionDialog = false;
		for (Permission permission : Permission.values()) {
			permission.initPermissionGrantCode(context);
		}
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

		if (isPermissionAllGranted() || isShowingPermissionDialog) {
			return;
		}

		showCustomPermissionsDialog();
	}

	public void showSystemPermissionsDialog() {
		String[] deniedPermissions = getDeniedPermissions();

		Log.i(TAG, String.format("[Permission Request] %s 권한 추가", Arrays.toString(deniedPermissions)));

		if (hasDeniedPermissions(deniedPermissions)) {
			ActivityCompat.requestPermissions((Activity) context, deniedPermissions, PERMISSION_REQUEST_CODE);
		}
	}

	// 권한 요청 커스텀 팝업
	public void showCustomPermissionsDialog() {
		Log.i(TAG, "[showCustomPermissionDialog]");

		try {
			AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
			dialogBuilder.setTitle("권한 요청")
				.setMessage(getPermissionMessage())
				.setCancelable(false)
				.setNegativeButton("거부", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int _) {
						handleDenyClick(dialog);
					}
				})
				.setPositiveButton("설정", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int _) {
						handleSettingsClick(dialog);
					}
				})
				.create()
				.show();

			isShowingPermissionDialog = true;
		} catch (Exception e) {
			e.toString();
			Log.e(TAG, "[showCustomPermissionsDialog] Exception : " + e.toString());
		}
	}

	private void handleDenyClick(DialogInterface dialog) {
		Log.i(TAG, "[showRequestPermissionDialog] 거부 클릭 ");
		isShowingPermissionDialog = false;
		dialog.cancel();
		((Activity) context).finish();
	}

	private void handleSettingsClick(DialogInterface dialog) {
		Log.i(TAG, "[showRequestPermissionDialog] 설정 클릭 ");
		isShowingPermissionDialog = false;
		dialog.cancel();
		
		moveToAppropriateSettings();
	}

	private void moveToAppropriateSettings() {
		if (isOnlyNotificationPermissionDenied()) {
			moveNotificationSetting();
			return;
		}
		moveSetting();
	}

	private String getPermissionMessage() {
		if (isOnlyNotificationPermissionDenied()) {
			return NOTIFICATION_PERMISSION_MESSAGE;
		}
		return BASIC_PERMISSION_MESSAGE;
	}

	private boolean hasDeniedPermissions(String[] deniedPermissions) {
		return deniedPermissions.length > 0;
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
		try {
			Intent intent = new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
			intent.putExtra(Settings.EXTRA_APP_PACKAGE, context.getPackageName());
			((Activity) context).startActivity(intent);
		} catch (Exception e) {
			Log.e(TAG, "알림 설정 화면 이동 실패, 기본 설정 화면으로 이동", e);
			moveSetting();
		}
	}
}
