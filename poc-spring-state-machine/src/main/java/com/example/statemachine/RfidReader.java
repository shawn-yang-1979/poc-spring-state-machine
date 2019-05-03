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
	@Setter
	private boolean health;
	@Getter
	private StateMachine<States, Events> stateMachine;
	@Setter
	private RfidReaderCallback callback;

	public RfidReader() {
		super();
		this.stateMachine = new DeviceStateMachineBuilder()//
				.onlineEntry(this::setOnline)//
				.onlineExit(this::setOffline)//
				.initializing(this::connect, this::disconnect)//
				.uninitializing(this::disconnect)//
				.removing(this::disconnect)//
				.build();
		stateMachine.start();
	}

	public void setOffline() {
		callback.offline(host);
	}

	public void setOnline() {
		callback.online(host);
	}

	public void connect() {
		log.info("Connecting RFID reader");
		try {
			Thread.sleep(10000);
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
