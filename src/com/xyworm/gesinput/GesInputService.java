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
 * @Description  ���뷨����Ҫ����
 * @author linweizh@qq.com
 * @date 2014-11-14  
 */ 
public class GesInputService extends InputMethodService implements
		DetectView.OnEventListener, OnRingEventListener {

	private final String TAG = "GesInputService";
	/** ��Ҫ������ͼ */ 
	private View mInputView;
	/** �����ͼ */ 
	private DetectView mDetectView;
	/** ���ڹ����� */ 
	private WindowManager mWindowManager;
	/** ������ʾ���Ƶ���ͼ */ 
	private TrajectoryView mTrajectoryView;
	/** ָ���¼������� */ 
	private RingManager mRingManager;
	/** ����ʶ���� */ 
	private RingTrajectory mRingTrajectory;
	/** ��ѡ����ͼ */ 
	private LinearLayout mCandidateView;
	/** ��ѡ�ʿ����� */ 
	private CandiControl mCandiControl;

	/** �Ƿ��ɴ���������ȡ�����¼� */ 
	private static boolean isTouchGestureMode = false;
	/** ������������ʱ���ڵĵ� */
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
	 * ��ʼ�����ϲ�ļ����ͼ
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
		mRingTrajectory.commitPath();  // ����켣
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
		// ��Ԥ�Ƚ��յ��ĵ�������Ƶ���
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
		// ���ڰ��µ������ж��Ƿ񳤰�����ʱ����ʱ�䵱��
		if(!DetectView.isTouching && DetectView.isDown){
			preparePoint.add(event);
		}

		// ��������ģʽ�˲�����������Ļ�����¼�
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
	 * ��RingTrajectory�ص�����ʾһ������д���
	 * @param name 
	 */
	public void onInputOver(ArrayList<String> suggestions) {
		/*if (!RingTrajectory.RT_Gesture_Valid) {
			Toast.makeText(this, "������Ч", Toast.LENGTH_SHORT).show();
			return;
		}*/
		mCandiControl.updateSuggestions(suggestions);
		// Toast.makeText(this, suggestions, Toast.LENGTH_SHORT).show();
	}

}
