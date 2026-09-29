package os.juanamd.backgroundtimer;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.util.Log;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.WritableMap;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class BackgroundTimerModule extends RNBackgroundTimerAndroidSpec {
	public static final String NAME = "RNBackgroundTimerAndroid";
	private static final String TAG = "RNBackgroundTimerAndroid";

	private final Handler handler = new Handler(Looper.getMainLooper());
	private final Map<Integer, Timer> timers = new ConcurrentHashMap<>();
	private final PowerManager powerManager;

	public BackgroundTimerModule(ReactApplicationContext reactContext) {
		super(reactContext);
		powerManager = (PowerManager) reactContext.getSystemService(Context.POWER_SERVICE);
	}

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public void setTimer(int id, double millis, boolean repeats, Promise promise) {
		try {
			clearTimerInternal(id);

			long delayMillis = Math.max(0L, (long) millis);
			Timer timer = new Timer(id, delayMillis, repeats);
			timers.put(id, timer);
			timer.acquireWakeLock();
			handler.postDelayed(timer.runnable, delayMillis);
			promise.resolve(null);
			Log.d(TAG, "setTimer for id: " + id + " for " + millis + " ms. Repeats: " + repeats);
		} catch (Exception e) {
			clearTimerInternal(id);
			Log.e(TAG, "Unable to set timer for id: " + id, e);
			promise.reject("E_SET_TIMER", "Unable to set timer", e);
		}
	}

	@Override
	public void clearTimer(int id, Promise promise) {
		try {
			clearTimerInternal(id);
			promise.resolve(null);
			Log.d(TAG, "clearTimer for id: " + id);
		} catch (Exception e) {
			Log.e(TAG, "Unable to clear timer for id: " + id, e);
			promise.reject("E_CLEAR_TIMER", "Unable to clear timer", e);
		}
	}

	private void clearTimerInternal(int id) {
		Timer timer = timers.remove(id);
		if (timer != null) {
			handler.removeCallbacks(timer.runnable);
			timer.releaseWakeLock();
		}
	}

	@Override
	public void invalidate() {
		Log.d(TAG, "Invalidating background timer module with " + timers.size() + " active timer(s)");
		for (Timer timer : timers.values()) {
			handler.removeCallbacks(timer.runnable);
			timer.releaseWakeLock();
		}
		timers.clear();
		super.invalidate();
	}

	private final class Timer {
		private final int id;
		private final long delayMillis;
		private final boolean repeats;
		private final PowerManager.WakeLock wakeLock;

		private final Runnable runnable = new Runnable() {
			@Override
			public void run() {
				Timer currentTimer = timers.get(id);
				if (currentTimer != Timer.this) return;

				Log.d(TAG, "timer event for id: " + id);
				try {
					WritableMap event = Arguments.createMap();
					event.putInt("id", id);
					emitOnTimer(event);
					Log.d(TAG, "send timer event for id: " + id);
				} catch (Exception e) {
					Log.e(TAG, "Unable to send timer event for id: " + id, e);
					clearTimerInternal(id);
					return;
				}

				if (repeats && timers.get(id) == Timer.this) {
					handler.postDelayed(this, delayMillis);
				} else if (timers.remove(id, Timer.this)) {
					releaseWakeLock();
					Log.d(TAG, "timer completed for id: " + id);
				}
			}
		};

		private Timer(int id, long delayMillis, boolean repeats) {
			this.id = id;
			this.delayMillis = delayMillis;
			this.repeats = repeats;
			if (powerManager == null) {
				throw new IllegalStateException("PowerManager is not available");
			}
			wakeLock = powerManager.newWakeLock(
				PowerManager.PARTIAL_WAKE_LOCK,
				"RNBackgroundTimerAndroid:" + id
			);
			wakeLock.setReferenceCounted(false);
		}

		private void acquireWakeLock() {
			if (!wakeLock.isHeld()) {
				wakeLock.acquire();
				Log.d(TAG, "acquired wakeLock for id: " + id);
			}
		}

		private void releaseWakeLock() {
			if (wakeLock.isHeld()) {
				wakeLock.release();
				Log.d(TAG, "released wakeLock for id: " + id);
			}
		}
	}
}
