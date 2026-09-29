import NativeBackgroundTimerAndroid from "./src/NativeBackgroundTimerAndroid";

const timerDataMap = {};
let uniqueIdCounter = 0;

if (NativeBackgroundTimerAndroid !== null) {
	NativeBackgroundTimerAndroid.onTimer(event => {
		const timerData = timerDataMap[event.id];
		if (timerData) {
			const { callback, repeats } = timerData;
			if (!repeats) delete timerDataMap[event.id];
			callback();
		}
	});
}

function setTimer(callback, millis, onError = () => { }, repeats) {
	assertAndroid();
	const id = ++uniqueIdCounter;
	timerDataMap[id] = { callback, repeats };
	NativeBackgroundTimerAndroid.setTimer(id, millis, repeats).catch(error => {
		delete timerDataMap[id];
		onError(error);
	});
	return id;
}

async function clearTimer(id) {
	assertAndroid();
	if (timerDataMap[id]) {
		delete timerDataMap[id];
		await NativeBackgroundTimerAndroid.clearTimer(id);
	}
}

function assertAndroid() {
	if (NativeBackgroundTimerAndroid === null) {
		throw new Error("Background timer can only be used in Android devices");
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
