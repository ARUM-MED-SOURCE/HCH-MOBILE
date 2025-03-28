package kr.co.less.consent.hch;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import kr.co.clipsoft.util.EFromViewer;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.Executors;

/**
 * 포어그라운드 서비스 관련 클래스 - 앱 상태가 백그라운드로 전환시, 포어그라운드 알림 표시 - 동의서 앱이 백그라운드 전환시, 안드로이드
 * OS에게 kill 당하는 문제 해결 위해 로직 작성
 * 
 * @author gkwns
 *
 */
public class MyForegroundService extends Service {

	// about notification
	NotificationChannel notificationChannel;
	final String CHANNEL_ID = "Foreground Service ID";
	final int FOREGROUND_NOTI_ID = 888;

	// about thread
	final static String CUSTOM_THREAD_NAME = "MyCustomThread";
	
	private ScheduledExecutorService executorService;
	private final AtomicInteger count = new AtomicInteger(0);
	private volatile boolean isServiceRunning = false;
	

	// about log
	final String LOG_TAG = "MyForegroundService";

	@Nullable
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();

		Log.d(LOG_TAG, "서비스(백그라운드) 인스턴스 생성");

		initNotificationChannel();
	}
	
	@Override
    public int onStartCommand(Intent intent, int flags, int startId) {

    	Notification notification = createNotification(0);
        
        startForeground(FOREGROUND_NOTI_ID, notification);
        startBackgroundTask();

        return START_STICKY;
    }
    
    @Override
    public void onDestroy() {
    	// TODO Auto-generated method stub
    	super.onDestroy();
    	
    	cleanupResources();
    	Log.d(LOG_TAG, "서비스(백그라운드) 인스턴스 소멸");
    }

	private void startBackgroundTask() {
		executorService = Executors.newSingleThreadScheduledExecutor(new ThreadFactory() {
			@Override
			public Thread newThread(Runnable r) {
				Thread thread = new Thread(r);
				thread.setName(CUSTOM_THREAD_NAME);
				thread.setDaemon(true);
				return thread;
			}
		});
		
		isServiceRunning = true;
		executorService.scheduleAtFixedRate(new Runnable() {
			@Override
			public void run() {
				if (!isServiceRunning) {
					return;
				}
			
			Log.e("Service", "서비스가 실행 중입니다...");
			int currentCount = count.get();
			Log.e("Service", "현재 카운트: " + currentCount);

				int newCount = currentCount >= Integer.MAX_VALUE ? 0 : currentCount + 2;
				updateNotification(newCount);
				count.set(newCount);
			}
		}, 0, 2, TimeUnit.SECONDS);
	}

	private void cleanupResources() {
		isServiceRunning = false;
		
		if (executorService != null && !executorService.isShutdown()) {
			try {
				executorService.shutdown();
				if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
					executorService.shutdownNow();
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
				executorService.shutdownNow();
			}
		}
	}

	private void initNotificationChannel() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			notificationChannel = new NotificationChannel(CHANNEL_ID, "Foreground Service Channel",
					NotificationManager.IMPORTANCE_LOW);
			NotificationManager notificationManager = getSystemService(NotificationManager.class);
			if (notificationManager != null) {
				notificationManager.createNotificationChannel(notificationChannel);
			}
		}
	}
    
    /**
     *  hajun :: 2024.12.30
     *  알림 생성 메소드
     * @param time
     * @return Notification
     */
    private Notification createNotification(int time) {
    	
    	if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) { // 오레오(API 26) 이상일 때           
            return new Notification.Builder(this, CHANNEL_ID)
                    .setContentText(String.format("서비스가 실행중입니다.(%ds)", time))
                    .setContentTitle("전자동의서 서비스")
                    .setSmallIcon(android.R.drawable.ic_menu_info_details)
            		.setOngoing(true)
					.build();
        } else { // 오레오 미만 버전에서는 NotificationCompat 사용
            return new NotificationCompat.Builder(this)
                    .setContentText(String.format("서비스가 실행중입니다.(%ds)", time))
                    .setContentTitle("전자동의서 서비스")
                    .setSmallIcon(android.R.drawable.ic_menu_info_details)
                    .setOngoing(true)
                    .setPriority(NotificationCompat.PRIORITY_LOW)
                    .build();
        }
    }
    
    /**
     * hajun :: 2024.12.30
     *  알림 업데이트 메소드
     * @param time: 백그라운드 전환 후 경과 시간
     */
    private void updateNotification(int time) {	
    	NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.notify(
				FOREGROUND_NOTI_ID, 
				createNotification(time)
				); // 동일한 ID로 알림 갱신
        }
    }
}