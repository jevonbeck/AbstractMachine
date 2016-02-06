package org.ricts.abstractmachine.components.interfaces;

/**
 * Created by Jevon on 18/12/2015.
 */
public interface ControlUnitInterface {
    int getPC();
    int getIR();
    void setPC(int currentPC);
    void setIR(int currentIR);
    void setStartExecFrom(int currentPC);

    void setToFetchState();
    void setToExecuteState();
    void setToHaltState();
    boolean isAboutToExecute();
    void fetchInstruction(ReadPort instructionCache);

    void performNextAction();
    int nextActionDuration();
}
