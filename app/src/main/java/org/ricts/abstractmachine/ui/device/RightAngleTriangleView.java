package org.ricts.abstractmachine.ui.device;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.ViewGroup;

import org.ricts.abstractmachine.R;
import org.ricts.abstractmachine.ui.UiUtils;

/**
 * Created by Jevon on 09/05/2015.
 */
public class RightAngleTriangleView extends ViewGroup {
    private static final String TAG = "RightAngleTriangleView";

    public enum PinDirection{
        IN, OUT
    }

    private int pinOrientation;
    private boolean isRightFilled, diagonalIsTopRightBottomLeft;
    private Paint trianglePaint, pinPaint;

    private Path trianglePath, pinPath;
    private PointF triStart, triMiddle, triEnd;

    private PinView pinView;
    private float pinLengthDiff, pinThickness;
    protected DevicePin.PinDirection inDirection, outDirection, actualDirection;

    public RightAngleTriangleView(Context context) {
        this(context, null);
    }

    public RightAngleTriangleView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RightAngleTriangleView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setWillNotDraw(false); // remove default (non) drawing behaviour for ViewGroup

        /*** extract XML attributes ***/
        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.RightAngleTriangleView);
        int fillColour = a.getColor(R.styleable.RightAngleTriangleView_fillColour,
                context.getResources().getColor(android.R.color.darker_gray));
        int fillPosition = a.getInt(R.styleable.RightAngleTriangleView_fillPosition,
                context.getResources().getInteger(
                        R.integer.RightAngleTriangleView_fillPosition_left));
        int diagonal = a.getInt(R.styleable.RightAngleTriangleView_diagonal,
                context.getResources().getInteger(
                        R.integer.RightAngleTriangleView_diagonal_topLeftToBottomRight));

        isRightFilled = fillPosition == context.getResources().getInteger(
                R.integer.RightAngleTriangleView_fillPosition_right);

        diagonalIsTopRightBottomLeft = diagonal == getContext().getResources().getInteger(
                R.integer.RightAngleTriangleView_diagonal_topRightToBottomLeft);

        pinOrientation = a.getInt(R.styleable.RightAngleTriangleView_pinOrientation,
                context.getResources().getInteger(
                        R.integer.RightAngleTriangleView_pinOrientation_none));
        a.recycle();

        /*** setup drawing related variables ***/
        trianglePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        trianglePaint.setStyle(Paint.Style.FILL_AND_STROKE);
        trianglePaint.setColor(fillColour);

        trianglePath = new Path();
        trianglePath.setFillType(Path.FillType.EVEN_ODD);

        triStart = new PointF();
        triMiddle = new PointF();
        triEnd = new PointF();

        pinPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        pinPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        pinPaint.setColor(context.getResources().getColor(R.color.pin_color));

        pinPath = new Path();
        pinPath.setFillType(Path.FillType.EVEN_ODD);

        // create pinView (if present)
        if(pinOrientation != context.getResources().getInteger(
                R.integer.RightAngleTriangleView_pinOrientation_none)){
            pinView = new PinView(context, UiUtils.makeAttributeSet(context, getResourceId()));
            addView(pinView);
        }

        /*** initialise other vars ***/
        actualDirection = DevicePin.PinDirection.LEFT;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec){
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        if(hasPin()){
            // measure child to obtain 'wrapped' valid dimension
            measureChild(pinView, widthMeasureSpec, heightMeasureSpec);
        }
    }

    @Override
    protected void onLayout (boolean changed, int left, int top, int right, int bottom){
        if(hasPin()){
            // use 'wrapped' valid dimension as pinThickness
            if(pinView.isHorizontal()) {
                pinThickness = pinView.getMeasuredHeight() / 2;
                pinLengthDiff = (pinThickness * getWidth()) / getHeight();
            }
            else{
                pinThickness = pinView.getMeasuredWidth() / 2;
                pinLengthDiff = (pinThickness * getHeight()) / getWidth();
            }
            placePinView(left, top, right, bottom);
        }
    }

    @Override
    protected void onDraw(Canvas canvas){
        // draw pin 'edge' if applicable
        if(hasPin()){
            pinPath.reset(); // remove any previously drawn paths
            if(pinView.isHorizontal()){
                pinPath.addRect((getWidth() - pinLengthDiff) / 2, (getHeight() - pinThickness) / 2,
                        (getWidth() + pinLengthDiff) / 2, (getHeight() + pinThickness) / 2,
                        Path.Direction.CW);
            }
            else{
                pinPath.addRect((getWidth() - pinThickness) / 2, (getHeight() - pinLengthDiff) / 2,
                        (getWidth() + pinThickness) / 2, (getHeight() + pinLengthDiff) / 2,
                        Path.Direction.CW);
            }
            canvas.drawPath(pinPath, pinPaint);
        }

        // determine triangle vertices
        if(diagonalIsTopRightBottomLeft){
            // draw diagonal
            triStart.set(getWidth(), 0);
            triMiddle.set(0, getHeight());

            // determine triEnd based on fill position
            if(isRightFilled){
                triEnd.set(getWidth(), getHeight());
            }
            else{
                triEnd.set(0, 0);
            }
        }
        else{
            // draw diagonal
            triStart.set(0, 0);
            triMiddle.set(getWidth(), getHeight());

            // determine triEnd based on fill position
            if(isRightFilled){
                triEnd.set(getWidth(), 0);
            }
            else{
                triEnd.set(0, getHeight());
            }
        }

        // draw triangle
        trianglePath.reset(); // remove any previously drawn paths
        trianglePath.moveTo(triStart.x, triStart.y);
        trianglePath.lineTo(triMiddle.x, triMiddle.y);
        trianglePath.lineTo(triEnd.x, triEnd.y);
        trianglePath.close(); // automatically draw third side

        canvas.drawPath(trianglePath, trianglePaint);
    }

    public void setPinData(DevicePin pinData){
        if(hasPin()) {
            pinData.direction = actualDirection;
            pinView.setData(pinData);
        }
    }

    public int getMinPinDimension(){
        if(hasPin()) {
            return 2 * pinView.getMinLength() + (int) pinLengthDiff;
        }
        else{
            return 0;
        }
    }

    public boolean hasPin(){
        return pinView != null;
    }

    public void setFillColour(int colourResourceId){
        trianglePaint.setColor(getContext().getResources().getColor(colourResourceId));
        invalidate();
    }

    public void setPinDirection(PinDirection direction){
        switch (direction){
            case IN:
                actualDirection = inDirection;
                break;
            case OUT:
                actualDirection = outDirection;
                break;
        }
    }

    private void placePinView(int left, int top, int right, int bottom){
        int l, t, r, b, pinPosition;

        int trbl = diagonalIsTopRightBottomLeft ? 1<<2 : 0;
        int rightFilled = isRightFilled ? 1<<1 : 0;
        int horizontal = pinView.isHorizontal() ? 1 : 0;
        int result = trbl + rightFilled + horizontal;
        // determine pin size and position
        switch (result){
            case 0: // diagonal = top-left to bottom-right, left-filled, pin vertical
                t = 0;
                b = (int) (getHeight() - pinLengthDiff) / 2;
                l = (int) (getWidth() - pinThickness)/2;
                r = l + pinView.getMeasuredWidth();

                pinPosition = getContext().getResources().getInteger(
                        R.integer.PinView_position_top);
                inDirection = DevicePin.PinDirection.DOWN;
                outDirection = DevicePin.PinDirection.UP;
                break;
            case 1: // diagonal = top-left to bottom-right, left-filled, pin horizontal
                l = (int) (getWidth() + pinLengthDiff)/2;
                r = right - left;
                b = (int) (getHeight() + pinThickness)/ 2;
                t = b - pinView.getMeasuredHeight();

                pinPosition = getContext().getResources().getInteger(
                        R.integer.PinView_position_right);
                inDirection = DevicePin.PinDirection.LEFT;
                outDirection = DevicePin.PinDirection.RIGHT;
                break;
            case 2: // diagonal = top-left to bottom-right, right-filled, pin vertical
                t = (int) (getHeight() + pinLengthDiff) / 2;
                b = bottom - top;
                r = (int) (getWidth() + pinThickness)/2;
                l = r - pinView.getMeasuredWidth();

                pinPosition = getContext().getResources().getInteger(
                        R.integer.PinView_position_bottom);
                inDirection = DevicePin.PinDirection.UP;
                outDirection = DevicePin.PinDirection.DOWN;
                break;
            case 3: // diagonal = top-left to bottom-right, right-filled, pin horizontal
                l = 0;
                t = (int) (getHeight() - pinThickness)/ 2;
                r = l + (int) (getWidth() - pinLengthDiff) / 2;
                b = t + pinView.getMeasuredHeight();

                pinPosition = getContext().getResources().getInteger(
                        R.integer.PinView_position_left);
                inDirection = DevicePin.PinDirection.RIGHT;
                outDirection = DevicePin.PinDirection.LEFT;
                break;
            case 4: // diagonal = top-right to bottom-left, left-filled, pin vertical
                t = (int) (getHeight() + pinLengthDiff) / 2;
                b = bottom - top;
                l = (int) (getWidth() - pinThickness)/2;
                r = l + pinView.getMeasuredWidth();

                pinPosition = getContext().getResources().getInteger(
                        R.integer.PinView_position_bottom);
                inDirection = DevicePin.PinDirection.UP;
                outDirection = DevicePin.PinDirection.DOWN;
                break;
            case 5: // diagonal = top-right to bottom-left, left-filled, pin horizontal
                l = (int) (getWidth() + pinLengthDiff)/2;
                t = (int) (getHeight() - pinThickness)/ 2;
                r = right - left;
                b = t + pinView.getMeasuredHeight();

                pinPosition = getContext().getResources().getInteger(
                        R.integer.PinView_position_right);
                inDirection = DevicePin.PinDirection.LEFT;
                outDirection = DevicePin.PinDirection.RIGHT;
                break;
            case 6: // diagonal = top-right to bottom-left, right-filled, pin vertical
                t = 0;
                b = (int) (getHeight() - pinLengthDiff) / 2;
                r = (int) (getWidth() + pinThickness)/2;
                l = r - pinView.getMeasuredWidth();

                pinPosition = getContext().getResources().getInteger(
                        R.integer.PinView_position_top);
                inDirection = DevicePin.PinDirection.DOWN;
                outDirection = DevicePin.PinDirection.UP;
                break;
            case 7: // diagonal = top-right to bottom-left, right-filled, pin horizontal
                l = 0;
                r = (int) (getWidth() - pinLengthDiff) / 2;
                b = (int) (getHeight() + pinThickness)/2;
                t = b - pinView.getMeasuredHeight();

                pinPosition = getContext().getResources().getInteger(
                        R.integer.PinView_position_left);
                inDirection = DevicePin.PinDirection.RIGHT;
                outDirection = DevicePin.PinDirection.LEFT;
                break;
            default:
                l = 0;
                t = 0;
                r = right - left;
                b = bottom - top;
                pinPosition = -1;
                inDirection = DevicePin.PinDirection.LEFT;
                outDirection = DevicePin.PinDirection.RIGHT;
                break;
        }

        // remeasure / resize pinView accounting for correct unspecified dimension
        measureChild(pinView, MeasureSpec.makeMeasureSpec(r - l, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(b - t, MeasureSpec.EXACTLY));

        pinView.setPosition(pinPosition); // ensure that pinName is in correct position
        pinView.layout(l, t, r, b); // position pinView

        // set pin initial direction as 'in'
        setPinDirection(PinDirection.IN);
    }

    private int getResourceId(){
        int trbl = diagonalIsTopRightBottomLeft ? 1<<2 : 0;
        int rightFilled = isRightFilled ? 1<<1 : 0;
        int horizontal = pinOrientation == getContext().getResources().getInteger(
                R.integer.RightAngleTriangleView_pinOrientation_horizontal) ? 1 : 0;

        int result = trbl + rightFilled + horizontal;
        switch (result){
            case 0: // diagonal = top-left to bottom-right, left-filled, pin vertical
                return R.xml.pinview_vertical_namebelow;
            case 1: // diagonal = top-left to bottom-right, left-filled, pin horizontal
                return R.xml.pinview_horizontal;
            case 2: // diagonal = top-left to bottom-right, right-filled, pin vertical
                return R.xml.pinview_vertical;
            case 3: // diagonal = top-left to bottom-right, right-filled, pin horizontal
                return R.xml.pinview_horizontal_namebelow;
            case 4: // diagonal = top-right to bottom-left, left-filled, pin vertical
                return R.xml.pinview_vertical_namebelow;
            case 5: // diagonal = top-right to bottom-left, left-filled, pin horizontal
                return R.xml.pinview_horizontal_namebelow;
            case 6: // diagonal = top-right to bottom-left, right-filled, pin vertical
                return R.xml.pinview_vertical;
            case 7: // diagonal = top-right to bottom-left, right-filled, pin horizontal
                return R.xml.pinview_horizontal;
            default:
                return -1;
        }
    }
}
