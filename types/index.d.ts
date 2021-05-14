declare module "react-native-background-timer-android" {
	export default class {
		static setTimeout(callback: () => void, millis: number, onError?: (error: Error) => void): number;
		static setInterval(callback: () => void, millis: number, onError?: (error: Error) => void): number;
		static clearTimeout(id: number): Promise<void>;
		static clearInterval(id: number): Promise<void>;
	}
}