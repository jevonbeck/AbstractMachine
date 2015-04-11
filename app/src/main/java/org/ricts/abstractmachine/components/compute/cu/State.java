package org.ricts.abstractmachine.components.compute.cu;

public abstract class State {
    private State nextState;

    public abstract void performAction();
    public abstract String name();

    public void setNextState(State newState){
        nextState = newState;
    }

    public State nextState(){
        return nextState;
    }
}
