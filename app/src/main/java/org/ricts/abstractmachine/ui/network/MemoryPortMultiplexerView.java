package org.ricts.abstractmachine.ui.network;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import org.ricts.abstractmachine.components.interfaces.MemoryPort;
import org.ricts.abstractmachine.ui.device.DeviceView;
import org.ricts.abstractmachine.ui.device.MultiPinView;
import org.ricts.abstractmachine.ui.storage.MemoryPortView;

/**
 * Created by Jevon on 07/06/2015.
 */
public class MemoryPortMultiplexerView extends MultiplexerView {
    private enum WidthIndex {
        DATA, ADDRESS
    }

    @Override
    protected MultiPinView createPinView(Context context, int pinPosition) {
        return new MemoryPortView(context, DeviceView.getDefaultAttributeSet(context, pinPosition));
    }

    @Override
    protected void initOutputPinView(View pinView, Integer... pinWidths) {
        MemoryPortView memoryPortPins =  (MemoryPortView) pinView;
        memoryPortPins.initParams(pinWidths[WidthIndex.DATA.ordinal()],
                pinWidths[WidthIndex.ADDRESS.ordinal()]);

        // implementations which use a MultiPinView subclass will probably want to do this
        memoryPortPins.setStartDelay(1);
    }

    @Override
    protected void initInputPinView(View pinView, final PinTrigger pinTrigger, Integer... pinWidths) {
        MemoryPortView memoryPortPins =  (MemoryPortView) pinView;
        memoryPortPins.initParams(pinWidths[WidthIndex.DATA.ordinal()],
                pinWidths[WidthIndex.ADDRESS.ordinal()]);
        memoryPortPins.setReadAnimationDelay(3);

        final MemoryPortView memOutputPins = (MemoryPortView) outputPins;
        memoryPortPins.setSource(new MemoryPort() {
            @Override
            public int read(int address) {
                if (pinTrigger.isSelected()) {
                    pinTrigger.triggerPin();
                    return memOutputPins.read(address);
                }
                return -1;
            }

            @Override
            public int accessTime() {
                return memOutputPins.accessTime();
            }

            @Override
            public void write(int address, int data) {
                if (pinTrigger.isSelected()) {
                    pinTrigger.triggerPin();
                    memOutputPins.write(address, data);
                }
            }
        });
    }

    /** Standard Constructors **/
    public MemoryPortMultiplexerView(Context context) {
        this(context, null);
    }

    public MemoryPortMultiplexerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MemoryPortMultiplexerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setOutputSource(MemoryPort source){
        ((MemoryPortView) outputPins).setSource(source);
    }

    public void initMux(int selW, int dataW, int addrW){
        init(selW, dataW, addrW);
    }
}
