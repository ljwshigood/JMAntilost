package com.cn.jmantiLost.activity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.location.LocationManagerProxy;
import com.amap.api.location.LocationProviderProxy;
import com.amap.api.maps2d.AMap;
import com.amap.api.maps2d.CameraUpdate;
import com.amap.api.maps2d.CameraUpdateFactory;
import com.amap.api.maps2d.LocationSource;
import com.amap.api.maps2d.MapView;
import com.amap.api.maps2d.model.BitmapDescriptorFactory;
import com.amap.api.maps2d.model.CameraPosition;
import com.amap.api.maps2d.model.CircleOptions;
import com.amap.api.maps2d.model.LatLng;
import com.amap.api.maps2d.model.Marker;
import com.amap.api.maps2d.model.MarkerOptions;
import com.amap.api.maps2d.model.MyLocationStyle;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.geocoder.GeocodeAddress;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.GeocodeSearch.OnGeocodeSearchListener;
import com.amap.api.services.geocoder.RegeocodeQuery;
import com.amap.api.services.geocoder.RegeocodeResult;
import com.cn.jmantiLost.R;
import com.cn.jmantiLost.application.AppContext;
import com.cn.jmantiLost.bean.DeviceSetInfo;
import com.cn.jmantiLost.db.DatabaseManager;
import com.cn.jmantiLost.service.BluetoothLeService;
import com.cn.jmantiLost.util.LocationUtils;
import com.cn.jmantiLost.view.FollowInfoDialog;

