package org.ricts.abstractmachine.ui.network;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import org.ricts.abstractmachine.R;
import org.ricts.abstractmachine.components.Device;
import org.ricts.abstractmachine.components.interfaces.MemoryPort;
import org.ricts.abstractmachine.components.network.MemoryPortMultiplexer;
import org.ricts.abstractmachine.ui.device.DevicePin;
import org.ricts.abstractmachine.ui.device.ReadPortView;
import org.ricts.abstractmachine.ui.device.RightAngleTriangleView;
import org.ricts.abstractmachine.ui.UiUtils;
import org.ricts.abstractmachine.ui.device.MemoryPortView;

/**
 * Created by Jevon on 07/06/2015.
 */
public class MemoryPortMultiplexerView extends RelativeLayout {
    private static final String TAG = "MemoryMultiplexerView";

    private MemoryPortView [] inputPins;
    private MemoryPortView outputPins;
    private LinearLayout inputPinsLayout;
    private RightAngleTriangleView firstTriangle, lastTriangle;
    private View middle;

    private MemoryPortMultiplexer mux;
    private int inputsPosition;
    private Responder commandResponder;
    private DevicePin selectPinData;
    private RightAngleTriangleView pinTriangle;

    private int selectWidth, selMask, currentSel;

    /** Standard Constructors **/
    public MemoryPortMultiplexerView(Context context) {
        this(context, null);
    }

    public MemoryPortMultiplexerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MemoryPortMultiplexerView(Context context, AttributeSet attrs, int defStyle) {
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
        inputPinsLayout.setId(R.id.memoryportmultiplexerview_inputpins);
        //inputPinsLayout.setShowDividers(LinearLayout.SHOW_DIVIDER_MIDDLE);
        //inputPinsLayout.setDividerPadding(10); // TODO: verify this works on its own (i.e. no Divider drawable)

        outputPins = new MemoryPortView(context,
                UiUtils.makeAttributeSet(context, getResourceId(inputsPosition)));
        outputPins.setId(R.id.memoryportmultiplexerview_outputpins);
        outputPins.setStartDelay(1);

        middle = new View(context);
        middle.setId(R.id.memoryportmultiplexerview_middle);
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
        firstTriangle.setId(R.id.memoryportmultiplexerview_firsttriangle);
        firstTriangle.setFillColour(R.color.mux_fill_color);

        lastTriangle.setId(R.id.memoryportmultiplexerview_lasttriangle);
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
        commandResponder = new Responder();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec){
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        // get dimensions of pins and use to size other components
        measureChild(outputPins, widthMeasureSpec, heightMeasureSpec);
        measureChild(inputPinsLayout, widthMeasureSpec, heightMeasureSpec);

        LayoutParams lpMiddle = (LayoutParams) middle.getLayoutParams();
        lpMiddle.width = outputPins.getMeasuredWidth();
        lpMiddle.height = outputPins.getMeasuredHeight();

        int triangleMinDimension = Math.max(firstTriangle.getMinPinDimension(),
                lastTriangle.getMinPinDimension());

        int difference, triangleW, triangleH;
        switch (inputsPosition){
            case 2: // top
            case 3: // bottom
                difference = Math.abs(inputPinsLayout.getMeasuredWidth() - outputPins.getMeasuredWidth());
                triangleW = Math.max(difference / 2, triangleMinDimension);
                triangleH = lpMiddle.height;
                break;
            case 0: // left
            case 1: // right
            default:
                difference = Math.abs(inputPinsLayout.getMeasuredHeight() - outputPins.getMeasuredHeight());
                triangleW = lpMiddle.width;
                triangleH = Math.max(difference / 2, triangleMinDimension);
                break;
        }

        LayoutParams lpFirstTriangle = (LayoutParams) firstTriangle.getLayoutParams();
        lpFirstTriangle.width = triangleW;
        lpFirstTriangle.height = triangleH;

        LayoutParams lpLastTriangle = (LayoutParams) lastTriangle.getLayoutParams();
        lpLastTriangle.width = triangleW;
        lpLastTriangle.height = triangleH;
    }

    public int getSelection(){
        return currentSel;
    }

    public void setSelection(int sel){
        currentSel = sel & selMask;
        selectPinData.data = Device.formatNumberInHex(currentSel, selectWidth);
    }

    public void setOutputSource(MemoryPort source){
        outputPins.setSource(source);
    }

    public MemoryPortView [] getInputs(){
        return inputPins;
    }

    public void initMux(int selW, int dataW, int addrW){
        selectWidth = selW;
        selMask = (1 << selectWidth) - 1; // bit mask of width selectWidth
        selectPinData.dataWidth = selectWidth;

        outputPins.initParams(dataW, addrW);
        inputPinsLayout.removeAllViewsInLayout();

        Context c = getContext();
        inputPins = new MemoryPortView[(int) Math.pow(2,selectWidth)];
        for(int x=0; x < inputPins.length; ++x){
            inputPins[x] = new MemoryPortView(c,
                    UiUtils.makeAttributeSet(c, getResourceId(inputsPosition)));

            inputPins[x].initParams(dataW, addrW);
            inputPins[x].setReadAnimationDelay(3);
            //inputPins[x].setReadResponder(commandResponder);
            //inputPins[x].setWriteResponder(commandResponder);

            //inputPins[x].setSource(outputPins);
            final int index = x;
            inputPins[x].setSource(new MemoryPort(){
                @Override
                public int read(int address) {
                    if(currentSel == index){
                        return outputPins.read(address);
                    }
                    return -1;
                }

                @Override
                public int accessTime() {
                    return outputPins.accessTime();
                }

                @Override
                public void write(int address, int data) {
                    if(currentSel == index){
                        outputPins.write(address, data);
                    }
                }
            });

            inputPinsLayout.addView(inputPins[x], x);
        }
    }

    private int getResourceId(int portPosition){
        switch (portPosition){
            case 2: // top
                return R.xml.memoryportview_top;
            case 3: // bottom
                return R.xml.memoryportview_bottom;
            case 0: // left
                return R.xml.memoryportview_left;
            case 1: // right
            default:
                return R.xml.memoryportview_right;
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

    private class Responder implements ReadPortView.ReadResponder, MemoryPortView.WriteResponder {
        @Override
        public void onReadStart() {
            initSelectPinAnimation();
        }

        @Override
        public void onWriteStart() {
            initSelectPinAnimation();
        }

        @Override
        public void onReadFinished() {

        }

        @Override
        public void onWriteFinished() {

        }

        private void initSelectPinAnimation(){
            pinTriangle.setPinData(selectPinData);
        }
    }
}
