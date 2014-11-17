/**
 * @author WY
 *
 * 功能：戒指轨迹类——
 * 	     
 * 算法：
 * 1.通过按键动作和陀螺仪穿过来的值，捕获出一个二维平面的轨迹；
 * 2.用一个时间间隔器，分析出多笔画的手势轨迹；
 * 3.用按键状态分析出 识别轨迹模式、方向模式;
 * 4.利用Android的手势识别接口，与加载好的手势库比对识别出 戒指轨迹，返回轨迹名称
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
 * @Description 戒指手势检测
 * @author linweizh@qq.com
 * @date 2014-11-14
 */
public class RingTrajectory {

	/** 屏幕宽高 */
	private static float ScreenWidth;
	private static float ScreenHeight;
	private Context mContext;
	/** 手势 */
	private GestureLibrary RT_Library;
	private ArrayList<GesturePoint> RT_GesturePointTogether = new ArrayList<GesturePoint>();
	private Gesture RT_Gesture;
	private GestureStroke RT_GestureStroke = null;
	private String RT_GESTURE_MSG = new String("");
	/** 当前手势是否有效 */
	public static boolean RT_Gesture_Valid = false;
	/** 最小手势长度 */
	private static final int MIN_POINT_SIZE = 15;

	/** 路径缩放值 */
	public static final float SCALE = 1.0f;
	/** 陀螺仪阈值 */
	public static final float Threshold_X_GRY = 0.001f;
	public static final float Threshold_Y_GRY = 0.001f;

	/** 根据按键判断是否捕获轨迹 */
	private static boolean isCaptureGesture = false;
	/** 时间戳 */
	// private static final float NS2S = 1.0f / 1000000000.0f;
	private static final float NS2S = 1.0f;
	/** 笔画时间间隔 */
	private static double STROKE_TIME_SPAN = 0;
	private static final double STROKE_TIME_SPAN_Threshold = 1;
	/** 识别模式 */
	private static final int TRAJECTORY = 1;
	private static final int DIRECTION = 0;
	private static int Recognise_Gesture_State = DIRECTION;
	/** 按键状态 */
	private static final int UP = 0;
	private static final int DOWN = 1;
	public static int ButtonState = UP;

	/** 绘制二维轨迹 */
	private static Path RT_Path = new Path();

	/** 移动点坐标 */
	private PointF RT_Point = new PointF();// 当前点
	private PointF RT_Prepoint = new PointF();// 先前点

	/** 手势类型 */
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
	 * 获取屏幕宽高的通用方法
	 */
	public void getScreenHW() {
		WindowManager manager = (WindowManager) mContext
				.getSystemService(Context.WINDOW_SERVICE);
		DisplayMetrics dm = new DisplayMetrics();
		manager.getDefaultDisplay().getMetrics(dm);
		ScreenWidth = dm.widthPixels;
		ScreenHeight = dm.heightPixels;
	}

	/** 初始化轨迹 */
	private void initalTrajectory() {

		// 获取屏幕宽高 不确定屏幕方向，每一个手势开始前都必须进行重新获取
		getScreenHW();
		RT_Point.set(ScreenWidth / 2, ScreenHeight / 2);
		RT_Prepoint.set(ScreenWidth / 2, ScreenHeight / 2);
		RT_Path.reset();
		RT_Path.moveTo(RT_Point.x, RT_Point.y);
	}

	/** 手势库 */
	private boolean loadTrajectory() {
		// 加载手势库
		RT_Library = GestureLibraries.fromRawResource(mContext,
				R.raw.gestures_allhan);// 加载手势文件
		if (!RT_Library.load()) {
			Toast.makeText(mContext, "手势库加载失败", Toast.LENGTH_SHORT).show();
			return false;
		}
		initalTrajectory();
		return true;
	}

