package de.ccc.muc.zeusplus.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class MultiColorBlockView extends View {

	private static final int RECT_ROUND = 5;
	private static final int PADDING_LR = 16;
	private static final int PADDING_TD = 2;
	private static final int SPACING = 8;
	private static final int COLOR_FRAME = Color.GRAY;
	private static final int COLOR_FRAME_HIGHLIGHT = Color.RED;

	private MultiColorBlockListener mListener = null;

	private Paint mPaintFrame;
	private Paint mPaintFill;

	private int mWidth = 10;
	private int mHeight = 10;

	private int mBlockCount = 0;
	private int[] mColors = new int[0];

	public MultiColorBlockView(Context context) {
		super(context);
		init();
	}

	public MultiColorBlockView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	private void init() {
		mPaintFrame = new Paint();
		mPaintFrame.setAntiAlias(true);
		mPaintFrame.setColor(COLOR_FRAME);

		mPaintFill = new Paint();
		mPaintFill.setAntiAlias(true);
		mPaintFill.setColor(Color.BLACK);
	}

	synchronized public void setColors(int[] colors) {
		if (colors == null) {
			colors = new int[0];
		}
		mColors = colors;
		mBlockCount = colors.length;
		postInvalidate();
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		if (mBlockCount != 0) {
			mWidth = (w - (PADDING_LR * 2) - ((mBlockCount - 1) * SPACING))
					/ mBlockCount;
			mHeight = mWidth;
		} else {
			mWidth = 0;
			mHeight = 0;
		}
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int width = MeasureSpec.getSize(widthMeasureSpec);
		if (mBlockCount != 0) {
			mWidth = (width - (PADDING_LR * 2) - ((mBlockCount - 1) * SPACING))
					/ mBlockCount;
			mHeight = mWidth;
		} else {
			mWidth = 0;
			mHeight = 0;
		}
		setMeasuredDimension(width, mHeight + (2 * PADDING_TD));
	}

	@Override
	synchronized protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		RectF rf;

		int x = PADDING_LR;
		for (int i = 0; i < mBlockCount; i++) {

			rf = new RectF(x, PADDING_TD, x + mWidth, mHeight + PADDING_TD);
			canvas.drawRoundRect(rf, RECT_ROUND, RECT_ROUND, mPaintFrame);

			mPaintFill.setColor(mColors[i]);
			rf = new RectF(x + 2, PADDING_TD + 2, x + mWidth - 2, mHeight
					+ PADDING_TD - 2);
			canvas.drawRoundRect(rf, RECT_ROUND, RECT_ROUND, mPaintFill);

			x += mWidth + SPACING;
		}

	}

	synchronized public boolean onTouchEvent(MotionEvent event) {
		if (mListener == null) {
			return false;
		}

		long eventTime = event.getEventTime();
		long eventDown = event.getDownTime();

		boolean longpress = (eventTime - eventDown) > 1000;

		if (MotionEvent.ACTION_UP != event.getAction()) {
			if (longpress) {
				mPaintFrame.setColor(COLOR_FRAME_HIGHLIGHT);
				postInvalidate();
			}
			return true;
		}
		mPaintFrame.setColor(COLOR_FRAME);

		int px = (int) event.getX();
		int idx = -1;
		int x = PADDING_LR;
		for (int i = 0; i < mBlockCount; i++) {
			if (px >= x && px <= x + mWidth) {
				idx = i;
			}
			x += mWidth + SPACING;
		}

		if (idx != -1) {
			if (!longpress) {
				mListener.changeColor(mColors[idx]);
			} else {
				mListener.changePreset(idx);
			}
		}

		return true;
	}

	synchronized public void setListener(MultiColorBlockListener mcbl) {
		mListener = mcbl;
	}
}
