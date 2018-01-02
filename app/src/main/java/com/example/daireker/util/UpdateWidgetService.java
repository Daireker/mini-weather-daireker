package com.example.daireker.util;

import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.widget.RemoteViews;

import com.example.daireker.MiniWeatherWidget;
import com.example.daireker.bean.TodayWeather;
import com.example.daireker.miniweather.R;

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
import java.util.Calendar;

/**
 * Created by daireker on 2018/1/2.
 */

public class UpdateWidgetService extends Service{

    private String citycode;
    private static final int UPDATE_TODAY_WEATHER = 1;
    private RemoteViews remoteViews;

    //通过消息机制，将解析的天气对象，通过消息发送给主线程，主线程接收到消息数 据后，调用updateTodayWeather函数，更新UI界面上的数据
    private Handler mHandler = new Handler(){
        public void handleMessage(android.os.Message msg){
            Log.d("myWeather","handler启动");
            switch (msg.what){
                case UPDATE_TODAY_WEATHER:
                    updateWidget((TodayWeather)msg.obj);
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        initWidget();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        citycode = intent.getStringExtra("citycode");
        if(citycode != null){
            queryWeatherCode(citycode);
        }
        IntentFilter updateIntent = new IntentFilter();
        updateIntent.addAction("android.intent.action.TIME_TICK");
        registerReceiver(broadcastReceiver, updateIntent);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(broadcastReceiver);
        Intent intent = new Intent(getApplicationContext(), UpdateWidgetService.class);
        getApplication().startService(intent);
        super.onDestroy();
    }

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Calendar calendar = Calendar.getInstance();
            int min = calendar.get(Calendar.MINUTE);
            if(min == 0 || min == 30){
                queryWeatherCode(citycode);
                Log.d("widget","widget已更新！");
            }
        }
    };

    private void initWidget(){
        remoteViews = new RemoteViews(getPackageName(), R.layout.weather_widget);
        remoteViews.setImageViewResource(R.id.widget_climate_image, R.drawable.biz_plugin_weather_qing);
        remoteViews.setTextViewText(R.id.widget_city_text, "N/A");
        remoteViews.setTextViewText(R.id.widget_climate_text, "N/A");
        remoteViews.setTextViewText(R.id.widget_date_text, "N/A");
        remoteViews.setTextViewText(R.id.widget_temperature_text, "N/A");
    }

    //利用TodayWeather对象更新UI中的控件
    void updateWidget(TodayWeather todayWeather){
        Log.d("widgetUI","更新widget启动");
        changeImage(todayWeather);
        remoteViews.setTextViewText(R.id.widget_city_text, todayWeather.getCity());
        remoteViews.setTextViewText(R.id.widget_climate_text, todayWeather.getType());
        remoteViews.setTextViewText(R.id.widget_date_text, todayWeather.getDate());
        remoteViews.setTextViewText(R.id.widget_temperature_text, todayWeather.getLow()+"~"+todayWeather.getHigh());
        ComponentName componentName = new ComponentName(getApplicationContext(), MiniWeatherWidget.class);
        AppWidgetManager.getInstance(getApplicationContext()).updateAppWidget(componentName, remoteViews);
    }