	/** 手势比对 */
	private String recogniseGesture(Gesture ges) {
		ArrayList<Prediction> gestures = RT_Library.recognize(ges);// 获得全部预测结果
		int index = 0;// 保存当前预测的索引号
		double score = 0.0;// 保存当前预测的得分
		for (int i = 0; i < gestures.size(); i++) {// 获得最佳匹配结果
			Prediction result = gestures.get(i);// 获得一个预测结果
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
	 * 获取手势结果的排序
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

	/** 手势添加 */
	public void addGesture(final String gesName) {
		RT_Library.addGesture(gesName, RT_Gesture);
		// 保存手势库
		RT_Library.save();
	}

	/** 获得戒指传来的事件值 */
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
	 * 收到由屏幕点坐标发来的接受信息
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
	 * 戒指按键按下事件
	 */
	public void onActionDown() {
		Log.i("****", "onActionDown");
		// 初始化mGesture
		RT_Gesture = new Gesture();
		RT_GesturePointTogether.clear();
		initalTrajectory();
		RT_Gesture_Valid = true;
		px = py = 0;
	}

	/**
	 * 戒指按键抬起事件
	 */
	public void onActionUp() {
		Log.i("****", "onActionUp");
		// 路径太短或者因为多开线程第一个执行了RT_GesturePointTogether.clear();
		// 最终只执行一次
		if (RT_GesturePointTogether.size() < MIN_POINT_SIZE) {
			Log.i("****", RT_GesturePointTogether.size()
					+ "---toSmallToReconize---");
			RT_Gesture_Valid = false;
			return;
		}
		// this.pointToPath();
		STROKE_TIME_SPAN = 0;
		// 生成手势
		RT_GestureStroke = new GestureStroke(RT_GesturePointTogether);
		RT_Gesture.addStroke(RT_GestureStroke);

	}

	/** 返回识别的手势名称 */
	public String getGestureName() {
		resetRT_GESTURE_MSG();
		RT_GESTURE_MSG = recogniseGesture(RT_Gesture);
		return RT_GESTURE_MSG;
	}

	/***/
	public void resetRT_GESTURE_MSG() {
		RT_GESTURE_MSG = null;
	}

	/** 返回当前二维 光标点 */
	public PointF getPoint() {
		return RT_Point;
	}

	/** 返回二维轨迹 */
	public static Path getPath() {
		return RT_Path;
	}

	public void commitPath() {
		RT_Path.reset();
	}

	/** 返回与屏幕坐标适配后的手势路径 */
	public Path getPathAfterAdapte() {

		RectF bounds = new RectF();
		RT_Path.computeBounds(bounds, true);

		// 最终边界的边长
		float edge = ScreenWidth < ScreenHeight ? ScreenWidth : ScreenHeight;
		Matrix matrix = new Matrix();
		matrix.reset();
		matrix.preTranslate(ScreenWidth / 2.0f - bounds.centerX(), ScreenHeight
				/ 2.0f - bounds.centerY());
		// 围绕中心进行缩放
		matrix.postScale((edge - 40) / bounds.width(),
				(edge - 40) / bounds.height(), ScreenWidth / 2.0f,
				ScreenHeight / 2.0f);

		// 路径应用变换
		RT_Path.transform(matrix);
		return RT_Path;
	}

	/**
	 * 用记录后的点进行平滑处理生成平滑轨迹
	 */
	public void pointToPath() {

		boolean isHorizon = true;
		PointF lastAdd = new PointF();
		PointF lastCorner = new PointF();
		float _x, _y;
		RT_Path.reset();
		for (GesturePoint p : RT_GesturePointTogether) {
			RT_Point.set(p.x, p.y);
			if (RT_Path.isEmpty()) { // 初始的点
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
	 * 接受预先接收到的点
	 * 
	 * @param preparePoint
	 */
	public void addPrePoints(ArrayList<RawEvent> preparePoint) {
		for (int i = 0; i < preparePoint.size(); i++) {
			this.onInputEventValues(preparePoint.get(i));
		}
	}
}
