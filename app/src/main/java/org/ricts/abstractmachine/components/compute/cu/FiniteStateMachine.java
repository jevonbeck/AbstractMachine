package org.ricts.abstractmachine.components.compute.cu;

import org.ricts.abstractmachine.components.interfaces.FsmInterface;

public abstract class FiniteStateMachine implements FsmInterface {	// 'Context'
    private State currentState, nextState;

    protected abstract State desiredNextState(State currentState);
    protected abstract State convertToState(String stateName);
    protected abstract String initialState();

    @Override
    public void reset() {
        setCurrentState(initialState());
    }

    @Override
    public String currentState(){
        return currentState.getName();
    }

    @Override
    public void setCurrentState(String state){
        currentState = convertToState(state);
    }

    @Override
    public void setNextState(String state){
        nextState = convertToState(state);
    }

    @Override
    public void triggerStateChange() {
        currentState.performAction(); // do current state action
        setCurrentState(determineNextState(currentState)); // go to next state
    }

    protected State determineNextState(State currentState) {
        if (nextState != null) { // If normal cycle interrupted ...
            // ... next state determined by interrupting state
            currentState = nextState;
            nextState = null;
            return currentState;
        }

        // Otherwise next state determined by current state
        return desiredNextState(currentState);
    }

    protected State getCurrentState() {
        return currentState;
    }

    protected void setCurrentState(State state){
        currentState = state;
    }

    protected State getNextState() {
        return nextState;
    }
}
