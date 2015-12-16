package org.ricts.abstractmachine.datastructures;

import org.ricts.abstractmachine.components.interfaces.MemoryPort;

public class Stack {
    private MemoryPort storage;
    private int startAddress;
    private int stackSize;
    private int currentIndex;

    public Stack(MemoryPort ram, int strtAddr, int stkSize){
        storage = ram;
        resizeStack(stkSize, strtAddr);
    }

    public Stack(MemoryPort ram){
        storage = ram;
    }

    public void push(int data){
        if(!isFull()){
            storage.write(startAddress + currentIndex, data);
            ++currentIndex;
        }
    }

    public int pop(){
        if(!isEmpty()){
            --currentIndex;
            return storage.read(startAddress + currentIndex);
        }
        return 0;
    }

    public boolean isFull(){
        return currentIndex == stackSize;
    }

    public boolean isEmpty(){
        return currentIndex == startAddress;
    }

    public void loadStackFrom(MemoryPort storageSpace, int memStartAddress, int memStackSize){
        for(int x=0; x != memStackSize; ++x){
            push(storageSpace.read(memStartAddress + x));
        }
    }

    public void storeStackTo(MemoryPort freeSpace, int memStartAddress){
        for(int x=0; x != currentIndex; ++x){
            freeSpace.write(memStartAddress + x, storage.read(startAddress + x));
        }
        currentIndex = 0; // reset stack
    }

    public void resizeStack(int stkSize, int strtAddr){
        currentIndex = 0;
        startAddress = strtAddr;
        stackSize = stkSize;
    }
}