package org.ricts.abstractmachine.components.interfaces;

/**
 * Created by Jevon on 18/12/2015.
 */
public interface ControlUnitInterface {
    void reset();

    boolean isPipelined();
    void setNextFetch(int instructionAddress);
    void setNextFetchAndExecute(int instructionAddress, int nopInstruction);

    void setNextStateToHalt();
    void setNextStateToSleep();

    void performNextAction();
    int nextActionDuration();
}
