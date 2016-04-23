/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */

package com.cn.jmantiLost.activity;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.cn.jmantiLost.R;
import com.cn.jmantiLost.application.AppContext;
import com.cn.jmantiLost.bean.DeviceSetInfo;
import com.cn.jmantiLost.bean.DisturbInfo;
import com.cn.jmantiLost.bean.SoundInfo;
import com.cn.jmantiLost.db.DatabaseManager;
import com.cn.jmantiLost.impl.IDismissListener;
import com.cn.jmantiLost.service.AlarmService;
import com.cn.jmantiLost.service.BgMusicControlService;
import com.cn.jmantiLost.service.BluetoothLeService;
import com.cn.jmantiLost.util.AlarmManager;
import com.cn.jmantiLost.util.Constant;
import com.cn.jmantiLost.util.EncriptyUtils;
import com.cn.jmantiLost.view.FollowProgressDialog;

/**
 * Activity for scanning and displaying available Bluetooth LE devices.
 */
@SuppressLint("NewApi")
public class DeviceScanActivity extends Activity implements OnClickListener ,IDismissListener{

	private BluetoothAdapter mBluetoothAdapter;
	private boolean mScanning;
	private Handler mHandler;

	private static final int REQUEST_ENABLE_BT = 1;
	private static final long SCAN_PERIOD = 10000;
	
	private Context mContext;

	private BluetoothDevice mDevice;

	private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			final String action = intent.getAction();
			if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
				String address = intent.getStringExtra(BluetoothDevice.EXTRA_DEVICE);
				
