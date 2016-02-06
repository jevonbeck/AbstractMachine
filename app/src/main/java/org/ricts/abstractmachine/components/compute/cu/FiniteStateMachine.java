package org.ricts.abstractmachine.components.compute.cu;

import org.ricts.abstractmachine.components.interfaces.FSMInterface;

public abstract class FiniteStateMachine implements FSMInterface {	// 'Context'
    private State currentState;

    protected abstract State getNextState(State currentState);

    @Override
    public State currentState(){
        return currentState;
    }

    @Override
    public void setCurrentState(State state){
        currentState = state;
    }

    @Override
    public void triggerStateChange() {
        currentState.performAction(); // do current state action
        setCurrentState(getNextState(currentState)); // go to next state
    }
}
