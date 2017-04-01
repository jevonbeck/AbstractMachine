package org.ricts.abstractmachine.components.interfaces;

public interface MemoryPort extends ReadPort{
    void write(int address, int data);

    String dataString(int data);
}