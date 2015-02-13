package org.ricts.abstractmachine.components.storage;

import java.util.List;

import org.ricts.abstractmachine.components.*;
import org.ricts.abstractmachine.components.interfaces.ReadPort;

public class ROM extends Device implements AddressDevice, DataDevice, ReadPort {
    protected Integer[] dataArray;
    protected int addressBitMask;
    protected int dataBitMask;
    private int addressWidth;
    private int dataWidth;
    private int accessTime;

    public ROM(int dWidth, int aWidth, int access) {
        super();
        accessTime = access;

        dataWidth = dWidth;
        dataBitMask = bitMaskOfWidth(dWidth);

        addressWidth = aWidth;
        addressBitMask = bitMaskOfWidth(aWidth);

        dataArray = new Integer[(int) Math.pow(2, addressWidth)];

        Integer zeroRef = Integer.valueOf(0);
        for (int x = 0; x != dataArray.length; ++x) {
            dataArray[x] = zeroRef;
        }
    }

    public Integer[] dataArray() {
        return dataArray;
    }

    public int addressWidth() {
        return addressWidth;
    }

    public int dataWidth() {
        return dataWidth;
    }

    public int read(int address) {
        return dataArray[address & addressBitMask].intValue();
    }

    public int accessTime() {
        return accessTime;
    }

    public void setData(List<Integer> data, int addrOffset) {
        int dataSize = data.size();
        int offset = addrOffset & addressBitMask;
        int addrLimit = Math.min((int) Math.pow(2, addressWidth), offset + dataSize);

        //for(int index = 0, address = offset; index < dataSize && address < addrLimit; ++index, ++address){
        for (int index = 0, address = offset; address < addrLimit; ++index, ++address) {
            dataArray[address] = Integer.valueOf(data.get(index).intValue() & dataBitMask);
        }
    }
}