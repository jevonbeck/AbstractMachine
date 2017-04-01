package org.ricts.abstractmachine.components.interfaces;

/**
 * Created by Jevon on 25/03/2017.
 */

public interface FsmInterface {
    void reset();
    void triggerStateChange();
    void setCurrentState(String state);
    void setNextState(String state);

    String currentState();
}
