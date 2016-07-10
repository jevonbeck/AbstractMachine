package org.ricts.abstractmachine.components.observables;

import org.ricts.abstractmachine.components.compute.cu.ControlUnit;
import org.ricts.abstractmachine.components.interfaces.ControlUnitInterface;

/**
 * Created by Jevon on 23/01/2016.
 */
public class ObservableControlUnit extends ObservableFSM<ControlUnit> implements ControlUnitInterface {
    public ObservableControlUnit(ControlUnit type) {
        super(type);
    }

    @Override
    public void setPC(int currentPC) {
        observable_data.setPC(currentPC);
        setChanged();
        notifyObservers();
    }

    @Override
    public void setStartExecFrom(int currentPC) {
        observable_data.setStartExecFrom(currentPC);
        setChanged();
        notifyObservers();
    }

    @Override
    public void reset() {
        observable_data.reset();
        setChanged();
        notifyObservers();
    }

    @Override
    public void setNextStateToHalt() {
        observable_data.setNextStateToHalt();
        setChanged();
        notifyObservers();
    }

    @Override
    public void performNextAction() {
        observable_data.performNextAction();
        setChanged();
        notifyObservers();
    }

    @Override
    public int nextActionDuration() {
        return observable_data.nextActionDuration();
    }

    public void setIR(int currentIR) {
        observable_data.setIR(currentIR);
        setChanged();
        notifyObservers();
    }

    public void setToExecuteState() {
        observable_data.setToExecuteState();
        setChanged();
        notifyObservers();
    }
}
