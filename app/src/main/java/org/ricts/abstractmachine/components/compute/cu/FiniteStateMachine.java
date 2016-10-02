package org.ricts.abstractmachine.components.compute.cu;

public abstract class FiniteStateMachine {	// 'Context'
    private State currentState;

    protected abstract State getNextState(State currentState);

    public State currentState(){
        return currentState;
    }

    public void setCurrentState(State state){
        currentState = state;
    }

    public void triggerStateChange() {
        currentState.performAction(); // do current state action
        setCurrentState(getNextState(currentState)); // go to next state
    }
}
