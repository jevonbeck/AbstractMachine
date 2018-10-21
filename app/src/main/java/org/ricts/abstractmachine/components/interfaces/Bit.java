package org.ricts.abstractmachine.components.interfaces;

public interface Bit {
    boolean read();
    void write(boolean value);

    void set();
    void clear();
}
