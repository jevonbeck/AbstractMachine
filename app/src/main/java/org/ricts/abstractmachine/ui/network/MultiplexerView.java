package org.ricts.abstractmachine.ui.network;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.ShapeDrawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import org.ricts.abstractmachine.R;
import org.ricts.abstractmachine.components.devices.Device;
import org.ricts.abstractmachine.ui.device.DevicePin;
import org.ricts.abstractmachine.ui.device.MultiPinView;
import org.ricts.abstractmachine.ui.device.RightAngleTriangleView;
import org.ricts.abstractmachine.ui.utils.UiUtils;

/**
 * Created by Jevon on 07/06/2015.
 */
public abstract class MultiplexerView extends RelativeLayout {
    public interface PinTrigger{
        boolean isSelected();
        void triggerPin();
    }

    protected View [] inputPins;
    protected View outputPins;

    private LinearLayout inputPinsLayout;
    private RightAngleTriangleView firstTriangle, lastTriangle, pinTriangle;
    private View middle;

    private int inputsPosition;
    private DevicePin selectPinData;

    private int selectWidth, selMask, currentSel;
    private static final int dividerThickness = 30;

    protected abstract MultiPinView createPinView(Context context, int pinPosition);
    protected abstract void initOutputPinView(View pinView, Integer... pinWidths);
    protected abstract void initInputPinView(View pinView, PinTrigger pinTrigger, Integer... pinWidths);

    /** Standard Constructors **/
    public MultiplexerView(Context context) {
        this(context, null);
    }

    public MultiplexerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MultiplexerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        currentSel = 0;

