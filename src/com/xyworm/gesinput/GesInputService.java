package com.xyworm.gesinput;

import java.util.ArrayList;

import android.graphics.PixelFormat;
import android.inputmethodservice.InputMethodService;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.view.inputmethod.EditorInfo;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.xyworm.ringsdk.OnRingEventListener;
import com.xyworm.ringsdk.RingManager;
import com.xyworm.ringsdk.utils.RawEvent;

/** 
 * @ClassName  GesInputService 
 * @Description  输入法的主要服务
 * @author linweizh@qq.com
 * @date 2014-11-14  
 */ 
public class GesInputService extends InputMethodService implements
		DetectView.OnEventListener, OnRingEventListener {

	private final String TAG = "GesInputService";
	/** 主要输入视图 */ 
	private View mInputView;
	/** 检测视图 */ 
	private DetectView mDetectView;
	/** 窗口管理器 */ 
	private WindowManager mWindowManager;
	/** 用于显示手势的视图 */ 
	private TrajectoryView mTrajectoryView;
	/** 指环事件管理器 */ 
	private RingManager mRingManager;
	/** 手势识别器 */ 
	private RingTrajectory mRingTrajectory;
	/** 候选词视图 */ 
	private LinearLayout mCandidateView;
	/** 候选词控制器 */ 
	private CandiControl mCandiControl;

	/** 是否由触摸屏来获取手势事件 */ 
	private static boolean isTouchGestureMode = false;
	/** 保留长按缓冲时间内的点 */
	public static ArrayList<RawEvent> preparePoint = new ArrayList<RawEvent>();
	
	@Override
	public void onCreate() {
		super.onCreate();
		initDetectView();
		mRingTrajectory = new RingTrajectory(this);
		mRingManager = new RingManager();
		mRingManager.setRingName("Logitech"); // Logitech,MTK
		mRingManager.setRingEventListener(this);
	}

	@Override
	public View onCreateInputView() {
		Log.i(TAG, "onCreateInputView");
		mInputView = getLayoutInflater().inflate(R.layout.inputview, null);
		mTrajectoryView = (TrajectoryView) mInputView
				.findViewById(R.id.trajectoryView);
		mCandidateView = (LinearLayout) mInputView
				.findViewById(R.id.candiView1);
		mCandiControl = new CandiControl(this, mCandidateView,mTrajectoryView);
		return mInputView;
	}

	/**
	 * 初始化最上层的检测视图
	 */
	private void initDetectView() {
		mWindowManager = (WindowManager) this.getSystemService(WINDOW_SERVICE);
		mDetectView = new DetectView(this);
		mDetectView.setOnEventListener(this);
		WindowManager.LayoutParams mParams = new LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT,
				LayoutParams.TYPE_SYSTEM_ERROR, // 2010
				LayoutParams.FLAG_FULLSCREEN // 400
						| LayoutParams.FLAG_NOT_TOUCH_MODAL // 20
						| LayoutParams.FLAG_LAYOUT_IN_SCREEN
						| LayoutParams.FLAG_NOT_FOCUSABLE, // 8
				PixelFormat.TRANSLUCENT); // -3
		mWindowManager.addView(mDetectView, mParams);
		mDetectView.setVisibility(View.GONE);

	}

	@Override
	public void onStartInput(EditorInfo attribute, boolean restarting) {
		super.onStartInput(attribute, restarting);
		Log.i(TAG, "onStartInput");

	}

	@Override
	public void onFinishInput() {
		super.onFinishInput();
		Log.i(TAG, "onFinishInput");

	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		mWindowManager.removeView(mDetectView);
	}

	@Override
	public void onWindowShown() {
		super.onWindowShown();
		Log.i(TAG, "onWindowShown");
		mDetectView.setVisibility(View.VISIBLE);
		if(!isTouchGestureMode)
			mRingManager.runListener();

	}

	@Override
	public void onWindowHidden() {
		super.onWindowHidden();
		Log.i(TAG, "onWindowHidden");
		mDetectView.setVisibility(View.GONE);
		if(!isTouchGestureMode)
			mRingManager.killListener();
		mRingTrajectory.commitPath();  // 清除轨迹
		mCandiControl.clear();
	}

	@Override
	public void onDeteClick() {
		Log.i(TAG, "Click");
		mCandiControl.clickAndCommit(); 
		mRingTrajectory.commitPath(); 
	}

	@Override
	public void onDeteDoubleClick() {
		Log.i(TAG, "DoubleClick");
		this.hideWindow();
	}

	@Override
	public void onDeteMove(MotionEvent ev) {
		if (isTouchGestureMode) {
			mRingTrajectory.onInputEventValues(ev);
		}
	}

	@Override
	public void onDeteGesStart() {
		mRingTrajectory.onActionDown();
		// 将预先接收到的点加入手势当中
		mRingTrajectory.addPrePoints(preparePoint);

	}

	@Override
	public void onDeteGesEnd() {
		mRingTrajectory.onActionUp();
		
	}

	@Override
	public void onDirectEvent(int deviecDataStruct) {
	
	}

	private int leftIncrese = 0;
	private int rightIncrese = 0;

	@Override
	public void onRawEvent(RawEvent event) {
		// 处于按下但处于判断是否长按的延时缓冲时间当中
		if(!DetectView.isTouching && DetectView.isDown){
			preparePoint.add(event);
		}

		// 进入手势模式了并且屏蔽了屏幕触摸事件
		if (DetectView.isTouching && !isTouchGestureMode) {
			mRingTrajectory.onInputEventValues(event);
			return;
		}
		if (event.getType() != RawEvent.EV_REL
				|| event.getCode() != RawEvent.REL_X)
			return;
		int value = event.getValue();
		if (Math.abs(value) > 20) {
			if (value > 0)
				rightIncrese += value;
			else
				leftIncrese += value;
			if (Math.abs(leftIncrese) > 400) {
				leftIncrese = 0;
				mCandiControl.moveLeft();
			}

			if (Math.abs(rightIncrese) > 400) {
				rightIncrese = 0;
				mCandiControl.moveRight();
			}
		}

	}

	@Override
	public void onError(String msg) {
		Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();

	}

	/**
	 * 由RingTrajectory回调，表示一个字书写完成
	 * @param name 
	 */
	public void onInputOver(ArrayList<String> suggestions) {
		/*if (!RingTrajectory.RT_Gesture_Valid) {
			Toast.makeText(this, "手势无效", Toast.LENGTH_SHORT).show();
			return;
		}*/
		mCandiControl.updateSuggestions(suggestions);
		// Toast.makeText(this, suggestions, Toast.LENGTH_SHORT).show();
	}

}
