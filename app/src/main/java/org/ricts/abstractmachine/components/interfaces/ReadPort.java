package org.ricts.abstractmachine.components.interfaces;

public interface ReadPort {
    public int read(int address);
    public int accessTime(); // in microseconds
}