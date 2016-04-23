package com.cn.jmantiLost.activity;

import java.util.Timer;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;

import com.cn.jmantiLost.R;
import com.cn.jmantiLost.application.AppContext;
import com.cn.jmantiLost.service.BluetoothLeService;
import com.cn.jmantiLost.util.RecordManager;

public class RecordActivity extends Activity implements OnClickListener {

	private Context mContext;

	private ImageView mIvRecord;

	private RecordManager mRecordManger;

	private boolean isFirstRecord;

	private ImageView mRvRecord;
	
	private ImageView mIvBack ;

	private int mFlag = 0 ;
	
	private void getIntentData(){
		Intent intent = getIntent();
		mFlag = intent.getIntExtra("flag", 0);	
	}
	
	public Handler handler = new Handler(){
		
		public void handleMessage(android.os.Message msg) {
    		mMediaStatusCode = mRecordManger.startRecord();
    		mIvRecord.setBackgroundResource(R.drawable.ic_record_pause);
			isFirstRecord = false;
		};
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		AppContext.isAlarm = false;
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_record);
		
		getIntentData();
		mContext = RecordActivity.this;
		mRecordManger = RecordManager.getInstance(mContext);
		initView();
		makeGattUpdateIntentFilter();
		vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
		
		registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
		
		if(mFlag == 1){
			vibrator.vibrate(200);
	    	if(mMediaStatusCode == -1){
				new Thread(){
					
					@Override
					public void run() {
						handler.sendEmptyMessage(0);
					};
					
				}.start();
				
	    	}else{
	    		saveRecord();
	    		mMediaStatusCode = -1 ;
	    		isSave = true ;
	    	}
		}
		
	}
	
	private Vibrator vibrator;

	@Override
	protected void onDestroy() {
		super.onDestroy();
		
		unregisterReceiver(mGattUpdateReceiver);
		
		if(vibrator != null){
    		vibrator.cancel();
    	}
		
		AppContext.isAlarm = true ;
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		isFirstRecord = true;
	}

	@Override
	protected void onPause() {
		super.onPause();
		saveRecord();
	}

	private void initView() {
		mIvBack  =  (ImageView)findViewById(R.id.iv_back);
		mRvRecord = (ImageView) findViewById(R.id.rv_record);
		mIvRecord = (ImageView) findViewById(R.id.cb_record);
		mIvRecord.setOnClickListener(this);
		mIvBack.setOnClickListener(this);
	}

	private boolean isSave = false ;
	
	private int mMediaStatusCode = -1 ;

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.iv_record_menu:
			if(mMediaStatusCode == -1){
				Intent intent = new Intent(mContext, RecordMenuActivity.class);
				startActivity(intent);
				return ;
			}else if(mMediaStatusCode != 2 && !isSave){
				saveRecord();
				mMediaStatusCode = -1 ;
				isSave = true ;
				return ;
			}else if(isSave){
				Intent intent = new Intent(mContext, RecordMenuActivity.class);
				startActivity(intent);
				return ;
			}
			break;
		case R.id.cb_record:
			if(mMediaStatusCode == -1){
				mIvRecord.setBackgroundResource(R.drawable.ic_record_pause);
				mMediaStatusCode = mRecordManger.startRecord();
				isFirstRecord = false;
			}else if(mMediaStatusCode == 0){
				mMediaStatusCode = mRecordManger.pauseRecord();
				if (mMediaStatusCode == 1) { 
					mIvRecord.setBackgroundResource(R.drawable.ic_record);
					timer.cancel();
				} else {
					mIvRecord.setBackgroundResource(R.drawable.ic_record_pause);
				}
				
			}else if(mMediaStatusCode == 1){
				
				mMediaStatusCode = mRecordManger.pauseRecord();
				if (mMediaStatusCode == 1) { 
					mIvRecord.setBackgroundResource(R.drawable.ic_record);
					timer.cancel();
				} else { 
					mIvRecord.setBackgroundResource(R.drawable.ic_record_pause);
				}
			}
			break ;
		case R.id.iv_back:
			finish();
			break ;
		default:
			break;
		}
	}

	private int saveRecord() {
		int ret = 0 ;
		ret = mRecordManger.saveRecord();
		mIvRecord.setBackgroundResource(R.drawable.ic_record);
		isSave = true;
		if (timer != null) {
			timer.cancel();
		}
		return ret ;
	}


	private Timer timer;

	private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			final String action = intent.getAction();
			if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
			} else if (BluetoothLeService.ACTION_GATT_DISCONNECTED
					.equals(action)) {
			} else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED
					.equals(action)) {

			} else if (BluetoothLeService.ACTION_NOTIFY_DATA_AVAILABLE.equals(action)) {
		    	vibrator.vibrate(200);
		    	if(mMediaStatusCode == -1){
		    		mIvRecord.setBackgroundResource(R.drawable.ic_record_pause);
		    		mMediaStatusCode = mRecordManger.startRecord();
					isFirstRecord = false;
		    	}else{
		    		saveRecord();
		    		mMediaStatusCode = -1 ;
		    		isSave = true ;
		    	}
		    	
			}
		}
	};

	private static IntentFilter makeGattUpdateIntentFilter() {
		final IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
		intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
		intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
		intentFilter.addAction(BluetoothLeService.ACTION_NOTIFY_DATA_AVAILABLE);
		intentFilter.addAction(BluetoothLeService.ACTION_GATT_RSSI);
		return intentFilter;
	}

}
