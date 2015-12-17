package org.ricts.abstractmachine.components.compute.cu;


public abstract class ControlUnitState extends State {
    public ControlUnitState(String name){
        super(name);
    }

    public abstract int actionDuration();
}
