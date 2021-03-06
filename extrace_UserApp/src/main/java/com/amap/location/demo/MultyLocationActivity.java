package com.amap.location.demo;

import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.location.LocationManagerProxy;
import com.amap.api.location.LocationProviderProxy;
import com.amap.api.maps.AMap;
import com.amap.api.maps.AMap.OnMapClickListener;
import com.amap.api.maps.AMap.OnMapLoadedListener;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.PolylineOptions;
import com.amap.api.maps.overlay.PoiOverlay;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.poisearch.PoiItemDetail;
import com.amap.api.services.poisearch.PoiResult;
import com.amap.api.services.poisearch.PoiSearch;
import com.amap.api.services.poisearch.PoiSearch.OnPoiSearchListener;
import com.amap.api.services.poisearch.PoiSearch.SearchBound;
import com.track.app.user.R;

import java.util.ArrayList;
import java.util.List;

/**
 * 混合定位示例
 * */
public class MultyLocationActivity extends AppCompatActivity implements
		LocationSource, AMapLocationListener, OnCheckedChangeListener,
		OnPoiSearchListener, OnMapClickListener, OnMapLoadedListener {
	private AMap aMap;
	private AMapLocation mLocation;
	private MapView mapView;
	private OnLocationChangedListener mListener;
	private LocationManagerProxy mAMapLocationManager;
	private RadioGroup mGPSModeGroup;
	private List<LatLng> traceList;
	private double trace[] = { 39.917339, 116.418568, 39.915798, 116.412391,
			39.915325, 116.407993, 39.915739, 116.402972, 39.923323,
			116.394707, 39.919176, 116.391075, 39.922671, 116.385668,
			39.922907, 116.378485, 39.918405, 116.381336, 39.915561,
			116.381336, 39.914969, 116.380561, 39.915087, 116.373614,
			39.915561, 116.364495 };

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_multy_location);
		mapView = (MapView) findViewById(R.id.map);
		mapView.onCreate(savedInstanceState);// 此方法必须重写
		mapView.getMap().setOnMapLoadedListener(this);
		traceList = new ArrayList<LatLng>();
		for (int i = 0; i < trace.length - 1; i++) {
			LatLng mLatlng = new LatLng(trace[i], trace[++i]);
			traceList.add(mLatlng);
		}
		init();
		PolylineOptions po = new PolylineOptions();
		po.color(Color.BLUE);
		mapView.getMap().addPolyline(new PolylineOptions().addAll(traceList));
		addMarker(traceList.get(0), "起点");
		addMarker(traceList.get(traceList.size() - 1), "终点");
	}

	private void init() {
		if (aMap == null) {
			aMap = mapView.getMap();
			setUpMap();
		}
		mGPSModeGroup = (RadioGroup) findViewById(R.id.gps_radio_group);
		mGPSModeGroup.setOnCheckedChangeListener(this);
	}

	private void setUpMap() {

		aMap.setLocationSource(this);// 设置定位监听
		aMap.getUiSettings().setMyLocationButtonEnabled(true);// 设置默认定位按钮是否显示
		aMap.setMyLocationEnabled(true);// 设置为true表示显示定位层并可触发定位，false表示隐藏定位层并不可触发定位，默认是false
		// 设置定位的类型为定位模式 ，可以由定位、跟随或地图根据面向方向旋转几种
		aMap.setMyLocationType(AMap.LOCATION_TYPE_LOCATE);
		aMap.setOnMapClickListener(this);

	}

	/**
	 * 定位成功后回调函数
	 */
	@Override
	public void onLocationChanged(AMapLocation amapLocation) {
		if (mListener != null && amapLocation != null) {
			if (amapLocation != null
					&& amapLocation.getAMapException().getErrorCode() == 0) {
				mLocation = amapLocation;
				mListener.onLocationChanged(amapLocation);// 显示系统小蓝点

			} else {
				Log.e("AmapErr", "Location ERR:"
						+ amapLocation.getAMapException().getErrorCode());
			}
		}
	}

	/**
	 * 激活定位
	 * 
	 * 
	 */
	@Override
	public void activate(OnLocationChangedListener listener) {
		mListener = listener;
		if (mAMapLocationManager == null) {
			mAMapLocationManager = LocationManagerProxy.getInstance(this);
			// 第三个参数是移动多少距离会回调方法
			// 注意设置合适的定位时间的间隔（最小间隔支持为2000ms）
			// 其中如果间隔时间为-1，则定位只定一次
			mAMapLocationManager.requestLocationData(
					LocationProviderProxy.AMapNetwork, 10 * 1000, 10, this);
		}
	}

	/**
	 * 停止定位
	 */
	@Override
	public void deactivate() {
		mListener = null;
		if (mAMapLocationManager != null) {
			// removeUpdates(this)取消定位请求
			mAMapLocationManager.removeUpdates(this);
			mAMapLocationManager.destroy();
		}
		mAMapLocationManager = null;
	}

	/**
	 * 搜索成功后回调函数
	 */
	@Override
	public void onPoiSearched(PoiResult arg0, int arg1) {
		// TODO Auto-generated method stub
		if (arg1 == 0) {
			aMap.clear();// 清理之前的图标
			Log.e("result", "success");
			List<PoiItem> poiItems = arg0.getPois();
			if (poiItems.isEmpty()) {
				Toast.makeText(getApplicationContext(), "无查询结果！",
						Toast.LENGTH_SHORT).show();
			} else {
				for (PoiItem p : poiItems) {
					Log.e("result", p.toString());
				}
				PoiOverlay poiOverlay = new PoiOverlay(aMap, poiItems);
				poiOverlay.removeFromMap();
				poiOverlay.addToMap();
				poiOverlay.zoomToSpan();
			}
		}

	}

	// 搜索周边标志物
	public void search(String keyCode, String POI) {
		PoiSearch.Query query = new PoiSearch.Query(keyCode, POI, "郑州市");
		query.setPageSize(10);
		query.setPageNum(0);
		PoiSearch poisearch = new PoiSearch(this, query);
		poisearch.setOnPoiSearchListener(this);
		LatLonPoint point = new LatLonPoint(mLocation.getLatitude(),
				mLocation.getLongitude());
		Toast.makeText(getApplicationContext(),
				mLocation.getLatitude() + "||" + mLocation.getAltitude(),
				Toast.LENGTH_SHORT).show();

		// 第一个参数为自己的定位点，第二个为搜索半径
		poisearch.setBound(new SearchBound(point, 3000, true));
		poisearch.searchPOIAsyn();
	}

	@Override
	public void onMapClick(LatLng arg0) {
		// TODO Auto-generated method stub
		Toast.makeText(getApplicationContext(),
				arg0.latitude + "||" + arg0.longitude, Toast.LENGTH_SHORT)
				.show();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub

		int id = item.getItemId();
		if (id == R.id.location_details) {
			// 网络定位（Wifi+基站）
			Intent netIntent = new Intent(MultyLocationActivity.this,
					NetLocationActivity.class);
			startActivity(netIntent);
		}
		if (id == R.id.location_clear) {
			aMap.clear();
			return true;
		}
		if (id == R.id.location_trace) {
			PolylineOptions po = new PolylineOptions();
			po.color(Color.BLUE);
			mapView.getMap().addPolyline(
					new PolylineOptions().addAll(traceList));
			addMarker(traceList.get(0), "起点");
			addMarker(traceList.get(traceList.size() - 1), "终点");
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	private void addMarker(LatLng lt, String des) {
		// TODO Auto-generated method stub
		MarkerOptions markerOptions = new MarkerOptions();
		markerOptions.title(des);
		// 设置Marker的坐标，为我们点击地图的经纬度坐标
		markerOptions.position(lt);
		// 设置Marker的可见性
		markerOptions.visible(true);
		// 设置Marker是否可以被拖拽，这里先设置为false，之后会演示Marker的拖拽功能
		markerOptions.draggable(false);
		// 将Marker添加到地图上去
		aMap.addMarker(markerOptions);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.map, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public void onCheckedChanged(RadioGroup group, int checkedId) {
		switch (checkedId) {
		case R.id.gps_locate_button:
			// 设置定位的类型为定位模式
			aMap.setMyLocationType(AMap.LOCATION_TYPE_LOCATE);
			break;
		case R.id.gps_follow_button:
			// 设置定位的类型为 跟随模式
			aMap.setMyLocationType(AMap.LOCATION_TYPE_MAP_FOLLOW);
			break;
		case R.id.gps_rotate_button:
			// 设置定位的类型为根据地图面向方向旋转
			aMap.setMyLocationType(AMap.LOCATION_TYPE_MAP_ROTATE);
			break;
		}

	}

	@Override
	public void onPoiItemDetailSearched(PoiItemDetail arg0, int arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onMapLoaded() {

	}

	/**
	 * 方法必须重写
	 */
	@Override
	protected void onResume() {
		super.onResume();
		mapView.onResume();
	}

	/**
	 * 方法必须重写
	 */
	@Override
	protected void onPause() {
		super.onPause();
		mapView.onPause();
		deactivate();
	}

	/**
	 * 方法必须重写
	 */
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		mapView.onSaveInstanceState(outState);
	}

	/**
	 * 方法必须重写
	 */
	@Override
	protected void onDestroy() {
		super.onDestroy();
		mapView.onDestroy();
	}

	/**
	 * 此方法已经废弃
	 */
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

}
