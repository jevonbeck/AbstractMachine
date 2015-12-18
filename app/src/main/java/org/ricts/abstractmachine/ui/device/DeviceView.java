package org.ricts.abstractmachine.ui.device;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;

import org.ricts.abstractmachine.R;

/**
 * Created by Jevon on 18/12/2015.
 */
public abstract class DeviceView extends RelativeLayout {
    protected View mainView, pinView;

    protected abstract View createPinView(Context context, int pinPosition);
    protected abstract View createMainView(Context context, int pinPosition);

    /** Standard Constructors **/
    public DeviceView(Context context) {
        this(context, null);
    }

    public DeviceView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DeviceView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        /*** extract XML attributes ***/
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.DeviceView);
        int pinPosition = a.getInt(R.styleable.DeviceView_pinPosition, 1);
        a.recycle();

        /*** create children and determine layouts & positions based on attributes ***/
        LayoutParams lpMainView = new LayoutParams(
                LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);

        LayoutParams lpPinView = new LayoutParams(
                LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);

        pinView = createPinView(context, pinPosition);
        pinView.setId(R.id.DeviceView_pin_view);

        mainView = createMainView(context, pinPosition);
        mainView.setId(R.id.DeviceView_main_view);

        switch (pinPosition) {
            case 2: // top
                lpPinView.addRule(RelativeLayout.CENTER_HORIZONTAL);
                addView(pinView, lpPinView);

                lpMainView.addRule(RelativeLayout.BELOW, pinView.getId());
                addView(mainView, lpMainView);
                break;
            case 3: // bottom
                addView(mainView, lpMainView);

                lpPinView.addRule(RelativeLayout.BELOW, mainView.getId());
                lpPinView.addRule(RelativeLayout.CENTER_HORIZONTAL);
                addView(pinView, lpPinView);
                break;
            case 0: // left
                lpPinView.addRule(RelativeLayout.CENTER_VERTICAL);
                addView(pinView, lpPinView);

                lpMainView.addRule(RelativeLayout.RIGHT_OF, pinView.getId());
                addView(mainView, lpMainView);
                break;
            case 1: // right
            default:
                addView(mainView, lpMainView);

                lpPinView.addRule(RelativeLayout.RIGHT_OF, mainView.getId());
                lpPinView.addRule(RelativeLayout.CENTER_VERTICAL);
                addView(pinView, lpPinView);
                break;
        }
    }

    public static int getDefaultResourceId(int pinPosition){
        switch (pinPosition){
            case 2: // top
                return R.xml.deviceview_pins_top;
            case 3: // bottom
                return R.xml.deviceview_pins_bottom;
            case 0: // left
                return R.xml.deviceview_pins_left;
            case 1: // right
            default:
                return R.xml.deviceview_pins_right;
        }
    }
}
