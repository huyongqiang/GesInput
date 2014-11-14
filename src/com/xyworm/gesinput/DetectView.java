package com.xyworm.gesinput;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/** 
 * @ClassName  DetectView 
 * @Description  用于检测触摸事件的全屏透明悬浮视图
 * @author linweizh@qq.com
 * @date 2014-11-14 
 */ 
public class DetectView extends View {

	final static String TAG = "DetectView";
	/** 记录按下时的点坐标，与抬起坐标进行比对，用于判断是否为有距离的滑动或点击 */
	private Point prePoint = new Point();
	/** 上次按下的时间 */
	private long preDowntime;
	/** 上次抬起的时间 */ 
	private long preUptime;
	/** 是否正在触摸事件中 */ 
	public static boolean isTouching;
	
	private OnEventListener l;
	
	/** 双击事件的最大间隔 */ 
	public final long DOUBLE_CLICK_SPACE = 200L;
	/** 长按，触摸事件的最大间隔 */ 
	public final long LONG_TOUCH_SPACE = 130L;

	static Handler mHandler = new Handler();
	Runnable clickRunnable = new Runnable() {
		@Override
		public void run() {
			l.onDeteClick();
		}
	};
	Runnable touchRunnable = new Runnable() {
		@Override
		public void run() {
			isTouching = true;
			l.onDeteGesStart();
		}
	};
	
	public void setOnEventListener(OnEventListener l){
		this.l = l;
	}

	public DetectView(Context context) {
		super(context);
		init();
	}

	public DetectView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	private void init() {
		preDowntime = 0;
		preUptime = 0;
		isTouching = false;
		// alpha值越大，屏幕越暗
		this.setBackgroundColor(Color.argb(0, 0, 100, 0));

	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
//		Log.i(TAG, "onTouchEvent" + event.getAction());
		long currTime = event.getEventTime();
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			// 延时判断是否为滑动
			mHandler.postDelayed(touchRunnable, LONG_TOUCH_SPACE);
			prePoint.x = (int) event.getRawX();
			prePoint.y = (int) event.getRawY();
			preDowntime = currTime;
		}

		if (event.getAction() == MotionEvent.ACTION_UP) {
			// 清除滑动延时
			mHandler.removeCallbacks(touchRunnable);
			if(isTouching){
				l.onDeteGesEnd();
				isTouching = false;
			}
			// 双击操作
			if (currTime - preUptime < DOUBLE_CLICK_SPACE) {
				// 清除单击延时
				mHandler.removeCallbacks(clickRunnable);
				l.onDeteDoubleClick();
				return false;
			}
			// 单击延时操作，避免双击冲突
			if (currTime - preDowntime < LONG_TOUCH_SPACE) {
				mHandler.postDelayed(clickRunnable, DOUBLE_CLICK_SPACE);
			}
			preUptime = currTime;

		}
		
		if (event.getAction() == MotionEvent.ACTION_MOVE) {
			if(isTouching){
				l.onDeteMove(event);
			}
		}
		return true;
	}
	
	/** 
	 * @ClassName  OnEventListener 
	 * @Description  各触摸事件的监听接口
	 * @author linweizh@qq.com
	 * @date 2014-11-14  
	 */ 
	public interface OnEventListener{
		/**
		 *  单击事件
		 */
		void onDeteClick();
		/**
		 *  双击事件
		 */
		void onDeteDoubleClick();
		/**
		 *  移动事件
		 * @param ev MotionEvent
		 */
		void onDeteMove(MotionEvent ev);
		
		/**
		 *  手势开始
		 */
		void onDeteGesStart();
		/**
		 *  手势结束
		 */
		void onDeteGesEnd();
	}
	

}
