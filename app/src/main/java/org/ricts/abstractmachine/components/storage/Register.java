package org.ricts.abstractmachine.components.storage;

import org.ricts.abstractmachine.components.*;
import org.ricts.abstractmachine.components.interfaces.RegisterPort;

public class Register extends Device implements DataDevice, RegisterPort{
  private int dataWord;
  private int dataBitMask;
  private int dataWidth;
  
  public Register(int dWidth){
    super();
    dataWidth = dWidth;
    dataBitMask = bitMaskOfWidth(dWidth);
  }  

  public int read(){
    return dataWord;
  }

  public void write(int data){
    dataWord = data & dataBitMask;
  }

  public int dataWidth(){
    return dataWidth;
  }     
}