package org.ricts.abstractmachine.components.interfaces;

public interface MemoryPort extends ReadPort{ 
  public void write(int address, int data);
}