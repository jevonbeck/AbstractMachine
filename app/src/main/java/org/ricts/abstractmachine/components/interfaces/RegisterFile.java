package org.ricts.abstractmachine.components.interfaces;


import java.util.Map;

public interface RegisterFile {
    int read(int address);
    void write(int address, int data);

    int read(String addressName);
    void write(String addressName, int data);

    Register getRegisterAt(int address);
    Register getRegisterByName(String name);
    Map<String, Integer> getAddressMap();
}
