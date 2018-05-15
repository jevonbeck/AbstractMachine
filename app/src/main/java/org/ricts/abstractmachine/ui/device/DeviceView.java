package org.ricts.abstractmachine.ui.device;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;

import org.ricts.abstractmachine.R;
import org.ricts.abstractmachine.ui.utils.UiUtils;

/**
 * Created by Jevon on 18/12/2015.
 */
public abstract class DeviceView extends RelativeLayout {
    protected View mainView, pinView;

    protected abstract View createPinView(Context context, RelativePosition position);
    protected abstract View createMainView(Context context, RelativePosition position);
    protected abstract LayoutParams createMainViewLayoutParams();

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
        int position = a.getInt(R.styleable.DeviceView_pinPosition, RelativePosition.RIGHT.ordinal());
        int alignment = a.getInt(R.styleable.DeviceView_pinBodyAlignment, PinBodyAlignment.CENTER.ordinal());
        a.recycle();

        /*** create children and determine layouts & positions based on attributes ***/
        LayoutParams lpMainView = createMainViewLayoutParams();

        LayoutParams lpPinView = new LayoutParams(
                LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);

        RelativePosition pinPosition = RelativePosition.getPositionFromInt(position);
        PinBodyAlignment bodyAlignment = PinBodyAlignment.getAlignmentFromInt(alignment);
        pinView = createPinView(context, pinPosition);
        pinView.setId(R.id.DeviceView_pin_view);

        mainView = createMainView(context, pinPosition);
        mainView.setId(R.id.DeviceView_main_view);

        switch (pinPosition) {
            case TOP:
                if(bodyAlignment == PinBodyAlignment.CENTER) {
                    lpPinView.addRule(RelativeLayout.CENTER_HORIZONTAL);
                }
                else if(bodyAlignment == PinBodyAlignment.END) {
                    lpPinView.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                }
                addView(pinView, lpPinView);

                lpMainView.addRule(RelativeLayout.BELOW, pinView.getId());
                addView(mainView, lpMainView);
                break;
            case BOTTOM:
                addView(mainView, lpMainView);

                lpPinView.addRule(RelativeLayout.BELOW, mainView.getId());
                if(bodyAlignment == PinBodyAlignment.CENTER) {
                    lpPinView.addRule(RelativeLayout.CENTER_HORIZONTAL);
                }
                else if(bodyAlignment == PinBodyAlignment.END) {
                    lpPinView.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                }
                addView(pinView, lpPinView);
                break;
            case LEFT:
                if(bodyAlignment == PinBodyAlignment.CENTER) {
                    lpPinView.addRule(RelativeLayout.CENTER_VERTICAL);
                }
                else if(bodyAlignment == PinBodyAlignment.END) {
                    lpPinView.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                }
                addView(pinView, lpPinView);

                lpMainView.addRule(RelativeLayout.RIGHT_OF, pinView.getId());
                addView(mainView, lpMainView);
                break;
            case RIGHT:
            default:
                addView(mainView, lpMainView);

                lpPinView.addRule(RelativeLayout.RIGHT_OF, mainView.getId());
                if(bodyAlignment == PinBodyAlignment.CENTER) {
                    lpPinView.addRule(RelativeLayout.CENTER_VERTICAL);
                }
                else if(bodyAlignment == PinBodyAlignment.END) {
                    lpPinView.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                }
                addView(pinView, lpPinView);
                break;
        }
    }

    public static AttributeSet getDefaultAttributeSet(Context context, RelativePosition pinPosition){
        return UiUtils.makeAttributeSet(context, getDefaultResourceId(pinPosition));
    }

    private static int getDefaultResourceId(RelativePosition pinPosition){
        switch (pinPosition){
            case TOP:
                return R.xml.deviceview_pins_top;
            case BOTTOM:
                return R.xml.deviceview_pins_bottom;
            case LEFT:
                return R.xml.deviceview_pins_left;
            case RIGHT:
            default:
                return R.xml.deviceview_pins_right;
        }
    }
}
