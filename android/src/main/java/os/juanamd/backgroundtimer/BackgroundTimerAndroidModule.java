package os.juanamd.backgroundtimer;

import android.content.Context;
import android.os.Handler;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;

import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.modules.core.DeviceEventManagerModule;

import java.lang.Runnable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class BackgroundTimerAndroidModule extends ReactContextBaseJavaModule {
	private static final String TAG = "RNBackgroundTimerAndroid";
	private static final String TIMER_EVENT = "RNBackgroundTimerAndroid.timer";

	private Map<Integer, TimerData> timerDataMap = new HashMap<>();
	private Handler handler;

	public BackgroundTimerAndroidModule(ReactApplicationContext reactContext) {
		super(reactContext);
	}

	@Override
	public String getName() {
		return TAG;
	}

	@ReactMethod
	public void setTimer(final int id, final double millis, final boolean repeats, Promise promise) {
		try {
			setTimer(id, (long) millis, repeats);
			promise.resolve(null);
			Log.d(TAG, "setTimer for id: " + String.valueOf(id) + " for " + String.valueOf(millis) + " ms. Repeats: " + String.valueOf(repeats));
		} catch (Exception e) {
			promise.reject(e);
		}
	}

	private void setTimer(final int id, final long millis, final boolean repeats) {
		if (handler == null) handler = new Handler();
		Runnable runnable = repeats ? getIntervalRunnable(id, millis) : getTimeoutRunnable(id);
		handler.postDelayed(runnable, millis);

		PowerManager powerManager = (PowerManager) getReactApplicationContext().getSystemService(Context.POWER_SERVICE);
		WakeLock wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);
		if (repeats) wakeLock.acquire();
		else wakeLock.acquire(millis);

		TimerData timerData = new TimerData(id, runnable, wakeLock);
		timerDataMap.put(id, timerData);
	}

	private Runnable getTimeoutRunnable(final int id) {
		return new Runnable() {
			@Override
			public void run() {
				timerDataMap.remove(id);
				sendTimerEventToJS(id);
			}
		};
	}

	private Runnable getIntervalRunnable(final int id, final long millis) {
		return new Runnable() {
			@Override
			public void run() {
				sendTimerEventToJS(id);
				handler.postDelayed(this, millis);
			}
		};
	}

	private void sendTimerEventToJS(final int id) {
		if (getReactApplicationContext().hasActiveCatalystInstance()) {
			getReactApplicationContext()
			.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
			.emit(TIMER_EVENT, id);
			Log.d(TAG, "send timer event for id: " + String.valueOf(id));
		} else {
			Log.d(TAG, "could not send event as there is no active react instance. Event id: " + String.valueOf(id));
		}
	}

	@ReactMethod
	public void clearTimer(final int id, final Promise promise) {
		try {
			clearTimer(id);
			promise.resolve(null);
			Log.d(TAG, "clearTimer for id: " + String.valueOf(id));
		} catch (Exception e) {
			promise.reject(e);
		}
	}

	private void clearTimer(final int id) {
		TimerData timerData = timerDataMap.get(id);
		if (timerData != null) {
			if (timerData.wakeLock.isHeld()) timerData.wakeLock.release();
			handler.removeCallbacks(timerData.runnable);
			timerDataMap.remove(id);
		}
	}

	@Override
	public void onCatalystInstanceDestroy() {
		super.onCatalystInstanceDestroy();
		for (TimerData timerData : timerDataMap.values()) {
			if (timerData == null) continue;
			try {
				if (timerData.wakeLock.isHeld()) timerData.wakeLock.release();
			} catch (Exception e) {
				Log.e(TAG, "Could not release wakeLock of id: " + timerData.id, e);
			}
		}
	}

	@Override
	public Map<String, Object> getConstants() {
		final Map<String, Object> constants = new HashMap<>();
		constants.put("TIMER_EVENT", TIMER_EVENT);
		return constants;
	}

	private class TimerData {
		public int id;
		public Runnable runnable;
		public WakeLock wakeLock;

		public TimerData(int id, Runnable runnable, WakeLock wakeLock) {
			this.id = id;
			this.runnable = runnable;
			this.wakeLock = wakeLock;
		}
	}
}
