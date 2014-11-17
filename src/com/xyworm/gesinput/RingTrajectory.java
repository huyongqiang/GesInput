/**
 * @author WY
 *
 * ���ܣ���ָ�켣�ࡪ��
 * 	     
 * �㷨��
 * 1.ͨ�����������������Ǵ�������ֵ�������һ����άƽ��Ĺ켣��
 * 2.��һ��ʱ����������������ʻ������ƹ켣��
 * 3.�ð���״̬������ ʶ��켣ģʽ������ģʽ;
 * 4.����Android������ʶ��ӿڣ�����غõ����ƿ�ȶ�ʶ��� ��ָ�켣�����ع켣����
 * 
 * 
 * */

package com.xyworm.gesinput;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import android.content.Context;
import android.gesture.Gesture;
import android.gesture.GestureLibraries;
import android.gesture.GestureLibrary;
import android.gesture.GesturePoint;
import android.gesture.GestureStroke;
import android.gesture.Prediction;
import android.graphics.Matrix;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RectF;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.WindowManager;
import android.widget.Toast;

import com.xyworm.ringsdk.utils.RawEvent;

/**
 * @ClassName RingTrajectory
 * @Description ��ָ���Ƽ��
 * @author linweizh@qq.com
 * @date 2014-11-14
 */
public class RingTrajectory {

	/** ��Ļ��� */
	private static float ScreenWidth;
	private static float ScreenHeight;
	private Context mContext;
	/** ���� */
	private GestureLibrary RT_Library;
	private ArrayList<GesturePoint> RT_GesturePointTogether = new ArrayList<GesturePoint>();
	private Gesture RT_Gesture;
	private GestureStroke RT_GestureStroke = null;
	private String RT_GESTURE_MSG = new String("");
	/** ��ǰ�����Ƿ���Ч */
	public static boolean RT_Gesture_Valid = false;
	/** ��С���Ƴ��� */
	private static final int MIN_POINT_SIZE = 15;

	/** ·������ֵ */
	public static final float SCALE = 1.0f;
	/** ��������ֵ */
	public static final float Threshold_X_GRY = 0.001f;
	public static final float Threshold_Y_GRY = 0.001f;

	/** ���ݰ����ж��Ƿ񲶻�켣 */
	private static boolean isCaptureGesture = false;
	/** ʱ��� */
	// private static final float NS2S = 1.0f / 1000000000.0f;
	private static final float NS2S = 1.0f;
	/** �ʻ�ʱ���� */
	private static double STROKE_TIME_SPAN = 0;
	private static final double STROKE_TIME_SPAN_Threshold = 1;
	/** ʶ��ģʽ */
	private static final int TRAJECTORY = 1;
	private static final int DIRECTION = 0;
	private static int Recognise_Gesture_State = DIRECTION;
	/** ����״̬ */
	private static final int UP = 0;
	private static final int DOWN = 1;
	public static int ButtonState = UP;

	/** ���ƶ�ά�켣 */
	private static Path RT_Path = new Path();

	/** �ƶ������� */
	private PointF RT_Point = new PointF();// ��ǰ��
	private PointF RT_Prepoint = new PointF();// ��ǰ��

	/** �������� */
	public static final String GES_TYPE_HOME = "circle";
	public static final String GES_TYPE_BACK = "back";
	public static final String GES_TYPE_TRIANGLE = "triangle";
	// public static final String GES_TYPE_YES = "yes";

	public static final String GES_TYPE_ONE = "one";
	// public static final String GES_TYPE_TWO = "two";
	public static final String GES_TYPE_THREE = "three";
	public static final String GES_TYPE_FOUR = "four";
	public static final String GES_TYPE_FIVE = "five";
	public static final String GES_TYPE_SIX = "six";
	public static final String GES_TYPE_SEVEN = "seven";
	public static final String GES_TYPE_EIGHT = "eight";
	public static final String GES_TYPE_NINE = "nine";

	public static final String GES_TYPE_LET_M = "m";
	public static final String GES_TYPE_LET_V = "v";
	public static final String GES_TYPE_LET_W = "w";
	public static final String GES_TYPE_LET_X = "x";
	public static final String GES_TYPE_LET_Z = "z";

	private Path Adapter_Path = new Path();

	public RingTrajectory(Context context) {
		mContext = context;
		loadTrajectory();
		RT_Path.moveTo(0, 0);
	}

