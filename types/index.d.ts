declare module "react-native-background-timer-android" {
	type TimerCallback = () => void;
	type TimerErrorCallback = (error: Error) => void;

	export default class BackgroundTimer {
		static setTimeout(callback: TimerCallback, millis: number, onError?: TimerErrorCallback): number;
		static setInterval(callback: TimerCallback, millis: number, onError?: TimerErrorCallback): number;
		static clearTimeout(id: number): Promise<void>;
		static clearInterval(id: number): Promise<void>;
	}
}
