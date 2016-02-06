package org.ricts.abstractmachine.components.interfaces;

import org.ricts.abstractmachine.components.compute.cu.State;

/**
 * Created by Jevon on 03/01/2016.
 */
public interface FSMInterface {
    State currentState();
    void setCurrentState(State state);

    void triggerStateChange();
}
