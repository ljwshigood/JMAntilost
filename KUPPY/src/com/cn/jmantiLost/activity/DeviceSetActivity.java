package com.cn.jmantiLost.activity;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;

import com.cn.jmantiLost.R;
import com.cn.jmantiLost.application.AppContext;
import com.cn.jmantiLost.bean.DeviceSetInfo;
import com.cn.jmantiLost.db.DatabaseManager;
import com.cn.jmantiLost.service.BluetoothLeService;
import com.cn.jmantiLost.util.AlarmManager;
import com.cn.jmantiLost.util.ImageTools;
import com.cn.jmantiLost.util.LocationUtils;
import com.cn.jmantiLost.view.FollowEditDialog;
import com.cn.jmantiLost.view.FollowEditDialog.ICallbackUpdateView;
import com.cn.jmantiLost.view.FollowInfoDialog;
import com.cn.jmantiLost.view.SelectPicPopupWindow;

public class DeviceSetActivity extends BaseActivity implements OnClickListener,ICallbackUpdateView {

	private Context mContext;

	private LinearLayout mLlNotDisturb;

	private LinearLayout mLLSound;

	private Intent mIntent;

	private ImageView mIvBack;

	private String mDeviceAddress;

	SelectPicPopupWindow menuWindow;

	private TextView mTvChangePicView;

	private LinearLayout mLLDeviceDelete;

	private CheckBox mCbLocation;

	private EditText mEtName;

	private ImageView mIvDistanceInfo;

	private LinearLayout mLLDeviceName;

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

			String address = intent
					.getStringExtra(BluetoothDevice.EXTRA_DEVICE);
			final String action = intent.getAction();
			if (BluetoothLeService.ACTION_READ_DATA_AVAILABLE.equals(action)) {
				if (address.equals(mDeviceAddress)) {
					progressBatteryData(intent);
				}
			}

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

	private void progressBatteryData(Intent intent) {
		byte[] msg = intent.getByteArrayExtra(BluetoothLeService.BATTERY_DATA);
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
		
		mLLDeviceName = (LinearLayout) findViewById(R.id.ll_device);
		mLLDeviceName.setOnClickListener(this);
		mEtName = (EditText) findViewById(R.id.et_device_name);
		mEtName.setOnClickListener(this);
		mLlNotDisturb = (LinearLayout) findViewById(R.id.ll_not_disturb);
		mLLSound = (LinearLayout) findViewById(R.id.ll_sound);
		mIvBack = (ImageView) findViewById(R.id.iv_back);
		mTvChangePicView = (TextView) findViewById(R.id.tv_change_device);
		mLLDeviceDelete = (LinearLayout) findViewById(R.id.ll_delete_device);
		mCbLocation = (CheckBox) findViewById(R.id.cb_location_switch);
		mIvDistanceInfo = (ImageView) findViewById(R.id.iv_distance_info);
		mIvDistanceInfo.setOnClickListener(this);
		mLLDeviceDelete.setOnClickListener(this);
		mTvChangePicView.setOnClickListener(this);
		mIvBack.setOnClickListener(this);
		mLlNotDisturb.setOnClickListener(this);
		mLLSound.setOnClickListener(this);
		mCbLocation.setOnClickListener(this);
		mCbLocation.setChecked(LocationUtils.isOPen(mContext));

		mEtName.setFocusable(false);
		mEtName.setFocusableInTouchMode(false);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.ll_device:
			showWindows(0);
			break;
		case R.id.iv_distance_info:
			FollowInfoDialog dialog = new FollowInfoDialog(mContext,
					R.style.MyDialog, mContext.getString(R.string.notify),
					mContext.getString(R.string.distance_info), 0);
			dialog.show();
			break;
		case R.id.et_device_name:
			
			break;
		case R.id.cb_location_switch:
			if (mCbLocation.isChecked()) {
				if (!LocationUtils.isOPen(mContext)) {
					FollowInfoDialog dialogLocation = new FollowInfoDialog(
							mContext, R.style.MyDialog, null,
							mContext.getString(R.string.open_gps), 0);
					dialogLocation.show();
				}

			} else {
				if (LocationUtils.isOPen(mContext)) {

					FollowInfoDialog dialogLocation = new FollowInfoDialog(
							mContext, R.style.MyDialog, null,
							mContext.getString(R.string.close_gps), 1);
					dialogLocation.show();
				}
			}
			break;
		case R.id.ll_delete_device:
			showWindows(1);
			break;
		case R.id.ll_not_disturb:
			mIntent = new Intent(mContext, DonotDistubActivity.class);
			startActivity(mIntent);
			break;
		case R.id.ll_sound:
			mIntent = new Intent(mContext, SoundActivity.class);
			startActivity(mIntent);
			break;
		case R.id.iv_back:
			finish();
			break;
		case R.id.tv_change_device:
			showWindows(0);
			break;

		default:
			break;
		}
	}

	private void showWindows(int type) {
		
		menuWindow = new SelectPicPopupWindow(DeviceSetActivity.this,itemsOnClick, type);
		
		menuWindow.showAtLocation(this.findViewById(R.id.main), Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
	}

	private OnClickListener itemsOnClick = new OnClickListener() {

		public void onClick(View v) {
			menuWindow.dismiss();
			switch (v.getId()) {
			case R.id.btn_take_photo:
				takePhoto();
				break;
			case R.id.btn_pick_photo:
				Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
				intent.setType("image/*");
				startActivityForResult(intent, CAMERA_SELECT);
				break;
			case R.id.btn_delete:
				if (AppContext.mBluetoothLeService == null) {
					return;
				}
				AppContext.mBluetoothLeService.close();
				finish();
				break;
			default:
				break;
			}
		}
	};

	public String name;

	private static final String PATH = Environment.getExternalStorageDirectory() + "/DCIM";
	
	private static final int CAMERA_TAKE = 1;
	
	private static final int CAMERA_SELECT = 2;

	public void takePhoto() {
		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		new DateFormat();
		name = DateFormat.format("yyyyMMdd_hhmmss",Calendar.getInstance(Locale.CHINA)) + ".jpg";
		Uri imageUri = Uri.fromFile(new File(PATH, name));
		intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);

		startActivityForResult(intent, CAMERA_TAKE);
	}

	private Bitmap mBitmap;

	private String mFilePath;

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == CAMERA_TAKE) {
			mFilePath = PATH + "/" + name;
			mBitmap = ImageTools.getBitmapFromFile(mFilePath, 8);
			if (mBitmap == null) {
				return;
			}

		} else {
			ContentResolver resolver = getContentResolver();
			if (data == null) {
				return;
			}
			Uri imgUri = data.getData();
			try {
				mBitmap = MediaStore.Images.Media.getBitmap(resolver, imgUri);

			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	};

	private String getGalleryPhotoPath(Uri originalUri) {
		String[] proj = { MediaStore.Images.Media.DATA };
		// 好像是android多媒体数据库的封装接口，具体的看Android文档
		Cursor cursor = managedQuery(originalUri, proj, null, null, null);
		// 按我个人理解 这个是获得用户选择的图片的索引值
		int column_index = cursor
				.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
		// 将光标移至开头 ，这个很重要，不小心很容易引起越界
		cursor.moveToFirst();
		// 最后根据索引值获取图片路径
		String path = cursor.getString(column_index);
		return path;
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
