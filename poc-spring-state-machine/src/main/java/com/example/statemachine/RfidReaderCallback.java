package com.example.statemachine;

interface RfidReaderCallback {
	void online(String host);

	void offline(String host);

	void received(String tag);
}