public class DeviceLocationActivity extends FragmentActivity implements OnClickListener,
																		LocationSource,
																		AMapLocationListener,
																		OnGeocodeSearchListener{

	private DatabaseManager mDatabaseManager;

	private Context mContext;

	private ArrayList<DeviceSetInfo> mDeviceList = new ArrayList<DeviceSetInfo>();

	private MapView mapView;
	
	private TextView mTvPlace ;
	
	private TextView mTvTime ;
	
	private ImageView mIvLocate ;
	
	private void init() {
		if (aMap == null) {
			aMap = mapView.getMap();
			setUpMap();
		}
	}
	
	private RelativeLayout mRlDeviceInfo ;
	
	private void initView(){
		mRlDeviceInfo = (RelativeLayout)findViewById(R.id.rl_device_info);
		mIvLocate = (ImageView)findViewById(R.id.iv_locate);
		mTvPlace = (TextView)findViewById(R.id.tv_place) ;
		mTvTime = (TextView)findViewById(R.id.tv_time) ;
		mIvLocate.setOnClickListener(this) ;
		mRlDeviceInfo.setVisibility(View.GONE);
		mRlDeviceInfo.setOnClickListener(this) ;
	}

	
	private void setUpMap() {
		MyLocationStyle myLocationStyle = new MyLocationStyle();
		myLocationStyle.myLocationIcon(BitmapDescriptorFactory.fromResource(R.drawable.location_marker));
		myLocationStyle.strokeColor(Color.TRANSPARENT);
		myLocationStyle.radiusFillColor(Color.TRANSPARENT);
		myLocationStyle.strokeWidth(1.0f);
		aMap.setMyLocationStyle(myLocationStyle);
		aMap.setLocationSource(this);
		aMap.getUiSettings().setMyLocationButtonEnabled(false);
		aMap.setMyLocationEnabled(true);
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


	private GeocodeSearch geocoderSearch;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContext = DeviceLocationActivity.this;
		mDatabaseManager = DatabaseManager.getInstance(mContext);
		initDeviceList();
		setContentView(R.layout.activity_location);
		mapView = (MapView) findViewById(R.id.map);
		mapView.onCreate(savedInstanceState) ;
		if (!LocationUtils.isOPen(mContext)) {
			
			FollowInfoDialog dialogLocation = new FollowInfoDialog(
					mContext, R.style.MyDialog, null,
					mContext.getString(R.string.open_gps), 1);
			dialogLocation.show();
		}
		
		initView() ;
		init();
		registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
	}

	private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			final String action = intent.getAction();
			BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
			if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
				initDeviceList() ;
				mRlDeviceInfo.setVisibility(View.VISIBLE) ;
				initDisconnectLocation() ;
			}
		}
	};
	
	private void initDeviceList() {
		mDeviceList = mDatabaseManager.selectDeviceInfoByLocation();
	}
	
	@Override
	protected void onResume() {
		AppContext.isAlarm = true ;
		super.onResume();
		
		if(AppContext.mBluetoothLeService != null && !AppContext.mBluetoothLeService.isConnect() && isExistLocationRecord()){
			initDisconnectLocation() ;
			mRlDeviceInfo.setVisibility(View.VISIBLE) ;
		}else{
			aMap.clear() ;
			initLocationMark();
			if(mMark != null){
				mMark.showInfoWindow();
			}
			mRlDeviceInfo.setVisibility(View.GONE) ;
		}
		
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mapView.onDestroy();
		unregisterReceiver(mGattUpdateReceiver) ;
	}

	private AMap aMap ;
	
	private Marker mMark;
	
	
	public void getAddress(final LatLonPoint latLonPoint) {
		RegeocodeQuery query = new RegeocodeQuery(latLonPoint, 200,GeocodeSearch.AMAP);
		geocoderSearch.getFromLocationAsyn(query);
	}
	
	public String formatHourAndMinute(long time) {
		
		String timer ; 
		SimpleDateFormat formatHourMin = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date duraTime = new Date(time);
		timer = formatHourMin.format(duraTime) ; 
		return timer;
		
	}
	
	private void initDisconnectLocation(){
		
		if(mDeviceList != null && mDeviceList.size() > 0){
			DeviceSetInfo deviceSetInfo = mDeviceList.get(0) ;
			getAddress(new LatLonPoint(Double.valueOf(deviceSetInfo.getLat()), Double.valueOf(deviceSetInfo.getLng())));
			
			mTvTime.setText(formatHourAndMinute(deviceSetInfo.getTime())) ;
			double lat = Double.valueOf(deviceSetInfo.getLat()) ;
			double lng = Double.valueOf(deviceSetInfo.getLng()) ;
			
			mMark = aMap.addMarker(new MarkerOptions().anchor(0.5f, 0.5f)
					.position(new LatLng(lat,lng))
					.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_pin))
					.draggable(true));

			CameraUpdate update = CameraUpdateFactory.newCameraPosition(new CameraPosition(new LatLng(lat,lng), 15, 30, 0));
			aMap.animateCamera(update, 500, null);

			mMark.showInfoWindow();
			
			aMap.addCircle(new CircleOptions().center(new LatLng(lat,lng))
					.radius(500)
					.strokeColor(Color.argb(255, 19, 167, 72))
					.fillColor(Color.argb(50, 203, 240, 143)).strokeWidth(1));
		}
		
	}


	private void initLocationMark() {
		
		if(mAmapLocation == null){
			return ;
		}
		mMark = aMap.addMarker(new MarkerOptions().anchor(0.5f, 0.5f)
				.position(new LatLng(mAmapLocation.getLatitude(),mAmapLocation.getLongitude()))
				.icon(BitmapDescriptorFactory.fromResource(R.drawable.location_marker))
				.draggable(true));
		
		CameraUpdate update = CameraUpdateFactory.newCameraPosition(new CameraPosition(new LatLng(mAmapLocation.getLatitude(),mAmapLocation.getLongitude()), 15, 30, 0));
		aMap.animateCamera(update, 500, null);
	}
	
	private void initLocationPlace(Location aLocation) {
		mListener.onLocationChanged(aLocation);
		aMap.moveCamera(CameraUpdateFactory.zoomTo(19));
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.iv_locate :
			if(mAmapLocation != null){
				initLocationPlace(mAmapLocation) ;
			}
			
			break ;
		case R.id.rl_device_info :
			initDisconnectLocation() ;
			break ;
		}
	}

	
	private OnLocationChangedListener mListener;
	
	private LocationManagerProxy mAMapLocationManager;
	
	@Override
	public void onLocationChanged(Location location) {
		
	}

	@Override
	public void onProviderDisabled(String provider) {
		
	}

	@Override
	public void onProviderEnabled(String provider) {
		
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		
	}
	
	private AMapLocation mAmapLocation ;
	
	
	private boolean isExistLocationRecord(){
		boolean ret = true ;
		if(mDeviceList == null || mDeviceList.size() == 0){
			ret = false ;
		}
		return ret ;
	}
	
	@Override
	public void onLocationChanged(AMapLocation aLocation) {
		if (mListener != null && aLocation != null) {
			mAmapLocation = aLocation ;
			if(AppContext.mBluetoothLeService != null && AppContext.mBluetoothLeService.isConnect()){
				aMap.clear() ;
				initLocationMark() ;
				mMark.showInfoWindow();
			}else if(!isExistLocationRecord()){
				aMap.clear() ;
				initLocationMark();
				mMark.showInfoWindow();
			}
		}
	}

	@Override
	public void activate(OnLocationChangedListener listener) {
		mListener = listener;
		if (mAMapLocationManager == null) {
			geocoderSearch = new GeocodeSearch(this);
			geocoderSearch.setOnGeocodeSearchListener(this);
			
			mAMapLocationManager = LocationManagerProxy.getInstance(this);
			mAMapLocationManager.requestLocationData(LocationProviderProxy.AMapNetwork, 7000, 10, this);
		}
	}

	@Override
	public void deactivate() {
		mListener = null;
		if (mAMapLocationManager != null) {
			mAMapLocationManager.removeUpdates(this);
			mAMapLocationManager.destroy();
		}
		mAMapLocationManager = null;
	}

	@Override
	public void onGeocodeSearched(GeocodeResult result, int rCode) {
		if (rCode == 0) {
			if (result != null && result.getGeocodeAddressList() != null
					&& result.getGeocodeAddressList().size() > 0) {
				GeocodeAddress address = result.getGeocodeAddressList().get(0);
				String addressName = "经纬度值:" + address.getLatLonPoint() + "\n位置描述:"
						+ address.getFormatAddress();
				
				mTvPlace.setText(mContext.getString(R.string.disconnect_location)+" "+addressName);
			} 

		} 
	}

	@Override
	public void onRegeocodeSearched(RegeocodeResult result, int rCode) {
		if (rCode == 0) {
			if (result != null && result.getRegeocodeAddress() != null
					&& result.getRegeocodeAddress().getFormatAddress() != null) {
				String addressName = result.getRegeocodeAddress().getFormatAddress() ;
				mTvPlace.setText(addressName);
			} 
		}
	}

}
