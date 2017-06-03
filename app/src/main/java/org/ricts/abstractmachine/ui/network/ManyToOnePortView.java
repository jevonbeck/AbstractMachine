package org.ricts.abstractmachine.ui.network;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.ShapeDrawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import org.ricts.abstractmachine.R;
import org.ricts.abstractmachine.ui.device.RelativePosition;

/**
 * Created by Jevon on 07/06/2015.
 */
public abstract class ManyToOnePortView extends RelativeLayout {
    protected View mainView;

    private View [] inputPins;
    private View outputPins;
    private RelativePosition inputsPosition;
    private LinearLayout inputPinsLayout;

    private static final int dividerDefaultThickness = 30;

    protected abstract View createPinView(Context context, RelativePosition position);
    protected abstract void initOutputPinView(View pinView);
    protected abstract void initInputPinView(View pinView);

    protected abstract View createMainBodyView(Context context, AttributeSet attrs);

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
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ManyToOnePortView);
        int oPosition = a.getInt(R.styleable.ManyToOnePortView_outputPosition, RelativePosition.RIGHT.ordinal());
        int dividerThickness = a.getDimensionPixelSize(R.styleable.ManyToOnePortView_dividerThickness,
                dividerDefaultThickness);
        a.recycle();

        RelativePosition outputPosition = RelativePosition.getPositionFromInt(oPosition);
        inputsPosition = RelativePosition.getOppositePosition(outputPosition);

        /*** create children ***/
        inputPinsLayout = new LinearLayout(context);
        inputPinsLayout.setId(R.id.ManyToOnePortView_input_pins);
        inputPinsLayout.setShowDividers(LinearLayout.SHOW_DIVIDER_MIDDLE);
        setDividerThickness(dividerThickness);

        outputPins = createPinView(context, inputsPosition);
        outputPins.setId(R.id.ManyToOnePortView_output_pins);
        initOutputPinView(outputPins);

        mainView = createMainBodyView(context, attrs);
        mainView.setId(R.id.ManyToOnePortView_mainView);

        /*** determine children layouts & positions based on attributes ***/
        LayoutParams lpOutputPins = new LayoutParams(
                LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);

        LayoutParams lpInputPins = new LayoutParams(
                LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);

        LayoutParams lpMainView = new LayoutParams(
                LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);

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
        // get dimensions of pins and use to size main view
        measureChild(outputPins, widthMeasureSpec, heightMeasureSpec);
        measureChild(inputPinsLayout, widthMeasureSpec, heightMeasureSpec);
        measureChild(mainView, widthMeasureSpec, heightMeasureSpec);

        int outputPinsW = outputPins.getMeasuredWidth();
        int outputPinsH = outputPins.getMeasuredHeight();
        int inputPinsW = inputPinsLayout.getMeasuredWidth();
        int inputPinsH = inputPinsLayout.getMeasuredHeight();
        int mainViewW = mainView.getMeasuredWidth();
        int mainViewH = mainView.getMeasuredHeight();

        int fullW, fullH;
        switch (inputsPosition) {
            case TOP:
            case BOTTOM:
                fullW = Math.max(mainViewW, inputPinsW);
                fullH = outputPinsH + mainViewH + inputPinsH;
                break;
            case LEFT:
            case RIGHT:
            default:
                fullW = outputPinsW + mainViewW + inputPinsW;
                fullH = Math.max(mainViewH, inputPinsH);
                break;
        }

        fullW += getPaddingLeft() + getPaddingRight();
        fullH += getPaddingTop() + getPaddingBottom();

        super.onMeasure(MeasureSpec.makeMeasureSpec(fullW, MeasureSpec.getMode(widthMeasureSpec)),
                MeasureSpec.makeMeasureSpec(fullH, MeasureSpec.getMode(heightMeasureSpec)));
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

    protected void createInputPins(int inputsCount) {
        if(inputsCount > 1) {
            inputPinsLayout.removeAllViewsInLayout();

            Context c = getContext();
            inputPins = new View[inputsCount];
            for(int x=0; x < inputPins.length; ++x){
                inputPins[x] = createPinView(c, inputsPosition);
                initInputPinView(inputPins[x]);
                inputPinsLayout.addView(inputPins[x], x);
            }
        }
    }
}
