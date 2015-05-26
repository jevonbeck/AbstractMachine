package org.ricts.abstractmachine.ui.storage;

import java.util.List;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.RelativeLayout;

import org.ricts.abstractmachine.R;
import org.ricts.abstractmachine.components.Device;
import org.ricts.abstractmachine.components.interfaces.ReadPort;
import org.ricts.abstractmachine.components.storage.ROM;
import org.ricts.abstractmachine.ui.CustomDimenRecyclerView;
import org.ricts.abstractmachine.ui.device.DevicePin;
import org.ricts.abstractmachine.ui.device.DevicePin.PinDirection;
import org.ricts.abstractmachine.ui.device.HorizontalPinDataView;
import org.ricts.abstractmachine.ui.device.PinDataAdapter;
import org.ricts.abstractmachine.ui.device.VerticalPinDataView;

public class RomView extends RelativeLayout implements ReadPort {
	private static final String TAG = "RomView";
	
	public interface AnimationResponder{
		public void onAnimationFinished();
	}
	
	private AnimationResponder animResponder;
	
	protected int dataWidth;
	protected int addressWidth;
	private int pinPosition;
	
	protected PinDirection inDirection, outDirection;
	protected PinDataAdapter pinAdapter;
	protected DevicePin[] pinArray;
	
	protected MemoryDataAdapter dataAdapter;
	protected ROM rom;
	
	private CustomDimenRecyclerView ramView;
	private int ramItemLayout;
	
	protected enum PinNames{
		COMMAND, ADDRESS, DATA
	}
	
	/** Standard Constructors **/
	public RomView(Context context) {
		super(context);
		
		pinPosition = 1; // right (attrs.xml)
		
		init(context);
	}
	
	public RomView(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		init(context, attrs);
	}
	
	public RomView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		
		init(context, attrs);
	}	
	
	private void init(Context context, AttributeSet attrs){
		/*** extract XML attributes ***/
		TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.RomView);
		pinPosition = a.getInt(R.styleable.RomView_pinPosition, 1);
		a.recycle();
		
		init(context);
	}
	
	private void init(Context context) {
        /*** create children ***/
        CustomDimenRecyclerView pinView;

        /*** determine children layouts and positions based on attributes ***/
        LayoutParams lpRamView = new LayoutParams(
                LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);

        LayoutParams lpPinView = new LayoutParams(
                LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);


        switch (pinPosition) {
            case 2: // top
                ramItemLayout = R.layout.mem_data_vertical;

                ramView = new HorizontalRamDataView(context);
                ramView.setId(R.id.romview_romdata);

                pinView = new HorizontalPinDataView(context);
                pinView.setId(R.id.romview_pindata);

                lpPinView.addRule(RelativeLayout.CENTER_HORIZONTAL);
                addView(pinView, lpPinView);

                lpRamView.addRule(RelativeLayout.BELOW, pinView.getId());
                addView(ramView, lpRamView);

                inDirection = PinDirection.DOWN;
                outDirection = PinDirection.UP;
                break;
            case 3: // bottom
                ramItemLayout = R.layout.mem_data_vertical;

                ramView = new HorizontalRamDataView(context);
                ramView.setId(R.id.romview_romdata);

                pinView = new HorizontalPinDataView(context);
                pinView.setId(R.id.romview_pindata);

                addView(ramView, lpRamView);

                lpPinView.addRule(RelativeLayout.BELOW, ramView.getId());
                lpPinView.addRule(RelativeLayout.CENTER_HORIZONTAL);
                addView(pinView, lpPinView);

                inDirection = PinDirection.UP;
                outDirection = PinDirection.DOWN;
                break;
            case 0: // left
                ramItemLayout = R.layout.mem_data_horizontal;

                ramView = new VerticalRamDataView(context);
                ramView.setId(R.id.romview_romdata);

                pinView = new VerticalPinDataView(context);
                pinView.setId(R.id.romview_pindata);

                lpPinView.addRule(RelativeLayout.CENTER_VERTICAL);
                addView(pinView, lpPinView);

                lpRamView.addRule(RelativeLayout.RIGHT_OF, pinView.getId());
                addView(ramView, lpRamView);

                inDirection = PinDirection.RIGHT;
                outDirection = PinDirection.LEFT;
                break;
            case 1: // right
            default:
                ramItemLayout = R.layout.mem_data_horizontal;

                ramView = new VerticalRamDataView(context);
                ramView.setId(R.id.romview_romdata);

                pinView = new VerticalPinDataView(context);
                pinView.setId(R.id.romview_pindata);

                addView(ramView, lpRamView);

                lpPinView.addRule(RelativeLayout.RIGHT_OF, ramView.getId());
                lpPinView.addRule(RelativeLayout.CENTER_VERTICAL);
                addView(pinView, lpPinView);

                inDirection = PinDirection.LEFT;
                outDirection = PinDirection.RIGHT;
                break;
        }

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

	public void initMemory(int dWidth, int aWidth, int accessTime){
		dataWidth = dWidth;
		addressWidth = aWidth;
		
		// initialise rom (ramView data)
		rom = new ROM(dataWidth, addressWidth, accessTime);
		
		init();
	}

    public void setDataSource(ROM r){
        rom = r;
        dataWidth = rom.dataWidth();
        addressWidth = rom.addressWidth();

        init();
    }

    protected void init(){
        // update pin data (pinView data)
        pinArray[PinNames.ADDRESS.ordinal()].dataWidth = addressWidth;
        pinArray[PinNames.DATA.ordinal()].dataWidth = dataWidth;

        /*** bind memoryView to their data ***/
        try {
            dataAdapter = new MemoryDataAdapter(getContext(), ramItemLayout,
                    R.id.data, rom.dataArray());
        } catch (Exception e) {
            e.printStackTrace();
        }

        dataAdapter.setMemoryParams(dataWidth, addressWidth);
        ramView.setAdapter(dataAdapter);
    }

	public void setMemoryData(List<Integer> data, int addrOffset){
		rom.setData(data, addrOffset);
	}
	
	public void setAnimationResponder(AnimationResponder responder){
		animResponder = responder;
	}
	
	@Override
	public int read(int address) {
		int data = rom.read(address);
		
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
		pin.data = Device.formatNumberInHex(data, dataWidth);
		pin.direction = outDirection;
		pin.action = DevicePin.PinAction.MOVING;
		pin.startBehaviour = DevicePin.AnimStartBehaviour.DELAY;
		pin.animListener = new AnimationListener(){
			@Override
			public void onAnimationEnd(Animation animation){
				if(animResponder != null){
					Log.d(TAG, "animation responder called!");
					animResponder.onAnimationFinished();
				}
			}

			@Override
			public void onAnimationRepeat(Animation arg0) {
				
			}

			@Override
			public void onAnimationStart(Animation arg0) {
				
			}
		};
		
		//scrollToPosition(address); // ensure that address is visible
		pinAdapter.notifyDataSetChanged(); // Animate pin UI
		
		return data; // return actual data to underlying requester
	}

	@Override
	public int accessTime() {
		return rom.accessTime();
	}
}
