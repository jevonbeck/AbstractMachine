package org.ricts.abstractmachine.components.observables;

import org.ricts.abstractmachine.components.interfaces.ReadPort;
import org.ricts.abstractmachine.components.storage.ROM;

import java.util.List;

/**
 * Created by Jevon on 16/01/2016.
 */
public class ObservableROM<T extends ROM> extends ObservableType<T> implements ReadPort {
    public static class ReadParams extends ObservableROM.Params{
        protected enum Args{
            ADDRESS
        }

        public ReadParams(Object... a){
            super(a);
        }

        public int getAddress(){
            return (Integer) params[Args.ADDRESS.ordinal()];
        }
    }

    public ObservableROM(T rom){
        super(rom);
    }

    @Override
    public int read(int address) {
        setChanged();
        notifyObservers(new ReadParams(address));
        return observable_data.read(address);
    }

    @Override
    public int accessTime() {
        return observable_data.accessTime();
    }

    public void setData(List<Integer> data, int addrOffset) {
        observable_data.setData(data, addrOffset);
        setChanged();
        notifyObservers(true);
    }
}
