
# react-native-background-timer-android
This library provides `setTimeout` and `setInterval` implementations that keep working even if the app is running in the background or the screen is locked.

Based on **[react-native-background-timer](https://github.com/ocetnik/react-native-background-timer)** but with major code changes and only works on Android.

## Features
- Clear and simple API
- Can set multiple timers
- Keeps running when the screen is locked
- Each timer has its own [WakeLock](https://developer.android.com/reference/android/os/PowerManager.WakeLock) that remains active while it's running
- Intervals are handled entirely on the native side
- Fault-tolerant: any exception gets reported back to JS via promises
- TypeScript declarations included

## Install
	yarn add react-native-background-timer-android

## Usage
```typescript
import Timer from "react-native-background-timer-android";

// Start a timer that will repeatedly log "tic" after 500 milliseconds
const intervalId = Timer.setInterval(() => console.log("tic"), 500);

// Cancel the timer when you are done with it
Timer.clearInterval(intervalId);

// Start a timer that will log "tic" after 500 milliseconds just once
const timeoutId = Timer.setTimeout(() => console.log("tic"), 500);

// Cancel the timer if needed
Timer.clearTimeout(timeoutId);
```

## API
```typescript
Timer.setInterval(callback: () => void, millis: number, onError?: (error: Error) => void): number;
Timer.setTimeout(callback: () => void, millis: number, onError?: (error: Error) => void): number;
Timer.clearInterval(id: number): Promise<void>;
Timer.clearTimeout(id: number): Promise<void>;
```