				if (AppContext.mBluetoothLeService != null) {
					displayGattServices(AppContext.mBluetoothLeService.getSupportedGattServices(),address);
				}
			}
		}
	};

	private void displayGattServices(List<BluetoothGattService> gattServices,
			String address) {
		if (gattServices == null)
			return;
		for (BluetoothGattService gattService : gattServices) {
			if (gattService.getUuid().toString().startsWith("0000ffe0")) {
				List<BluetoothGattCharacteristic> gattCharacteristics = gattService.getCharacteristics();
				for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
					if (gattCharacteristic.getUuid().toString().startsWith("0000ffe1")) {
						AppContext.mBluetoothLeService.setCharacteristicNotification(gattCharacteristic, true);
					}else if(gattCharacteristic.getUuid().toString().startsWith("0000ffe2")){
						if (AppContext.mBluetoothLeService != null) {
							String orgData = EncriptyUtils.encripty(Constant.AUTH_ID+Constant.SESSION_KEY, Constant.Key) ;
							
							AppContext.mBluetoothLeService.sendMsg(address,EncriptyUtils.zhuanHuan(orgData));
						}
					}
				}
			}
		}
		
		saveDatabaseAndStartActivity();
	}

	private ImageView mIvBack;

	private AudioManager mAudioManager;

	private AlarmManager mAlarmManager;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		
		if (getRequestedOrientation() != ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		}
		
		super.onCreate(savedInstanceState);
		mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		mHandler = new Handler();

		if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
			Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
			finish();
		}
		final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
		mBluetoothAdapter = bluetoothManager.getAdapter();

		if (mBluetoothAdapter == null) {
			Toast.makeText(this, R.string.error_bluetooth_not_supported,Toast.LENGTH_SHORT).show();
			finish();
			return;
		}

		setContentView(R.layout.activity_device_scan);
		mContext = this;

		mAlarmManager = AlarmManager.getInstance(mContext);
		initView();

		Intent intent = new Intent(mContext, BgMusicControlService.class);
		startService(intent);
		Intent intentAlarm = new Intent(mContext, AlarmService.class);
		startService(intentAlarm);
		
		setTitle(mContext.getString(R.string.device_scanning));
		
		if(AppContext.mBluetoothLeService != null){
			AppContext.mBluetoothLeService.setmIDismissListener(this);
		}
	}

	private View mView;

	private TextView mTvTitleInfo;

	private void setTitle(String info) {
		mView = (View) findViewById(R.id.include_head);
		mTvTitleInfo = (TextView) mView.findViewById(R.id.tv_title_info);
		mTvTitleInfo.setText(info);
	}

	private LinearLayout mLLInfo ;

	private void initView() {
		mLLInfo = (LinearLayout)findViewById(R.id.ll_info);
		mIvBack = (ImageView) findViewById(R.id.iv_back);
		mIvBack.setOnClickListener(this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		return true;
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (!mBluetoothAdapter.isEnabled()) {
			if (!mBluetoothAdapter.isEnabled()) {
				Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
				startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
			}
		}
		registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
		scanLeDevice(true);
	}
	


	@Override
	protected void onDestroy() {
		scanLeDevice(false);
		super.onDestroy();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQUEST_ENABLE_BT
				&& resultCode == Activity.RESULT_CANCELED) {
			finish();
			return;
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	protected void onPause() {
		super.onPause();
		scanLeDevice(false);
		unregisterReceiver(mGattUpdateReceiver);
		
	}

	/**
	 * @param enable
	 */
	private void scanLeDevice(final boolean enable) {
		if (enable) {
			mScanning = true;
			mBluetoothAdapter.startLeScan(mLeScanCallback);
		} else {
			mScanning = false;
			mBluetoothAdapter.stopLeScan(mLeScanCallback);
		}
	}

	private ArrayList<BluetoothDevice> mLeDevices;

	private DatabaseManager mDatabaseManager;

	private void saveDatabaseAndStartActivity() {
		if(mDevice == null){
			return ;
		}
		mDatabaseManager = DatabaseManager.getInstance(mContext);
		mDatabaseManager.deleteAllDeviceInfo();
		// query mac address
		ArrayList<DeviceSetInfo> deviceList = mDatabaseManager.selectDeviceInfo(mDevice.getAddress());
		if (deviceList.size() == 0) {
			DeviceSetInfo info = new DeviceSetInfo();
			info.setDistanceType(2);
			info.setDisturb(false);
			info.setFilePath(null);
			info.setLocation(true);
			info.setmDeviceAddress(mDevice.getAddress());
			info.setmDeviceName(mDevice.getName());
			info.setConnected(true);
			info.setVisible(false);
			info.setActive(true);
			info.setLat(String.valueOf(AppContext.mLatitude));
			info.setLng(String.valueOf(AppContext.mLongitude));
			DisturbInfo disturbInfo = new DisturbInfo();
			disturbInfo.setDisturb(false);
			disturbInfo.setEndTime("23:59");
			disturbInfo.setStartTime("00:00");
			SoundInfo soundInfo = new SoundInfo();
			soundInfo.setDurationTime(180);
			soundInfo.setRingId(R.raw.crickets);
			soundInfo.setRingName(mContext.getString(R.string.ringset_qsmusic));
			soundInfo.setRingVolume(mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC));
			soundInfo.setShock(true);
			mDatabaseManager.insertDeviceInfo(mDevice.getAddress(), info);
			mDatabaseManager.insertDisurbInfo(mDevice.getAddress(), disturbInfo);
			mDatabaseManager.insertSoundInfo(mDevice.getAddress(), soundInfo);
		}
		if (mScanning) {
			mBluetoothAdapter.stopLeScan(mLeScanCallback);
			mScanning = false;
		}
		
		Intent intent = new Intent(mContext, MainActivity.class);
		intent.putExtra(DeviceDisplayActivity.EXTRAS_DEVICE_NAME,mDevice.getName());
		intent.putExtra(DeviceDisplayActivity.EXTRAS_DEVICE_ADDRESS,mDevice.getAddress());
		intent.putExtra("device", mDevice);
		startActivity(intent);
		finish();
	}

	private class LeDeviceListAdapter extends BaseAdapter implements OnItemClickListener {

		private LayoutInflater mInflator;

		public LeDeviceListAdapter() {
			super();
			mLeDevices = new ArrayList<BluetoothDevice>();
			mInflator = DeviceScanActivity.this.getLayoutInflater();
		}

		public void addDevice(BluetoothDevice device) {
			if (!mLeDevices.contains(device)) {
				mLeDevices.add(device);
			}
		}

		public BluetoothDevice getDevice(int position) {
			return mLeDevices.get(position);
		}

		@Override
		public int getCount() {
			return mLeDevices.size();
		}

		@Override
		public Object getItem(int i) {
			return mLeDevices.get(i);
		}

		@Override
		public long getItemId(int i) {
			return i;
		}

		@Override
		public View getView(int i, View view, ViewGroup viewGroup) {
			ViewHolder viewHolder;
			if (view == null) {
				view = mInflator.inflate(R.layout.list_item_device, null);
				viewHolder = new ViewHolder();
				viewHolder.ivDevice = (ImageView) view.findViewById(R.id.iv_device);
				viewHolder.deviceName = (TextView) view.findViewById(R.id.tv_name);
				view.setTag(viewHolder);
			} else {
				viewHolder = (ViewHolder) view.getTag();
			}

			BluetoothDevice device = mLeDevices.get(i);
			final String deviceName = device.getName();
			if (deviceName != null && deviceName.length() > 0)
				viewHolder.deviceName.setText(deviceName);
			else
				viewHolder.deviceName.setText(R.string.unknown_device);

			DeviceSetInfo info = new DeviceSetInfo();
			info.setFilePath("null");
			Bitmap circleBitmap = mAlarmManager.getDeviceBitmap(info, mContext);
			viewHolder.ivDevice.setImageBitmap(circleBitmap);

			return view;
		}
		
		@Override
		public void onItemClick(AdapterView<?> arg0, View view, int position,long arg3) {
			
		}
	}

	private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {

		@Override
		public void onLeScan(final BluetoothDevice device, int rssi,
				byte[] scanRecord) {
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					mLLInfo.setVisibility(View.GONE);
				}
			});
		}
	};
	
	

	public boolean iteraGattHashMap(Map map, String address) {
		Iterator iter = map.entrySet().iterator();
		while (iter.hasNext()) {
			Map.Entry entry = (Map.Entry) iter.next();
			Object key = entry.getKey();
			Object val = entry.getValue();
			if (key.toString().equals(address)) {
				return true;
			}
		}
		return false;
	}

	static class ViewHolder {
		TextView deviceName;
		ImageView ivDevice;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.iv_back:
			finish();
			break;
		default:
			break;
		}
	}

	private static IntentFilter makeGattUpdateIntentFilter() {
		final IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
		intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
		intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
		intentFilter.addAction(BluetoothLeService.ACTION_NOTIFY_DATA_AVAILABLE);
		return intentFilter;
	}

	@Override
	public void dismiss() {
		
	}

}