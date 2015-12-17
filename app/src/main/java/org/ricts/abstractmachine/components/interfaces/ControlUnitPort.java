package org.ricts.abstractmachine.components.interfaces;

/**
 * Created by Jevon on 17/12/2015.
 */
public interface ControlUnitPort {
    void setToFetchState();
    void setToExecuteState();
    boolean isAboutToExecute();

    void performNextAction();
}
