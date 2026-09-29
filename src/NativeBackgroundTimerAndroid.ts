import type { CodegenTypes, TurboModule } from "react-native";
import { TurboModuleRegistry } from "react-native";

export type TimerEvent = {
	id: CodegenTypes.Int32;
};

export interface Spec extends TurboModule {
	setTimer(id: CodegenTypes.Int32, millis: CodegenTypes.Double, repeats: boolean): Promise<void>;
	clearTimer(id: CodegenTypes.Int32): Promise<void>;
	readonly onTimer: CodegenTypes.EventEmitter<TimerEvent>;
}

export default TurboModuleRegistry.get<Spec>("RNBackgroundTimerAndroid");
