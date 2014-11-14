
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
 * @Description  ���뷨�����ĺ�ѡ�����������ṩ��ѡ���е���ز���
 * @author linweizh@qq.com
 * @date 2014-11-14 
 *  
 */ 
public class CandiControl {

	private final String TAG = "CandiView";
	private GesInputService mService;
	/** ��ѡ����ɵ��ַ��� */ 
	private String mSuggestions;
	/** ��ѡ��TextView���� */
	private ArrayList<CandiItem> mCandiItems = new ArrayList<CandiItem>();
	/** ����ѡ���� */
	private final int MAX_SUGGES = 10;
	/** ��ѡ�ʸ�����*/
	private LinearLayout mCandiView;
	/** ��ǰѡ���±�*/
	private static int currentIndex;

	public CandiControl(Context context,LinearLayout candiView) {
		this.mService = (GesInputService) context;
		this.mCandiView = candiView;
		this.clear();
	}

	/**
	 * ������к�ѡ��
	 */
	public void clear() {
		updateSuggestions(null);
	}


	/**
	 * @Description ���º�ѡ�ʽ���
	 * @param sugg
	 */
	public void updateSuggestions(String sugg) {
		Log.i(TAG, "upatesugges" + sugg);
		mCandiView.removeAllViews();
		mCandiItems.clear();
		this.mSuggestions = sugg;
		mCandiItems.add(new CandiItem("DEL"));
		if (mSuggestions != null) {
			for (int i = 0; i < MAX_SUGGES && i < mSuggestions.length(); i++) {
				CandiItem view = new CandiItem(String.valueOf(mSuggestions
						.charAt(i)));
				mCandiItems.add(view);
			}
			currentIndex = 1;
		} else {
			currentIndex = 0;
		}
		mCandiItems.get(currentIndex).select(true);
		for (int i = 0; i < mCandiItems.size(); i++) {
			mCandiView.addView(mCandiItems.get(i), i);
		}
		Log.i(TAG, "upatesugges" + mCandiItems.size());
	}

	/**
	 *  ���±�ѡ���ѡ��
	 * @param iselect ����ѡ�е��±�
	 */
	public void selectIndex(int iselect){
		mCandiItems.get(currentIndex).select(false);
		mCandiItems.get(iselect).select(true);
		currentIndex = iselect;
	}
	
	public void moveLeft(){
		if(currentIndex > 0){
			selectIndex(currentIndex-1);
		}
	}
	
	
	/**
	 * ��ѡ������
	 */
	public void moveRight(){
		if(currentIndex < mCandiItems.size()-1){
			selectIndex(currentIndex+1);
		}
	}

	/**
	 * ��ѡ��ѡ��ִ�ж���
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
	 * ��ʾ��ѡ�ʵ��ı���
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
			layoutParams.gravity = Gravity.CENTER; // ���־���
			this.setLayoutParams(layoutParams);
			this.setGravity(Gravity.CENTER); // ���־���
		}

		/**
		 *  ����ѡ��״̬
		 * @param selected �Ƿ�ѡ��
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
