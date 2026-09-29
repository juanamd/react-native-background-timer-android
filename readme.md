# react-native-background-timer-android

Android-only background-capable `setTimeout` and `setInterval` implementations for React Native.

This package uses React Native's New Architecture (Turbo Native Modules + Codegen). The legacy architecture is intentionally not supported by this branch/version.

## Requirements

- React Native 0.81 or newer
- Android
- React Native New Architecture enabled

For Android applications, enable the New Architecture in `android/gradle.properties`:

```properties
newArchEnabled=true
```

## Install

```bash
yarn add react-native-background-timer-android
```

Autolinking handles the Android native package. No manual package registration is required in a standard React Native application.

## Usage

```js
import Timer from "react-native-background-timer-android";

const intervalId = Timer.setInterval(() => {
	console.log("tic");
}, 500);

Timer.clearInterval(intervalId);

const timeoutId = Timer.setTimeout(() => {
	console.log("tac");
}, 10000);

Timer.clearTimeout(timeoutId);
```

## Debug logging

The Android module uses the log tag `RNBackgroundTimerAndroid`. Debug messages are written when timers are created, fired, cleared, and when their WakeLocks are acquired or released. Errors while creating timers or emitting events are logged with stack traces.

On a connected Android device, logs can be filtered with:

```bash
adb logcat -s RNBackgroundTimerAndroid:D
```

## API

```ts
Timer.setInterval(
	callback: () => void,
	millis: number,
	onError?: (error: Error) => void,
): number;

Timer.setTimeout(
	callback: () => void,
	millis: number,
	onError?: (error: Error) => void,
): number;

Timer.clearInterval(id: number): Promise<void>;
Timer.clearTimeout(id: number): Promise<void>;
```

`clearTimeout` and `clearInterval` use the same native cancellation operation, so either method can clear a timer created by either method.

## How it works

The JavaScript layer owns the callback associated with each timer. Native Android owns the actual timer scheduling and emits a typed TurboModule event when a timer fires.

Each active native timer acquires its own Android `PARTIAL_WAKE_LOCK`, which keeps the CPU awake while that timer is active. The wake lock is released when the timer is cancelled or, for a timeout, after it fires.

Intervals are rescheduled entirely on the native side rather than repeatedly scheduling work from JavaScript.

## Errors

Native scheduling and cancellation failures reject their underlying promises. The optional `onError` callback passed to `setTimeout` or `setInterval` receives a native scheduling error.

## Background execution notes

The timer can continue firing while the Android screen is locked because the native scheduler and wake lock do not depend on the JavaScript event loop being actively scheduling each interval.

Android can still stop or restrict an application's process because of force-stop, OEM-specific battery management, or other system policies. A timer is not a persistent service and does not survive an application process being terminated.

## New Architecture implementation

The package exposes a typed Codegen specification in `src/NativeBackgroundTimerAndroid.ts`. Codegen generates `RNBackgroundTimerAndroidSpec`, and `BackgroundTimerModule` implements that generated TurboModule interface.

The package deliberately does not use `NativeModules`, `ReactMethod`, `NativeEventEmitter`, or the legacy `ReactContextBaseJavaModule` API.
