import { NativeEventEmitter, NativeModules } from "react-native";

const { RNBackgroundTimerAndroid } = NativeModules;
const eventEmitter = new NativeEventEmitter(RNBackgroundTimerAndroid);
const timerDataMap = {};
let uniqueIdCounter = 0;

eventEmitter.addListener(RNBackgroundTimerAndroid.TIMER_EVENT, id => {
	const timerData = timerDataMap[id];
	if (timerData) {
		const { callback, repeats } = timerData;
		if (!repeats) delete timerDataMap[id];
		callback();
	}
});

function setTimer(callback, millis, onError = () => {}, repeats) {
	const id = ++uniqueIdCounter;
	timerDataMap[id] = { callback, repeats };
	RNBackgroundTimerAndroid.setTimer(id, millis, repeats).catch(onError);
	return id;
}

async function clearTimer(id) {
	if (timerDataMap[id]) {
		delete timerDataMap[id];
		await RNBackgroundTimerAndroid.clearTimer(id);
	}
}

class BackgroundTimer {
	static setTimeout(callback, millis, onError) {
		return setTimer(callback, millis, onError, false);
	}

	static setInterval(callback, millis, onError) {
		return setTimer(callback, millis, onError, true);
	}

	static clearTimeout(id) {
		return clearTimer(id);
	}

	static clearInterval(id) {
		return clearTimer(id);
	}
}

export default BackgroundTimer;
