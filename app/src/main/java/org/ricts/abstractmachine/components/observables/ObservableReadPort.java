package org.ricts.abstractmachine.components.observables;

import org.ricts.abstractmachine.components.interfaces.ReadPort;

/**
 * Created by Jevon on 14/02/2017.
 */
public class ObservableReadPort<T extends ReadPort> extends ObservableType<T> implements ReadPort {
    public static class ReadParams extends Params{
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

    public ObservableReadPort(T readPort){
        super(readPort);
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

    @Override
    public String addressString(int address) {
        return observable_data.addressString(address);
    }

    @Override
    public String dataAtAddressString(int address) {
        return observable_data.dataAtAddressString(address);
    }
}
