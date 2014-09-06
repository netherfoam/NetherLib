package org.maxgamer.event;

public class Event {
	/** Calls this action through the ActionManager. Convenience method */
	public void call(){
		EventManager.callEvent(this);
	}
}