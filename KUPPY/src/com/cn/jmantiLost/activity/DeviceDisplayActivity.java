package com.cn.jmantiLost.activity;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.cn.jmantiLost.R;
import com.cn.jmantiLost.adapter.DeviceAdapter;
import com.cn.jmantiLost.application.AppContext;
import com.cn.jmantiLost.bean.DeviceSetInfo;
import com.cn.jmantiLost.bean.DisturbInfo;
import com.cn.jmantiLost.db.DatabaseManager;
import com.cn.jmantiLost.service.BluetoothLeService;
import com.cn.jmantiLost.view.FollowEditDialog.ICallbackUpdateView;

public class DeviceDisplayActivity extends BaseActivity implements OnClickListener, ICallbackUpdateView {

	private Context mContext;

	private DeviceAdapter mDeviceAdapter;

	public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";

	public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";

	public final static int RESULT_ADRESS = 1000;

	private static String TAG = "DeviceDisplayActivity";

	private BluetoothAdapter mBluetoothAdapter;

	private ArrayList<DeviceSetInfo> mDeviceList = new ArrayList<DeviceSetInfo>();

	private DatabaseManager mDatabaseManager;

	public void updateDeviceAdapter(String address) {
		for (int i = 0; i < mDeviceList.size(); i++) {
			DeviceSetInfo info = mDeviceList.get(i);
			if (info.getmDeviceAddress().equals(address)) {
				info.setActive(true);
				info.setConnected(true);
				info.setVisible(false);
				mDatabaseManager.updateDeviceActiveStatus(info.getmDeviceAddress(), info);
			}
		}
	}

	private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			final String action = intent.getAction();
			if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {

				String address = intent.getStringExtra(BluetoothDevice.EXTRA_DEVICE);
				if (AppContext.mBluetoothLeService != null) {
					displayGattServices(AppContext.mBluetoothLeService.getSupportedGattServices(),address);
				}
				updateDeviceAdapter(address);

			} else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
				String address = intent.getStringExtra(BluetoothDevice.EXTRA_DEVICE);

			}
		}
	};

	Runnable mRunable = new Runnable() {
		@Override
		public void run() {

			for (int i = 0; i < mDeviceList.size(); i++) {
				DeviceSetInfo info = mDeviceList.get(i);
				info.setVisible(false);
			}
			mDeviceAdapter.notifyDataSetChanged();
		}
	};

	private DeviceSetInfo mDeviceSetInfo;

	private String mDeviceAddress;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_device);
		mDatabaseManager = DatabaseManager.getInstance(mContext);

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
		mContext = DeviceDisplayActivity.this;
		mDeviceSetInfo = mDatabaseManager.selectSingleDeviceInfo();

		initView();

		if (mDeviceSetInfo != null) {
			mDeviceAddress = mDeviceSetInfo.getmDeviceAddress();
			AppContext.mDeviceAddress = mDeviceAddress;
			initData();
			initDeviceListInfo();
		}
		
	}

	@SuppressLint("NewApi")
	private void displayGattServices(List<BluetoothGattService> gattServices,String address) {
		if (gattServices == null) {
			return;
		}
		for (BluetoothGattService gattService : gattServices) {
			if (gattService.getUuid().toString().startsWith("00001802")) {
				List<BluetoothGattCharacteristic> gattCharacteristics = gattService
						.getCharacteristics();
				for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
					if (gattCharacteristic.getUuid().toString()
							.startsWith("00002a06")) {

					}
				}
			} else if (gattService.getUuid().toString().startsWith("0000ffe0")) {
				List<BluetoothGattCharacteristic> gattCharacteristics = gattService
						.getCharacteristics();
				for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
					if (gattCharacteristic.getUuid().toString().startsWith("0000ffe1")) {
						AppContext.mBluetoothLeService.setCharacteristicNotification(gattCharacteristic, true);
					}
				}
			}
		}
	}

	public void initDeviceListInfo() {

		mDeviceList = mDatabaseManager.selectDeviceInfo();
		
		if(AppContext.mBluetoothLeService != null && AppContext.mBluetoothLeService.isConnect()){
			if(mDeviceList != null && mDeviceList.size() > 0){
				mDeviceSetInfo =  mDeviceList.get(0);
				mDeviceSetInfo.setConnected(true);
				mDeviceSetInfo.setVisible(false);
			}
		}
	}

	private void initData() {
		
		ArrayList<DeviceSetInfo> list = mDatabaseManager.selectDeviceInfo(mDeviceAddress);
		
		if (list.size() > 0) {
			mDeviceSetInfo = mDatabaseManager.selectDeviceInfo(mDeviceAddress).get(0);
		}
		
		ArrayList<DisturbInfo> disturbList = mDatabaseManager.selectDisturbInfo(mDeviceAddress);
		if (disturbList == null) {
			return;
		}

		for (int i = 0; i < disturbList.size(); i++) {
			mDisturbInfo = disturbList.get(i);
		}

	}

	private DisturbInfo mDisturbInfo;
	
	private TextView mTvMainInfo ;
	
	private ImageView mIvLeft ;

	private void initView() {
		mTvMainInfo = (TextView)findViewById(R.id.tv_title_info);
		mIvLeft = (ImageView)findViewById(R.id.iv_back);
		mIvLeft.setVisibility(View.INVISIBLE);
		mTvMainInfo.setText(mContext.getString(R.string.new_info));
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		ArrayList<DeviceSetInfo> deviceList = mDatabaseManager.selectDeviceInfo(mDeviceAddress);
		if (deviceList.size() > 0) {
			mDatabaseManager.updateDeviceInfo(mDeviceAddress, mDeviceSetInfo);
		}
	}

	private static final int REQUEST_ENABLE_BT = 1;

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
		setShakeConfig();
	}

	@Override
	protected void onStop() {
		super.onStop();
	}

	private static IntentFilter makeGattUpdateIntentFilter() {
		final IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
		intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
		intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
		intentFilter.addAction(BluetoothLeService.ACTION_NOTIFY_DATA_AVAILABLE);
		intentFilter.addAction(BluetoothLeService.ACTION_GATT_RSSI);
		intentFilter.addAction(BluetoothLeService.ACTION_READ_DATA_AVAILABLE);
		return intentFilter;
	}

	@Override
	protected void onPause() {
		super.onPause();
		unregisterReceiver(mGattUpdateReceiver);
	}

	private void updateDatabase() {
		ArrayList<DisturbInfo> list = mDatabaseManager.selectDisturbInfo(mDeviceAddress);
		if (list.size() > 0) {
			mDatabaseManager.updateDisturbIsDisturbInfo(mDeviceAddress,mDisturbInfo);
		}
	}

	@Override
	public void onClick(View v) {
		
	}

	@Override
	public void updateView() {
		initData();
		initView();
	};
}
