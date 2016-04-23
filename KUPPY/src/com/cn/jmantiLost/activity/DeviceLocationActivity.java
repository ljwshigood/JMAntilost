package com.cn.jmantiLost.activity;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

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
import com.cn.jmantiLost.R;
import com.cn.jmantiLost.adapter.LocationDeviceAdapter;
import com.cn.jmantiLost.bean.DeviceSetInfo;
import com.cn.jmantiLost.db.DatabaseManager;
import com.cn.jmantiLost.util.LocationUtils;
import com.cn.jmantiLost.view.FollowInfoDialog;

public class DeviceLocationActivity extends FragmentActivity implements OnItemClickListener, 
																		OnClickListener,
																		LocationSource,
																		AMapLocationListener {

	private boolean showFlag = true;

	private DatabaseManager mDatabaseManager;

	private Context mContext;

	private ArrayList<DeviceSetInfo> mDeviceList = new ArrayList<DeviceSetInfo>();

	private MapView mapView;
	
	private void init() {
		if (aMap == null) {
			aMap = mapView.getMap();
			setUpMap();
		}
	}

	/**
	 * 设置一些amap的属性
	 */
	private void setUpMap() {
		// 自定义系统定位小蓝点
		MyLocationStyle myLocationStyle = new MyLocationStyle();
		myLocationStyle.myLocationIcon(BitmapDescriptorFactory.fromResource(R.drawable.location_marker));// 设置小蓝点的图标
		myLocationStyle.strokeColor(Color.BLACK);// 设置圆形的边框颜色
		myLocationStyle.radiusFillColor(Color.argb(100, 0, 0, 180));// 设置圆形的填充颜色
		// myLocationStyle.anchor(int,int)//设置小蓝点的锚点
		myLocationStyle.strokeWidth(1.0f);// 设置圆形的边框粗细
		aMap.setMyLocationStyle(myLocationStyle);
		aMap.setLocationSource(this);// 设置定位监听
		aMap.getUiSettings().setMyLocationButtonEnabled(true);// 设置默认定位按钮是否显示
		aMap.setMyLocationEnabled(true);// 设置为true表示显示定位层并可触发定位，false表示隐藏定位层并不可触发定位，默认是false
	}


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContext = DeviceLocationActivity.this;
		mDatabaseManager = DatabaseManager.getInstance(mContext);
		setContentView(R.layout.activity_location);
		mapView = (MapView) findViewById(R.id.map);
		mapView.onCreate(savedInstanceState);// 此方法必须重写
		if (!LocationUtils.isOPen(mContext)) {
			FollowInfoDialog dialogLocation = new FollowInfoDialog(
					mContext, R.style.MyDialog, null,
					mContext.getString(R.string.open_gps), 1);
			dialogLocation.show();
		}
		
		init();
		initDeviceList();
	}

	private void initDeviceList() {
		mDeviceList = mDatabaseManager.selectDeviceInfoByLocation();
		LocationDeviceAdapter mFindDeviceAdapter = new LocationDeviceAdapter(this, mDeviceList);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
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
	}

	private AMap aMap ;
	
	private Marker mMark;
	
	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int position,long arg3) {
		
		if(mDeviceList == null){
			return ;
		}
		
		DeviceSetInfo deviceSetInfo = mDeviceList.get(position);
		if(deviceSetInfo == null){
			return ;
		}
		
		if(deviceSetInfo.getLat() == null || deviceSetInfo.getLat().equals("")){
			return ;
		}
		
		if(deviceSetInfo.getLng() == null || deviceSetInfo.getLng().equals("")){
			return ;
		}
		
		double lat = Double.valueOf(deviceSetInfo.getLat()) ;
		double lng = Double.valueOf(deviceSetInfo.getLng()) ;
		
		aMap.clear();
		
		mMark = aMap.addMarker(new MarkerOptions().anchor(0.5f, 0.5f)
				.position(new LatLng(lat,lng))
				.icon(BitmapDescriptorFactory.fromResource(R.drawable.location_marker))
				.draggable(true));

		CameraUpdate update = CameraUpdateFactory.newCameraPosition(new CameraPosition(new LatLng(lat,lng), 15, 30, 0));
		aMap.animateCamera(update, 500, null);

		mMark.showInfoWindow();
		
		
		
		aMap.addCircle(new CircleOptions().center(new LatLng(lat,lng))
				.radius(500)
				.strokeColor(Color.argb(255, 19, 167, 72))
				.fillColor(Color.argb(50, 203, 240, 143)).strokeWidth(1));
		
		showFlag = true;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_find:
			if (showFlag) {
				showFlag = false;
			} else {
				showFlag = true;
			}
			break;
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

	@Override
	public void onLocationChanged(AMapLocation aLocation) {
		if (mListener != null && aLocation != null) {
			mListener.onLocationChanged(aLocation);
			aMap.moveCamera(CameraUpdateFactory.zoomTo(19));
			mAMapLocationManager.removeUpdates(this);
			mAMapLocationManager.destroy();
		}
	}

	@Override
	public void activate(OnLocationChangedListener listener) {
		mListener = listener;
		if (mAMapLocationManager == null) {
			mAMapLocationManager = LocationManagerProxy.getInstance(this);
			mAMapLocationManager.requestLocationData(LocationProviderProxy.AMapNetwork, 2000, 10, this);
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

}
