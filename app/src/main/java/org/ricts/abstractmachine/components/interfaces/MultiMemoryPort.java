package org.ricts.abstractmachine.components.interfaces;

/**
 * Created by Jevon on 14/01/2017.
 */

public interface MultiMemoryPort {
    int read(int portId, int address);
    void write(int portId, int address, int data);
    int accessTime();

    String addressString(int address);
    String dataAtAddressString(int address);
    String dataString(int data);
}
