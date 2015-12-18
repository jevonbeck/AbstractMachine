package org.ricts.abstractmachine.components.interfaces;

/**
 * Created by Jevon on 18/12/2015.
 */
public interface ControlUnitInterface {
    int getPC();
    void setPC(int currentPC);
    void setIR(int currentIR);

    void setToFetchState();
    void setToExecuteState();
    boolean isAboutToExecute();

    void performNextAction();
    int nextActionDuration();
}
