package com.ayuan.guaguale;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

public class GuaGuaKa extends View {

	private Paint mOutterPaint;
	private Path mPath;
	private Canvas mCanvas;
	private Bitmap mBitmap;
	
	private int mLastX;
	private int mLastY;
	
	private Bitmap mOutterBitmap;
	
	//private Bitmap bitmap;
	
	private String mText;
	private Paint mBackPaint;
	
	/**
	 * 记录文本信息的宽和高
	 */
	private Rect mTextBound;
	private int mTextSize;
	private int mTextColor;
	
	/**
	 * 判断遮盖层区域是否达到预值(60%)
	 */
	private volatile boolean mComplete = false;
	
	private OnCompletedListener mListener;
	
	public interface OnCompletedListener{
		public void onCompleted();
	}
	
	public void setOnCompletedListener(OnCompletedListener listener){
		mListener = listener;
	}
	
	
	public GuaGuaKa(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		
		TypedArray ta = context.getTheme().obtainStyledAttributes(attrs, R.styleable.GuaGuaKa, defStyleAttr, 0);
		int n = ta.getIndexCount();
		for (int i = 0; i < n; i++) {
			int attr = ta.getIndex(i);
			switch (attr) {
			case R.styleable.GuaGuaKa_text:
				mText = ta.getString(attr);
				break;

			case R.styleable.GuaGuaKa_textColor:
				mTextColor = ta.getColor(attr, 0x00000000);
				break;
				
			case R.styleable.GuaGuaKa_textSize:
				mTextSize = (int) ta.getDimension(attr, TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 22, getResources().getDisplayMetrics()));
				break;
			}
		}
		ta.recycle();
		
		init();
	}

	public GuaGuaKa(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public GuaGuaKa(Context context) {
		this(context, null);
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		
		int width = getMeasuredWidth();
		int height = getMeasuredHeight();
		
		// 初始化bitmap
		mBitmap = Bitmap.createBitmap(width, height, Config.ARGB_8888);
		mCanvas = new Canvas(mBitmap);
		
		setupOutPaint();
		setupBackPaint();
		
		//mCanvas.drawColor(Color.parseColor("#c0c0c0"));
		mCanvas.drawRoundRect(new RectF(0, 0, width, height), 30, 30, mOutterPaint);
		mCanvas.drawBitmap(mOutterBitmap, null, new Rect(0,0,width,height), null);
	}

	/**
	 * 设置获奖信息的画笔
	 */
	private void setupBackPaint() {
		mBackPaint.setColor(mTextColor);
		mBackPaint.setStyle(Style.FILL);
		mBackPaint.setTextSize(mTextSize);
		// 获得文本信息的宽和高
		mBackPaint.getTextBounds(mText, 0, mText.length(), mTextBound);;
		
	}

	private void setupOutPaint() {
		// 设置绘制path画笔的一些属性
		mOutterPaint.setColor(Color.parseColor("#c0c0c0"));
		mOutterPaint.setAntiAlias(true);
		mOutterPaint.setDither(true);
		mOutterPaint.setStrokeJoin(Paint.Join.ROUND);
		mOutterPaint.setStrokeCap(Paint.Cap.ROUND);
		mOutterPaint.setStyle(Style.FILL);
		mOutterPaint.setAlpha(150);
		mOutterPaint.setStrokeWidth(20);
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		
		int x = (int) event.getX();
		int y = (int) event.getY();
		
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			mLastX = x;
			mLastY = y;
			mPath.moveTo(mLastX, mLastY);
			break;

		case MotionEvent.ACTION_MOVE:
			int dx = Math.abs(x - mLastX);
			int dy = Math.abs(y - mLastY);
			if(dx > 3 || dy > 3){
				mPath.quadTo(mLastX, mLastY, x, y);
			}
			
			mLastX = x;
			mLastY = y;
			break;
			
		case MotionEvent.ACTION_UP:
			mPath.reset();
			if (!mComplete)
				new Thread(mRunnable).start();
			break;
		}
		if (!mComplete)
			invalidate();
		return true;
	}
	
	private Runnable mRunnable = new Runnable() {
		
		@Override
		public void run() {
			int w = getWidth();
			int h = getHeight();
			
			float wipeArea = 0;
			float totalArea = w*h;
			
			Bitmap bitmap = mBitmap;
			int[] mPixels = new int[w*h];
			
			// 获得Bitmap上所有像素信息
			bitmap.getPixels(mPixels, 0, w, 0,0, w, h);
			
			for (int i = 0; i < w; i++) {
				for (int j = 0; j < h; j++) {
					int index = i + j*w;
					
					if(mPixels[index] == 0){
						wipeArea++;
					}
				}
			}
			
			if(wipeArea > 0 && totalArea > 0){
				int percent = (int) (wipeArea * 100 / totalArea);
				if(percent > 60){
					// 清除图层区域
					mComplete = true;
					postInvalidate();
				}
			}
		}
	};
	
	
	@Override
	protected void onDraw(Canvas canvas) {
		//canvas.drawBitmap(bitmap, 0, 0, null);
		canvas.drawText(mText, getWidth()/2 - mTextBound.width()/2, getHeight()/2 + mTextBound.height()/2, mBackPaint);
		if(!mComplete ){
			drawPath();
			canvas.drawBitmap(mBitmap, 0, 0, null);
		}
		if (mComplete)
		{
			if (mListener != null)
			{
				mListener.onCompleted();
			}
		}
	}
	
	private void drawPath() {
		mOutterPaint.setColor(Color.RED);
		mOutterPaint.setStyle(Style.STROKE);
		mOutterPaint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
		mCanvas.drawPath(mPath, mOutterPaint);
	}

	private void init() {
		mOutterPaint = new Paint();
		mPath = new Path();
		
		//bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.t2);
		mOutterBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.fg_guaguaka);
	
		//mText = "谢谢惠顾";
		mTextBound = new Rect();
		mBackPaint = new Paint();
		//mTextSize = 30;
	}


}
