package com.example.android.softkeyboard;

import android.R.color;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Paint.Style;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;


public class EditView extends View {
	private Paint mPaint;
	private Context mContext;
	private static final String mString = "Hallelujah";
	private GestureDetector mGestureDetector;
	private boolean mScrolled;
	private int mTotalHeight;
	private int mTargetScrollY;
	private SoftKeyboard mService;
	private int mTouchY;
	/**当处于编辑状态下该值为ture，处于书写状态下该值为false*/
	private boolean mIsCompleteForm=false; 
	
	private Paint mSwitchingResponseRectPaint;
	private int mSwitchingResponseAreaWidth, mSwitchingResponseAreaHeight;
	
	private static final int FLING_MIN_DISTANCE = 80;  
    private static final int FLING_MIN_VELOCITY = 150;
    
	public EditView(Context context) {
		this(context, null);
	}
	
	public EditView(Context context, AttributeSet attrs){
		super(context, attrs);
		TypedArray typedArray=context.obtainStyledAttributes(attrs, R.styleable.EditView);
		for(int i=0; i<typedArray.getIndexCount(); i++){
			int attr=typedArray.getIndex(i);
			switch (attr) {
			case R.styleable.EditView_completeForm:
				mIsCompleteForm=typedArray.getBoolean(attr, false);
				break;
			}
		}
		typedArray.recycle();
		TypedArray a=context.obtainStyledAttributes(attrs, R.styleable.WritingView);
		mSwitchingResponseAreaHeight=a.getDimensionPixelSize(R.styleable
				.WritingView_switchingResponseAreaHeight, 50);
		mSwitchingResponseAreaWidth=a.getDimensionPixelSize(R.styleable
				.WritingView_switchingResponseAreaWidth, 50);
		mContext=context;
		mPaint = new Paint();
        mPaint.setTextSize(20);
        Log.v("XXOO", "EditViewCreated!");
        setBackgroundColor(0xbbffffff);
        mGestureDetector = new GestureDetector(new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2,
                    float distanceX, float distanceY) {
            	if(mIsCompleteForm)
            		return false;
                mScrolled = true;
                int sy = getScrollY();
                Log.v("XXOO", "scrolling..."+sy);
                sy += distanceY;
                if (sy < 0) {
                    sy = 0;
                }
                if (sy +getHeight() > mTotalHeight) {                    
                    sy = mTotalHeight-getHeight();
                }
                mTargetScrollY = sy;
                scrollTo(getScrollX(), sy);
                invalidate();
                return true;
            }
            public boolean onFling(MotionEvent e1, MotionEvent e2,
                    float velocityX, float velocityY){
            	if(!mIsCompleteForm)
            		return false;
            	Log.v("XXOO", "fling...");
            	if(e1.getX()>getWidth()-mSwitchingResponseAreaWidth&&
            			e1.getY()>getHeight()-mSwitchingResponseAreaHeight){
	    	        if (e1.getY() - e2.getY() > FLING_MIN_DISTANCE  
	    	                && Math.abs(velocityY) > FLING_MIN_VELOCITY) {  
	    	        	
		    	        //切换View
		    	        mService.onSwitch();
	    	        }
	    	    }
            	return true;
            	
            }
        });
        setWillNotDraw(false);
        mSwitchingResponseRectPaint=new Paint();
        mSwitchingResponseRectPaint.setColor(Color.YELLOW);
        mSwitchingResponseRectPaint.setStyle(Paint.Style.FILL);
	}
	public void setService(SoftKeyboard listener) {
        mService = listener;
    }
	@Override
    public int computeVerticalScrollRange() {
        return mTotalHeight;
    }
	@Override
    public boolean onTouchEvent(MotionEvent me) {
		//完整形态下不允许滚动
        	if(mGestureDetector.onTouchEvent(me)) {
        		return true;
        	}
        int action = me.getAction();
        int x = (int) me.getX();
        int y = (int) me.getY();
        mTouchY = y;

        switch (action) {
        case MotionEvent.ACTION_DOWN:
        	if(mIsCompleteForm&&x>getWidth()-mSwitchingResponseAreaWidth&&
        			y>getHeight()-mSwitchingResponseAreaHeight){
        		mSwitchingResponseRectPaint.setColor(Color.GREEN);
        	}
            mScrolled = false;
            invalidate();
            break;
        case MotionEvent.ACTION_MOVE:
            
            invalidate();
            break;
        case MotionEvent.ACTION_UP:
            if (!mScrolled) {
            	Toast.makeText(mContext ,  "点击了("+x+", "+(mTouchY+getScrollY())+")" , Toast.LENGTH_SHORT).show();
            }
            if(mIsCompleteForm)
            	restoreSwitchingResponseRectColor();
            invalidate();
            requestLayout();
            break;
        }
        return true;
    }
    protected void onDraw(Canvas canvas) {
    	if (canvas != null) {
            super.onDraw(canvas);
        }
    	if(mIsCompleteForm){
    		canvas.drawRect(getWidth()-mSwitchingResponseAreaWidth, getHeight()-mSwitchingResponseAreaHeight, 
            		getWidth(), getHeight(), mSwitchingResponseRectPaint);
    	}
		mPaint.setColor(Color.WHITE);
		mPaint.setStyle(Style.FILL);	//填充
		canvas.drawRect(new Rect(10,10,50,50), mPaint);
		mPaint.setColor(Color.GRAY);
		canvas.drawCircle(50, 100, 30, mPaint);
		mPaint.setColor(Color.MAGENTA);
		canvas.drawText(mString, 20, 140, mPaint);
		mPaint.setColor(Color.BLUE);
		canvas.drawText(mString,10,20, mPaint);
		mTotalHeight=140+10;
	}
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int measuredWidth = resolveSize(50, widthMeasureSpec);
        
        // Get the desired height of the icon menu view (last row of items does
        // not have a divider below)
        Log.v("XXOO", "measuredWdith:"+measuredWidth+"  " +
        		"measuredHeight:"+resolveSize(mTotalHeight, heightMeasureSpec));
        // Maximum possible width and desired height
        setMeasuredDimension(measuredWidth,
                resolveSize(mIsCompleteForm?mTotalHeight:50, heightMeasureSpec));
    }
    public boolean isCompleteForm(){
    	return mIsCompleteForm;
    }
    public void restoreSwitchingResponseRectColor(){
		mSwitchingResponseRectPaint.setColor(Color.YELLOW);
	}
}


