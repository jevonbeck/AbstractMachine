package org.ricts.abstractmachine.components.observable;

import org.ricts.abstractmachine.components.interfaces.CuFsmInterface;

/**
 * Created by Jevon on 25/03/2017.
 */

public class ObservableCuFSM extends ObservableType<CuFsmInterface> implements CuFsmInterface {
    public ObservableCuFSM(CuFsmInterface type) {
        super(type);
    }

    @Override
    public void reset() {
        observable_data.reset();
        setChanged();
        notifyObservers(true); // to differentiate that this update is from a reset!
    }

    @Override
    public void triggerStateChange() {
        observable_data.triggerStateChange();
        setChanged();
        notifyObservers();
    }

    @Override
    public void setCurrentState(String state) {
        observable_data.setCurrentState(state);
        setChanged();
        notifyObservers();
    }

    @Override
    public void setNextState(String state) {
        observable_data.setNextState(state);
        setChanged();
        notifyObservers();
    }

    @Override
    public String currentState() {
        return observable_data.currentState();
    }

    @Override
    public boolean isInHaltState() {
        return observable_data.isInHaltState();
    }

    @Override
    public boolean isInSleepState() {
        return observable_data.isInSleepState();
    }

    @Override
    public int nextActionDuration() {
        return observable_data.nextActionDuration();
    }
}
