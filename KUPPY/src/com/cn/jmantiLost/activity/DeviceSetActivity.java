package com.cn.jmantiLost.activity;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cn.jmantiLost.R;
import com.cn.jmantiLost.application.AppContext;
import com.cn.jmantiLost.service.BluetoothLeService;
import com.cn.jmantiLost.view.FollowEditDialog.ICallbackUpdateView;

public class DeviceSetActivity extends BaseActivity implements OnClickListener,ICallbackUpdateView {

	private Context mContext;

	private LinearLayout mLLRecord;

	private Intent mIntent;

	private ImageView mIvBack;

	private String mDeviceAddress;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		if (getRequestedOrientation() != ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		}
		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
		
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_device_set);
		this.mContext = DeviceSetActivity.this;
		getIntentExtra();
		initView();
		setTitle(mContext.getString(R.string.device_setting));
		
	}

	private View mView;

	private TextView mTvTitleInfo;

	private void setTitle(String info) {
		mView = (View) findViewById(R.id.include_head);
		mTvTitleInfo = (TextView) mView.findViewById(R.id.tv_title_info);
		mTvTitleInfo.setText(info);
	}

	@Override
	protected void onResume() {
		registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
		super.onResume();
	}
	
	private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {

			String address = intent.getStringExtra(BluetoothDevice.EXTRA_DEVICE);
			final String action = intent.getAction();

		}
	};

	private static IntentFilter makeGattUpdateIntentFilter() {
		final IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
		intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
		intentFilter
				.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
		intentFilter.addAction(BluetoothLeService.ACTION_NOTIFY_DATA_AVAILABLE);
		intentFilter.addAction(BluetoothLeService.ACTION_READ_DATA_AVAILABLE);
		intentFilter.addAction(BluetoothLeService.ACTION_GATT_RSSI);
		return intentFilter;
	}


	@Override
	protected void onPause() {
		super.onPause();
	}
	
	private void getIntentExtra() {
		Intent intent = getIntent();
		mDeviceAddress = intent.getStringExtra("address");
		AppContext.mDeviceAddress = mDeviceAddress;
	}

	private void initView() {
		
		mLLRecord = (LinearLayout) findViewById(R.id.ll_record);
		mIvBack = (ImageView) findViewById(R.id.iv_back);
		mIvBack.setVisibility(View.INVISIBLE) ;
		mLLRecord.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.ll_device:
			
			break;
		case R.id.ll_record:
			mIntent = new Intent(mContext, RecordActivity.class);
			startActivity(mIntent);
			break;
		case R.id.iv_back:
			finish();
			break;
		default:
			break;
		}
	}


	@Override
	protected void onDestroy() {
		unregisterReceiver(mGattUpdateReceiver);
		super.onDestroy();
	}

	@Override
	public void updateView() {
		initView();
	}
}
