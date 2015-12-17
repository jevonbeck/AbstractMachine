package org.ricts.abstractmachine.components.compute.cu;

public abstract class State {
    private State nextState;
    private String name;

    public abstract void performAction();


    public State(String stateName){
        name = stateName;
    }

    public String getName(){
        return name;
    }

    public void setNextState(State newState){
        nextState = newState;
    }

    public State nextState(){
        return nextState;
    }
}
