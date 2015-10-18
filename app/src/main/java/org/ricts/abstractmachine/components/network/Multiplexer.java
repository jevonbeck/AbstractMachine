package org.ricts.abstractmachine.components.network;

import org.ricts.abstractmachine.components.AddressDevice;
import org.ricts.abstractmachine.components.Device;
import org.ricts.abstractmachine.ui.device.DevicePin;

/**
 * Created by Jevon on 28/05/2015.
 */
public class Multiplexer extends Device implements AddressDevice {
    private int selectWidth, selMask, currentSel;
    private DevicePin[][] inputs;

    public Multiplexer(int selW, DevicePin[] portTemplate){
        super();
        currentSel = 0;
        selectWidth = selW;
        selMask = bitMaskOfWidth(selectWidth);

        inputs = new DevicePin[(int) Math.pow(2,selectWidth)][];
        for(int x=0; x!= inputs.length; ++x){
            inputs[x] = new DevicePin[portTemplate.length];
            for(int y=0; y!= portTemplate.length; ++y){
                inputs[x][y] = new DevicePin();
                inputs[x][y].name = portTemplate[y].name;
                inputs[x][y].dataWidth = portTemplate[y].dataWidth;
            }
        }
    }

    @Override
    public int addressWidth() {
        return selectWidth;
    }

    public void setSelection(int sel){
        currentSel = sel & selMask;
    }

    public final DevicePin[][] getInputs(){
        return inputs;
    }

    public final DevicePin[] getOutput(){
        return inputs[currentSel];
    }
}
