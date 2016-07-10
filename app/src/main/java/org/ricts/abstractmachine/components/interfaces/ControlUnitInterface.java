package org.ricts.abstractmachine.components.interfaces;

/**
 * Created by Jevon on 18/12/2015.
 */
public interface ControlUnitInterface {
    void setPC(int currentPC);
    void setStartExecFrom(int currentPC);
    void reset();

    void setNextStateToHalt();

    void performNextAction();
    int nextActionDuration();
}
