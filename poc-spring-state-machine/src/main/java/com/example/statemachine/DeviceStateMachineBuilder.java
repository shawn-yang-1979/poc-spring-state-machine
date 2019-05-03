package com.example.statemachine;

import java.util.EnumSet;

import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineBuilder;
import org.springframework.statemachine.config.StateMachineBuilder.Builder;
import org.springframework.statemachine.config.configurers.ExternalTransitionConfigurer;
import org.springframework.statemachine.config.configurers.StateConfigurer;

public class DeviceStateMachineBuilder {

	private Builder<States, Events> wrappedBuilder = StateMachineBuilder.builder();
	private StateConfigurer<States, Events> stateConfigurer;
	private ExternalTransitionConfigurer<States, Events> initializingTransitionConfigurer;
	private ExternalTransitionConfigurer<States, Events> uninitializingTransitionConfigurer;
	private ExternalTransitionConfigurer<States, Events> removing1TransitionConfigurer;
	private ExternalTransitionConfigurer<States, Events> removing2TransitionConfigurer;

	public DeviceStateMachineBuilder() {
		try {
			stateConfigurer = wrappedBuilder.configureStates().withStates();
			stateConfigurer.initial(States.UNINITIALIZED)//
					.end(States.REMOVED)//
					.states(EnumSet.allOf(States.class));

			initializingTransitionConfigurer = wrappedBuilder.configureTransitions().withExternal();
			initializingTransitionConfigurer//
					.source(States.UNINITIALIZED).target(States.ONLINE).event(Events.INITIALIZING);

			uninitializingTransitionConfigurer = wrappedBuilder.configureTransitions().withExternal();
			uninitializingTransitionConfigurer//
					.source(States.ONLINE).target(States.UNINITIALIZED).event(Events.UNINITIALIZING);

			removing1TransitionConfigurer = wrappedBuilder.configureTransitions().withExternal();
			removing1TransitionConfigurer//
					.source(States.UNINITIALIZED).target(States.REMOVED).event(Events.REMOVING);

			removing2TransitionConfigurer = wrappedBuilder.configureTransitions().withExternal();
			removing2TransitionConfigurer//
					.source(States.ONLINE).target(States.REMOVED).event(Events.REMOVING);

		} catch (Exception e) {
			throw new ConfigureException();
		}
	}

	public StateMachine<States, Events> build() {
		return wrappedBuilder.build();
	}

	public DeviceStateMachineBuilder onlineEntry(DeviceAction action) {
		this.stateConfigurer.stateEntry(States.ONLINE, context -> action.doAction());
		return this;
	}

	public DeviceStateMachineBuilder onlineExit(DeviceAction action) {
		this.stateConfigurer.stateExit(States.ONLINE, context -> action.doAction());
		return this;
	}

	public DeviceStateMachineBuilder initializing(DeviceAction action, DeviceAction error) {
		this.initializingTransitionConfigurer.action(context -> action.doAction(), context -> error.doAction());
		return this;
	}

	public DeviceStateMachineBuilder uninitializing(DeviceAction action) {
		this.uninitializingTransitionConfigurer.action(context -> action.doAction());
		return this;
	}

	public DeviceStateMachineBuilder removing(DeviceAction action) {
		this.removing1TransitionConfigurer.action(context -> action.doAction());
		this.removing2TransitionConfigurer.action(context -> action.doAction());
		return this;
	}

	public static class ConfigureException extends RuntimeException {

		private static final long serialVersionUID = 1L;

		public ConfigureException() {
			super();
		}

	}

}
