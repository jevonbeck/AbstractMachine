package org.ricts.abstractmachine.components.observables;

import org.ricts.abstractmachine.components.interfaces.MultiMemoryPort;

/**
 * Created by Jevon on 14/01/2017.
 */

public class ObservableMultiMemoryPort extends ObservableType<MultiMemoryPort> implements MultiMemoryPort {
    public static class MemoryPortParams extends Params{
        protected enum Args{
            INDEX, ADDRESS, DATA
        }

        public MemoryPortParams(Object... a){
            super(a);
        }

        public int getPortId(){
            return (Integer) params[Args.INDEX.ordinal()];
        }

        public int getAddress(){
            return (Integer) params[Args.ADDRESS.ordinal()];
        }

        public int getData(){
            return (Integer) params[Args.DATA.ordinal()];
        }

        public boolean hasData() {
            return params.length > 2;
        }
    }

    public ObservableMultiMemoryPort(MultiMemoryPort type) {
        super(type);
    }

    @Override
    public int read(int portId, int address) {
        setChanged();
        notifyObservers(new MemoryPortParams(portId, address));
        return observable_data.read(portId, address);
    }

    @Override
    public void write(int portId, int address, int data) {
        observable_data.write(portId, address, data);
        setChanged();
        notifyObservers(new MemoryPortParams(portId, address, data));
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

    @Override
    public String dataString(int data) {
        return observable_data.dataString(data);
    }
}
