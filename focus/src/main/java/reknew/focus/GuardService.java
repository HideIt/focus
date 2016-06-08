package reknew.focus;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.display.DisplayManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.view.Display;

import java.util.Timer;
import java.util.TimerTask;

import reknew.focus.Util.BetterToast;
import reknew.focus.Util.Reflection;
import reknew.focus.Util.Utils;

public class GuardService extends Service {

	public final int MAX_USE_TIME = 55 * 60 * 1000;
	public final int MAX_LOCK_TIME = 5 * 60 * 1000;

	public final int SHOW_USE_TIME_LEFT_PERIOD = 5 * 60 * 1000;
	public final int SHOW_LOCK_TIME_LEFT_PERIOD = 5 * 60 * 1000;

	private long useTimeLeft = MAX_USE_TIME;

	private long lastTrigger = 0;

	private long timerStartAt = 0;

	private BroadcastReceiver receiver = null;

	private Handler handler = new Handler();

	private Timer lockStateTimer = null;
	private Timer screenStateTimer = null;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		Utils.d("GuardService created");
		super.onCreate();

		//register receiver to know screen state
		receiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				switch (intent.getAction()) {
					case Intent.ACTION_SCREEN_ON:
						screenStateTimerManager(true);
						break;
					case Intent.ACTION_SCREEN_OFF:
						screenStateTimerManager(false);
						break;
				}
			}
		};
		IntentFilter filter = new IntentFilter();
		filter.addAction(Intent.ACTION_SCREEN_ON);
		filter.addAction(Intent.ACTION_SCREEN_OFF);
		registerReceiver(receiver, filter);

		//set Timer to show use time left
		Timer showUseTimeLeftTimer = new Timer();
		showUseTimeLeftTimer.schedule(new TimerTask() {//only show and change no data

			long innerUseTimeLeft = 0;
			long lastInnerUseTimeLeft = 0;

			@Override
			public void run() {
				//TODO 逻辑混乱
				if (!LockService.isRunning() && screenOn) {
					Utils.d("         useTimeLeft = " + useTimeLeft);
					Utils.d("lastInnerUseTimeLeft = " + lastInnerUseTimeLeft);
					Utils.d("    innerUseTimeLeft = " + innerUseTimeLeft);

					if (lastInnerUseTimeLeft != useTimeLeft || innerUseTimeLeft <= 0) {
						lastInnerUseTimeLeft = useTimeLeft;
						innerUseTimeLeft = useTimeLeft;
					}
					//if (!Utils.isServiceRunning("reknew.focus.LockService")) {
					handler.post(() -> BetterToast.showToast("use time left : " + innerUseTimeLeft / 1000 / 60 +
							" minutes " + (innerUseTimeLeft / 1000) % 60 + " seconds"));
					//}
					innerUseTimeLeft -= SHOW_USE_TIME_LEFT_PERIOD;
				}
			}
		}, 0, SHOW_USE_TIME_LEFT_PERIOD);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Utils.d("GuardService started");
		Reflection.print(this);
		//Maybe that's not necessary
		//startForeground(startId, new Notification());
		initLockState();
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void onDestroy() {
		Utils.d("GuardService destroyed");
		unregisterReceiver(receiver);
		//stopForeground(true);
		super.onDestroy();
		Utils.startService(GuardService.class);//并无卵用 被杀掉的话连调用onDestroy()的机会都没有
	}

	/**
	 * 在onStartCommand()中被调用
	 * 如果新的trigger被设置，则旧Timer和PendingIntent应当作废
	 */
	private void initLockState() {
		long trigger = Long.parseLong(Utils.load("trigger"));

		boolean shouldOnLock = trigger >= System.currentTimeMillis();
		boolean onLock = Utils.isServiceRunning("reknew.focus.LockService");

		Utils.d("        onLock = " + onLock);
		Utils.d("  shouldOnLock = " + shouldOnLock);
		Utils.d("triggerChanged = " + (lastTrigger != trigger));

		//shouldOnLock && onLock && lastTrigger != trigger will never be true
		if (shouldOnLock && (!onLock || lastTrigger != trigger)) {
			lastTrigger = trigger;
			Utils.d("will start LockService --- by GuardService.initLockState()");

			//set PendingIntent
			Intent intent = new Intent(this, MyBroadcastReceiver.class);
			intent.setAction(Utils.ACTION_STOP_LOCK);
			PendingIntent pi = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
			AlarmManager manager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
			manager.setExact(AlarmManager.RTC_WAKEUP, trigger, pi);

			Utils.startService(LockService.class, Utils.ACTION_START_LOCK);

			//set Timer
			try {
				//trigger改变则取消旧Timer
				lockStateTimer.cancel();
			} catch (Exception e) {
				Utils.e("lockStateTimer.cancel() exception");
			}
			lockStateTimer = new Timer();
			//lockStateTimer.schedule(new TimerTask() {...}, new Date(trigger));
			lockStateTimer.schedule(new TimerTask() {
				@Override
				public void run() {
					long trigger = Long.parseLong(Utils.load("trigger"));
					long current = System.currentTimeMillis();
					if (trigger <= current) {
						Utils.d("will stop LockService --- by initLockState() -> timer");
						Utils.stopService(LockService.class);
						useTimeLeft = MAX_USE_TIME;
						screenStateTimerManager(isScreenOn());
						lockStateTimer.cancel();
					} else {
						Utils.d("lock time left : " + (trigger - current) /
								1000 / 60 + " minutes " + (trigger - current) / 1000 % 60 + " seconds");
						handler.post(() -> BetterToast.showToast("lock time left : " + (trigger - current) /
								1000 / 60 + " minutes " + (trigger - current) / 1000 % 60 + " seconds"));
					}
				}
			}, 0, SHOW_LOCK_TIME_LEFT_PERIOD);
		} else if (!shouldOnLock && onLock) {
			Utils.d("will stop LockService --- by initLockState() itself");
			Utils.stopService(LockService.class);
		} else {
			screenStateTimerManager(isScreenOn());
		}
	}

	@SuppressWarnings("deprecation")
	private boolean isScreenOn() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {//API 20 or more
			DisplayManager dm = (DisplayManager) Utils.getContext().getSystemService(Context.DISPLAY_SERVICE);
			for (Display display : dm.getDisplays()) {
				if (display.getState() != Display.STATE_OFF) {
					return true;
				}
			}
			return false;
		} else {
			PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
			return powerManager.isScreenOn();
		}
	}

	private boolean screenOn = false;

	private void screenStateTimerManager(Boolean screenOn) {
		this.screenOn = screenOn;
		long trigger = Long.parseLong(Utils.load("trigger"));
		boolean shouldOnLock = trigger >= System.currentTimeMillis();
		boolean onLock = Utils.isServiceRunning("reknew.focus.LockService");


		Utils.d("    screenOn = " + screenOn);
		Utils.d("      onLock = " + onLock);
		Utils.d("shouldOnLock = " + shouldOnLock);

		if (shouldOnLock && onLock) {//已锁
			Utils.d("already locked");
		} else if (!shouldOnLock && !onLock && screenOn) {//未锁 screenOn 则开始计时
			Utils.d("timer begin");
			timerStartAt = System.currentTimeMillis();
			startTimer();
		} else if (!shouldOnLock && !onLock && timerStartAt > 0) {//未锁 screenOff 停止计时
			Utils.d("timer pause");

			long usage = System.currentTimeMillis() - timerStartAt;
			useTimeLeft -= usage;

			Utils.d("      usage = " + usage);
			Utils.d("useTimeLeft = " + useTimeLeft);

			stopTimer();
		} else if (!shouldOnLock && !onLock && timerStartAt <= 0) {
			//不加这句的话
			//若 shouldOnLock = false && onLock = false && screenOn = false
			//将会在screenStateTimerManager()与initLock()之间循环调用...呵呵...
			Utils.d("...");
		} else {//不正常状态
			Utils.d("abnormal state -> invoke initLockState()");
			initLockState();
		}
		//Utils.d("useTimeLeft : " + (useTimeLeft / 1000) / 60 + " minutes " + (useTimeLeft / 1000) % 60 + " seconds");
	}

	private void startTimer() {
		Utils.d("startTimer()");
		try {
			screenStateTimer.cancel();
		} catch (Exception e) {
			Utils.e("screenStateTimer.cancel() exception");
		}
		screenStateTimer = new Timer();
		screenStateTimer.schedule(new TimerTask() {
			@Override
			public void run() {
				useTimeLeft = MAX_USE_TIME;
				Reflection.print(screenStateTimer);
				Utils.save("trigger", String.valueOf(System.currentTimeMillis() + MAX_LOCK_TIME), Context
						.MODE_PRIVATE);
				initLockState();
			}
		}, useTimeLeft);
	}

	private void stopTimer() {
		Utils.d("stopTimer()");
		try {
			screenStateTimer.cancel();
		} catch (Exception e) {
			Utils.d("screenStateTimer.cancel() exception");
		}
	}
}