package com.example.statemachine;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class RfidReaderAsyncOperation {

	@Async
	public void handleMonitor(RfidReader device) {
		if (device.isEnabled()) {
			device.getStateMachine().sendEvent(Events.HEALTH_CHECKING);
			device.getStateMachine().sendEvent(Events.INITIALIZING);
		} else {
			device.getStateMachine().sendEvent(Events.UNINITIALIZING);
		}
	}

}
