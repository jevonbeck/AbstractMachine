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
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.TrapeziumView);
        TypedArray b = context.obtainStyledAttributes(attrs, R.styleable.ManyToOnePortView);
        int sPosition = a.getInt(R.styleable.TrapeziumView_selectPosition, TrapeziumEdge.NONE.ordinal());
        int oPosition = b.getInt(R.styleable.ManyToOnePortView_outputPosition, RelativePosition.RIGHT.ordinal());
        a.recycle();
        b.recycle();

        RelativePosition outputPosition = RelativePosition.getPositionFromInt(oPosition);
        TrapeziumEdge selectPosition = TrapeziumEdge.getEdgeFromInt(sPosition);
        inputsPosition = RelativePosition.getOppositePosition(outputPosition);

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
        int parentW = MeasureSpec.getSize(widthMeasureSpec);
        int parentH = MeasureSpec.getSize(heightMeasureSpec);
        int parentWMode = MeasureSpec.getMode(widthMeasureSpec);
        int parentHMode = MeasureSpec.getMode(heightMeasureSpec);

        int triangleMinBase = Math.max(firstTriangle.getMinBaseLength(),
                lastTriangle.getMinBaseLength());
        int triangleMinHeight = Math.max(firstTriangle.getMinHeightLength(),
                lastTriangle.getMinHeightLength());

        int triangleW, triangleH, middleW, middleH, fullW, fullH;
        switch (inputsPosition){
            case TOP:
            case BOTTOM:
                triangleW = getTriangleDimension(triangleMinBase, parentW/3, parentWMode);
                middleW = getMiddleBase(triangleW, parentW - 2*triangleW);
                fullW = middleW + 2*triangleW;

                triangleH = getTriangleDimension(triangleMinHeight, parentH, parentHMode);
                middleH = triangleH;
                fullH = middleH;
                break;
            case LEFT:
            case RIGHT:
            default:
                triangleH = getTriangleDimension(triangleMinBase, parentH/3, parentHMode);
                middleH = getMiddleBase(triangleH, parentH - 2*triangleH);
                fullH = middleH + 2*triangleH;

                triangleW = getTriangleDimension(triangleMinHeight, parentW, parentWMode);
                middleW = triangleW;
                fullW = middleW;
                break;
        }

        LayoutParams lpFirstTriangle = (LayoutParams) firstTriangle.getLayoutParams();
        lpFirstTriangle.width = triangleW;
        lpFirstTriangle.height = triangleH;

        LayoutParams lpLastTriangle = (LayoutParams) lastTriangle.getLayoutParams();
        lpLastTriangle.width = triangleW;
        lpLastTriangle.height = triangleH;

        LayoutParams lpMiddle = (LayoutParams) middle.getLayoutParams();
        lpMiddle.width = middleW;
        lpMiddle.height = middleH;

        fullW += getPaddingLeft() + getPaddingRight();
        fullH += getPaddingTop() + getPaddingBottom();

        super.onMeasure(MeasureSpec.makeMeasureSpec(fullW, parentWMode),
                MeasureSpec.makeMeasureSpec(fullH, parentHMode));
    }

    public void animateEdgePin(){
        if(pinTriangle != null) {
            pinTriangle.setPinData(edgePinData);
        }
    }

    public void setEdgePinText(String text) {
        edgePinData.data = text;
    }

    private int getTriangleDimension(int minDimension, int parentAllowedDimension, int measureSpecMode) {
        switch (measureSpecMode) {
            case MeasureSpec.UNSPECIFIED:
            case MeasureSpec.AT_MOST:
                return minDimension > 0 ?
                        Math.min(minDimension, parentAllowedDimension) : parentAllowedDimension;
            default:
                return parentAllowedDimension;
        }
    }

    private int getMiddleBase(int preferredDimension, int maxAllowedDimension){
        return Math.min(preferredDimension, maxAllowedDimension);
    }
}
