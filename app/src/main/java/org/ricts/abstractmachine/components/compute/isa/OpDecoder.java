package org.ricts.abstractmachine.components.compute.isa;

import org.ricts.abstractmachine.components.devicetype.DataDevice;
import org.ricts.abstractmachine.components.devicetype.Device;

public class OpDecoder extends Device implements DataDevice {
	private int dataWidth;
	private int dataBitMask;
	private int offset;
		
	public OpDecoder(int dWidth, int off){
		offset = off;
		updateDataWidth(dWidth);
	}
	
	public int getOpFrom(int instruction){
		return (instruction & dataBitMask) >>> offset;
	}
		
	public int putOpIn(int instruction, int op){
		return (instruction & ~dataBitMask) | ((op << offset) & dataBitMask);
	}
	
	public int dataWidth(){
		return dataWidth;
	}
	
	public void updateDataWidth(int dWidth){
		dataWidth = dWidth;
		dataBitMask = bitMaskOfWidth(dataWidth) << offset;
	}
}
