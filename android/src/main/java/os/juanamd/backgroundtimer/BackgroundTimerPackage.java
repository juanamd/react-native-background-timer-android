package os.juanamd.backgroundtimer;

import com.facebook.react.BaseReactPackage;
import com.facebook.react.bridge.NativeModule;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.module.model.ReactModuleInfo;
import com.facebook.react.module.model.ReactModuleInfoProvider;

import java.util.HashMap;
import java.util.Map;

public class BackgroundTimerPackage extends BaseReactPackage {
	@Override
	public NativeModule getModule(String name, ReactApplicationContext reactContext) {
		if (name.equals(BackgroundTimerModule.NAME)) {
			return new BackgroundTimerModule(reactContext);
		}
		return null;
	}

	@Override
	public ReactModuleInfoProvider getReactModuleInfoProvider() {
		return new ReactModuleInfoProvider() {
			@Override
			public Map<String, ReactModuleInfo> getReactModuleInfos() {
				Map<String, ReactModuleInfo> map = new HashMap<>();
				map.put(BackgroundTimerModule.NAME, new ReactModuleInfo(
					BackgroundTimerModule.NAME,
					BackgroundTimerModule.class.getName(),
					false,
					false,
					false,
					true
				));
				return map;
			}
		};
	}
}
