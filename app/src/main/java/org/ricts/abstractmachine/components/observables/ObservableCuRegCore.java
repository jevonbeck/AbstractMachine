package org.ricts.abstractmachine.components.observables;

import org.ricts.abstractmachine.components.compute.cu.CuRegCore;
import org.ricts.abstractmachine.components.interfaces.ControlUnitRegCore;

/**
 * Created by Jevon on 11/03/2017.
 */

public class ObservableCuRegCore extends ObservableType<CuRegCore> implements ControlUnitRegCore {
    public ObservableCuRegCore(CuRegCore type) {
        super(type);
    }

    @Override
    public void fetchInstruction() {
        observable_data.fetchInstruction();
        setChanged();
        notifyObservers();
    }

    @Override
    public void setPC(int currentPC) {
        observable_data.setPC(currentPC);
        setChanged();
        notifyObservers();
    }

    @Override
    public void setPcAndIr(int currentPC, int currentIR) {
        observable_data.setPcAndIr(currentPC, currentIR);
        setChanged();
        notifyObservers();
    }

    @Override
    public void updatePcWithExpectedValues() {
        observable_data.updatePcWithExpectedValues();
        setChanged();
        notifyObservers();
    }

    @Override
    public int fetchTime() {
        return observable_data.fetchTime();
    }

    @Override
    public int getPC() {
        return observable_data.getPC();
    }

    @Override
    public int getIR() {
        return observable_data.getIR();
    }

    @Override
    public String getPCDataString() {
        return observable_data.getPCDataString();
    }

    @Override
    public String getIRDataString() {
        return observable_data.getIRDataString();
    }
}
