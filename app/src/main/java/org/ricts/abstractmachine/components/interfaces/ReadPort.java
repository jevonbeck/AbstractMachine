package org.ricts.abstractmachine.components.interfaces;

public interface ReadPort {
    int read(int address);
    int accessTime(); // in microseconds

    String addressString(int address);
    String dataAtAddressString(int address);
}