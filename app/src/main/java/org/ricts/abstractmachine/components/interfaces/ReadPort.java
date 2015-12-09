package org.ricts.abstractmachine.components.interfaces;

public interface ReadPort {
    int read(int address);
    int accessTime(); // in microseconds
}