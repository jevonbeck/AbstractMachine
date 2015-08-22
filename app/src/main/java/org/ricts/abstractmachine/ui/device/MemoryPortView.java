package org.ricts.abstractmachine.ui.device;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.animation.Animation;
import android.widget.RelativeLayout;

import org.ricts.abstractmachine.R;
import org.ricts.abstractmachine.components.Device;
import org.ricts.abstractmachine.components.interfaces.MemoryPort;
import org.ricts.abstractmachine.ui.CustomDimenRecyclerView;

/**
 * Created by Jevon on 21/08/2015.
 */
public class MemoryPortView extends RelativeLayout implements MemoryPort {
    public interface ReadResponder{
        void onReadFinished();
    }
    public interface WriteResponder{
        void onWriteFinished();
    }

    private int dataWidth, addressWidth, access, readData;

    private ReadResponder readResponder;
    private WriteResponder writeResponder;

    protected DevicePin.PinDirection inDirection, outDirection;
    protected PinDataAdapter pinAdapter;
    protected DevicePin[] pinArray;

    protected enum PinNames{
        COMMAND, ADDRESS, DATA
    }

    /** Standard Constructors **/
    public MemoryPortView(Context context) {
        this(context, null);
    }

    public MemoryPortView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MemoryPortView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        /*** extract XML attributes ***/
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.MemoryPortView);
        int pinPosition = a.getInt(R.styleable.MemoryPortView_devicePosition, 1);
        a.recycle();

        /*** create child and determine View properties based on attributes ***/
        RelativeLayout.LayoutParams lpPinView = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);

        CustomDimenRecyclerView pinView;
        switch (pinPosition) {
            case 2: // top
                pinView = new HorizontalPinDataView(context);

                inDirection = DevicePin.PinDirection.DOWN;
                outDirection = DevicePin.PinDirection.UP;
                break;
            case 3: // bottom
                pinView = new HorizontalPinDataView(context);

                inDirection = DevicePin.PinDirection.UP;
                outDirection = DevicePin.PinDirection.DOWN;
                break;
            case 0: // left
                pinView = new VerticalPinDataView(context);

                inDirection = DevicePin.PinDirection.RIGHT;
                outDirection = DevicePin.PinDirection.LEFT;
                break;
            case 1: // right
            default:
                pinView = new VerticalPinDataView(context);

                inDirection = DevicePin.PinDirection.LEFT;
                outDirection = DevicePin.PinDirection.RIGHT;
                break;
        }
        pinView.setId(R.id.romview_pindata);
        addView(pinView, lpPinView);

        /*** create pin child data ***/
        // initialise pin names (pinView data)
        pinArray = new DevicePin[PinNames.values().length];
        DevicePin pin = new DevicePin();
        pin.name = "command";
        pin.dataWidth = 2;
        pinArray[PinNames.COMMAND.ordinal()] = pin;

        pin = new DevicePin();
        pin.name = "address";
        pinArray[PinNames.ADDRESS.ordinal()] = pin;

        pin = new DevicePin();
        pin.name = "data";
        pinArray[PinNames.DATA.ordinal()] = pin;

        /*** bind pin child to its data ***/
        try {
            pinAdapter = new PinDataAdapter(pinArray, pinPosition);
        } catch (Exception e) {
            e.printStackTrace();
        }
        pinView.setAdapter(pinAdapter);
    }

    @Override
    public void write(int address, int data) {
        // Setup correct data in pin UI
        DevicePin pin = pinArray[PinNames.ADDRESS.ordinal()];
        pin.data = Device.formatNumberInHex(address, addressWidth);
        pin.direction = inDirection;
        pin.action = DevicePin.PinAction.MOVING;

        pin = pinArray[PinNames.DATA.ordinal()];
        pin.data = Device.formatNumberInHex(data, dataWidth);
        pin.direction = inDirection;
        pin.action = DevicePin.PinAction.MOVING;
        pin.startBehaviour = DevicePin.AnimStartBehaviour.IMMEDIATE;
        pin.animListener = null;

        pin = pinArray[PinNames.COMMAND.ordinal()];
        pin.data = "write";
        pin.direction = inDirection;
        pin.action = DevicePin.PinAction.MOVING;

        pin.animListener = new Animation.AnimationListener(){
            @Override
            public void onAnimationEnd(Animation animation) {
                if(writeResponder != null){
                    writeResponder.onWriteFinished(); // - RamView and RomView should update rom data UI
                }
            }

            @Override
            public void onAnimationRepeat(Animation arg0) {

            }

            @Override
            public void onAnimationStart(Animation arg0) {

            }
        };

        pinAdapter.notifyDataSetChanged(); // Animate pin UI
    }

    @Override
    public int read(int address) {
        // Setup correct data in pin UI
        DevicePin pin = pinArray[PinNames.COMMAND.ordinal()];
        pin.data = "read";
        pin.direction = inDirection;
        pin.action = DevicePin.PinAction.MOVING;
        pin.animListener = null;

        pin = pinArray[PinNames.ADDRESS.ordinal()];
        pin.data = Device.formatNumberInHex(address, addressWidth);
        pin.direction = inDirection;
        pin.action = DevicePin.PinAction.MOVING;

        pin = pinArray[PinNames.DATA.ordinal()];
        pin.data = Device.formatNumberInHex(readData, dataWidth);
        pin.direction = outDirection;
        pin.action = DevicePin.PinAction.MOVING;
        pin.startBehaviour = DevicePin.AnimStartBehaviour.DELAY;
        pin.animListener = new Animation.AnimationListener(){
            @Override
            public void onAnimationEnd(Animation animation){
                if(readResponder != null){
                    readResponder.onReadFinished();
                }
            }

            @Override
            public void onAnimationRepeat(Animation arg0) {

            }

            @Override
            public void onAnimationStart(Animation arg0) {

            }
        };

        pinAdapter.notifyDataSetChanged(); // Animate pin UI

        return readData; // return actual data to underlying requester
    }

    @Override
    public int accessTime() {
        return access;
    }

    public void initParams(int dWidth, int aWidth, int accessTime){
        dataWidth = dWidth;
        addressWidth = aWidth;
        access = accessTime;

        // update pin data (pinView data)
        pinArray[PinNames.ADDRESS.ordinal()].dataWidth = addressWidth;
        pinArray[PinNames.DATA.ordinal()].dataWidth = dataWidth;
    }

    public void setReadResponder(ReadResponder responder){
        readResponder = responder;
    }

    public void setWriteResponder(WriteResponder responder){
        writeResponder = responder;
    }

    public void setReadData(int data){
        readData = data;
    }
}
