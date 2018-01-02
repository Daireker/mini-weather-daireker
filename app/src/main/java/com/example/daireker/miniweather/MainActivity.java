package com.example.daireker.miniweather;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.example.daireker.bean.TodayWeather;
import com.example.daireker.util.MyService;
import com.example.daireker.util.NetUtil;
import com.example.daireker.util.ToastUtil;
import com.example.daireker.util.UpdateWidgetService;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private static final int UPDATE_TODAY_WEATHER = 1;
    private static final  int REQUEST_CODE_ACCESS_COARSE_LOCATION = 1;

    private ImageView mUpdateBtn, mCitySelect, mLocation, mShare,weatherImg, pmImg,
            image_day_1, image_day_2, image_day_3, image_day_4, image_day_5, image_day_6;

    private ProgressBar mUpdateBtnProgress;

    private TextView cityTv, timeTv, humidityTv, weekTv, pmDataTv, pmQualityTv,
            temperatureTv, climateTv ,windTv, city_name_Tv, nowTemperatureTv,
            tx_day_1, tx_day_2, tx_day_3, tx_day_4, tx_day_5, tx_day_6,
            tx_day_1_temperature, tx_day_2_temperature, tx_day_3_temperature,
            tx_day_4_temperature, tx_day_5_temperature, tx_day_6_temperature,
            tx_day_1_climate, tx_day_2_climate, tx_day_3_climate,
            tx_day_4_climate, tx_day_5_climate, tx_day_6_climate,
            tx_day_1_wind, tx_day_2_wind, tx_day_3_wind, tx_day_4_wind, tx_day_5_wind, tx_day_6_wind;

    private ViewPager vpWeather;
    private ViewPagerAdapter vpAdapterWeather;
    private List<View> views;
    private ImageView[] imageViews;
    private int[] ids = {R.id.show_1_3, R.id.show_4_6};

    public LocationClient mLocationClient = null;
    private MyLocationListener myLocationListener = new MyLocationListener();

    private final int SDK_PERMISSION_REQUEST = 127;
    private String permissionInfo;

    //通过消息机制，将解析的天气对象，通过消息发送给主线程，主线程接收到消息数 据后，调用updateTodayWeather函数，更新UI界面上的数据
    private Handler mHandler = new Handler(){
        public void handleMessage(android.os.Message msg){
            Log.d("myWeather","handler启动");
            switch (msg.what){
                case UPDATE_TODAY_WEATHER:
                    updateTodayWeather((TodayWeather)msg.obj);
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.weather_info);

        initView();
        initViewViewPager();
        Log.d("myWeather","创建成功");
        initDots();
        mLocationClient = new LocationClient(getApplicationContext());
        //声明LocationClient类
        mLocationClient.registerLocationListener(myLocationListener);
        //注册监听函数

        LocationClientOption option = new LocationClientOption();
        option.setOpenGps(true);
        option.setCoorType("bd09ll"); // 设置坐标类型
        option.setIsNeedAddress(true);
        option.setAddrType("all");
        //可选，是否需要地址信息，默认为不需要，即参数为false

        mLocationClient.setLocOption(option);
        //mLocationClient为第二步初始化过的LocationClient对象
        //需将配置好的LocationClientOption对象，通过setLocOption方法传递给LocationClient对象使用


        //测试是否连接到网络
        if(NetUtil.getNetworkState(this) != NetUtil.NETWORN_NONE){
            Log.d("myWeather","网络OK");
            ToastUtil.showToast(MainActivity.this,"网络OK！");
        }else{
            Log.d("myWeather","网络挂了");
            ToastUtil.showToast(MainActivity.this,"网络挂了！");
        }
    }

    protected void onStart(){
        super.onStart();
        MyBroadcst mBroadcast = new MyBroadcst();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("UPDATE_WEATHER");
        registerReceiver(mBroadcast, intentFilter);
    }

    protected void startService(){
        Intent updateService = new Intent(this, MyService.class);
        startService(updateService);
    }

    protected void updateWidgetService(String cityCode){
        Intent widgetUpdate = new Intent(this, UpdateWidgetService.class);
        widgetUpdate.putExtra("citycode",cityCode);
        startService(widgetUpdate);
    }

    protected void onDestroy(){
        super.onDestroy();
        stopService(new Intent(MainActivity.this, MyService.class));
        if (mLocationClient != null && mLocationClient.isStarted()) {
            mLocationClient.stop();
            mLocationClient = null;
        }
    }

    public class MyBroadcst extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            SharedPreferences sharedPreferences = getSharedPreferences("config",MODE_PRIVATE);
            String cityCode = sharedPreferences.getString("main_city_code","101010100");
            queryWeatherCode(cityCode);
        }
    }

    public class MyLocationListener implements BDLocationListener {
        @Override
        public void onReceiveLocation(BDLocation location){
            //此处的BDLocation为定位结果信息类，通过它的各种get方法可获取定位相关的全部结果
            //以下只列举部分获取地址相关的结果信息
            //更多结果信息获取说明，请参照类参考中BDLocation类中的说明

            String addr = location.getAddrStr();    //获取详细地址信息
            String country = location.getCountry();    //获取国家
            String province = location.getProvince();    //获取省份
            String city = location.getCity();    //获取城市
            String district = location.getDistrict();    //获取区县
            String street = location.getStreet();    //获取街道信息
            String citycode = location.getCityCode();

            double latitude = location.getLatitude();    //获取纬度信息
            double longitude = location.getLongitude();    //获取经度信息
            int errorCode = location.getLocType();

            Log.d("Location","定位内部！");

            Log.d("Location","country= " + country);
            Log.d("Location","province= " + province);
            Log.d("Location","city= " + city);
            Log.d("Location","addr= " + addr);
            Log.d("Location","citycode= " + citycode);

            Log.d("Location","latitude= " + latitude);
            Log.d("Location","longitude= " + longitude);
            Log.d("Location","errorCode= " + errorCode);
            ToastUtil.showToast(MainActivity.this,"城市定位为：" + city);
        }
    }

    void initDots(){
        imageViews = new ImageView[views.size()];
        for(int i=0;i<views.size();i++){
            imageViews[i] = (ImageView) findViewById(ids[i]);
        }
    }

    void initViewViewPager(){
        tx_day_1 = views.get(0).findViewById(R.id.tx_day_1);
        tx_day_2 = views.get(0).findViewById(R.id.tx_day_2);
        tx_day_3 = views.get(0).findViewById(R.id.tx_day_3);
        tx_day_4 = views.get(1).findViewById(R.id.tx_day_4);
        tx_day_5 = views.get(1).findViewById(R.id.tx_day_5);
        tx_day_6 = views.get(1).findViewById(R.id.tx_day_6);
        tx_day_1.setText("N/A");
        tx_day_2.setText("N/A");
        tx_day_3.setText("N/A");
        tx_day_4.setText("N/A");
        tx_day_5.setText("N/A");
        tx_day_6.setText("N/A");

        tx_day_1_temperature = views.get(0).findViewById(R.id.tx_day_1_temperature);
        tx_day_2_temperature = views.get(0).findViewById(R.id.tx_day_2_temperature);
        tx_day_3_temperature = views.get(0).findViewById(R.id.tx_day_3_temperature);
        tx_day_4_temperature = views.get(1).findViewById(R.id.tx_day_4_temperature);
        tx_day_5_temperature = views.get(1).findViewById(R.id.tx_day_5_temperature);
        tx_day_6_temperature = views.get(1).findViewById(R.id.tx_day_6_temperature);
        tx_day_1_temperature.setText("N/A");
        tx_day_2_temperature.setText("N/A");
        tx_day_3_temperature.setText("N/A");
        tx_day_4_temperature.setText("N/A");
        tx_day_5_temperature.setText("N/A");
        tx_day_6_temperature.setText("N/A");

        tx_day_1_climate = views.get(0).findViewById(R.id.tx_day_1_climate);
        tx_day_2_climate = views.get(0).findViewById(R.id.tx_day_2_climate);
        tx_day_3_climate = views.get(0).findViewById(R.id.tx_day_3_climate);
        tx_day_4_climate = views.get(1).findViewById(R.id.tx_day_4_climate);
        tx_day_5_climate = views.get(1).findViewById(R.id.tx_day_5_climate);
        tx_day_6_climate = views.get(1).findViewById(R.id.tx_day_6_climate);
        tx_day_1_climate.setText("N/A");
        tx_day_2_climate.setText("N/A");
        tx_day_3_climate.setText("N/A");
        tx_day_4_climate.setText("N/A");
        tx_day_5_climate.setText("N/A");
        tx_day_6_climate.setText("N/A");

        tx_day_1_wind = views.get(0).findViewById(R.id.tx_day_1_wind);
        tx_day_2_wind = views.get(0).findViewById(R.id.tx_day_2_wind);
        tx_day_3_wind = views.get(0).findViewById(R.id.tx_day_3_wind);
        tx_day_4_wind = views.get(1).findViewById(R.id.tx_day_4_wind);
        tx_day_5_wind = views.get(1).findViewById(R.id.tx_day_5_wind);
        tx_day_6_wind = views.get(1).findViewById(R.id.tx_day_6_wind);
        tx_day_1_wind.setText("N/A");
        tx_day_2_wind.setText("N/A");
        tx_day_3_wind.setText("N/A");
        tx_day_4_wind.setText("N/A");
        tx_day_5_wind.setText("N/A");
        tx_day_6_wind.setText("N/A");

        image_day_1 = views.get(0).findViewById(R.id.image_day_1);
        image_day_2 = views.get(0).findViewById(R.id.image_day_2);
        image_day_3 = views.get(0).findViewById(R.id.image_day_3);
        image_day_4 = views.get(1).findViewById(R.id.image_day_4);
        image_day_5 = views.get(1).findViewById(R.id.image_day_5);
        image_day_6 = views.get(1).findViewById(R.id.image_day_6);
        image_day_1.setImageResource(R.drawable.biz_plugin_weather_qing);
        image_day_2.setImageResource(R.drawable.biz_plugin_weather_qing);
        image_day_3.setImageResource(R.drawable.biz_plugin_weather_qing);
        image_day_4.setImageResource(R.drawable.biz_plugin_weather_qing);
        image_day_5.setImageResource(R.drawable.biz_plugin_weather_qing);
        image_day_6.setImageResource(R.drawable.biz_plugin_weather_qing);
    }

    void initView(){
        city_name_Tv = (TextView) findViewById(R.id.title_city_name);
        cityTv = (TextView) findViewById(R.id.city);
        timeTv = (TextView) findViewById(R.id.time);
        humidityTv = (TextView) findViewById(R.id.humidity);
        weekTv = (TextView) findViewById(R.id.week_today);
        pmDataTv = (TextView) findViewById(R.id.pm_data);
        pmQualityTv = (TextView) findViewById(R.id.pm2_5_quality);
        pmImg = (ImageView) findViewById(R.id.pm2_5_img);
        temperatureTv = (TextView) findViewById(R.id.temperature);
        nowTemperatureTv = (TextView) findViewById(R.id.temperature_now);
        climateTv = (TextView) findViewById(R.id.climate);
        windTv = (TextView) findViewById(R.id.wind);
        weatherImg = (ImageView) findViewById(R.id.weather_img);

        mUpdateBtn = (ImageView) findViewById(R.id.title_update_btn);
        mUpdateBtn.setOnClickListener(this);

        mCitySelect = (ImageView) findViewById(R.id.title_city_manager);
        mCitySelect.setOnClickListener(this);

        mLocation = (ImageView) findViewById(R.id.title_location);
        mLocation.setOnClickListener(this);

        mShare = (ImageView) findViewById(R.id.title_share);
        mShare.setOnClickListener(this);

        mUpdateBtnProgress = (ProgressBar) findViewById(R.id.title_update_progress);

        city_name_Tv.setText("N/A");
        cityTv.setText("N/A");
        timeTv.setText("N/A");
        humidityTv.setText("N/A");
        pmDataTv.setText("N/A");
        pmQualityTv.setText("N/A");
        weekTv.setText("N/A");
        temperatureTv.setText("N/A");
        nowTemperatureTv.setText("N/A");
        climateTv.setText("N/A");
        windTv.setText("N/A");

        LayoutInflater inflater = LayoutInflater.from(this);
        views = new ArrayList<View>();
        views.add(inflater.inflate(R.layout.show_1_3_weather,null));
        views.add(inflater.inflate(R.layout.show_4_6_weather,null));
        vpAdapterWeather = new ViewPagerAdapter(views,this);
        vpWeather = (ViewPager) findViewById(R.id.weather_viewpager);
        vpWeather.setAdapter(vpAdapterWeather);
        vpWeather.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                for (int a=0;a<ids.length;a++){
                    if(a == position){
                        imageViews[a].setImageResource(R.drawable.page_indicator_focused);
                    }else {
                        imageViews[a].setImageResource(R.drawable.page_indicator_unfocused);
                    }
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    @TargetApi(23)
    private void getPersimmions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ArrayList<String> permissions = new ArrayList<String>();
            /***
             * 定位权限为必须权限，用户如果禁止，则每次进入都会申请
             */
            // 定位精确位置
            if(checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
            }
            if(checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION);
            }
			/*
			 * 读写权限和电话状态权限非必要权限(建议授予)只会申请一次，用户同意或者禁止，只会弹一次
			 */
            // 读写权限
            if (addPermission(permissions, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                permissionInfo += "Manifest.permission.WRITE_EXTERNAL_STORAGE Deny \n";
            }
            // 读取电话状态权限
            if (addPermission(permissions, Manifest.permission.READ_PHONE_STATE)) {
                permissionInfo += "Manifest.permission.READ_PHONE_STATE Deny \n";
            }

            if (permissions.size() > 0) {
                requestPermissions(permissions.toArray(new String[permissions.size()]), SDK_PERMISSION_REQUEST);
            }
        }
    }

    @TargetApi(23)
    private boolean addPermission(ArrayList<String> permissionsList, String permission) {
        if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) { // 如果应用没有获得对应权限,则添加到列表中,准备批量申请
            if (shouldShowRequestPermissionRationale(permission)){
                return true;
            }else{
                permissionsList.add(permission);
                return false;
            }

        }else{
            return true;
        }
    }

    @TargetApi(23)
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        // TODO Auto-generated method stub
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

    }

    public void onClick(View view){
        switch (view.getId()){
            case R.id.title_city_manager:
                Intent i = new Intent(this,SelectCity.class);
                //startActivity(intent);
                startActivityForResult(i,1);
                break;
            case R.id.title_update_btn:
                //通过SharedPreferences读取城市id，如果没有定义则缺省为101010100(北京城市 ID)
                SharedPreferences sharedPreferences = getSharedPreferences("config",MODE_PRIVATE);
                String cityCode = sharedPreferences.getString("main_city_code","101010100");
                Log.d("myWeather",cityCode);

                //测试是否连接到网络
                if(NetUtil.getNetworkState(this) != NetUtil.NETWORN_NONE){
                    Log.d("myWeather","网络OK");
                    ToastUtil.showToast(MainActivity.this,"网络OK！");
                    mUpdateBtn.setVisibility(View.INVISIBLE);
                    mUpdateBtnProgress.setVisibility(View.VISIBLE);
                    queryWeatherCode(cityCode);
                    startService();
                    updateWidgetService(cityCode);
                }else{
                    Log.d("myWeather","网络挂了");
                    ToastUtil.showToast(MainActivity.this,"网络挂了！");
                }
                break;
            case R.id.title_location:
                Log.d("Location","开始定位！");
                getPersimmions();

                LocationClientOption option = new LocationClientOption();
                //option.setScanSpan(100);
                option.setOpenGps(true);// 打开gps
                option.setCoorType("bd09ll"); // 设置坐标类型
                option.setIsNeedAddress(true);
                option.setAddrType("all");
                //可选，是否需要地址信息，默认为不需要，即参数为false

                mLocationClient.setLocOption(option);
                //mLocationClient为第二步初始化过的LocationClient对象
                //需将配置好的LocationClientOption对象，通过setLocOption方法传递给LocationClient对象使用
                if(mLocationClient == null){
                    return;
                }
                if(mLocationClient.isStarted()){
                    mLocationClient.stop();
                    mLocationClient.start();
                }else {
                    mLocationClient.start();
                }
                break;
            case R.id.title_share:
                ToastUtil.showToast(MainActivity.this,"To Be Continue...");
                break;
            default:
                break;
        }

    }

    //利用TodayWeather对象更新UI中的控件
    void updateTodayWeather(TodayWeather todayWeather){
        Log.d("myWeather","更新视图启动");

        city_name_Tv.setText(todayWeather.getCity()+"天气");
        cityTv.setText(todayWeather.getCity());
        timeTv.setText(todayWeather.getUpdatetime()+"发布");
        humidityTv.setText("湿度:"+todayWeather.getShidu());
        pmDataTv.setText(todayWeather.getPm25());
        pmQualityTv.setText(todayWeather.getQuality());
        weekTv.setText(todayWeather.getDate());
        temperatureTv.setText(todayWeather.getLow()+"~"+todayWeather.getHigh());
        nowTemperatureTv.setText("当前温度:"+todayWeather.getWendu()+"℃");
        climateTv.setText(todayWeather.getType());
        windTv.setText("风力:"+todayWeather.getFengli());

        tx_day_1.setText(todayWeather.getDate_1());
        tx_day_1_temperature.setText(todayWeather.getLow_1()+"~"+todayWeather.getHigh_1());
        tx_day_1_climate.setText(todayWeather.getType_1());
        tx_day_1_wind.setText(todayWeather.getFengli_1());

        tx_day_2.setText(todayWeather.getDate());
        tx_day_2_temperature.setText(todayWeather.getLow()+"~"+todayWeather.getHigh());
        tx_day_2_climate.setText(todayWeather.getType());
        tx_day_2_wind.setText(todayWeather.getFengli());

        tx_day_3.setText(todayWeather.getDate_3());
        tx_day_3_temperature.setText(todayWeather.getLow_3()+"~"+todayWeather.getHigh_3());
        tx_day_3_climate.setText(todayWeather.getType_3());
        tx_day_3_wind.setText(todayWeather.getFengli_3());

        tx_day_4.setText(todayWeather.getDate_4());
        tx_day_4_temperature.setText(todayWeather.getLow_4()+"~"+todayWeather.getHigh_4());
        tx_day_4_climate.setText(todayWeather.getType_4());
        tx_day_4_wind.setText(todayWeather.getFengli_4());

        tx_day_5.setText(todayWeather.getDate_5());
        tx_day_5_temperature.setText(todayWeather.getLow_5()+"~"+todayWeather.getHigh_5());
        tx_day_5_climate.setText(todayWeather.getType_5());
        tx_day_5_wind.setText(todayWeather.getFengli_5());

        tx_day_6.setText(todayWeather.getDate_6());
        tx_day_6_temperature.setText(todayWeather.getLow_6()+"~"+todayWeather.getHigh_6());
        tx_day_6_climate.setText(todayWeather.getType_6());
        tx_day_6_wind.setText(todayWeather.getFengli_6());

        mUpdateBtn.setVisibility(View.VISIBLE);
        mUpdateBtnProgress.setVisibility(View.INVISIBLE);

        changeImage(todayWeather);

        ToastUtil.showToast(MainActivity.this,"更新成功！");
    }

    void changeImage(TodayWeather todayWeather){
        Log.d("myWeather","更新pm weather图标");
        if(todayWeather.getPm25() != null){
            int p = Integer.parseInt(todayWeather.getPm25());
            try{
                if(0 < p && p <= 50){
                    pmImg.setImageDrawable(getResources().getDrawable(R.drawable.biz_plugin_weather_0_50));
                }
                else if(50 < p && p <=100){
                    pmImg.setImageDrawable(getResources().getDrawable(R.drawable.biz_plugin_weather_51_100));
                }
                else if(100 < p && p <= 150){
                    pmImg.setImageDrawable(getResources().getDrawable(R.drawable.biz_plugin_weather_101_150));
                }
                else if(150 < p && p <= 200){
                    pmImg.setImageDrawable(getResources().getDrawable(R.drawable.biz_plugin_weather_151_200));
                }
                else if(200 < p && p <= 300){
                    pmImg.setImageDrawable(getResources().getDrawable(R.drawable.biz_plugin_weather_201_300));
                }
                else {
                    pmImg.setImageDrawable(getResources().getDrawable(R.drawable.biz_plugin_weather_greater_300));
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }else {

        }

        String t = todayWeather.getType();
        String t1 = todayWeather.getType_1();
        String t2 = todayWeather.getType_2();
        String t3 = todayWeather.getType_3();
        String t4 = todayWeather.getType_4();
        String t5 = todayWeather.getType_5();
        String t6 = todayWeather.getType_6();

        try{
            if(t.equals("多云")){
                weatherImg.setImageDrawable(getResources().getDrawable(R.drawable.biz_plugin_weather_duoyun));
            }
            else if(t.equals("暴雪")){
                weatherImg.setImageDrawable(getResources().getDrawable(R.drawable.biz_plugin_weather_baoxue));
            }
            else if(t.equals("暴雨")){
                weatherImg.setImageDrawable(getResources().getDrawable(R.drawable.biz_plugin_weather_baoyu));
            }
            else if(t.equals("大暴雨")){
                weatherImg.setImageDrawable(getResources().getDrawable(R.drawable.biz_plugin_weather_dabaoyu));
            }
            else if(t.equals("特大暴雨")){
                weatherImg.setImageDrawable(getResources().getDrawable(R.drawable.biz_plugin_weather_tedabaoyu));
            }
            else if(t.equals("大雪")){
                weatherImg.setImageDrawable(getResources().getDrawable(R.drawable.biz_plugin_weather_daxue));
            }
            else if(t.equals("大雨")){
                weatherImg.setImageDrawable(getResources().getDrawable(R.drawable.biz_plugin_weather_dayu));
            }
            else if(t.equals("雷阵雨")){
                weatherImg.setImageDrawable(getResources().getDrawable(R.drawable.biz_plugin_weather_leizhenyu));
            }
            else if(t.equals("雷阵雨冰雹")){
                weatherImg.setImageDrawable(getResources().getDrawable(R.drawable.biz_plugin_weather_leizhenyubingbao));
            }
            else if(t.equals("沙尘暴")){
                weatherImg.setImageDrawable(getResources().getDrawable(R.drawable.biz_plugin_weather_shachenbao));
            }
            else if(t.equals("雾")){
                weatherImg.setImageDrawable(getResources().getDrawable(R.drawable.biz_plugin_weather_wu));
            }
            else if(t.equals("小雪")){
                weatherImg.setImageDrawable(getResources().getDrawable(R.drawable.biz_plugin_weather_xiaoxue));
            }
            else if(t.equals("小雨")){
                weatherImg.setImageDrawable(getResources().getDrawable(R.drawable.biz_plugin_weather_xiaoyu));
            }
            else if(t.equals("阴")){
                weatherImg.setImageDrawable(getResources().getDrawable(R.drawable.biz_plugin_weather_yin));
            }
            else if(t.equals("雨夹雪")){
                weatherImg.setImageDrawable(getResources().getDrawable(R.drawable.biz_plugin_weather_yujiaxue));
            }
            else if(t.equals("阵雪")){
                weatherImg.setImageDrawable(getResources().getDrawable(R.drawable.biz_plugin_weather_zhenxue));
            }
            else if(t.equals("阵雨")){
                weatherImg.setImageDrawable((getResources().getDrawable(R.drawable.biz_plugin_weather_zhenyu)));
            }
            else if(t.equals("中雪")){
                weatherImg.setImageDrawable(getResources().getDrawable(R.drawable.biz_plugin_weather_zhongxue));
            }
            else if(t.equals("中雨")){
                weatherImg.setImageDrawable(getResources().getDrawable(R.drawable.biz_plugin_weather_zhongyu));
            }
            else {
                weatherImg.setImageDrawable(getResources().getDrawable(R.drawable.biz_plugin_weather_qing));
            }

            if(t1.equals("多云")){
                image_day_1.setImageDrawable(getResources().getDrawable(R.drawable.biz_plugin_weather_duoyun));
            }
            else if(t1.equals("暴雪")){
                image_day_1.setImageDrawable(getResources().getDrawable(R.drawable.biz_plugin_weather_baoxue));
            }
            else if(t1.equals("暴雨")){
                image_day_1.setImageDrawable(getResources().getDrawable(R.drawable.biz_plugin_weather_baoyu));
            }
            else if(t1.equals("大暴雨")){
                image_day_1.setImageDrawable(getResources().getDrawable(R.drawable.biz_plugin_weather_dabaoyu));
            }
            else if(t1.equals("特大暴雨")){
                image_day_1.setImageDrawable(getResources().getDrawable(R.drawable.biz_plugin_weather_tedabaoyu));
            }
            else if(t1.equals("大雪")){
                image_day_1.setImageDrawable(getResources().getDrawable(R.drawable.biz_plugin_weather_daxue));
            }
            else if(t1.equals("大雨")){
                image_day_1.setImageDrawable(getResources().getDrawable(R.drawable.biz_plugin_weather_dayu));
            }
            else if(t1.equals("雷阵雨")){
                image_day_1.setImageDrawable(getResources().getDrawable(R.drawable.biz_plugin_weather_leizhenyu));
            }
            else if(t1.equals("雷阵雨冰雹")){
                image_day_1.setImageDrawable(getResources().getDrawable(R.drawable.biz_plugin_weather_leizhenyubingbao));
            }
            else if(t1.equals("沙尘暴")){
                image_day_1.setImageDrawable(getResources().getDrawable(R.drawable.biz_plugin_weather_shachenbao));
            }
            else if(t1.equals("雾")){
                image_day_1.setImageDrawable(getResources().getDrawable(R.drawable.biz_plugin_weather_wu));
            }
            else if(t1.equals("小雪")){
                image_day_1.setImageDrawable(getResources().getDrawable(R.drawable.biz_plugin_weather_xiaoxue));
            }
            else if(t1.equals("小雨")){
                image_day_1.setImageDrawable(getResources().getDrawable(R.drawable.biz_plugin_weather_xiaoyu));
            }
            else if(t1.equals("阴")){
                image_day_1.setImageDrawable(getResources().getDrawable(R.drawable.biz_plugin_weather_yin));
            }
            else if(t1.equals("雨夹雪")){
                image_day_1.setImageDrawable(getResources().getDrawable(R.drawable.biz_plugin_weather_yujiaxue));
            }
            else if(t1.equals("阵雪")){
                image_day_1.setImageDrawable(getResources().getDrawable(R.drawable.biz_plugin_weather_zhenxue));
            }
            else if(t1.equals("阵雨")){
                image_day_1.setImageDrawable((getResources().getDrawable(R.drawable.biz_plugin_weather_zhenyu)));
            }
            else if(t1.equals("中雪")){
                image_day_1.setImageDrawable(getResources().getDrawable(R.drawable.biz_plugin_weather_zhongxue));
            }
            else if(t1.equals("中雨")){
                image_day_1.setImageDrawable(getResources().getDrawable(R.drawable.biz_plugin_weather_zhongyu));
            }
            else {
                image_day_1.setImageDrawable(getResources().getDrawable(R.drawable.biz_plugin_weather_qing));
            }

            if(t.equals("多云")){
                image_day_2.setImageDrawable(getResources().getDrawable(R.drawable.biz_plugin_weather_duoyun));
            }
            else if(t.equals("暴雪")){
                image_day_2.setImageDrawable(getResources().getDrawable(R.drawable.biz_plugin_weather_baoxue));
            }
            else if(t.equals("暴雨")){
                image_day_2.setImageDrawable(getResources().getDrawable(R.drawable.biz_plugin_weather_baoyu));
            }
            else if(t.equals("大暴雨")){
                image_day_2.setImageDrawable(getResources().getDrawable(R.drawable.biz_plugin_weather_dabaoyu));
            }
            else if(t.equals("特大暴雨")){
                image_day_2.setImageDrawable(getResources().getDrawable(R.drawable.biz_plugin_weather_tedabaoyu));
            }
            else if(t.equals("大雪")){
                image_day_2.setImageDrawable(getResources().getDrawable(R.drawable.biz_plugin_weather_daxue));
            }
            else if(t.equals("大雨")){
                image_day_2.setImageDrawable(getResources().getDrawable(R.drawable.biz_plugin_weather_dayu));
            }
            else if(t.equals("雷阵雨")){
                image_day_2.setImageDrawable(getResources().getDrawable(R.drawable.biz_plugin_weather_leizhenyu));
            }
            else if(t.equals("雷阵雨冰雹")){
                image_day_2.setImageDrawable(getResources().getDrawable(R.drawable.biz_plugin_weather_leizhenyubingbao));
            }
            else if(t.equals("沙尘暴")){
                image_day_2.setImageDrawable(getResources().getDrawable(R.drawable.biz_plugin_weather_shachenbao));
            }
            else if(t.equals("雾")){
                image_day_2.setImageDrawable(getResources().getDrawable(R.drawable.biz_plugin_weather_wu));
            }
            else if(t.equals("小雪")){
                image_day_2.setImageDrawable(getResources().getDrawable(R.drawable.biz_plugin_weather_xiaoxue));
            }
            else if(t.equals("小雨")){
                image_day_2.setImageDrawable(getResources().getDrawable(R.drawable.biz_plugin_weather_xiaoyu));
            }
            else if(t.equals("阴")){
                image_day_2.setImageDrawable(getResources().getDrawable(R.drawable.biz_plugin_weather_yin));
            }
            else if(t.equals("雨夹雪")){
                image_day_2.setImageDrawable(getResources().getDrawable(R.drawable.biz_plugin_weather_yujiaxue));
            }
            else if(t.equals("阵雪")){
                image_day_2.setImageDrawable(getResources().getDrawable(R.drawable.biz_plugin_weather_zhenxue));
            }
            else if(t.equals("阵雨")){
                image_day_2.setImageDrawable((getResources().getDrawable(R.drawable.biz_plugin_weather_zhenyu)));
            }
            else if(t.equals("中雪")){
                image_day_2.setImageDrawable(getResources().getDrawable(R.drawable.biz_plugin_weather_zhongxue));
            }
            else if(t.equals("中雨")){
                image_day_2.setImageDrawable(getResources().getDrawable(R.drawable.biz_plugin_weather_zhongyu));
            }
            else {
                image_day_2.setImageDrawable(getResources().getDrawable(R.drawable.biz_plugin_weather_qing));
            }

            if(t3.equals("多云")){
                image_day_3.setImageDrawable(getResources().getDrawable(R.drawable.biz_plugin_weather_duoyun));
            }
            else if(t3.equals("暴雪")){
                image_day_3.setImageDrawable(getResources().getDrawable(R.drawable.biz_plugin_weather_baoxue));
            }
            else if(t3.equals("暴雨")){
                image_day_3.setImageDrawable(getResources().getDrawable(R.drawable.biz_plugin_weather_baoyu));
            }
            else if(t3.equals("大暴雨")){
                image_day_3.setImageDrawable(getResources().getDrawable(R.drawable.biz_plugin_weather_dabaoyu));
            }
            else if(t3.equals("特大暴雨")){
                image_day_3.setImageDrawable(getResources().getDrawable(R.drawable.biz_plugin_weather_tedabaoyu));
            }
            else if(t3.equals("大雪")){
                image_day_3.setImageDrawable(getResources().getDrawable(R.drawable.biz_plugin_weather_daxue));
            }
            else if(t3.equals("大雨")){
                image_day_3.setImageDrawable(getResources().getDrawable(R.drawable.biz_plugin_weather_dayu));
            }
            else if(t3.equals("雷阵雨")){
                image_day_3.setImageDrawable(getResources().getDrawable(R.drawable.biz_plugin_weather_leizhenyu));
            }
            else if(t3.equals("雷阵雨冰雹")){
                image_day_3.setImageDrawable(getResources().getDrawable(R.drawable.biz_plugin_weather_leizhenyubingbao));
            }
            else if(t3.equals("沙尘暴")){
                image_day_3.setImageDrawable(getResources().getDrawable(R.drawable.biz_plugin_weather_shachenbao));
            }
            else if(t3.equals("雾")){
                image_day_3.setImageDrawable(getResources().getDrawable(R.drawable.biz_plugin_weather_wu));
            }
            else if(t3.equals("小雪")){
                image_day_3.setImageDrawable(getResources().getDrawable(R.drawable.biz_plugin_weather_xiaoxue));
            }
            else if(t3.equals("小雨")){
                image_day_3.setImageDrawable(getResources().getDrawable(R.drawable.biz_plugin_weather_xiaoyu));
            }
            else if(t3.equals("阴")){
                image_day_3.setImageDrawable(getResources().getDrawable(R.drawable.biz_plugin_weather_yin));
            }
            else if(t3.equals("雨夹雪")){
                image_day_3.setImageDrawable(getResources().getDrawable(R.drawable.biz_plugin_weather_yujiaxue));
            }
            else if(t3.equals("阵雪")){
                image_day_3.setImageDrawable(getResources().getDrawable(R.drawable.biz_plugin_weather_zhenxue));
            }
            else if(t3.equals("阵雨")){
                image_day_3.setImageDrawable((getResources().getDrawable(R.drawable.biz_plugin_weather_zhenyu)));
            }
            else if(t3.equals("中雪")){
                image_day_3.setImageDrawable(getResources().getDrawable(R.drawable.biz_plugin_weather_zhongxue));
            }
            else if(t3.equals("中雨")){
                image_day_3.setImageDrawable(getResources().getDrawable(R.drawable.biz_plugin_weather_zhongyu));
            }
            else {
                image_day_3.setImageDrawable(getResources().getDrawable(R.drawable.biz_plugin_weather_qing));
            }

            if(t4.equals("多云")){
                image_day_4.setImageDrawable(getResources().getDrawable(R.drawable.biz_plugin_weather_duoyun));
            }
            else if(t4.equals("暴雪")){
                image_day_4.setImageDrawable(getResources().getDrawable(R.drawable.biz_plugin_weather_baoxue));
            }
            else if(t4.equals("暴雨")){
                image_day_4.setImageDrawable(getResources().getDrawable(R.drawable.biz_plugin_weather_baoyu));
            }
            else if(t4.equals("大暴雨")){
                image_day_4.setImageDrawable(getResources().getDrawable(R.drawable.biz_plugin_weather_dabaoyu));
            }
            else if(t4.equals("特大暴雨")){
                image_day_4.setImageDrawable(getResources().getDrawable(R.drawable.biz_plugin_weather_tedabaoyu));
            }
            else if(t4.equals("大雪")){
                image_day_4.setImageDrawable(getResources().getDrawable(R.drawable.biz_plugin_weather_daxue));
            }
            else if(t4.equals("大雨")){
                image_day_4.setImageDrawable(getResources().getDrawable(R.drawable.biz_plugin_weather_dayu));
            }
            else if(t4.equals("雷阵雨")){
                image_day_4.setImageDrawable(getResources().getDrawable(R.drawable.biz_plugin_weather_leizhenyu));
            }
            else if(t4.equals("雷阵雨冰雹")){
                image_day_4.setImageDrawable(getResources().getDrawable(R.drawable.biz_plugin_weather_leizhenyubingbao));
            }
            else if(t4.equals("沙尘暴")){
                image_day_4.setImageDrawable(getResources().getDrawable(R.drawable.biz_plugin_weather_shachenbao));
            }
            else if(t4.equals("雾")){
                image_day_4.setImageDrawable(getResources().getDrawable(R.drawable.biz_plugin_weather_wu));
            }
            else if(t4.equals("小雪")){
                image_day_4.setImageDrawable(getResources().getDrawable(R.drawable.biz_plugin_weather_xiaoxue));
            }
            else if(t4.equals("小雨")){
                image_day_4.setImageDrawable(getResources().getDrawable(R.drawable.biz_plugin_weather_xiaoyu));
            }
            else if(t4.equals("阴")){
                image_day_4.setImageDrawable(getResources().getDrawable(R.drawable.biz_plugin_weather_yin));
            }
            else if(t4.equals("雨夹雪")){
                image_day_4.setImageDrawable(getResources().getDrawable(R.drawable.biz_plugin_weather_yujiaxue));
            }
            else if(t4.equals("阵雪")){
                image_day_4.setImageDrawable(getResources().getDrawable(R.drawable.biz_plugin_weather_zhenxue));
            }
            else if(t4.equals("阵雨")){
                image_day_4.setImageDrawable((getResources().getDrawable(R.drawable.biz_plugin_weather_zhenyu)));
            }
            else if(t4.equals("中雪")){
                image_day_4.setImageDrawable(getResources().getDrawable(R.drawable.biz_plugin_weather_zhongxue));
            }
            else if(t4.equals("中雨")){
                image_day_4.setImageDrawable(getResources().getDrawable(R.drawable.biz_plugin_weather_zhongyu));
            }
            else {
                image_day_4.setImageDrawable(getResources().getDrawable(R.drawable.biz_plugin_weather_qing));
            }

            if(t5.equals("多云")){
                image_day_5.setImageDrawable(getResources().getDrawable(R.drawable.biz_plugin_weather_duoyun));
            }
            else if(t5.equals("暴雪")){
                image_day_5.setImageDrawable(getResources().getDrawable(R.drawable.biz_plugin_weather_baoxue));
            }
            else if(t5.equals("暴雨")){
                image_day_5.setImageDrawable(getResources().getDrawable(R.drawable.biz_plugin_weather_baoyu));
            }
            else if(t5.equals("大暴雨")){
                image_day_5.setImageDrawable(getResources().getDrawable(R.drawable.biz_plugin_weather_dabaoyu));
            }
            else if(t5.equals("特大暴雨")){
                image_day_5.setImageDrawable(getResources().getDrawable(R.drawable.biz_plugin_weather_tedabaoyu));
            }
            else if(t5.equals("大雪")){
                image_day_5.setImageDrawable(getResources().getDrawable(R.drawable.biz_plugin_weather_daxue));
            }
            else if(t5.equals("大雨")){
                image_day_5.setImageDrawable(getResources().getDrawable(R.drawable.biz_plugin_weather_dayu));
            }
            else if(t5.equals("雷阵雨")){
                image_day_5.setImageDrawable(getResources().getDrawable(R.drawable.biz_plugin_weather_leizhenyu));
            }
            else if(t5.equals("雷阵雨冰雹")){
                image_day_5.setImageDrawable(getResources().getDrawable(R.drawable.biz_plugin_weather_leizhenyubingbao));
            }
            else if(t5.equals("沙尘暴")){
                image_day_5.setImageDrawable(getResources().getDrawable(R.drawable.biz_plugin_weather_shachenbao));
            }
            else if(t5.equals("雾")){
                image_day_5.setImageDrawable(getResources().getDrawable(R.drawable.biz_plugin_weather_wu));
            }
            else if(t5.equals("小雪")){
                image_day_5.setImageDrawable(getResources().getDrawable(R.drawable.biz_plugin_weather_xiaoxue));
            }
            else if(t5.equals("小雨")){
                image_day_5.setImageDrawable(getResources().getDrawable(R.drawable.biz_plugin_weather_xiaoyu));
            }
            else if(t5.equals("阴")){
                image_day_5.setImageDrawable(getResources().getDrawable(R.drawable.biz_plugin_weather_yin));
            }
            else if(t5.equals("雨夹雪")){
                image_day_5.setImageDrawable(getResources().getDrawable(R.drawable.biz_plugin_weather_yujiaxue));
            }
            else if(t5.equals("阵雪")){
                image_day_5.setImageDrawable(getResources().getDrawable(R.drawable.biz_plugin_weather_zhenxue));
            }
            else if(t5.equals("阵雨")){
                image_day_5.setImageDrawable((getResources().getDrawable(R.drawable.biz_plugin_weather_zhenyu)));
            }
            else if(t5.equals("中雪")){
                image_day_5.setImageDrawable(getResources().getDrawable(R.drawable.biz_plugin_weather_zhongxue));
            }
            else if(t5.equals("中雨")){
                image_day_5.setImageDrawable(getResources().getDrawable(R.drawable.biz_plugin_weather_zhongyu));
            }
            else {
                image_day_5.setImageDrawable(getResources().getDrawable(R.drawable.biz_plugin_weather_qing));
            }

            if(t6.equals("多云")){
                image_day_6.setImageDrawable(getResources().getDrawable(R.drawable.biz_plugin_weather_duoyun));
            }
            else if(t6.equals("暴雪")){
                image_day_6.setImageDrawable(getResources().getDrawable(R.drawable.biz_plugin_weather_baoxue));
            }
            else if(t6.equals("暴雨")){
                image_day_6.setImageDrawable(getResources().getDrawable(R.drawable.biz_plugin_weather_baoyu));
            }
            else if(t6.equals("大暴雨")){
                image_day_6.setImageDrawable(getResources().getDrawable(R.drawable.biz_plugin_weather_dabaoyu));
            }
            else if(t6.equals("特大暴雨")){
                image_day_6.setImageDrawable(getResources().getDrawable(R.drawable.biz_plugin_weather_tedabaoyu));
            }
            else if(t6.equals("大雪")){
                image_day_6.setImageDrawable(getResources().getDrawable(R.drawable.biz_plugin_weather_daxue));
            }
            else if(t6.equals("大雨")){
                image_day_6.setImageDrawable(getResources().getDrawable(R.drawable.biz_plugin_weather_dayu));
            }
            else if(t6.equals("雷阵雨")){
                image_day_6.setImageDrawable(getResources().getDrawable(R.drawable.biz_plugin_weather_leizhenyu));
            }
            else if(t6.equals("雷阵雨冰雹")){
                image_day_6.setImageDrawable(getResources().getDrawable(R.drawable.biz_plugin_weather_leizhenyubingbao));
            }
            else if(t6.equals("沙尘暴")){
                image_day_6.setImageDrawable(getResources().getDrawable(R.drawable.biz_plugin_weather_shachenbao));
            }
            else if(t6.equals("雾")){
                image_day_6.setImageDrawable(getResources().getDrawable(R.drawable.biz_plugin_weather_wu));
            }
            else if(t6.equals("小雪")){
                image_day_6.setImageDrawable(getResources().getDrawable(R.drawable.biz_plugin_weather_xiaoxue));
            }
            else if(t6.equals("小雨")){
                image_day_6.setImageDrawable(getResources().getDrawable(R.drawable.biz_plugin_weather_xiaoyu));
            }
            else if(t6.equals("阴")){
                image_day_6.setImageDrawable(getResources().getDrawable(R.drawable.biz_plugin_weather_yin));
            }
            else if(t6.equals("雨夹雪")){
                image_day_6.setImageDrawable(getResources().getDrawable(R.drawable.biz_plugin_weather_yujiaxue));
            }
            else if(t6.equals("阵雪")){
                image_day_6.setImageDrawable(getResources().getDrawable(R.drawable.biz_plugin_weather_zhenxue));
            }
            else if(t6.equals("阵雨")){
                image_day_6.setImageDrawable((getResources().getDrawable(R.drawable.biz_plugin_weather_zhenyu)));
            }
            else if(t6.equals("中雪")){
                image_day_6.setImageDrawable(getResources().getDrawable(R.drawable.biz_plugin_weather_zhongxue));
            }
            else if(t6.equals("中雨")){
                image_day_6.setImageDrawable(getResources().getDrawable(R.drawable.biz_plugin_weather_zhongyu));
            }
            else {
                image_day_6.setImageDrawable(getResources().getDrawable(R.drawable.biz_plugin_weather_qing));
            }
        }catch(Exception e){
            e.printStackTrace();
        }

    }

    //修改更新按钮的单击事件处理程序，并编写onActivityResult函数用于接收返回的数据
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 1 && resultCode == RESULT_OK){
            String newCityCode = data.getStringExtra("cityCode");
            Log.d("myWeather","选择的城市代码为" + newCityCode);

            //测试是否连接到网络
            if(NetUtil.getNetworkState(this) != NetUtil.NETWORN_NONE){
                Log.d("myWeather","网络OK");
                ToastUtil.showToast(MainActivity.this,"网络OK！");
                if(newCityCode != null){
                    mUpdateBtn.setVisibility(View.INVISIBLE);
                    mUpdateBtnProgress.setVisibility(View.VISIBLE);
                    SharedPreferences sharedPreferences = getSharedPreferences("config",MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("main_city_code",newCityCode);
                    editor.commit();
                    queryWeatherCode(newCityCode);
                    startService();
                    updateWidgetService(newCityCode);
                }
            }else{
                Log.d("myWeather","网络挂了");
                ToastUtil.showToast(MainActivity.this,"网络挂了！");
            }
        }
    }

    //获取网络数据
    public void queryWeatherCode(String cityCode){
        final String address = "http://wthrcdn.etouch.cn/WeatherApi?citykey=" + cityCode;
        Log.d("myWeather",address);
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection con = null;
                TodayWeather todayWeather = null;
                try {
                    URL url = new URL(address);
                    con = (HttpURLConnection)url.openConnection();
                    con.setRequestMethod("GET");
                    con.setConnectTimeout(8000);
                    con.setReadTimeout(8000);
                    InputStream inputStream = con.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                    StringBuilder response = new StringBuilder();
                    String str;
                    while((str=reader.readLine()) != null){
                        response.append(str);
                        Log.d("str",str);
                    }
                    String responseStr = response.toString();
                    Log.d("responseStr",responseStr);
                    parseXML(responseStr);   //在获取网络数据后，调用解析函数

                    todayWeather = parseXML(responseStr);  //调用parseXML，并返回TodayWeather对象
                    if(todayWeather != null){
                        Log.d("myWeather",todayWeather.toString());

                        Message msg = new Message();
                        msg.what = UPDATE_TODAY_WEATHER;
                        msg.obj = todayWeather;
                        mHandler.sendMessage(msg);
                    }

                }catch (Exception e){
                    e.printStackTrace();
                }finally {
                    if(con != null){
                        con.disconnect();
                    }
                }
            }
        }).start();
    }

    //编写解析函数，解析出城市名称以及更新时间信息，将解析的数据存入TodayWeather对象中
    private TodayWeather parseXML(String xmldata){
        TodayWeather todayWeather = null;
        int fengxiangCount = 0;
        int fengliCount = 0;
        int dateCount = 0;
        int highCount = 0;
        int lowCount = 0;
        int typeCount = 0;
        try {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            XmlPullParser xmlPullParser = factory.newPullParser();
            xmlPullParser.setInput(new StringReader(xmldata));
            int eventType = xmlPullParser.getEventType();
            Log.d("myWeather","parseXML");
            while(eventType != XmlPullParser.END_DOCUMENT){
                switch (eventType){
                    //判断当前事件是否为文档开始事件
                    case XmlPullParser.START_DOCUMENT:
                        break;
                    //判断当前事件是否为标签元素开始事件
                    case XmlPullParser.START_TAG:
                        if (xmlPullParser.getName().equals("resp")){
                            todayWeather = new TodayWeather();
                        }
                        if(todayWeather != null){
                            if(xmlPullParser.getName().equals("city")){
                                eventType = xmlPullParser.next();
                                todayWeather.setCity(xmlPullParser.getText());
                                Log.d("myWeather","city:    "+xmlPullParser.getText());
                            }else if(xmlPullParser.getName().equals("updatetime")){
                                eventType = xmlPullParser.next();
                                todayWeather.setUpdatetime(xmlPullParser.getText());
                                Log.d("myWeather","updatetime:    "+xmlPullParser.getText());
                            }else if(xmlPullParser.getName().equals("shidu")){
                                eventType = xmlPullParser.next();
                                todayWeather.setShidu(xmlPullParser.getText());
                                Log.d("myWeather","shidu:    "+xmlPullParser.getText());
                            }else if(xmlPullParser.getName().equals("wendu")){
                                eventType = xmlPullParser.next();
                                todayWeather.setWendu(xmlPullParser.getText());
                                Log.d("myWeather","wendu:    "+xmlPullParser.getText());
                            }else if(xmlPullParser.getName().equals("pm25")){
                                eventType = xmlPullParser.next();
                                todayWeather.setPm25(xmlPullParser.getText());
                                Log.d("myWeather","pm25:    "+xmlPullParser.getText());
                            }else if(xmlPullParser.getName().equals("quality")){
                                eventType = xmlPullParser.next();
                                todayWeather.setQuality(xmlPullParser.getText());
                                Log.d("myWeather","quality:    "+xmlPullParser.getText());
                            }else if(xmlPullParser.getName().equals("fengxiang") && fengxiangCount ==0){
                                eventType = xmlPullParser.next();
                                todayWeather.setFengxiang(xmlPullParser.getText());
                                Log.d("myWeather","fengxiang:    "+xmlPullParser.getText());
                                fengxiangCount++;
                            }else if(xmlPullParser.getName().equals("fengli") && fengliCount == 0){
                                eventType = xmlPullParser.next();
                                todayWeather.setFengli(xmlPullParser.getText());
                                Log.d("myWeather","fengli:    "+xmlPullParser.getText());
                                fengliCount++;
                            }else if(xmlPullParser.getName().equals("date") && dateCount == 0){
                                eventType = xmlPullParser.next();
                                todayWeather.setDate(xmlPullParser.getText());
                                Log.d("myWeather","date:    "+xmlPullParser.getText());
                                dateCount++;
                            }else if(xmlPullParser.getName().equals("high") && highCount == 0){
                                eventType = xmlPullParser.next();
                                todayWeather.setHigh(xmlPullParser.getText().substring(2).trim());
                                Log.d("myWeather","high:    "+xmlPullParser.getText());
                                highCount++;
                            }else if(xmlPullParser.getName().equals("low") && lowCount == 0){
                                eventType = xmlPullParser.next();
                                todayWeather.setLow(xmlPullParser.getText().substring(2).trim());
                                Log.d("myWeather","low:    "+xmlPullParser.getText());
                                lowCount++;
                            }else if(xmlPullParser.getName().equals("type") && typeCount == 0){
                                eventType = xmlPullParser.next();
                                todayWeather.setType(xmlPullParser.getText());
                                Log.d("myWeather","type:    "+xmlPullParser.getText());
                                typeCount++;
                            }else if(xmlPullParser.getName().equals("date_1")){
                                eventType = xmlPullParser.next();
                                todayWeather.setDate_1(xmlPullParser.getText());
                            }else if(xmlPullParser.getName().equals("high_1")){
                                eventType = xmlPullParser.next();
                                todayWeather.setHigh_1(xmlPullParser.getText().substring(2).trim());
                            }else if(xmlPullParser.getName().equals("low_1")){
                                eventType = xmlPullParser.next();
                                todayWeather.setLow_1(xmlPullParser.getText().substring(2).trim());
                            }else if(xmlPullParser.getName().equals("fl_1")){
                                eventType = xmlPullParser.next();
                                todayWeather.setFengli_1(xmlPullParser.getText());
                            }else if(xmlPullParser.getName().equals("type_1")){
                                eventType = xmlPullParser.next();
                                todayWeather.setType_1(xmlPullParser.getText());
                            }else if(xmlPullParser.getName().equals("date") && dateCount == 1){
                                eventType = xmlPullParser.next();
                                todayWeather.setDate_3(xmlPullParser.getText());
                                dateCount++;
                            }else if(xmlPullParser.getName().equals("high") && highCount == 1){
                                eventType = xmlPullParser.next();
                                todayWeather.setHigh_3(xmlPullParser.getText().substring(2).trim());
                                highCount++;
                            }else if(xmlPullParser.getName().equals("low") && lowCount == 1){
                                eventType = xmlPullParser.next();
                                todayWeather.setLow_3(xmlPullParser.getText().substring(2).trim());
                                lowCount++;
                            }else if(xmlPullParser.getName().equals("type") && typeCount == 1){
                                eventType = xmlPullParser.next();
                                todayWeather.setType_3(xmlPullParser.getText());
                                typeCount++;
                            }else if(xmlPullParser.getName().equals("fengli") && fengliCount == 1){
                                eventType = xmlPullParser.next();
                                todayWeather.setFengli_3(xmlPullParser.getText());
                                fengliCount++;
                            }else if(xmlPullParser.getName().equals("date") && dateCount == 2){
                                eventType = xmlPullParser.next();
                                todayWeather.setDate_4(xmlPullParser.getText());
                                dateCount++;
                            }else if(xmlPullParser.getName().equals("high") && highCount == 2){
                                eventType = xmlPullParser.next();
                                todayWeather.setHigh_4(xmlPullParser.getText().substring(2).trim());
                                highCount++;
                            }else if(xmlPullParser.getName().equals("low") && lowCount == 2){
                                eventType = xmlPullParser.next();
                                todayWeather.setLow_4(xmlPullParser.getText().substring(2).trim());
                                lowCount++;
                            }else if(xmlPullParser.getName().equals("type") && typeCount == 2){
                                eventType = xmlPullParser.next();
                                todayWeather.setType_4(xmlPullParser.getText());
                                typeCount++;
                            }else if(xmlPullParser.getName().equals("fengli") && fengliCount == 2){
                                eventType = xmlPullParser.next();
                                todayWeather.setFengli_4(xmlPullParser.getText());
                                fengliCount++;
                            }else if(xmlPullParser.getName().equals("date") && dateCount == 3){
                                eventType = xmlPullParser.next();
                                todayWeather.setDate_5(xmlPullParser.getText());
                                dateCount++;
                            }else if(xmlPullParser.getName().equals("high") && highCount == 3){
                                eventType = xmlPullParser.next();
                                todayWeather.setHigh_5(xmlPullParser.getText().substring(2).trim());
                                highCount++;
                            }else if(xmlPullParser.getName().equals("low") && lowCount == 3){
                                eventType = xmlPullParser.next();
                                todayWeather.setLow_5(xmlPullParser.getText().substring(2).trim());
                                lowCount++;
                            }else if(xmlPullParser.getName().equals("type") && typeCount == 3){
                                eventType = xmlPullParser.next();
                                todayWeather.setType_5(xmlPullParser.getText());
                                typeCount++;
                            }else if(xmlPullParser.getName().equals("fengli") && fengliCount == 3){
                                eventType = xmlPullParser.next();
                                todayWeather.setFengli_5(xmlPullParser.getText());
                                fengliCount++;
                            }else if(xmlPullParser.getName().equals("date") && dateCount == 4){
                                eventType = xmlPullParser.next();
                                todayWeather.setDate_6(xmlPullParser.getText());
                                dateCount++;
                            }else if(xmlPullParser.getName().equals("high") && highCount == 4){
                                eventType = xmlPullParser.next();
                                todayWeather.setHigh_6(xmlPullParser.getText().substring(2).trim());
                                highCount++;
                            }else if(xmlPullParser.getName().equals("low") && lowCount == 4){
                                eventType = xmlPullParser.next();
                                todayWeather.setLow_6(xmlPullParser.getText().substring(2).trim());
                                lowCount++;
                            }else if(xmlPullParser.getName().equals("type") && typeCount == 4){
                                eventType = xmlPullParser.next();
                                todayWeather.setType_6(xmlPullParser.getText());
                                typeCount++;
                            }else if(xmlPullParser.getName().equals("fengli") && fengliCount == 4){
                                eventType = xmlPullParser.next();
                                todayWeather.setFengli_6(xmlPullParser.getText());
                                fengliCount++;
                            }
                        }
                        break;
                    //判断当前时间是否为标签元素结束事件
                    case  XmlPullParser.END_TAG:
                        break;
                }
                //进入下一个元素并触发相应事件
                eventType = xmlPullParser.next();
            }
        }catch (XmlPullParserException e){
            e.printStackTrace();
        }catch (IOException e){
            e.printStackTrace();
        }
        return todayWeather;
    }
}
