package org.ricts.abstractmachine.components.compute.cu;

public abstract class State {
    private String name;

    public abstract void performAction();

    public State(String stateName){
        name = stateName;
    }

    public String getName(){
        return name;
    }
}
