package com.cn.jmantiLost.activity;

import java.util.ArrayList;
import java.util.List;

import android.app.TabActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;

import com.cn.jmantiLost.R;
import com.cn.jmantiLost.service.AlarmService;
import com.cn.jmantiLost.service.BgMusicControlService;

public class MainActivity extends TabActivity implements OnClickListener,OnTabChangeListener{

	private Context mContext;
	
	private TabHost mTabHost;
	
	private ImageView mIvBack ;  
	
	private LinearLayout mLLMain ;
	
	private LinearLayout mLLContent ;
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main_follow);
		mContext = MainActivity.this;
		initView();
		setTabBackGroundSelect(0);
	}
	
	private void setupTab(Class<?> ccls, String name, int label, Integer iconId) {
		Intent intent = new Intent().setClass(this, ccls);
		View view = LayoutInflater.from(this).inflate(R.layout.tab_item_view,null);
		ImageView image = (ImageView) view.findViewById(R.id.ic_icon);
		TextView text = (TextView) view.findViewById(R.id.tv_info);
		image.setImageResource(iconId);
		text.setText(label);
		TabSpec spec = mTabHost.newTabSpec(name).setIndicator(view).setContent(intent);
		
		
		mTabTextViewList.add(text);
		mImageViewList.add(image);
		mLLTabItemViewList.add((LinearLayout)view);
		
		mTabHost.addTab(spec);
		
	}
	
	private List<LinearLayout> mLLTabItemViewList = new ArrayList<LinearLayout>();
	
	private void initView() {
		mLLMain = (LinearLayout)findViewById(R.id.ll_main) ;
		mLLContent = (LinearLayout)findViewById(R.id.ll_scan) ;
		mIvBack = (ImageView)findViewById(R.id.iv_back) ;
		mIvBack.setOnClickListener(this) ;
		mLLMain.setVisibility(View.GONE) ;
		mLLContent.setVisibility(View.VISIBLE) ;
		mTabHost = this.getTabHost();
		setupTab(DeviceDisplayActivity.class, "tab_device", R.string.antilost,R.drawable.ic_device_nomal);
		setupTab(CameraActivity.class, "tab_camera", R.string.camera,R.drawable.ic_camera_big_nomal);
		setupTab(DeviceLocationActivity.class, "tab_location", R.string.location,R.drawable.ic_location_nomal);
		setupTab(DeviceSetActivity.class, "tab_record", R.string.setting,R.drawable.ic_set_press);
		
		mTabHost.setOnTabChangedListener(this);
	
	}
	
	private List<TextView> mTabTextViewList = new ArrayList<TextView>();
	
	private List<ImageView> mImageViewList = new ArrayList<ImageView>();
	

	private int[] mTabImageNomalList = {R.drawable.ic_device_nomal,
										R.drawable.ic_camera_big_nomal, 
										R.drawable.ic_location_nomal,
										R.drawable.ic_set_nomal};

	private int[] mTabImagePressList = {R.drawable.ic_device_selected,
										R.drawable.ic_camera_big_press, 
										R.drawable.ic_location_selected,
										R.drawable.ic_set_press};;
	
	private void setTabBackGroundSelect(int position) {
		for (int i = 0; i < mTabTextViewList.size(); i++) {
			if (i == position) {
				mImageViewList.get(i).setImageResource(mTabImagePressList[i]);
				mTabTextViewList.get(i).setTextColor(mContext.getResources().getColor(R.color.blue_light));
			} else {
				mImageViewList.get(i).setImageResource(mTabImageNomalList[i]);
				mTabTextViewList.get(i).setTextColor(mContext.getResources().getColor(R.color.grey));
			}
		}
	}

	
	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	public void onClick(View v) {
		
		switch (v.getId()) {
		case R.id.iv_back:
			mLLMain.setVisibility(View.VISIBLE) ;
			mLLContent.setVisibility(View.GONE) ;
			break;

		default:
			break;
		}
		
	}

	@Override
	public void onTabChanged(String tabId) {
		int position = mTabHost.getCurrentTab();
		setTabBackGroundSelect(position);
	}
	
}
