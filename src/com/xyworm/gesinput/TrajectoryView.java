package com.xyworm.gesinput;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.CornerPathEffect;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathEffect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;


/** 
 * @ClassName  TrajectoryView 
 * @Description  TODO
 * @author linweizh@qq.com
 * @date 2014-11-14  
 */ 
public class TrajectoryView extends View {

	public static final int UP = 0;
	public static final int DOWN = 1;
	private Paint paint;
	private int[] colors;
	private PathEffect effect = new PathEffect();
	private Path path;

	public TrajectoryView(Context context) {
		super(context);
		init();
	}

	public TrajectoryView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
		init();
	}

	public TrajectoryView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		init();
	}

	public void init() {
		paint = new Paint();
		paint.setStyle(Paint.Style.STROKE);
		paint.setStrokeWidth(10);
		colors = new int[] { Color.BLACK, Color.BLUE, Color.CYAN, Color.GREEN,
				Color.MAGENTA, Color.RED, Color.YELLOW };
		effect = new CornerPathEffect(10);
		path = null;
	}

	public void setPath(Path path) {
		this.path = path;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		// 将背景填充成白色
		path = RingTrajectory.getPath();
		path = getPathAfterAdapte(path);
		if (path == null) {
			Log.i("TrajectoryView", "null");
			return;
		}
		// 绘制红色轨迹
		paint.setStyle(Paint.Style.STROKE);
		paint.setPathEffect(effect);
		paint.setColor(colors[2]);
		canvas.drawPath(path, paint);
	}

	/** 返回与屏幕坐标适配后的手势路径 */
	@SuppressLint("NewApi")
	public Path getPathAfterAdapte(Path path) {
		Path newPath = new Path();
		RectF bounds = new RectF();
		path.computeBounds(bounds, true);

		int[] loc = new int[2];
		this.getLocationInWindow(loc);

		float gesPx = bounds.centerX();
		float gesPy = bounds.centerY();///猜测这里获取的是父布局中的位置,不包含状态栏
		float gesWidth = bounds.width();
		float gesHeight = bounds.height(); 
		float gesMax = gesWidth > gesHeight ? gesWidth : gesHeight;

		float showPx = (float) (loc[0] + this.getWidth() / 2.0);
		float showPy = (float) (loc[1] + this.getHeight() / 2.0);
		float showWidth = this.getWidth()-40;
		float showHeight = this.getHeight()-40;
		float showMax = showWidth > showHeight ? showWidth : showHeight;

		Matrix matrix = new Matrix();
		matrix.reset();
		if (gesHeight > showHeight) {
			matrix.preScale(showHeight / gesHeight, showHeight / gesHeight,
					gesPx, gesPy);
		}
		matrix.postTranslate(showPx - gesPx, showPy - gesPy - 60);

		// 路径应用变换
		path.transform(matrix, newPath);
		return newPath;
	}
	
	

}