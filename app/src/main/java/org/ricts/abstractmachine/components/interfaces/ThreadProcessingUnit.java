package org.ricts.abstractmachine.components.interfaces;

public interface ThreadProcessingUnit {
	int nextActionTransitionTime(); // in clock cycles
	void triggerNextAction();
	void setStartExecFrom(int currentPC);
}
