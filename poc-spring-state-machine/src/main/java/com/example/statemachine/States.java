package com.example.statemachine;

import lombok.Getter;

@Getter
public enum States {
	UNINITIALIZED(false), INITIALIZING(false), ONLINE(true), REMOVING(false), REMOVED(false), UNINITIALIZING(false);

	private States(boolean allowedToSendCommands) {
		this.allowedToSendCommands = allowedToSendCommands;
	}

	private boolean allowedToSendCommands;
}
