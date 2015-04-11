package org.ricts.abstractmachine.components.storage;

import java.util.List;

import org.ricts.abstractmachine.components.*;
import org.ricts.abstractmachine.components.interfaces.ReadPort;

public class ROM extends Device implements AddressDevice, DataDevice, ReadPort {
    protected int[] dataArray;
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

        dataArray = new int[(int) Math.pow(2, addressWidth)];
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

    public int[] dataArray() {
        return dataArray;
    }

    public void setData(List<Integer> data, int addrOffset) {
        int dataSize = data.size();
        int offset = addrOffset & addressBitMask;
        int addrLimit = Math.min((int) Math.pow(2, addressWidth), offset + dataSize);

        for (int index = 0, address = offset; address < addrLimit; ++index, ++address) {
            dataArray[address] = data.get(index) & dataBitMask;
        }
    }
}