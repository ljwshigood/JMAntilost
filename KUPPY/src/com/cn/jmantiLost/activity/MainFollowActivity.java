package com.cn.jmantiLost.activity;

import android.app.TabActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;

import com.cn.jmantiLost.R;

public class MainFollowActivity extends TabActivity implements OnClickListener {

	private Context mContext;
	
	private TabHost mTabHost;

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main_follow);
		mContext = MainFollowActivity.this;
		initView();
	}
	
	private void setupTab(Class<?> ccls, String name, int label, Integer iconId) {
		Intent intent = new Intent().setClass(this, ccls);
		View view = LayoutInflater.from(this).inflate(R.layout.tab_item_view,null);
		ImageView image = (ImageView) view.findViewById(R.id.ic_icon);
		TextView text = (TextView) view.findViewById(R.id.tv_info);
		image.setImageResource(iconId);
		text.setText(label);
		TabSpec spec = mTabHost.newTabSpec(name).setIndicator(view).setContent(intent);
		mTabHost.addTab(spec);
	}

	
	private void initView() {
		
		mTabHost = this.getTabHost();
		setupTab(DeviceDisplayActivity.class, "tab_device", R.string.antilost,R.drawable.ic_device_selector);
		setupTab(CameraActivity.class, "tab_camera", R.string.camera,R.drawable.ic_camera_big);
		setupTab(DeviceLocationActivity.class, "tab_location", R.string.location,R.drawable.ic_location_selector);
		setupTab(DeviceSetActivity.class, "tab_record", R.string.setting,R.drawable.ic_record_selector);
	
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
		
	}
}
