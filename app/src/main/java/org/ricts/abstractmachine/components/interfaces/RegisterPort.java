package org.ricts.abstractmachine.components.interfaces;

public interface RegisterPort {
	int read();
	void write(int data);
}
