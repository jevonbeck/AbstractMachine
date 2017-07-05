package org.ricts.abstractmachine.components.storage;

import org.ricts.abstractmachine.components.devicetype.DataDevice;
import org.ricts.abstractmachine.components.devicetype.Device;

/**
 * Created by Jevon on 19/06/2017.
 */

public class CircularBuffer extends Device implements DataDevice {
    private RAM storage;
    private int stackWidth, stackSize;
    private int currentIndex;

    public CircularBuffer(int stkWidth, int stkSize){
        stackWidth = stkWidth;
        storage = new RAM(stackWidth, bitWidth(stkSize), 0);

        currentIndex = 0;
        stackSize = stkSize;
    }

    @Override
    public int dataWidth() {
        return stackWidth;
    }

    public void push(int data){
        storage.write(currentIndex, data);
        ++currentIndex;
        if(currentIndex == stackSize) {
            currentIndex = 0;
        }
    }

    public int pop(){
        --currentIndex;
        if(currentIndex == -1) {
            currentIndex = stackSize - 1;
        }

        return storage.read(currentIndex);
    }
}
