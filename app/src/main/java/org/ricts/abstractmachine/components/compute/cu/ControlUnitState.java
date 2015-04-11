package org.ricts.abstractmachine.components.compute.cu;


public abstract class ControlUnitState extends State {
    public ControlUnitState(){
        super();
    }

    public abstract int actionDuration();
}