    void changeImage(TodayWeather todayWeather){
        Log.d("widgetImg","更新widget图标");
        String t = todayWeather.getType();

        try{
            if(t.equals("多云")){
                remoteViews.setImageViewResource(R.id.widget_climate_image, R.drawable.biz_plugin_weather_duoyun);
            }
            else if(t.equals("暴雪")){
                remoteViews.setImageViewResource(R.id.widget_climate_image, R.drawable.biz_plugin_weather_baoxue);
            }
            else if(t.equals("暴雨")){
                remoteViews.setImageViewResource(R.id.widget_climate_image, R.drawable.biz_plugin_weather_baoyu);
            }
            else if(t.equals("大暴雨")){
                remoteViews.setImageViewResource(R.id.widget_climate_image, R.drawable.biz_plugin_weather_dabaoyu);
            }
            else if(t.equals("特大暴雨")){
                remoteViews.setImageViewResource(R.id.widget_climate_image, R.drawable.biz_plugin_weather_tedabaoyu);
            }
            else if(t.equals("大雪")){
                remoteViews.setImageViewResource(R.id.widget_climate_image, R.drawable.biz_plugin_weather_daxue);
            }
            else if(t.equals("大雨")){
                remoteViews.setImageViewResource(R.id.widget_climate_image, R.drawable.biz_plugin_weather_dayu);
            }
            else if(t.equals("雷阵雨")){
                remoteViews.setImageViewResource(R.id.widget_climate_image, R.drawable.biz_plugin_weather_leizhenyu);
            }
            else if(t.equals("雷阵雨冰雹")){
                remoteViews.setImageViewResource(R.id.widget_climate_image, R.drawable.biz_plugin_weather_leizhenyubingbao);
            }
            else if(t.equals("沙尘暴")){
                remoteViews.setImageViewResource(R.id.widget_climate_image, R.drawable.biz_plugin_weather_shachenbao);
            }
            else if(t.equals("雾")){
                remoteViews.setImageViewResource(R.id.widget_climate_image, R.drawable.biz_plugin_weather_wu);
            }
            else if(t.equals("小雪")){
                remoteViews.setImageViewResource(R.id.widget_climate_image, R.drawable.biz_plugin_weather_xiaoxue);
            }
            else if(t.equals("小雨")){
                remoteViews.setImageViewResource(R.id.widget_climate_image, R.drawable.biz_plugin_weather_xiaoyu);
            }
            else if(t.equals("阴")){
                remoteViews.setImageViewResource(R.id.widget_climate_image, R.drawable.biz_plugin_weather_yin);
            }
            else if(t.equals("雨夹雪")){
                remoteViews.setImageViewResource(R.id.widget_climate_image, R.drawable.biz_plugin_weather_yujiaxue);
            }
            else if(t.equals("阵雪")){
                remoteViews.setImageViewResource(R.id.widget_climate_image, R.drawable.biz_plugin_weather_zhenxue);
            }
            else if(t.equals("阵雨")){
                remoteViews.setImageViewResource(R.id.widget_climate_image, R.drawable.biz_plugin_weather_zhenyu);
            }
            else if(t.equals("中雪")){
                remoteViews.setImageViewResource(R.id.widget_climate_image, R.drawable.biz_plugin_weather_zhongxue);
            }
            else if(t.equals("中雨")){
                remoteViews.setImageViewResource(R.id.widget_climate_image, R.drawable.biz_plugin_weather_zhongyu);
            }
            else {
                remoteViews.setImageViewResource(R.id.widget_climate_image, R.drawable.biz_plugin_weather_qing);
            }
        }catch(Exception e){
            e.printStackTrace();
        }

    }

    //获取网络数据
    public void queryWeatherCode(String cityCode){
        final String address = "http://wthrcdn.etouch.cn/WeatherApi?citykey=" + cityCode;
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
                    }
                    String responseStr = response.toString();
                    parseXML(responseStr);   //在获取网络数据后，调用解析函数
                    todayWeather = parseXML(responseStr);  //调用parseXML，并返回TodayWeather对象
                    if(todayWeather != null){
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
                            }else if(xmlPullParser.getName().equals("updatetime")){
                                eventType = xmlPullParser.next();
                                todayWeather.setUpdatetime(xmlPullParser.getText());
                            }else if(xmlPullParser.getName().equals("shidu")){
                                eventType = xmlPullParser.next();
                                todayWeather.setShidu(xmlPullParser.getText());
                            }else if(xmlPullParser.getName().equals("wendu")){
                                eventType = xmlPullParser.next();
                                todayWeather.setWendu(xmlPullParser.getText());
                            }else if(xmlPullParser.getName().equals("pm25")){
                                eventType = xmlPullParser.next();
                                todayWeather.setPm25(xmlPullParser.getText());
                            }else if(xmlPullParser.getName().equals("quality")){
                                eventType = xmlPullParser.next();
                                todayWeather.setQuality(xmlPullParser.getText());
                            }else if(xmlPullParser.getName().equals("fengxiang") && fengxiangCount ==0){
                                eventType = xmlPullParser.next();
                                todayWeather.setFengxiang(xmlPullParser.getText());
                                fengxiangCount++;
                            }else if(xmlPullParser.getName().equals("fengli") && fengliCount == 0){
                                eventType = xmlPullParser.next();
                                todayWeather.setFengli(xmlPullParser.getText());
                                fengliCount++;
                            }else if(xmlPullParser.getName().equals("date") && dateCount == 0){
                                eventType = xmlPullParser.next();
                                todayWeather.setDate(xmlPullParser.getText());
                                dateCount++;
                            }else if(xmlPullParser.getName().equals("high") && highCount == 0){
                                eventType = xmlPullParser.next();
                                todayWeather.setHigh(xmlPullParser.getText().substring(2).trim());
                                highCount++;
                            }else if(xmlPullParser.getName().equals("low") && lowCount == 0){
                                eventType = xmlPullParser.next();
                                todayWeather.setLow(xmlPullParser.getText().substring(2).trim());
                                lowCount++;
                            }else if(xmlPullParser.getName().equals("type") && typeCount == 0){
                                eventType = xmlPullParser.next();
                                todayWeather.setType(xmlPullParser.getText());
                                typeCount++;
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
