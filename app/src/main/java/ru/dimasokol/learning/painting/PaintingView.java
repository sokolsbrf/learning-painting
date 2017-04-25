package ru.dimasokol.learning.painting;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.*;
import android.os.Build;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.View;

/**
 * @author Дмитрий Соколов <DPSokolov.SBT@sberbank.ru>
 */

public class PaintingView extends View {

    private PaintingTool mPaintingTool = new PaintingTool();

    private Bitmap mBitmap;
    private Canvas mBitmapCanvas;

    private int mNextPaint = 0;
    private Paint mEditModePaint = new Paint();

    private SparseArray<PointF> mLastPoints = new SparseArray<>(10);
    private PointF mCurrentPoint = new PointF();

    public PaintingView(Context context) {
        super(context);
        mPaintingTool.init(this);
    }

    public PaintingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mPaintingTool.init(this);
    }

    public PaintingView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mPaintingTool.init(this);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public PaintingView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        mPaintingTool.init(this);
    }

    public void setPaintingToolType(int paintingToolType) {
        mPaintingTool.setToolType(paintingToolType);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        if (w > 0 && h > 0) {
            Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);

            if (mBitmap != null) {
                canvas.drawBitmap(mBitmap, 0, 0, null);
                mBitmap.recycle();
            }

            mBitmap = bitmap;
            mBitmapCanvas = canvas;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_POINTER_DOWN:
                int actionIndex = event.getActionIndex();
                int pointerId = event.getPointerId(actionIndex);

                mLastPoints.put(pointerId, new PointF(event.getX(actionIndex), event.getY(actionIndex)));
                mNextPaint++;
                return true;
            case MotionEvent.ACTION_MOVE:

                // refresh current point coordinates
                mCurrentPoint.x = event.getX();
                mCurrentPoint.y = event.getY();

                if (mPaintingTool.getToolType() == PaintingTool.BRUSH) {
                    drawBrush(event);
                }
                invalidate();
                return true;

            case MotionEvent.ACTION_POINTER_UP:
                return true;
            case MotionEvent.ACTION_UP:
                if (mPaintingTool.getToolType() == PaintingTool.RECTANGLE) {
                    drawRect(event);
                }

                mLastPoints.clear();
                return true;
        }

        return super.onTouchEvent(event);
    }

    private void drawRect(MotionEvent event) {
        mCurrentPoint.x = event.getX();
        mCurrentPoint.y = event.getY();

        PointF last = mLastPoints.get(event.getPointerId(0));
        mPaintingTool.draw(last.x, last.y, mCurrentPoint.x, mCurrentPoint.y);
    }

    private void preDrawRect(Canvas canvas) {
        if (mLastPoints.size() > 0 && mCurrentPoint != null) {
            PointF last = mLastPoints.get(0);
            canvas.drawRect(last.x, last.y, mCurrentPoint.x, mCurrentPoint.y, mPaintingTool.getPaint());
        }
    }

    private void drawBrush(MotionEvent event) {
        for (int i = 0; i < event.getPointerCount(); i++) {
            PointF last = mLastPoints.get(event.getPointerId(i));

            if (last != null) {
                float x = event.getX(i);
                float y = event.getY(i);

                mPaintingTool.draw(last.x, last.y, x, y);
                last.x = x;
                last.y = y;
            }
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (isInEditMode()) {
            canvas.drawRect(getWidth() / 10, getHeight() / 10, (getWidth() / 10) * 9,
                    (getHeight() / 10) * 9, mEditModePaint);
        }

        canvas.drawBitmap(mBitmap, 0, 0, null);

        if (mPaintingTool.getToolType() == PaintingTool.RECTANGLE) {
            preDrawRect(canvas);
        }
    }

    /**
     * Очищает нарисованное
     */
    public void clear() {
        mBitmapCanvas.drawColor(Color.BLACK, PorterDuff.Mode.CLEAR);
        invalidate();
    }

    class PaintingTool {
        static final int BRUSH = 0;
        static final int RECTANGLE = 1;

        private int toolType = BRUSH;

        private Paint[] mPredefinedPaints;
        private Paint mRectanglePaint;

        void setToolType(int toolType) {
            this.toolType = toolType;
        }

        int getToolType() {
            return toolType;
        }

        void draw(float startX, float startY, float endX, float endY) {
            switch (toolType) {
                case BRUSH:
                    mBitmapCanvas.drawLine(startX, startY, endX, endY, getPaint());
                    break;
                case RECTANGLE:
                    mBitmapCanvas.drawRect(startX, startY, endX, endY, getPaint());
                    break;

                default:
                    throw new IllegalStateException();
            }
        }

        Paint getPaint() {
            return mPredefinedPaints[PaintingView.this.mNextPaint % mPredefinedPaints.length];
        }

        private void init(PaintingView paintingView) {
            if (paintingView.getRootView().isInEditMode()) {
                paintingView.mEditModePaint.setColor(Color.MAGENTA);
            } else {
                TypedArray ta = paintingView.getResources().obtainTypedArray(R.array.paint_colors);
                mPredefinedPaints = new Paint[ta.length()];


                for (int i = 0; i < ta.length(); i++) {
                    Paint paint = new Paint();
                    paint.setAntiAlias(true);
                    paint.setColor(ta.getColor(i, 0));
                    paint.setStrokeCap(Paint.Cap.ROUND);
                    paint.setStrokeJoin(Paint.Join.ROUND);
                    paint.setStrokeWidth(paintingView.getResources().getDimension(R.dimen.default_paint_width));
                    mPredefinedPaints[i] = paint;
                }

                mRectanglePaint = new Paint();
                mRectanglePaint.setColor(paintingView.getResources().getColor(R.color.colorAccent));
            }
        }
    }
}
