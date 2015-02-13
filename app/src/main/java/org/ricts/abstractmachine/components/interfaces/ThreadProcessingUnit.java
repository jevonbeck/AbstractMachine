package org.ricts.abstractmachine.components.interfaces;

public interface ThreadProcessingUnit {
	public int nextActionTransitionTime(); // in clock cycles
	public void triggerNextAction();
	public void setStartExecFrom(int currentPC);
}
