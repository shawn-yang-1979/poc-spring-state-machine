package com.example.statemachine;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.PreDestroy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.statemachine.listener.StateMachineListenerAdapter;
import org.springframework.statemachine.state.State;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class RfidReaderComponent extends StateMachineListenerAdapter<States, Events> implements RfidReaderCallback {

	@Autowired
	private RfidReaderAsyncOperation rfidReaderAsyncOperation;

	private Map<Long, RfidReader> deviceById = new HashMap<>();

	@EventListener(ContextRefreshedEvent.class)
	void init() {
		try {
			RfidReader reader1 = new RfidReader();
			reader1.setCallback(this);
			reader1.getStateMachine().addStateListener(this);
			reader1.setEnabled(true);
			reader1.setHost("192.168.10.1");
			deviceById.put(1l, reader1);

			RfidReader reader2 = new RfidReader();
			reader2.setCallback(this);
			reader2.getStateMachine().addStateListener(this);
			reader2.setEnabled(true);
			reader2.setHost("192.168.10.2");
			deviceById.put(2l, reader2);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	@PreDestroy
	void preDestroy() {
		try {
			deviceById.values().forEach(device -> device.getStateMachine().sendEvent(Events.REMOVING));
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	@Scheduled(initialDelay = 10000, fixedDelay = 10000)
	void scheduledMonitor() {
		try {
			deviceById.values().forEach(device -> rfidReaderAsyncOperation.handleMonitor(device));
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	public void removed(long id) {
		try {
			RfidReader device = this.deviceById.remove(id);
			device.getStateMachine().sendEvent(Events.REMOVING);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	public void updated(long id) {
		try {
			RfidReader device = this.deviceById.get(id);
			RfidReader deviceNewState = new RfidReader();
			if (device.equals(deviceNewState)) {
				log.info("No changes to reload.");
				return;
			}
			device.getStateMachine().sendEvent(Events.UNINITIALIZING);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	public void add(long id) {
		try {
			RfidReader physicalRfidReader = new RfidReader();
			deviceById.put(id, physicalRfidReader);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	@Override
	public void online(String host) {
		log.info("Update " + host + " latest status as online");
	}

	@Override
	public void offline(String host) {
		log.info("Update " + host + " latest status as offline");
	}

	@Override
	public void received(String tag) {
		// TODO Auto-generated method stub

	}

	@Override
	public void stateChanged(State<States, Events> from, State<States, Events> to) {
		log.info("from " + from.getId());
		log.info("to " + to.getId());
	}
}
