package org.ricts.abstractmachine.ui.network;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.ShapeDrawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import org.ricts.abstractmachine.R;
import org.ricts.abstractmachine.ui.device.DeviceView;
import org.ricts.abstractmachine.ui.device.RelativePosition;

/**
 * Created by Jevon on 07/06/2015.
 */
public abstract class ManyToOnePortView extends RelativeLayout {
    private static final String TAG = "ManyToOnePortView";
    protected View [] inputPins;
    protected View outputPins, mainView;

    protected RelativePosition inputsPosition;
    protected LinearLayout inputPinsLayout;

    private static final int dividerDefaultThickness = 30;

    protected abstract View createPinView(Context context, RelativePosition position);
    protected abstract void initOutputPinView(View pinView);
    protected abstract void initInputPinView(View pinView);

    protected abstract View createMainBodyView(Context context, AttributeSet attrs);
    protected abstract LayoutParams createMainViewLayoutParams();

    /** Standard Constructors **/
    public ManyToOnePortView(Context context) {
        this(context, null);
    }

    public ManyToOnePortView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ManyToOnePortView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        /*** extract XML attributes ***/
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.MultiplexerView);
        int oPosition = a.getInt(R.styleable.MultiplexerView_outputPosition, RelativePosition.RIGHT.ordinal());
        int dividerThickness = a.getDimensionPixelSize(R.styleable.MultiplexerView_dividerThickness,
                dividerDefaultThickness);
        a.recycle();

        RelativePosition outputPosition = RelativePosition.getPositionFromInt(oPosition);
        //inputsPosition = DeviceView.getOppositePinPosition(outputPosition);

        /*** create children ***/
        inputPinsLayout = new LinearLayout(context);
        inputPinsLayout.setId(R.id.ManyToOnePortView_input_pins);
        inputPinsLayout.setShowDividers(LinearLayout.SHOW_DIVIDER_MIDDLE);
        setDividerThickness(dividerThickness);

        outputPins = createPinView(context, inputsPosition);
        outputPins.setId(R.id.ManyToOnePortView_output_pins);

        mainView = createMainBodyView(context, attrs);
        mainView.setId(R.id.ManyToOnePortView_mainView);

        /*** determine children layouts & positions based on attributes ***/
        LayoutParams lpOutputPins = new LayoutParams(
                LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);

        LayoutParams lpInputPins = new LayoutParams(
                LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);

        LayoutParams lpMainView = createMainViewLayoutParams();

        switch (outputPosition) {
            case TOP:
                lpOutputPins.addRule(RelativeLayout.CENTER_HORIZONTAL);
                addView(outputPins, lpOutputPins);

                lpMainView.addRule(RelativeLayout.BELOW, outputPins.getId());
                addView(mainView, lpMainView);

                lpInputPins.addRule(RelativeLayout.CENTER_HORIZONTAL);
                lpInputPins.addRule(RelativeLayout.BELOW, mainView.getId());
                inputPinsLayout.setOrientation(LinearLayout.HORIZONTAL);
                addView(inputPinsLayout, lpInputPins);
                break;
            case BOTTOM:
                lpInputPins.addRule(RelativeLayout.CENTER_HORIZONTAL);
                inputPinsLayout.setOrientation(LinearLayout.HORIZONTAL);
                addView(inputPinsLayout, lpInputPins);

                lpMainView.addRule(RelativeLayout.BELOW, inputPinsLayout.getId());
                addView(mainView, lpMainView);

                lpOutputPins.addRule(RelativeLayout.CENTER_HORIZONTAL);
                lpOutputPins.addRule(RelativeLayout.BELOW, mainView.getId());
                addView(outputPins, lpOutputPins);
                break;
            case LEFT:
                lpOutputPins.addRule(RelativeLayout.CENTER_VERTICAL);
                addView(outputPins, lpOutputPins);

                lpMainView.addRule(RelativeLayout.RIGHT_OF, outputPins.getId());
                addView(mainView, lpMainView);

                lpInputPins.addRule(RelativeLayout.CENTER_VERTICAL);
                lpInputPins.addRule(RelativeLayout.RIGHT_OF, mainView.getId());
                inputPinsLayout.setOrientation(LinearLayout.VERTICAL);
                addView(inputPinsLayout, lpInputPins);
                break;
            case RIGHT:
            default:
                lpInputPins.addRule(RelativeLayout.CENTER_VERTICAL);
                inputPinsLayout.setOrientation(LinearLayout.VERTICAL);
                addView(inputPinsLayout, lpInputPins);

                lpMainView.addRule(RelativeLayout.RIGHT_OF, inputPinsLayout.getId());
                addView(mainView, lpMainView);

                lpOutputPins.addRule(RelativeLayout.CENTER_VERTICAL);
                lpOutputPins.addRule(RelativeLayout.RIGHT_OF, mainView.getId());
                addView(outputPins, lpOutputPins);
                break;
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec){
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        // get dimensions of pins and use to size other components
        measureChild(outputPins, widthMeasureSpec, heightMeasureSpec);
        measureChild(inputPinsLayout, widthMeasureSpec, heightMeasureSpec);

        int widthMeasureSpecMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMeasureSpecMode = MeasureSpec.getMode(heightMeasureSpec);

        int parentW = MeasureSpec.getSize(widthMeasureSpec);
        int parentH = MeasureSpec.getSize(heightMeasureSpec);
        int outputPinsW = outputPins.getMeasuredWidth();
        int outputPinsH = outputPins.getMeasuredHeight();
        int inputPinsW = inputPinsLayout.getMeasuredWidth();
        int inputPinsH = inputPinsLayout.getMeasuredHeight();

        Log.d(TAG, "parentW = " + parentW);
        Log.d(TAG, "parentH = " + parentH);
        Log.d(TAG, "outputPinsW = " + outputPinsW);
        Log.d(TAG, "outputPinsH = " + outputPinsH);
        Log.d(TAG, "inputPinsW = " + inputPinsW);
        Log.d(TAG, "inputPinsH = " + inputPinsH);

        int mainViewW, mainViewH;
        switch (inputsPosition){
            case TOP:
            case BOTTOM:
                Log.d(TAG, "inputs position is TOP or BOTTOM");
                mainViewW = getMainViewLength(widthMeasureSpecMode, inputPinsW, parentW);

                mainViewH = getMainViewThickness(heightMeasureSpecMode, outputPinsH,
                        parentH - inputPinsH - outputPinsH);
                break;
            case LEFT:
            case RIGHT:
            default:
                Log.d(TAG, "inputs position is LEFT or RIGHT");
                mainViewW = getMainViewThickness(widthMeasureSpecMode, outputPinsW,
                        parentW - inputPinsW - outputPinsW);

                mainViewH = getMainViewLength(heightMeasureSpecMode, inputPinsH, parentH);
                break;
        }

        int mainViewWidthMeasureSpec = MeasureSpec.makeMeasureSpec(mainViewW,
                MeasureSpec.getMode(widthMeasureSpecMode));

        int mainViewHeightMeasureSpec = MeasureSpec.makeMeasureSpec(mainViewH,
                MeasureSpec.getMode(heightMeasureSpecMode));

        measureChild(mainView, mainViewWidthMeasureSpec, mainViewHeightMeasureSpec);
    }

    public View [] getInputs(){
        return inputPins;
    }

    public View getOutput(){
        return outputPins;
    }

    public void setDividerThickness(int thickness){
        ShapeDrawable inputsDivider = new ShapeDrawable();
        switch (inputsPosition){
            case TOP:
            case BOTTOM:
                inputsDivider.setIntrinsicWidth(thickness);
                break;
            case LEFT:
            case RIGHT:
            default:
                inputsDivider.setIntrinsicHeight(thickness);
                break;
        }
        inputsDivider.getPaint().setColor(getContext().getResources().getColor(android.R.color.transparent));
        inputPinsLayout.setDividerDrawable(inputsDivider);
    }

    private int getMainViewThickness(int measureSpecMode, int preferredThickness, int maxThickness){
        Log.d(TAG, "getMainViewThickness()");
        Log.d(TAG, "measureSpecMode = " + measureSpecMode);
        Log.d(TAG, "preferredThickness = " + preferredThickness);
        Log.d(TAG, "maxThickness = " + maxThickness);

        int mainViewThickness;
        switch(measureSpecMode) {
            case MeasureSpec.AT_MOST:
            case MeasureSpec.EXACTLY:
                mainViewThickness = Math.max(preferredThickness/4, maxThickness);

                if(measureSpecMode == MeasureSpec.AT_MOST){
                    mainViewThickness = Math.min(mainViewThickness, preferredThickness);
                }
                break;
            case MeasureSpec.UNSPECIFIED:
            default:
                mainViewThickness = preferredThickness;
                break;
        }
        Log.d(TAG, "mainViewThickness (result) = " + mainViewThickness);
        return mainViewThickness;
    }

    private int getMainViewLength(int measureSpecMode, int preferredLength, int maxLength){
        Log.d(TAG, "getMainViewLength()");
        Log.d(TAG, "measureSpecMode = " + measureSpecMode);
        Log.d(TAG, "preferredLength = " + preferredLength);
        Log.d(TAG, "maxLength = " + maxLength);

        int mainViewLength;
        switch(measureSpecMode) {
            case MeasureSpec.AT_MOST:
            case MeasureSpec.EXACTLY:
                mainViewLength = maxLength;

                if(measureSpecMode == MeasureSpec.AT_MOST){
                    mainViewLength = Math.min(mainViewLength, preferredLength);
                }
                break;
            case MeasureSpec.UNSPECIFIED:
            default:
                mainViewLength = preferredLength;
                break;
        }
        Log.d(TAG, "mainViewLength (result) = " + mainViewLength);
        return mainViewLength;
    }
}
