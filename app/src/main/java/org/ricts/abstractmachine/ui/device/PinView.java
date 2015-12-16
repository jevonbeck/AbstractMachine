package org.ricts.abstractmachine.ui.device;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.ricts.abstractmachine.R;
import org.ricts.abstractmachine.ui.VerticalTextView;

/**
 * Created by LXQL2591 on 20/05/2015.
 */
public class PinView extends RelativeLayout {
    private TextView pinNameView, signalTextView;

    private boolean isHorizontal;
    /** Standard Constructors **/
    public PinView(Context context) {
        this(context, null);
    }

    public PinView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PinView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        /*** extract XML attributes ***/
        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.PinView);
        boolean nameBelow = a.getBoolean(R.styleable.PinView_nameBelow, false);
        int orientation = a.getInt(R.styleable.PinView_orientation, 0);
        int position = a.getInt(R.styleable.PinView_position, -1);
        isHorizontal = orientation == 0;
        a.recycle();

        /*** create children ***/
        LinearLayout backgroundLayout = new LinearLayout(context);
        backgroundLayout.setId(R.id.PinView_background_layout);
        backgroundLayout.setBackgroundColor(context.getResources().getColor(R.color.pin_color));

        if(isHorizontal){
            pinNameView = new TextView(context);
            signalTextView = new TextView(context);
        }
        else{
            pinNameView = new VerticalTextView(context);
            signalTextView = new VerticalTextView(context);
        }

        pinNameView.setId(R.id.PinView_pin_name);
        pinNameView.setTextColor(context.getResources().getColor(android.R.color.black));

        signalTextView.setId(R.id.PinView_signal_text);
        signalTextView.setTextColor(context.getResources().getColor(android.R.color.black));
        signalTextView.setBackgroundColor(context.getResources().getColor(R.color.pin_sig_color));

        /*** determine children layouts and positions ***/
        LayoutParams lpSigText = new LayoutParams(
                LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        backgroundLayout.addView(signalTextView, lpSigText); // add signalView to backgroundLayout, NOT this view

        LayoutParams lpBackgroundLayout;
        if(isHorizontal){
            lpBackgroundLayout = new LayoutParams(
                    LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        }
        else{
            lpBackgroundLayout = new LayoutParams(
                    LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);
        }

        LayoutParams lpPinName = new LayoutParams(
                LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);

        // place pin name accordingly
        if(nameBelow){
            addView(backgroundLayout, lpBackgroundLayout);

            if(isHorizontal){
                lpPinName.addRule(RelativeLayout.BELOW, backgroundLayout.getId());
            }
            else{
                lpPinName.addRule(RelativeLayout.RIGHT_OF, backgroundLayout.getId());
            }
            addView(pinNameView, lpPinName);
            setPosition(position);
        }
        else{
            addView(pinNameView, lpPinName);
            setPosition(position);

            if(isHorizontal){
                lpBackgroundLayout.addRule(RelativeLayout.BELOW, pinNameView.getId());
            }
            else{
                lpBackgroundLayout.addRule(RelativeLayout.RIGHT_OF, pinNameView.getId());
            }
            addView(backgroundLayout, lpBackgroundLayout);
        }
    }

    public void setPosition(int position){
        // align pin name according to pin position on device
        LayoutParams params = (RelativeLayout.LayoutParams) pinNameView.getLayoutParams();
        switch(position){ // pin's position relative to parent device
            case 2: // top
                params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                break;
            case 3: // bottom
                params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
                break;
            case 0: // left
                params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                break;
            case 1: // right
                params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
                break;
            default:
                break;
        }
    }

    public void setData(DevicePin pin){
        pinNameView.setText(pin.name);
        signalTextView.setText(pin.data);

        // always create new animation, otherwise it will need to be reset
        Animation anim = null;
        switch (pin.direction){
            case LEFT:
                anim = AnimationUtils.loadAnimation(getContext(), R.anim.pin_transition_left);
                break;
            case RIGHT:
                anim = AnimationUtils.loadAnimation(getContext(), R.anim.pin_transition_right);
                break;
            case UP:
                anim = AnimationUtils.loadAnimation(getContext(), R.anim.pin_transition_up);
                break;
            case DOWN:
                anim = AnimationUtils.loadAnimation(getContext(), R.anim.pin_transition_down);
                break;
        }

        if(pin.startBehaviour == DevicePin.AnimStartBehaviour.DELAY){
            if(pin.animationDelay == -1){
                anim.setStartOffset(anim.getDuration());
            }
            else{
                anim.setStartOffset(pin.animationDelay);
            }
        }

        if(pin.action == DevicePin.PinAction.STATIONARY){
            anim.setDuration(0);
        }

        if(pin.animListener != null){
            anim.setAnimationListener(pin.animListener);
        }

        if(anim != null){
            signalTextView.setAnimation(anim);
        }
    }

    public int getMinLength(){
        return (int) pinNameView.getPaint().measureText((String) pinNameView.getText()) + 6;
    }

    public boolean isHorizontal(){
        return isHorizontal;
    }
}
