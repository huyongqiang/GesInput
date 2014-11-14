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
 * @Description  ���ڼ�ⴥ���¼���ȫ��͸��������ͼ
 * @author linweizh@qq.com
 * @date 2014-11-14 
 */ 
public class DetectView extends View {

	final static String TAG = "DetectView";
	/** ��¼����ʱ�ĵ����꣬��̧��������бȶԣ������ж��Ƿ�Ϊ�о���Ļ������� */
	private Point prePoint = new Point();
	/** �ϴΰ��µ�ʱ�� */
	private long preDowntime;
	/** �ϴ�̧���ʱ�� */ 
	private long preUptime;
	/** �Ƿ����ڴ����¼��� */ 
	public static boolean isTouching;
	
	private OnEventListener l;
	
	/** ˫���¼�������� */ 
	public final long DOUBLE_CLICK_SPACE = 200L;
	/** �����������¼�������� */ 
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
		// alphaֵԽ����ĻԽ��
		this.setBackgroundColor(Color.argb(0, 0, 100, 0));

	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
//		Log.i(TAG, "onTouchEvent" + event.getAction());
		long currTime = event.getEventTime();
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			// ��ʱ�ж��Ƿ�Ϊ����
			mHandler.postDelayed(touchRunnable, LONG_TOUCH_SPACE);
			prePoint.x = (int) event.getRawX();
			prePoint.y = (int) event.getRawY();
			preDowntime = currTime;
		}

		if (event.getAction() == MotionEvent.ACTION_UP) {
			// ���������ʱ
			mHandler.removeCallbacks(touchRunnable);
			if(isTouching){
				l.onDeteGesEnd();
				isTouching = false;
			}
			// ˫������
			if (currTime - preUptime < DOUBLE_CLICK_SPACE) {
				// ���������ʱ
				mHandler.removeCallbacks(clickRunnable);
				l.onDeteDoubleClick();
				return false;
			}
			// ������ʱ����������˫����ͻ
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
	 * @Description  �������¼��ļ����ӿ�
	 * @author linweizh@qq.com
	 * @date 2014-11-14  
	 */ 
	public interface OnEventListener{
		/**
		 *  �����¼�
		 */
		void onDeteClick();
		/**
		 *  ˫���¼�
		 */
		void onDeteDoubleClick();
		/**
		 *  �ƶ��¼�
		 * @param ev MotionEvent
		 */
		void onDeteMove(MotionEvent ev);
		
		/**
		 *  ���ƿ�ʼ
		 */
		void onDeteGesStart();
		/**
		 *  ���ƽ���
		 */
		void onDeteGesEnd();
	}
	

}
