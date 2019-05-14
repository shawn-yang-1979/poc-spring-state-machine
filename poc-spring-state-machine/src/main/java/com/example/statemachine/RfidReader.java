package com.example.statemachine;

import org.springframework.statemachine.StateMachine;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RfidReader {
	@Setter
	@EqualsAndHashCode.Include
	private String host;
	@Getter
	@Setter
	private boolean enabled;
	@Getter
	private StateMachine<States, Events> stateMachine;
	@Setter
	private RfidReaderCallback callback;

	public RfidReader() {
		super();
		this.stateMachine = new DeviceStateMachineBuilder()//
				.logger(log)//
				.onlineEntry(this::setOnline)//
				.onlineHealthChecking(this::checkHealth)//
				.onlineExit(this::resetHealthState)//
				.removingDo(this::disconnect)//
				.uninitializedEntry(this::setOffline)//
				.uninitializingDo(this::disconnect)//
				.initializingDo(this::connect)//
				.build();
	}

	public void checkHealth() {
		log.info("checking health");
		throw new RuntimeException("Not healthy");
	}

	public void resetHealthState() {
		log.info("Reset health state");
	}

	public void setOffline() {
		log.info("Set offline");
		callback.offline(host);
	}

	public void setOnline() {
		log.info("Set online");
		callback.online(host);
	}

	public void connect() {
		log.info("Connecting RFID reader");
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			// ignore
		}
	}

	public void disconnect() {
		log.info("Disconnecting RFID reader");
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			// ignore
		}
	}

}
