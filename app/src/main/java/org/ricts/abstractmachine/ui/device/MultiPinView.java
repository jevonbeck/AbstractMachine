package org.ricts.abstractmachine.ui.device;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

import org.ricts.abstractmachine.R;
import org.ricts.abstractmachine.ui.utils.CustomDimenRecyclerView;

/**
 * Created by Jevon on 18/12/2015.
 */
public class MultiPinView extends RelativeLayout {
    protected final DevicePin.PinDirection inDirection, outDirection;
    protected DevicePin[] pinArray;
    protected int startDelay;

    private PinDataAdapter pinAdapter;
    private RelativePosition pinPosition;
    private CustomDimenRecyclerView pinView;

    public MultiPinView(Context context) {
        this(context, null);
    }

    public MultiPinView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MultiPinView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        /*** extract XML attributes ***/
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.DeviceView);
        int position = a.getInt(R.styleable.DeviceView_pinPosition, RelativePosition.RIGHT.ordinal());
        a.recycle();

        /*** create child and determine View properties based on attributes ***/
        RelativeLayout.LayoutParams lpPinView = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);

        pinPosition = RelativePosition.getPositionFromInt(position);
        switch (pinPosition) {
            case TOP:
                pinView = new HorizontalPinDataView(context);

                inDirection = DevicePin.PinDirection.DOWN;
                outDirection = DevicePin.PinDirection.UP;
                break;
            case BOTTOM:
                pinView = new HorizontalPinDataView(context);

                inDirection = DevicePin.PinDirection.UP;
                outDirection = DevicePin.PinDirection.DOWN;
                break;
            case LEFT:
                pinView = new VerticalPinDataView(context);

                inDirection = DevicePin.PinDirection.RIGHT;
                outDirection = DevicePin.PinDirection.LEFT;
                break;
            case RIGHT:
            default:
                pinView = new VerticalPinDataView(context);

                inDirection = DevicePin.PinDirection.LEFT;
                outDirection = DevicePin.PinDirection.RIGHT;
                break;
        }
        addView(pinView, lpPinView);
    }

    public void setPinData(DevicePin[] pinData){
        try {
            pinArray = pinData;
            pinAdapter = new PinDataAdapter(pinArray, pinPosition);
        } catch (Exception e) {
            e.printStackTrace();
        }
        pinView.setAdapter(pinAdapter);
    }

    public void updateView(){
        pinAdapter.notifyDataSetChanged(); // Animate pin UI
    }

    public void setStartDelayByMultiple(int delayMultiple){
        startDelay = getDelay(delayMultiple);
    }

    public void setStartDelay(int delay){
        startDelay = delay;
    }

    protected int getDelay(int multiple){
        return multiple * getContext().getResources().getInteger(R.integer.pin_sig_trans_time);
    }
}
