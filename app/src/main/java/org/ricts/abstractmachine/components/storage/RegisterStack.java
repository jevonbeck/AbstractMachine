package org.ricts.abstractmachine.components.storage;

import org.ricts.abstractmachine.components.devicetype.DataDevice;
import org.ricts.abstractmachine.components.devicetype.Device;

public class RegisterStack extends Device implements DataDevice {
    private RAM storage;
    private int stackWidth, stackSize;
    private int currentIndex;

    public RegisterStack(int stkWidth, int stkSize){
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
        if(!isFull()){
            storage.write(currentIndex, data);
            ++currentIndex;
        }
    }

    public int pop(){
        if(!isEmpty()){
            --currentIndex;
            return storage.read(currentIndex);
        }
        return 0;
    }

    public boolean isFull(){
        return currentIndex == stackSize;
    }

    public boolean isEmpty(){
        return currentIndex == 0;
    }
}