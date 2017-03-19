package org.ricts.abstractmachine.components.interfaces;

import java.util.List;

public interface ReadPort {
    int read(int address);
    void setData(List<Integer> data);

    int accessTime(); // in microseconds
    String addressString(int address);
    String dataAtAddressString(int address);
}