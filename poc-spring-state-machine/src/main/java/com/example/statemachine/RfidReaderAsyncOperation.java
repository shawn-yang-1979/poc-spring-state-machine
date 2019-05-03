package com.example.statemachine;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class RfidReaderAsyncOperation {

	@Async
	public void handleMonitor(RfidReader device) {
		if (device.isEnabled()) {
			if (!device.isHealth()) {
				device.getStateMachine().sendEvent(Events.UNINITIALIZING);
			}
			device.getStateMachine().sendEvent(Events.INITIALIZING);
		} else {
			device.getStateMachine().sendEvent(Events.UNINITIALIZING);
		}
	}

}
