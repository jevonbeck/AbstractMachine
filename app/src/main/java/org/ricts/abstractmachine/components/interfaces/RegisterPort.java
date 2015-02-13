package org.ricts.abstractmachine.components.interfaces;

public interface RegisterPort {
	public int read();
	public void write(int data);
}
