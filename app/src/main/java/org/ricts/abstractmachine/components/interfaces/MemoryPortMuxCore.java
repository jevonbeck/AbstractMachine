package org.ricts.abstractmachine.components.interfaces;

/**
 * Created by Jevon on 14/01/2017.
 */

public interface MemoryPortMuxCore {
    int read(int portId, int address);
    void write(int portId, int address, int data);
}
