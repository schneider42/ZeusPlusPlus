package de.ccc.muc.zeusplus.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import de.ccc.muc.zeusplus.misc.Helper;

public class PresetSelectorView extends View {

	private static final int RECT_ROUND = 5;
	private static final int TEXT_SIZE = 28;
	private static final int BLOCK_HEIGHT = 80;
	private static final int PADDING_LR = 16;
	private static final int PADDING_TD = 2;
	private static final int COLOR_FRAME = Color.GRAY;
	private static final int COLOR_FRAME_HIGHLIGHT = Color.YELLOW;

	private OnClickListener mOnClickListener = null;
	private Long mLastClick = new Long(0);
	private Handler mClickHandler = new Handler();

	private Paint mPaintFrame;
	private Paint mPaintFill;
	private Paint mPaintText;

	private int mWidth = 10;

	private String mText = "#000000";

	public PresetSelectorView(Context context) {
		super(context);
		init();
	}

	public PresetSelectorView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	private void init() {
		// setPadding(PADDING, PADDING, PADDING, PADDING);

		mPaintFrame = new Paint();
		mPaintFrame.setAntiAlias(true);
		mPaintFrame.setColor(COLOR_FRAME);

		mPaintFill = new Paint();
		mPaintFill.setAntiAlias(true);
		mPaintFill.setColor(Color.BLACK);

		mPaintText = new Paint();
		mPaintText.setAntiAlias(true);
		mPaintText.setColor(Color.WHITE);
		mPaintText.setTextAlign(Align.CENTER);
		mPaintText.setTextSize(TEXT_SIZE);
		mPaintText.setTypeface(Typeface.DEFAULT_BOLD);
	}

	public void setColor(int r, int g, int b) {
		mPaintFill.setColor(Color.rgb(r, g, b));
		if (r > 128 || g > 128 || b > 128) {
			mPaintText.setColor(Color.BLACK);
		} else {
			mPaintText.setColor(Color.WHITE);
		}
		mText = "#" + Helper.tohex(r) + Helper.tohex(g) + Helper.tohex(b);

		postInvalidate();
	}

	public void blinkFrame() {
		mClickHandler.removeCallbacks(r);
		mPaintFrame.setColor(COLOR_FRAME_HIGHLIGHT);
		postInvalidate();
		mClickHandler.postDelayed(r, 200);
	}

	private Runnable r = new BlinkOffRunnable();

	private class BlinkOffRunnable implements Runnable {
		@Override
		public void run() {
			mPaintFrame.setColor(COLOR_FRAME);
			postInvalidate();
		}
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		mWidth = w - (PADDING_LR * 2);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int width = MeasureSpec.getSize(widthMeasureSpec);
		mWidth = width - (PADDING_LR * 2);
		setMeasuredDimension(width, BLOCK_HEIGHT + (2 * PADDING_TD));
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		RectF rf;

		rf = new RectF(PADDING_LR, PADDING_TD, PADDING_LR + mWidth, PADDING_TD
				+ BLOCK_HEIGHT);
		canvas.drawRoundRect(rf, RECT_ROUND, RECT_ROUND, mPaintFrame);

		rf = new RectF(PADDING_LR + 2, PADDING_TD + 2, PADDING_LR + mWidth - 2,
				PADDING_TD + BLOCK_HEIGHT - 2);
		canvas.drawRoundRect(rf, RECT_ROUND, RECT_ROUND, mPaintFill);

		canvas.drawText(mText, PADDING_LR + (mWidth / 2), PADDING_TD
				+ (BLOCK_HEIGHT / 2) + (TEXT_SIZE / 2), mPaintText);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		synchronized (mLastClick) {
			long now = System.currentTimeMillis();
			if (now - mLastClick < 200) {
				return false;
			}
			mLastClick = now;
		}

		if (mOnClickListener == null) {
			return super.onTouchEvent(event);
		}

		mOnClickListener.onClick(this);
		return true;
	}

	public void setOnClickListener(OnClickListener ocl) {
		mOnClickListener = ocl;
	}
}