	/**
	 * ��ȡ��Ļ��ߵ�ͨ�÷���
	 */
	public void getScreenHW() {
		WindowManager manager = (WindowManager) mContext
				.getSystemService(Context.WINDOW_SERVICE);
		DisplayMetrics dm = new DisplayMetrics();
		manager.getDefaultDisplay().getMetrics(dm);
		ScreenWidth = dm.widthPixels;
		ScreenHeight = dm.heightPixels;
	}

	/** ��ʼ���켣 */
	private void initalTrajectory() {

		// ��ȡ��Ļ��� ��ȷ����Ļ����ÿһ�����ƿ�ʼǰ������������»�ȡ
		getScreenHW();
		RT_Point.set(ScreenWidth / 2, ScreenHeight / 2);
		RT_Prepoint.set(ScreenWidth / 2, ScreenHeight / 2);
		RT_Path.reset();
		RT_Path.moveTo(RT_Point.x, RT_Point.y);
	}

	/** ���ƿ� */
	private boolean loadTrajectory() {
		// �������ƿ�
		RT_Library = GestureLibraries.fromRawResource(mContext,
				R.raw.gestures_allhan);// ���������ļ�
		if (!RT_Library.load()) {
			Toast.makeText(mContext, "���ƿ����ʧ��", Toast.LENGTH_SHORT).show();
			return false;
		}
		initalTrajectory();
		return true;
	}

	/** ���Ʊȶ� */
	private String recogniseGesture(Gesture ges) {
		ArrayList<Prediction> gestures = RT_Library.recognize(ges);// ���ȫ��Ԥ����
		int index = 0;// ���浱ǰԤ���������
		double score = 0.0;// ���浱ǰԤ��ĵ÷�
		for (int i = 0; i < gestures.size(); i++) {// ������ƥ����
			Prediction result = gestures.get(i);// ���һ��Ԥ����
			if (result.score > score) {
				index = i;
				score = result.score;
			}
		}
		return gestures.get(index).name;
	}

	Comparator<Prediction> comparator = new Comparator<Prediction>() {
		public int compare(Prediction s1, Prediction s2) {
			return s1.score - s2.score > 0 ? 1 : 0;
		}
	};

	/**
	 * ��ȡ���ƽ��������
	 * 
	 * @return String
	 */
	public String getGestureSort() {
		StringBuilder sb = new StringBuilder();
		ArrayList<Prediction> gestures = RT_Library.recognize(RT_Gesture);
		Collections.sort(gestures, comparator);
		for (int i = 0; i < gestures.size(); i++) {
			Prediction result = gestures.get(i);
			sb.append(result.name);
		}
		return sb.toString();
	}

	/** ������� */
	public void addGesture(final String gesName) {
		RT_Library.addGesture(gesName, RT_Gesture);
		// �������ƿ�
		RT_Library.save();
	}

	/** ��ý�ָ�������¼�ֵ */
	private int rel_x, rel_y;

	public void onInputEventValues(RawEvent ev) {

		double timestamp = ev.getTimestamp();
		int code = ev.getCode();
		int value = ev.getValue();
		int type = ev.getType();

		if (type == RawEvent.EV_REL) {
			if (code == RawEvent.REL_X)
				rel_x = value;
			if (code == RawEvent.REL_Y)
				rel_y = value;
		}

		if (type == RawEvent.EV_SYN) {
			RT_Point.x += (float) rel_x * SCALE;
			RT_Point.y += (float) rel_y * SCALE;
			RT_GesturePointTogether.add(new GesturePoint(RT_Point.x,
					RT_Point.y, (long) (timestamp * 1.0E6)));
			RT_Path.quadTo(RT_Prepoint.x, RT_Prepoint.y, RT_Point.x, RT_Point.y);
			RT_Prepoint.set(RT_Point);

		}
		Log.i("onInputFromRing", RT_Point.x + " " + RT_Point.y);
	}

	/**
	 * �յ�����Ļ�����귢���Ľ�����Ϣ
	 */
	double px, py;

	public void onInputEventValues(MotionEvent ev) {
		long timestamp = ev.getEventTime();
		if (px != 0 || py != 0) {
			RT_Point.x += (ev.getRawX() - px);
			RT_Point.y += (ev.getRawY() - py);
			px = ev.getRawX();
			py = ev.getRawY();
			RT_GesturePointTogether.add(new GesturePoint(RT_Point.x,
					RT_Point.y, timestamp));
			RT_Path.quadTo(RT_Prepoint.x, RT_Prepoint.y, RT_Point.x, RT_Point.y);
			RT_Prepoint.set(RT_Point);
			Log.i("onInputFromScreen", RT_Point.x + " " + RT_Point.y);

		} else {
			px = ev.getRawX();
			py = ev.getRawY();
		}
	}