        /*** extract XML attributes ***/
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.MemoryPortMultiplexerView);
        int outputPosition = a.getInt(R.styleable.MemoryPortMultiplexerView_outputPosition, 1);
        int selectPosition = a.getInt(R.styleable.MemoryPortMultiplexerView_selectPosition, 0);
        a.recycle();

        inputsPosition = getInputsPosition(outputPosition);

        /*** create children ***/
        inputPinsLayout = new LinearLayout(context);
        inputPinsLayout.setId(R.id.MemoryPortMultiplexerView_input_pins);
        inputPinsLayout.setShowDividers(LinearLayout.SHOW_DIVIDER_MIDDLE);
        ShapeDrawable inputsDivider = new ShapeDrawable();
        inputsDivider.getPaint().setColor(context.getResources().getColor(android.R.color.transparent));
        switch (inputsPosition){
            case 2: // top
            case 3: // bottom
                inputsDivider.setIntrinsicWidth(dividerThickness);
                break;
            case 0: // left
            case 1: // right
            default:
                inputsDivider.setIntrinsicHeight(dividerThickness);
                break;
        }
        inputPinsLayout.setDividerDrawable(inputsDivider);

        outputPins = createPinView(context, inputsPosition);
        outputPins.setId(R.id.MemoryPortMultiplexerView_output_pins);

        middle = new View(context);
        middle.setId(R.id.MemoryPortMultiplexerView_middle);
        middle.setBackgroundColor(context.getResources().getColor(R.color.mux_fill_color));

        selectPinData = new DevicePin();
        selectPinData.name = "select";
        switch (outputPosition) {
            case 2: // top
                if(selectPosition == 0){
                    firstTriangle = new RightAngleTriangleView(context,
                            UiUtils.makeAttributeSet(context, R.xml.triangleview_trbl_rightfilled_pinh));
                    lastTriangle = new RightAngleTriangleView(context,
                            UiUtils.makeAttributeSet(context, R.xml.triangleview_tlbr_leftfilled_nopin));
                    firstTriangle.setPinData(selectPinData);
                    pinTriangle = firstTriangle;
                }
                else{
                    firstTriangle = new RightAngleTriangleView(context,
                            UiUtils.makeAttributeSet(context, R.xml.triangleview_trbl_rightfilled_nopin));
                    lastTriangle = new RightAngleTriangleView(context,
                            UiUtils.makeAttributeSet(context, R.xml.triangleview_tlbr_leftfilled_pinh));
                    lastTriangle.setPinData(selectPinData);
                    pinTriangle = lastTriangle;
                }
                break;
            case 3: // bottom
                if(selectPosition == 0){
                    firstTriangle = new RightAngleTriangleView(context,
                            UiUtils.makeAttributeSet(context, R.xml.triangleview_tlbr_rightfilled_pinh));
                    lastTriangle = new RightAngleTriangleView(context,
                            UiUtils.makeAttributeSet(context, R.xml.triangleview_trbl_leftfilled_nopin));
                    firstTriangle.setPinData(selectPinData);
                    pinTriangle = firstTriangle;
                }
                else{
                    firstTriangle = new RightAngleTriangleView(context,
                            UiUtils.makeAttributeSet(context, R.xml.triangleview_tlbr_rightfilled_nopin));
                    lastTriangle = new RightAngleTriangleView(context,
                            UiUtils.makeAttributeSet(context, R.xml.triangleview_trbl_leftfilled_pinh));
                    lastTriangle.setPinData(selectPinData);
                    pinTriangle = lastTriangle;
                }
                break;
            case 0: // left
                if(selectPosition == 0){
                    firstTriangle = new RightAngleTriangleView(context,
                            UiUtils.makeAttributeSet(context, R.xml.triangleview_trbl_rightfilled_pinv));
                    lastTriangle = new RightAngleTriangleView(context,
                            UiUtils.makeAttributeSet(context, R.xml.triangleview_tlbr_rightfilled_nopin));
                    firstTriangle.setPinData(selectPinData);
                    pinTriangle = firstTriangle;
                }
                else{
                    firstTriangle = new RightAngleTriangleView(context,
                            UiUtils.makeAttributeSet(context, R.xml.triangleview_trbl_rightfilled_nopin));
                    lastTriangle = new RightAngleTriangleView(context,
                            UiUtils.makeAttributeSet(context, R.xml.triangleview_tlbr_rightfilled_pinv));
                    lastTriangle.setPinData(selectPinData);
                    pinTriangle = lastTriangle;
                }
                break;
            case 1: // right
            default:
                if(selectPosition == 0){
                    firstTriangle = new RightAngleTriangleView(context,
                            UiUtils.makeAttributeSet(context, R.xml.triangleview_tlbr_leftfilled_pinv));
                    lastTriangle = new RightAngleTriangleView(context,
                            UiUtils.makeAttributeSet(context, R.xml.triangleview_trbl_leftfilled_nopin));
                    firstTriangle.setPinData(selectPinData);
                    pinTriangle = firstTriangle;
                }
                else{
                    firstTriangle = new RightAngleTriangleView(context,
                            UiUtils.makeAttributeSet(context, R.xml.triangleview_tlbr_leftfilled_nopin));
                    lastTriangle = new RightAngleTriangleView(context,
                            UiUtils.makeAttributeSet(context, R.xml.triangleview_trbl_leftfilled_pinv));
                    lastTriangle.setPinData(selectPinData);
                    pinTriangle = lastTriangle;
                }
                break;
        }
        firstTriangle.setId(R.id.MemoryPortMultiplexerView_first_triangle);
        firstTriangle.setFillColour(R.color.mux_fill_color);

        lastTriangle.setId(R.id.MemoryPortMultiplexerView_last_triangle);
        lastTriangle.setFillColour(R.color.mux_fill_color);

        /*** determine children layouts & positions based on attributes ***/
        LayoutParams lpOutputPins = new LayoutParams(
                LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);

        LayoutParams lpInputPins = new LayoutParams(
                LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);

        LayoutParams lpFirstTriangle = new LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);

        LayoutParams lpLastTriangle = new LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);

        LayoutParams lpMiddle = new LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);

        switch (outputPosition) {
            case 2: // top
                lpOutputPins.addRule(RelativeLayout.CENTER_HORIZONTAL);
                addView(outputPins, lpOutputPins);

                lpFirstTriangle.addRule(RelativeLayout.BELOW, outputPins.getId());
                addView(firstTriangle, lpFirstTriangle);

                lpMiddle.addRule(RelativeLayout.BELOW, outputPins.getId());
                lpMiddle.addRule(RelativeLayout.RIGHT_OF, firstTriangle.getId());
                addView(middle, lpMiddle);

                lpLastTriangle.addRule(RelativeLayout.BELOW, outputPins.getId());
                lpLastTriangle.addRule(RelativeLayout.RIGHT_OF, middle.getId());
                addView(lastTriangle, lpLastTriangle);

                lpInputPins.addRule(RelativeLayout.CENTER_HORIZONTAL);
                lpInputPins.addRule(RelativeLayout.BELOW, middle.getId());
                inputPinsLayout.setOrientation(LinearLayout.HORIZONTAL);
                addView(inputPinsLayout, lpInputPins);
                break;
            case 3: // bottom
                lpInputPins.addRule(RelativeLayout.CENTER_HORIZONTAL);
                inputPinsLayout.setOrientation(LinearLayout.HORIZONTAL);
                addView(inputPinsLayout, lpInputPins);

                lpFirstTriangle.addRule(RelativeLayout.BELOW, inputPinsLayout.getId());
                addView(firstTriangle, lpFirstTriangle);

                lpMiddle.addRule(RelativeLayout.BELOW, inputPinsLayout.getId());
                lpMiddle.addRule(RelativeLayout.RIGHT_OF, firstTriangle.getId());
                addView(middle, lpMiddle);

                lpLastTriangle.addRule(RelativeLayout.BELOW, inputPinsLayout.getId());
                lpLastTriangle.addRule(RelativeLayout.RIGHT_OF, middle.getId());
                addView(lastTriangle, lpLastTriangle);

                lpOutputPins.addRule(RelativeLayout.CENTER_HORIZONTAL);
                lpOutputPins.addRule(RelativeLayout.BELOW, middle.getId());
                addView(outputPins, lpOutputPins);
                break;
            case 0: // left
                lpOutputPins.addRule(RelativeLayout.CENTER_VERTICAL);
                addView(outputPins, lpOutputPins);

                lpFirstTriangle.addRule(RelativeLayout.RIGHT_OF, outputPins.getId());
                addView(firstTriangle, lpFirstTriangle);

                lpMiddle.addRule(RelativeLayout.RIGHT_OF, outputPins.getId());
                lpMiddle.addRule(RelativeLayout.BELOW, firstTriangle.getId());
                addView(middle, lpMiddle);

                lpLastTriangle.addRule(RelativeLayout.RIGHT_OF, outputPins.getId());
                lpLastTriangle.addRule(RelativeLayout.BELOW, middle.getId());
                addView(lastTriangle, lpLastTriangle);

                lpInputPins.addRule(RelativeLayout.CENTER_VERTICAL);
                lpInputPins.addRule(RelativeLayout.RIGHT_OF, middle.getId());
                inputPinsLayout.setOrientation(LinearLayout.VERTICAL);
                addView(inputPinsLayout, lpInputPins);
                break;
            case 1: // right
            default:
                lpInputPins.addRule(RelativeLayout.CENTER_VERTICAL);
                inputPinsLayout.setOrientation(LinearLayout.VERTICAL);
                addView(inputPinsLayout, lpInputPins);

                lpFirstTriangle.addRule(RelativeLayout.RIGHT_OF, inputPinsLayout.getId());
                addView(firstTriangle, lpFirstTriangle);

                lpMiddle.addRule(RelativeLayout.RIGHT_OF, inputPinsLayout.getId());
                lpMiddle.addRule(RelativeLayout.BELOW, firstTriangle.getId());
                addView(middle, lpMiddle);

                lpLastTriangle.addRule(RelativeLayout.RIGHT_OF, inputPinsLayout.getId());
                lpLastTriangle.addRule(RelativeLayout.BELOW, middle.getId());
                addView(lastTriangle, lpLastTriangle);

                lpOutputPins.addRule(RelativeLayout.CENTER_VERTICAL);
                lpOutputPins.addRule(RelativeLayout.RIGHT_OF, middle.getId());
                addView(outputPins, lpOutputPins);
                break;
        }

        /*** Initialise other vars ***/
        selectPinData.action = DevicePin.PinAction.MOVING;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec){
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        // get dimensions of pins and use to size other components
        measureChild(outputPins, widthMeasureSpec, heightMeasureSpec);
        measureChild(inputPinsLayout, widthMeasureSpec, heightMeasureSpec);

        int outputPinsW = outputPins.getMeasuredWidth();
        int outputPinsH = outputPins.getMeasuredHeight();

        int triangleMinDimension = Math.max(firstTriangle.getMinPinDimension(),
                lastTriangle.getMinPinDimension());

        int difference, triangleW, triangleH;
        switch (inputsPosition){
            case 2: // top
            case 3: // bottom
                difference = Math.abs(inputPinsLayout.getMeasuredWidth() - outputPinsW);
                triangleW = Math.max(difference / 2, triangleMinDimension);
                triangleH = outputPinsH;
                break;
            case 0: // left
            case 1: // right
            default:
                difference = Math.abs(inputPinsLayout.getMeasuredHeight() - outputPinsH);
                triangleW = outputPinsW;
                triangleH = Math.max(difference / 2, triangleMinDimension);
                break;
        }

        int diffW, diffH, diffMax = 10;

        LayoutParams lpMiddle = (LayoutParams) middle.getLayoutParams();
        diffW = Math.abs(lpMiddle.width - outputPinsW);
        diffH = Math.abs(lpMiddle.height - outputPinsH);
        if(diffW > diffMax || diffH > diffMax) {
            lpMiddle.width = outputPinsW;
            lpMiddle.height = outputPinsH;
        }

        LayoutParams lpFirstTriangle = (LayoutParams) firstTriangle.getLayoutParams();
        diffW = Math.abs(lpFirstTriangle.width - triangleW);
        diffH = Math.abs(lpFirstTriangle.height - triangleH);
        if(diffW > diffMax || diffH > diffMax) {
            lpFirstTriangle.width = triangleW;
            lpFirstTriangle.height = triangleH;
        }

        LayoutParams lpLastTriangle = (LayoutParams) lastTriangle.getLayoutParams();
        diffW = Math.abs(lpLastTriangle.width - triangleW);
        diffH = Math.abs(lpLastTriangle.height - triangleH);
        if(diffW > diffMax || diffH > diffMax) {
            lpLastTriangle.width = triangleW;
            lpLastTriangle.height = triangleH;
        }
    }

    public int getSelection(){
        return currentSel;
    }

    public void setSelection(int sel){
        currentSel = sel & selMask;
        selectPinData.data = Device.formatNumberInHex(currentSel, selectWidth);
    }

    public View [] getInputs(){
        return inputPins;
    }

    protected void init(int selW, Integer... pinWidths){
        selectWidth = selW;
        selMask = (1 << selectWidth) - 1; // bit mask of width selectWidth
        selectPinData.dataWidth = selectWidth;

        inputPinsLayout.removeAllViewsInLayout();

        initOutputPinView(outputPins, pinWidths);

        Context c = getContext();
        inputPins = new View[(int) Math.pow(2,selectWidth)];
        for(int x=0; x < inputPins.length; ++x){
            final int index = x;
            PinTrigger pinActionTrigger = new PinTrigger() {
                @Override
                public boolean isSelected() {
                    return currentSel == index;
                }

                @Override
                public void triggerPin() {
                    if(isSelected()){
                        pinTriangle.setPinData(selectPinData);
                    }
                }
            };

            inputPins[x] = createPinView(c, inputsPosition);
            initInputPinView(inputPins[x], pinActionTrigger, pinWidths);
            inputPinsLayout.addView(inputPins[x], x);
        }
    }

    private int getInputsPosition(int ouputPosition){
        switch (ouputPosition){
            case 2: // top
                return 3;
            case 3: // bottom
                return 2;
            case 0: // left
                return 1;
            case 1: // right
            default:
                return 0;
        }
    }
}
