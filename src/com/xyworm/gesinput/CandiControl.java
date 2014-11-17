
package com.xyworm.gesinput;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

/** 
 * @ClassName  CandiControl 
 * @Description  输入法顶部的候选栏控制器，提供候选器中的相关操作
 * @author linweizh@qq.com
 * @date 2014-11-14 
 *  
 */ 
public class CandiControl {

	private final String TAG = "CandiView";
	private GesInputService mService;
	/** 候选词组成的字符串 */ 
	private ArrayList<String> mSuggestions;
	/** 候选词TextView集合 */
	private ArrayList<CandiItem> mCandiItems = new ArrayList<CandiItem>();
	/** 最大候选个数 */
	private final int MAX_SUGGES = 10;
	/** 候选词父容器*/
	private LinearLayout mCandiView;
	private TrajectoryView mTrajectoryView;
	/** 当前选中下标*/
	private static int currentIndex;

	public CandiControl(Context context,LinearLayout candiView, TrajectoryView mTrajectoryView) {
		this.mService = (GesInputService) context;
		this.mCandiView = candiView;
		this.mTrajectoryView = mTrajectoryView;
		this.clear();
	}

	/**
	 * 清除所有候选词
	 */
	public void clear() {
		updateSuggestions(null);
	}


	/**
	 * @Description 更新候选词界面
	 * @param suggestions
	 */
	public void updateSuggestions(ArrayList<String> suggestions) {
		Log.i(TAG, "upatesugges" + suggestions);
		mCandiView.removeAllViews();
		mCandiItems.clear();
		this.mSuggestions = suggestions;
		mCandiItems.add(new CandiItem("DEL"));
		if (mSuggestions != null) {
			for (int i = 0; i < MAX_SUGGES && i < mSuggestions.size(); i++) {
				CandiItem view = new CandiItem(mSuggestions.get(i));
				mCandiItems.add(view);
			}
			currentIndex = 1;
		} else {
			currentIndex = 0;
		}
		for (int i = 0; i < mCandiItems.size(); i++) {
			mCandiView.addView(mCandiItems.get(i), i);
		}
		this.selectIndex(currentIndex);
		Log.i(TAG, "upatesugges" + mCandiItems.size());
	}

	/**
	 *  由下标选择候选词
	 * @param iselect 设置选中的下标
	 */
	public void selectIndex(int iselect){
		mCandiItems.get(currentIndex).select(false);
		mCandiItems.get(iselect).select(true);
		currentIndex = iselect;
		
		mTrajectoryView.updateGesture(mCandiItems.get(iselect).getText().toString());
	}
	
	public void moveLeft(){
		if(currentIndex > 0){
			selectIndex(currentIndex-1);
		}
	}
	
	
	/**
	 * 候选词右移
	 */
	public void moveRight(){
		if(currentIndex < mCandiItems.size()-1){
			selectIndex(currentIndex+1);
		}
	}

	/**
	 * 候选词选中执行动作
	 */
	public void clickAndCommit() {
		if(mCandiItems.size() <= 1)
			mService.sendDownUpKeyEvents(KeyEvent.KEYCODE_DEL);
		else{
			if(currentIndex > 0){
				char ch = mCandiItems.get(currentIndex).getText().charAt(0);
				mService.sendKeyChar(ch);
			}
		}
		this.clear();
	}
	
	
	/**
	 * 显示候选词的文本框
	 * @author linwei
	 *
	 */
	class CandiItem extends TextView {

		public CandiItem(String text) {
			super(mService);
			select(false);
			this.setText(text);
			this.setTextColor(Color.rgb(0, 0, 0));
			this.setTextSize(24);
			LinearLayout.LayoutParams layoutParams = new LayoutParams(70,
					LayoutParams.FILL_PARENT);
			layoutParams.gravity = Gravity.CENTER; // 布局居中
			this.setLayoutParams(layoutParams);
			this.setGravity(Gravity.CENTER); // 文字居中
		}

		/**
		 *  设置选中状态
		 * @param selected 是否选中
		 */
		public void select(boolean selected) {
			if (selected) {
				this.setBackgroundColor(Color.argb(50, 0, 0, 0));
			} else {
				this.setBackgroundColor(Color.WHITE);
			}
		}

		
	}
	
	


}
