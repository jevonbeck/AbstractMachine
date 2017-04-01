package org.ricts.abstractmachine.ui.device;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;

import org.ricts.abstractmachine.R;
import org.ricts.abstractmachine.ui.utils.UiUtils;

/**
 * Created by Jevon on 07/06/2015.
 */
public abstract class TrapeziumView extends RelativeLayout {
    private static final String TAG = "TrapeziumView";

    protected RightAngleTriangleView firstTriangle, lastTriangle, pinTriangle;
    protected View middle;

    protected RelativePosition inputsPosition;
    protected DevicePin edgePinData;

    protected abstract void initEdgePin(DevicePin pin);

    /** Standard Constructors **/
    public TrapeziumView(Context context) {
        this(context, null);
    }

    public TrapeziumView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TrapeziumView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        /*** extract XML attributes ***/
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.MultiplexerView);
        int oPosition = a.getInt(R.styleable.MultiplexerView_outputPosition, RelativePosition.RIGHT.ordinal());
        int sPosition = a.getInt(R.styleable.MultiplexerView_selectPosition, TrapeziumEdge.NONE.ordinal());
        a.recycle();

        RelativePosition outputPosition = RelativePosition.getPositionFromInt(oPosition);
        TrapeziumEdge selectPosition = TrapeziumEdge.getEdgeFromInt(sPosition);
        //inputsPosition = DeviceView.getOppositePinPosition(outputPosition);

        /*** create children ***/
        middle = new View(context);
        middle.setId(R.id.TrapeziumView_middle);
        middle.setBackgroundColor(context.getResources().getColor(R.color.mux_fill_color));

        edgePinData = new DevicePin();
        initEdgePin(edgePinData);
        switch (outputPosition) {
            case TOP:
                switch (selectPosition) {
                    case FIRST_EDGE:
                        firstTriangle = new RightAngleTriangleView(context,
                                UiUtils.makeAttributeSet(context, R.xml.triangleview_trbl_rightfilled_pinh));
                        lastTriangle = new RightAngleTriangleView(context,
                                UiUtils.makeAttributeSet(context, R.xml.triangleview_tlbr_leftfilled_nopin));
                        firstTriangle.setPinData(edgePinData);
                        pinTriangle = firstTriangle;
                        break;
                    case LAST_EDGE:
                        firstTriangle = new RightAngleTriangleView(context,
                                UiUtils.makeAttributeSet(context, R.xml.triangleview_trbl_rightfilled_nopin));
                        lastTriangle = new RightAngleTriangleView(context,
                                UiUtils.makeAttributeSet(context, R.xml.triangleview_tlbr_leftfilled_pinh));
                        lastTriangle.setPinData(edgePinData);
                        pinTriangle = lastTriangle;
                        break;
                    case NONE:
                        firstTriangle = new RightAngleTriangleView(context,
                                UiUtils.makeAttributeSet(context, R.xml.triangleview_trbl_rightfilled_nopin));
                        lastTriangle = new RightAngleTriangleView(context,
                                UiUtils.makeAttributeSet(context, R.xml.triangleview_tlbr_leftfilled_nopin));
                        break;
                }
                break;
            case BOTTOM:
                switch (selectPosition) {
                    case FIRST_EDGE:
                        firstTriangle = new RightAngleTriangleView(context,
                                UiUtils.makeAttributeSet(context, R.xml.triangleview_tlbr_rightfilled_pinh));
                        lastTriangle = new RightAngleTriangleView(context,
                                UiUtils.makeAttributeSet(context, R.xml.triangleview_trbl_leftfilled_nopin));
                        firstTriangle.setPinData(edgePinData);
                        pinTriangle = firstTriangle;
                        break;
                    case LAST_EDGE:
                        firstTriangle = new RightAngleTriangleView(context,
                                UiUtils.makeAttributeSet(context, R.xml.triangleview_tlbr_rightfilled_nopin));
                        lastTriangle = new RightAngleTriangleView(context,
                                UiUtils.makeAttributeSet(context, R.xml.triangleview_trbl_leftfilled_pinh));
                        lastTriangle.setPinData(edgePinData);
                        pinTriangle = lastTriangle;
                        break;
                    case NONE:
                        firstTriangle = new RightAngleTriangleView(context,
                                UiUtils.makeAttributeSet(context, R.xml.triangleview_tlbr_rightfilled_nopin));
                        lastTriangle = new RightAngleTriangleView(context,
                                UiUtils.makeAttributeSet(context, R.xml.triangleview_trbl_leftfilled_nopin));
                        break;
                }
                break;
            case LEFT:
                switch (selectPosition) {
                    case FIRST_EDGE:
                        firstTriangle = new RightAngleTriangleView(context,
                                UiUtils.makeAttributeSet(context, R.xml.triangleview_trbl_rightfilled_pinv));
                        lastTriangle = new RightAngleTriangleView(context,
                                UiUtils.makeAttributeSet(context, R.xml.triangleview_tlbr_rightfilled_nopin));
                        firstTriangle.setPinData(edgePinData);
                        pinTriangle = firstTriangle;
                        break;
                    case LAST_EDGE:
                        firstTriangle = new RightAngleTriangleView(context,
                                UiUtils.makeAttributeSet(context, R.xml.triangleview_trbl_rightfilled_nopin));
                        lastTriangle = new RightAngleTriangleView(context,
                                UiUtils.makeAttributeSet(context, R.xml.triangleview_tlbr_rightfilled_pinv));
                        lastTriangle.setPinData(edgePinData);
                        pinTriangle = lastTriangle;
                        break;
                    case NONE:
                        firstTriangle = new RightAngleTriangleView(context,
                                UiUtils.makeAttributeSet(context, R.xml.triangleview_trbl_rightfilled_nopin));
                        lastTriangle = new RightAngleTriangleView(context,
                                UiUtils.makeAttributeSet(context, R.xml.triangleview_tlbr_rightfilled_nopin));
                        break;
                }
                break;
            case RIGHT:
            default:
                switch (selectPosition) {
                    case FIRST_EDGE:
                        firstTriangle = new RightAngleTriangleView(context,
                                UiUtils.makeAttributeSet(context, R.xml.triangleview_tlbr_leftfilled_pinv));
                        lastTriangle = new RightAngleTriangleView(context,
                                UiUtils.makeAttributeSet(context, R.xml.triangleview_trbl_leftfilled_nopin));
                        firstTriangle.setPinData(edgePinData);
                        pinTriangle = firstTriangle;
                        break;
                    case LAST_EDGE:
                        firstTriangle = new RightAngleTriangleView(context,
                                UiUtils.makeAttributeSet(context, R.xml.triangleview_tlbr_leftfilled_nopin));
                        lastTriangle = new RightAngleTriangleView(context,
                                UiUtils.makeAttributeSet(context, R.xml.triangleview_trbl_leftfilled_pinv));
                        lastTriangle.setPinData(edgePinData);
                        pinTriangle = lastTriangle;
                        break;
                    case NONE:
                        firstTriangle = new RightAngleTriangleView(context,
                                UiUtils.makeAttributeSet(context, R.xml.triangleview_tlbr_leftfilled_nopin));
                        lastTriangle = new RightAngleTriangleView(context,
                                UiUtils.makeAttributeSet(context, R.xml.triangleview_trbl_leftfilled_nopin));
                        break;
                }
                break;
        }
        firstTriangle.setId(R.id.TrapeziumView_first_triangle);
        firstTriangle.setFillColour(R.color.mux_fill_color);

        lastTriangle.setId(R.id.TrapeziumView_last_triangle);
        lastTriangle.setFillColour(R.color.mux_fill_color);

        /*** determine children layouts & positions based on attributes ***/
        LayoutParams lpFirstTriangle = new LayoutParams(
                LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);

        LayoutParams lpLastTriangle = new LayoutParams(
                LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);

        LayoutParams lpMiddle = new LayoutParams(
                LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);

        switch (outputPosition) {
            case TOP:
                addView(firstTriangle, lpFirstTriangle);

                lpMiddle.addRule(RelativeLayout.RIGHT_OF, firstTriangle.getId());
                addView(middle, lpMiddle);

                lpLastTriangle.addRule(RelativeLayout.RIGHT_OF, middle.getId());
                addView(lastTriangle, lpLastTriangle);
                break;
            case BOTTOM:
                addView(firstTriangle, lpFirstTriangle);

                lpMiddle.addRule(RelativeLayout.RIGHT_OF, firstTriangle.getId());
                addView(middle, lpMiddle);

                lpLastTriangle.addRule(RelativeLayout.RIGHT_OF, middle.getId());
                addView(lastTriangle, lpLastTriangle);
                break;
            case LEFT:
                addView(firstTriangle, lpFirstTriangle);

                lpMiddle.addRule(RelativeLayout.BELOW, firstTriangle.getId());
                addView(middle, lpMiddle);

                lpLastTriangle.addRule(RelativeLayout.BELOW, middle.getId());
                addView(lastTriangle, lpLastTriangle);
                break;
            case RIGHT:
            default:
                addView(firstTriangle, lpFirstTriangle);

                lpMiddle.addRule(RelativeLayout.BELOW, firstTriangle.getId());
                addView(middle, lpMiddle);

                lpLastTriangle.addRule(RelativeLayout.BELOW, middle.getId());
                addView(lastTriangle, lpLastTriangle);
                break;
        }

        /*** Initialise other vars ***/
        edgePinData.action = DevicePin.PinAction.MOVING;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec){
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int parentW = MeasureSpec.getSize(widthMeasureSpec);
        int parentH = MeasureSpec.getSize(heightMeasureSpec);

        Log.d(TAG, "parentW = " + parentW);
        Log.d(TAG, "parentH = " + parentH);

        // get dimensions of pins and use to size other components
        int triangleMinDimension = Math.max(firstTriangle.getMinPinDimension(),
                lastTriangle.getMinPinDimension());

        int difference, triangleW, triangleH, middleW, middleH;
        switch (inputsPosition){
            case TOP:
            case BOTTOM:
                middleW = parentH;
                middleH = parentH;
                difference = parentW - parentH;
                triangleW = Math.max(difference / 2, triangleMinDimension);
                triangleH = parentH;
                break;
            case LEFT:
            case RIGHT:
            default:
                middleW = parentW;
                middleH = parentW;
                difference = parentH - parentW;
                triangleW = parentW;
                triangleH = Math.max(difference / 2, triangleMinDimension);
                break;
        }

        int diffW, diffH, diffMax = 10;

        LayoutParams lpMiddle = (LayoutParams) middle.getLayoutParams();
        diffW = Math.abs(lpMiddle.width - middleW);
        diffH = Math.abs(lpMiddle.height - middleH);
        if(diffW > diffMax || diffH > diffMax) {
            lpMiddle.width = middleW;
            lpMiddle.height = middleH;
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

    public void animateEdgePin(){
        if(pinTriangle != null) {
            pinTriangle.setPinData(edgePinData);
        }
    }

    public void setEdgePinText(String text) {
        edgePinData.data = text;
    }
}
