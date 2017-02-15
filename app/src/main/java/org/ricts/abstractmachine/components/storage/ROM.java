package org.ricts.abstractmachine.components.storage;

import java.util.List;

import org.ricts.abstractmachine.components.devicetype.AddressDevice;
import org.ricts.abstractmachine.components.devicetype.DataDevice;
import org.ricts.abstractmachine.components.devicetype.Device;
import org.ricts.abstractmachine.components.interfaces.ReadPort;

public class ROM extends Device implements AddressDevice, DataDevice, ReadPort {
    protected int[] dataArray;
    protected int addressBitMask;
    protected int dataBitMask;
    protected int dataWidth;
    private int addressWidth;
    private int accessTime;

    public ROM(int dWidth, int aWidth, int access) {
        super();
        accessTime = access;

        dataWidth = dWidth;
        dataBitMask = bitMaskOfWidth(dWidth);

        addressWidth = aWidth;
        addressBitMask = bitMaskOfWidth(aWidth);

        dataArray = new int[1 << addressWidth]; // array size is 2^addressWidth
    }

    @Override
    public int addressWidth() {
        return addressWidth;
    }

    @Override
    public int dataWidth() {
        return dataWidth;
    }

    @Override
    public int read(int address) {
        return dataArray[address & addressBitMask];
    }

    @Override
    public int accessTime() {
        return accessTime;
    }

    @Override
    public String addressString(int address){
        return formatNumberInHex(address, addressWidth);
    }

    @Override
    public String dataAtAddressString(int address){
        return formatNumberInHex(read(address), dataWidth);
    }

    public void setData(List<Integer> data, int addrOffset) {
        int dataSize = data.size();
        int offset = addrOffset & addressBitMask;

        // limit is min of address full range and offset data size
        int addrLimit = Math.min(1 << addressWidth, offset + dataSize);

        for (int index = 0, address = offset; address < addrLimit; ++index, ++address) {
            dataArray[address] = data.get(index) & dataBitMask;
        }
    }

    public int size(){
        return dataArray.length;
    }
}