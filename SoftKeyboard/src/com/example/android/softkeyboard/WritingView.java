package com.example.android.softkeyboard;


import java.util.ArrayList;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.CornerPathEffect;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathEffect;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.MeasureSpec;

public class WritingView extends View{
	/**
	 * WritingView的listener
	 * */
	public interface OnWritingActionListener {
		/**
		 * 提笔时间超时
		 * */
		void onTimeout();
		/**
		 * 切换布局
		 * */
		void onSwitch();
		
	}
	
	private int mCharColor;
	private float mCharStrokeWidth;
	private Paint mCharPaint, mSwitchingResponseRectPaint;
	private ArrayList<Point> mPoints = new ArrayList<Point>();
	private ArrayList<Path> mPaths=new ArrayList<Path>();
	private Path mPath;
	private OnWritingActionListener mOnWritingActionListener;
	
	private int mSwitchingResponseAreaHeight, mSwitchingResponseAreaWidth;
	
	private GestureDetector mGestureDetector;
	private static final int FLING_MIN_DISTANCE = 80;  
    private static final int FLING_MIN_VELOCITY = 150;
	
	public WritingView (Context context){
		this(context, null);
	}
	public WritingView(Context context, AttributeSet attrs) {
		super(context, attrs);
		TypedArray a=context.obtainStyledAttributes(attrs, R.styleable.WritingView);
		Log.v("XXOO", "indexes: "+a.getIndexCount());
		for(int i=0; i<a.getIndexCount(); i++){
			int attr=a.getIndex(i);
			switch (attr) {
			case R.styleable.WritingView_charColor:
				mCharColor=a.getColor(attr, 0xff86abd9);
				break;
			case R.styleable.WritingView_charStrokeWidth:
				mCharStrokeWidth=a.getDimension(attr, 8);
				break;
			case R.styleable.WritingView_switchingResponseAreaHeight:
				mSwitchingResponseAreaHeight=a.getDimensionPixelSize(attr, 50);
				break;
			case R.styleable.WritingView_switchingResponseAreaWidth:
				mSwitchingResponseAreaWidth=a.getDimensionPixelSize(attr, 50);
				break;
			}
		}
		initData(context);
		initPaint();
		a.recycle();
	}
	
	private void initData(Context context) {
		setFocusable(true);
		setBackgroundColor(0xffdfdfdf);
		mGestureDetector=new GestureDetector(new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2,
                    float velocityX, float velocityY) {
            	if(e1.getX()>getWidth()-mSwitchingResponseAreaWidth&&e1.getY()<mSwitchingResponseAreaHeight){
	    	        if (e2.getY() - e1.getY() > FLING_MIN_DISTANCE  
	    	                && Math.abs(velocityY) > FLING_MIN_VELOCITY) {  
	    	        	
		    	        //切换View
		    	        mOnWritingActionListener.onSwitch();
	    	        }
	    	    }
            	return false;
    	    }
        });
	}
	private void initPaint() {
		mCharPaint = new Paint();
		mCharPaint.setColor(mCharColor);
        mCharPaint.setStrokeWidth(mCharStrokeWidth);
        mCharPaint.setAntiAlias(true);
        mCharPaint.setStyle(Paint.Style.STROKE);
        mCharPaint.setStrokeJoin(Paint.Join.ROUND);
        mCharPaint.setStrokeCap(Paint.Cap.ROUND);
        PathEffect effect = new CornerPathEffect(50f);
        mCharPaint.setPathEffect(effect);
        mSwitchingResponseRectPaint=new Paint();
        mSwitchingResponseRectPaint.setColor(Color.YELLOW);
        mSwitchingResponseRectPaint.setStyle(Paint.Style.FILL);
	}
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int widthMode = MeasureSpec.getMode(widthMeasureSpec);
		int heightMode = MeasureSpec.getMode(heightMeasureSpec);
		int widthSize = MeasureSpec.getSize(widthMeasureSpec);
		int heightSize = MeasureSpec.getSize(heightMeasureSpec);

		setMeasuredDimension(widthSize, heightSize);
	}
	
	protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawRect(getWidth()-mSwitchingResponseAreaWidth, 0, 
        		getWidth(), mSwitchingResponseAreaHeight, mSwitchingResponseRectPaint);
        if(mPoints.size()==0)
        	return ;
        else
        {
//        	for (Path path : mPaths) {
//				canvas.drawPath(path, mCharPaint);
//        	}
        	for(Point point: mPoints){
        		canvas.drawPoint(point.x, point.y, mCharPaint);
        	}
        }
	}
	public boolean onTouchEvent(final MotionEvent event)
	{
		if (mGestureDetector.onTouchEvent(event)) {
            return true;
        }
		short x = (short) event.getX();
		short y = (short) event.getY();

		Point point = new Point(x,y);
		// 触摸事件，ACTION_DOWN表示下触的点，
		if(event.getAction()==MotionEvent.ACTION_DOWN)
		{
			if(mPath==null){
				mPath=new Path();
			}
			if(x>getWidth()-mSwitchingResponseAreaWidth&&y<mSwitchingResponseAreaHeight){
				mSwitchingResponseRectPaint.setColor(Color.GREEN);
			}
			mPath.moveTo(x, y);
			mPoints.add(point);
		}
		else if(event.getAction()==MotionEvent.ACTION_MOVE)
		{
			mPath.lineTo(x, y);
			mPoints.add(point);
		}
		else if(event.getAction()==MotionEvent.ACTION_UP)
		{
			mPoints.add(point);
			mPath.lineTo(x, y);
			mPaths.add(mPath);
			restoreSwitchingResponseRectColor();
		}
		invalidate();
		return true;
	}
	public OnWritingActionListener getOnWritingActionListener(){
		return mOnWritingActionListener;
	}
	public void setOnWritingActionListener(OnWritingActionListener listener){
		mOnWritingActionListener=listener;
	}
	public void restoreSwitchingResponseRectColor(){
		mSwitchingResponseRectPaint.setColor(Color.YELLOW);
	}
	public void clearPoints(){
		mPoints.clear();
		mPath.close();
	}
}
