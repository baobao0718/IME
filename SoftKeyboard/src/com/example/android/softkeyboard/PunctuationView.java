/**
 * PunctinoView
 * 该View里面放置若干常用标点符号按钮，支持滚动，根据xml中列出的标点生成相应的按钮，放置在WritingView的右侧
 */

package com.example.android.softkeyboard;


import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

public class PunctuationView extends View{
	private int mButtonHeight;
	private int mButtonWidth;
	private Drawable mButtonBackground;
	private int mButtonTextSize;
	
	private Context mContext;
	private String mCommonPunctuations;
	private Paint mPaint;
	private Drawable mSelectionHighlight;
	private Rect mBgPadding;
	private GestureDetector mGestureDetector;
	private boolean mScrolled;
	private int mTargetScrollY;
	private int mTouchY;
	private int mSelectedIndex;
	private int mTotalHeight;
	
	private static final int BUTTON_ID=0x7f077000;
	
	public PunctuationView(Context context) {
		this(context, null);
//		setBackgroundColor(Color.GREEN);
//		LayoutInflater inflater =(LayoutInflater) getContext()
//				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//		inflater.inflate(R.xml.punctuation_xml,null);
		// TODO Auto-generated constructor stub
	}
	public PunctuationView(Context context, AttributeSet attrs) {
		// TODO Auto-generated constructor stub
		super(context, attrs);
		TypedArray typedArray=context.obtainStyledAttributes(attrs, R.styleable.PunctuationView);
		for(int i=0; i<typedArray.getIndexCount(); i++){
			int attr=typedArray.getIndex(i);
			switch (attr) {
			case R.styleable.PunctuationView_buttonHeight:
				mButtonHeight=typedArray.getDimensionPixelSize(attr, 30);
				break;
			case R.styleable.PunctuationView_buttonWidth:
				mButtonWidth=typedArray.getDimensionPixelSize(attr, 40);
				break;
			case R.styleable.PunctuationView_buttonBackground:
				mButtonBackground=typedArray.getDrawable(attr);
				break;
			case R.styleable.PunctuationView_buttonTextSize:
				mButtonTextSize=typedArray.getDimensionPixelSize(attr, 18);
				break;
			
			}
		}
		mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setTextSize(mButtonTextSize);
        mPaint.setStrokeWidth(0);
        mContext=context;
        mSelectedIndex = -1;
        mSelectionHighlight = context.getResources().getDrawable(
                android.R.drawable.list_selector_background);
        mSelectionHighlight.setState(new int[] {
                android.R.attr.state_enabled,
                android.R.attr.state_focused,
                android.R.attr.state_window_focused,
                android.R.attr.state_pressed
        });
        setBackgroundColor(0xbbd6eed8);
		Log.v("XXOO", "H:"+mButtonHeight+""+"W:"+mButtonWidth+"T:"+mButtonTextSize);
		mGestureDetector=new GestureDetector(new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2,
                    float distanceX, float distanceY) {
                Log.v("XXOO", "scrolling...");
            	mScrolled = true;
                int sy = getScrollY();
                sy += distanceY;
                if (sy < 0) {
                    sy = 0;
                }
                if (sy + getHeight() > mTotalHeight) {                    
                    sy =mTotalHeight-getHeight();
                }
                mTargetScrollY = sy;
                scrollTo(getScrollX(),sy);
                invalidate();
                return true;
            }
        });
		typedArray.recycle();
	}
	private void DrawButtons(Canvas canvas) {
		// TODO Auto-generated method stub
		mCommonPunctuations=getResources().getString(R.string.common_punctuations);
		if (mBgPadding == null) {
            mBgPadding = new Rect(0, 0, 0, 0);
            if (getBackground() != null) {
                getBackground().getPadding(mBgPadding);
            }
        }
		int y=0;
		int x; 
		int scrollY = getScrollY();
		int touchY = mTouchY;
		boolean scrolled = mScrolled;
		int buttonHeight = mButtonHeight;
		Rect bgPadding=mBgPadding;
		for(int i=0; i<mCommonPunctuations.length(); i++){
			char buttonLabel=mCommonPunctuations.charAt(i);
			Paint paint=mPaint;
			x= (int) (((getWidth() - mPaint.measureText(buttonLabel+"")) / 2) );
			if (touchY + scrollY >= y && touchY + scrollY < y + buttonHeight && !scrolled) {
                if (canvas != null) {
                    canvas.translate(0, y);
                    mSelectionHighlight.setBounds(bgPadding.left, 0, getWidth(), buttonHeight);
                    mSelectionHighlight.draw(canvas);
                    canvas.translate(0, -y);
                }
                mSelectedIndex = i;
            }
			if (canvas != null) {
                paint.setColor(Color.BLACK);
                canvas.drawText(buttonLabel+"", x, y+(buttonHeight-mPaint.getTextSize())/2- mPaint.ascent(), paint);
                paint.setColor(Color.GRAY); 
                canvas.drawLine(bgPadding.left, y + buttonHeight + 0.5f,  
                        getWidth()+1, y+buttonHeight + 0.5f, paint);
                paint.setFakeBoldText(false);
            }
            y += buttonHeight;
        }
        mTotalHeight = y;
			
	}
	
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}
	protected void onDraw(Canvas canvas) {
    	if (canvas != null) {
            super.onDraw(canvas);
        }
    	DrawButtons(canvas);
	}
	public boolean onTouchEvent(MotionEvent me) {

        if (mGestureDetector.onTouchEvent(me)) {
            return true;
        }

        int action = me.getAction();
        int x = (int) me.getX();
        int y = (int) me.getY();
        mTouchY = y;

        switch (action) {
        case MotionEvent.ACTION_DOWN:
            mScrolled = false;
            invalidate();
            break;
        case MotionEvent.ACTION_MOVE:
            invalidate();
            break;
        case MotionEvent.ACTION_UP:
            if (!mScrolled) {
                if (mSelectedIndex >= 0) {
                	Toast.makeText(mContext ,  "点击了("+mSelectedIndex+")" , Toast.LENGTH_SHORT).show();
                }
            }
            mSelectedIndex = -1;
            requestLayout();
            break;
        }
        return true;
    }
}
