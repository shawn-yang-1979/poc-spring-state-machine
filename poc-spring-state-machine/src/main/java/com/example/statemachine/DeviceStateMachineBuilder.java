package com.example.statemachine;

import java.util.EnumSet;

import org.slf4j.Logger;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineBuilder;
import org.springframework.statemachine.config.StateMachineBuilder.Builder;
import org.springframework.statemachine.config.configurers.InternalTransitionConfigurer;
import org.springframework.statemachine.config.configurers.StateConfigurer;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DeviceStateMachineBuilder {
	private static final String AUTO_SEND_EVENT_PREFIX = "Auto send event: {}";
	private Builder<States, Events> wrappedBuilder = StateMachineBuilder.builder();
	private StateConfigurer<States, Events> stateConfigurer;
	private InternalTransitionConfigurer<States, Events> onlineHealthChecking;
	private Logger specifiedLogger = log;

	public DeviceStateMachineBuilder() {
		try {
			stateConfigurer = wrappedBuilder.configureStates().withStates();
			stateConfigurer.initial(States.UNINITIALIZED)//
					.end(States.REMOVED)//
					.states(EnumSet.allOf(States.class));

			wrappedBuilder.configureTransitions().withExternal()//
					.source(States.UNINITIALIZED).target(States.INITIALIZING).event(Events.INITIALIZING)//
					.and().withExternal()//
					.source(States.INITIALIZING).target(States.ONLINE).event(Events.INITIALIZING_OK)//
					.and().withExternal()//
					.source(States.ONLINE).target(States.REMOVING).event(Events.REMOVING)//
					.and().withExternal()//
					.source(States.REMOVING).target(States.REMOVED).event(Events.REMOVED)//
					.and().withExternal()//
					.source(States.ONLINE).target(States.UNINITIALIZING).event(Events.UNINITIALIZING)//
					.and().withExternal()//
					.source(States.INITIALIZING).target(States.UNINITIALIZING).event(Events.INITIALIZING_ERROR)//
					.and().withExternal()//
					.source(States.UNINITIALIZING).target(States.UNINITIALIZED).event(Events.UNINITIALIZED);

			this.onlineHealthChecking = wrappedBuilder.configureTransitions().withInternal()//
					.source(States.ONLINE).event(Events.HEALTH_CHECKING);

		} catch (Exception e) {
			throw new DeviceStateMachineException(e);
		}
	}

	public StateMachine<States, Events> build() {
		return wrappedBuilder.build();
	}

	public DeviceStateMachineBuilder logger(Logger logger) {
		this.specifiedLogger = logger;
		return this;
	}

	public DeviceStateMachineBuilder initializingDo(DeviceAction action) {
		this.stateConfigurer.stateDo(States.INITIALIZING, //
				context -> {
					specifiedLogger.info("initializingDo");
					try {
						action.doAction();
						// if no exception, go online.
						specifiedLogger.info(AUTO_SEND_EVENT_PREFIX, Events.INITIALIZING_OK);
						context.getStateMachine().sendEvent(Events.INITIALIZING_OK);
					} catch (RuntimeException ex) {
						specifiedLogger.error(ex.getMessage(), ex);
						// if exception got, go uninitializing
						specifiedLogger.info(AUTO_SEND_EVENT_PREFIX, Events.INITIALIZING_ERROR);
						context.getStateMachine().sendEvent(Events.INITIALIZING_ERROR);
					}
				});
		return this;
	}

	public DeviceStateMachineBuilder uninitializingDo(DeviceAction action) {
		this.stateConfigurer.stateDo(States.UNINITIALIZING, //
				context -> {
					specifiedLogger.info("uninitializingDo");
					try {
						action.doAction();
					} catch (RuntimeException ex) {
						specifiedLogger.error(ex.getMessage(), ex);
					}
					specifiedLogger.info(AUTO_SEND_EVENT_PREFIX, Events.UNINITIALIZED);
					context.getStateMachine().sendEvent(Events.UNINITIALIZED);
				});
		return this;
	}

	public DeviceStateMachineBuilder removingDo(DeviceAction action) {
		this.stateConfigurer.stateDo(States.REMOVING, //
				context -> {
					specifiedLogger.info("removingDo");
					try {
						action.doAction();
					} catch (RuntimeException ex) {
						specifiedLogger.error(ex.getMessage(), ex);
					}
					specifiedLogger.info(AUTO_SEND_EVENT_PREFIX, Events.REMOVED);
					context.getStateMachine().sendEvent(Events.REMOVED);
				});
		return this;
	}

	public DeviceStateMachineBuilder onlineEntry(DeviceAction action) {
		this.stateConfigurer.stateEntry(States.ONLINE, //
				context -> {
					specifiedLogger.info("onlineEntry");
					action.doAction();
				});
		return this;
	}

	public DeviceStateMachineBuilder onlineHealthChecking(DeviceAction action) {
		this.onlineHealthChecking.action(context -> {
			try {
				action.doAction();
			} catch (RuntimeException ex) {
				specifiedLogger.error(ex.getMessage(), ex);
				context.getStateMachine().sendEvent(Events.UNINITIALIZING);
			}
		});
		return this;
	}

	public DeviceStateMachineBuilder onlineExit(DeviceAction action) {
		this.stateConfigurer.stateExit(States.ONLINE, //
				context -> {
					specifiedLogger.info("onlineExit");
					action.doAction();
				});
		return this;
	}

	public DeviceStateMachineBuilder uninitializedEntry(DeviceAction action) {
		this.stateConfigurer.stateEntry(States.UNINITIALIZED, //
				context -> {
					specifiedLogger.info("uninitializedEntry");
					action.doAction();
				});
		return this;
	}

	public static class DeviceStateMachineException extends RuntimeException {

		private static final long serialVersionUID = 1L;

		public DeviceStateMachineException(Throwable cause) {
			super(cause);
		}

	}

	/**
	 * functional interface
	 * 
	 * @author SHAWN.SH.YANG
	 *
	 */
	public static interface DeviceAction {

		public void doAction();

	}
}
