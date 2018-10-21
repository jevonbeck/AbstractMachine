package org.ricts.abstractmachine.components.storage;

import org.ricts.abstractmachine.components.devicetype.AddressDevice;
import org.ricts.abstractmachine.components.devicetype.DataDevice;
import org.ricts.abstractmachine.components.devicetype.Device;
import org.ricts.abstractmachine.components.interfaces.Register;
import org.ricts.abstractmachine.components.interfaces.RegisterFile;

import java.util.Map;

public abstract class AbstractRegisterFile extends Device implements RegisterFile, DataDevice, AddressDevice {
    private int dataWidth, addressWidth;
    private int addressBitMask;
    private Map<String, Integer> addressMap;
    private Register[] registers;

    protected abstract Map<String, Integer> createAddressMap();

    public AbstractRegisterFile(int dWidth, int addrWidth) {
        dataWidth = dWidth;
        addressWidth = addrWidth;
        addressBitMask = bitMaskOfWidth(addrWidth);

        addressMap = createAddressMap();

        registers = new Register[1 << addressWidth];
        populateRegisterArray(registers, dataWidth);
    }

    @Override
    public int read(int address) {
        return getRegisterAt(address).read();
    }

    @Override
    public void write(int address, int data) {
        getRegisterAt(address).write(data);
    }

    @Override
    public int read(String addressName) {
        Register register = getRegisterByName(addressName);
        return (register != null) ? register.read() : -1;
    }

    @Override
    public void write(String addressName, int data) {
        Register register = getRegisterByName(addressName);
        if(register != null) {
            register.write(data);
        }
    }

    @Override
    public Register getRegisterAt(int address) {
        return registers[address & addressBitMask];
    }

    @Override
    public Register getRegisterByName(String name) {
        return addressMap.containsKey(name) ? getRegisterAt(addressMap.get(name)) : null;
    }

    @Override
    public Map<String, Integer> getAddressMap() {
        return addressMap;
    }

    @Override
    public int dataWidth() {
        return dataWidth;
    }

    @Override
    public int addressWidth() {
        return addressWidth;
    }

    protected void populateRegisterArray(Register[] registers, int dataWidth) {
        for (int x = 0; x < registers.length; ++x) {
            registers[x] = new RegisterImpl(dataWidth);
        }
    }
}
