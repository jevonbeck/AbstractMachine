package org.ricts.abstractmachine.components.network;

import org.ricts.abstractmachine.components.AddressDevice;
import org.ricts.abstractmachine.components.Device;
import org.ricts.abstractmachine.components.interfaces.MemoryPort;

/**
 * Created by Jevon on 23/06/2015.
 */
public class MemoryPortMultiplexer extends Device implements AddressDevice {
    private int selectWidth, selMask, currentSel;
    private MemoryPortProxy[] inputs;

    public MemoryPortMultiplexer(int selW, MemoryPort out){ // 'out' is the MemoryPort shared by input requesters
        super();
        currentSel = 0;
        selectWidth = selW;
        selMask = bitMaskOfWidth(selectWidth);

        inputs = new MemoryPortProxy[(int) Math.pow(2,selectWidth)];
        for(int x=0; x < inputs.length; ++x){
            inputs[x] = new MemoryPortProxy(out);
        }
    }

    @Override
    public int addressWidth() {
        return selectWidth;
    }

    public void setActiveInput(int sel){
        inputs[currentSel].setEnabled(false);
        currentSel = sel & selMask;
        inputs[currentSel].setEnabled(true);
    }

    public MemoryPort getOutput(){
        return inputs[currentSel];
    }

    public MemoryPort getInput(int input){
        return inputs[input & selMask];
    }

    private static class MemoryPortProxy implements MemoryPort {
        private MemoryPort m_out;
        private boolean enabled = false;

        public MemoryPortProxy(MemoryPort out){
            m_out = out;
        }

        @Override
        public void write(int address, int data) {
            if(enabled) {
                m_out.write(address, data);
            }
        }

        @Override
        public int read(int address) {
            return enabled ? m_out.read(address) : -1;
        }

        @Override
        public int accessTime() {
            return enabled ? m_out.accessTime() : -1;
        }

        public void setEnabled(boolean enable){
            enabled = enable;
        }
    }
}