	/**
	 * ��ָ���������¼�
	 */
	public void onActionDown() {
		Log.i("****", "onActionDown");
		// ��ʼ��mGesture
		RT_Gesture = new Gesture();
		RT_GesturePointTogether.clear();
		initalTrajectory();
		RT_Gesture_Valid = true;
		px = py = 0;
	}

	/**
	 * ��ָ����̧���¼�
	 */
	public void onActionUp() {
		Log.i("****", "onActionUp");
		// ·��̫�̻�����Ϊ�࿪�̵߳�һ��ִ����RT_GesturePointTogether.clear();
		// ����ִֻ��һ��
		if (RT_GesturePointTogether.size() < MIN_POINT_SIZE) {
			Log.i("****", RT_GesturePointTogether.size()
					+ "---toSmallToReconize---");
			RT_Gesture_Valid = false;
			return;
		}
		// this.pointToPath();
		STROKE_TIME_SPAN = 0;
		// ��������
		RT_GestureStroke = new GestureStroke(RT_GesturePointTogether);
		RT_Gesture.addStroke(RT_GestureStroke);

	}

	/** ����ʶ����������� */
	public String getGestureName() {
		resetRT_GESTURE_MSG();
		RT_GESTURE_MSG = recogniseGesture(RT_Gesture);
		return RT_GESTURE_MSG;
	}

	/***/
	public void resetRT_GESTURE_MSG() {
		RT_GESTURE_MSG = null;
	}

	/** ���ص�ǰ��ά ���� */
	public PointF getPoint() {
		return RT_Point;
	}

	/** ���ض�ά�켣 */
	public static Path getPath() {
		return RT_Path;
	}

	public void commitPath() {
		RT_Path.reset();
	}

	/** ��������Ļ��������������·�� */
	public Path getPathAfterAdapte() {

		RectF bounds = new RectF();
		RT_Path.computeBounds(bounds, true);

		// ���ձ߽�ı߳�
		float edge = ScreenWidth < ScreenHeight ? ScreenWidth : ScreenHeight;
		Matrix matrix = new Matrix();
		matrix.reset();
		matrix.preTranslate(ScreenWidth / 2.0f - bounds.centerX(), ScreenHeight
				/ 2.0f - bounds.centerY());
		// Χ�����Ľ�������
		matrix.postScale((edge - 40) / bounds.width(),
				(edge - 40) / bounds.height(), ScreenWidth / 2.0f,
				ScreenHeight / 2.0f);

		// ·��Ӧ�ñ任
		RT_Path.transform(matrix);
		return RT_Path;
	}

	/**
	 * �ü�¼��ĵ����ƽ����������ƽ���켣
	 */
	public void pointToPath() {

		boolean isHorizon = true;
		PointF lastAdd = new PointF();
		PointF lastCorner = new PointF();
		float _x, _y;
		RT_Path.reset();
		for (GesturePoint p : RT_GesturePointTogether) {
			RT_Point.set(p.x, p.y);
			if (RT_Path.isEmpty()) { // ��ʼ�ĵ�
				RT_Path.moveTo(p.x, p.y);
				lastAdd.set(RT_Point);
				RT_Prepoint.set(RT_Point);
				lastCorner.set(RT_Point);
			} else {
				if ((Math.abs(RT_Point.x - RT_Prepoint.x) < 1.0f && isHorizon)
						|| (Math.abs(RT_Point.y - RT_Prepoint.y) < 1.0f && !isHorizon)) {
					RT_Prepoint.set(RT_Point);
				} else {
					isHorizon = !isHorizon;
					_x = (lastCorner.x + RT_Point.x) / 2.0f;
					_y = (lastCorner.y + RT_Point.y) / 2.0f;
					RT_Path.quadTo(lastAdd.x, lastAdd.y, _x, _y);
					lastAdd.set(_x, _y);
					lastCorner.set(RT_Point);
					RT_Prepoint.set(RT_Point);
				}
			}

		}

	}

	/**
	 * ����Ԥ�Ƚ��յ��ĵ�
	 * 
	 * @param preparePoint
	 */
	public void addPrePoints(ArrayList<RawEvent> preparePoint) {
		for (int i = 0; i < preparePoint.size(); i++) {
			this.onInputEventValues(preparePoint.get(i));
		}
	}
}
