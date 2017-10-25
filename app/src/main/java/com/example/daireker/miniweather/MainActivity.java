package com.example.daireker.miniweather;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.daireker.bean.TodayWeather;
import com.example.daireker.util.NetUtil;

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

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private static final int UPDATE_TODAY_WEATHER = 1;

    private ImageView mUpdateBtn;

    private ImageView mCitySelect;

    private TextView cityTv, timeTv, humidityTv, weekTv, pmDataTv, pmQualityTv,
            temperatureTv, climateTv ,windTv, city_name_Tv, nowTemperatureTv;

    private ImageView weatherImg, pmImg;

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
        Log.d("myWeather","创建成功");

        mUpdateBtn = (ImageView) findViewById(R.id.title_update_btn);
        mUpdateBtn.setOnClickListener(this);

        mCitySelect = (ImageView) findViewById(R.id.title_city_manager);
        mCitySelect.setOnClickListener(this);

        //测试是否连接到网络
        if(NetUtil.getNetworkState(this) != NetUtil.NETWORN_NONE){
            Log.d("myWeather","网络OK");
            Toast.makeText(MainActivity.this,"网络OK！",Toast.LENGTH_LONG).show();
        }else{
            Log.d("myWeather","网络挂了");
            Toast.makeText(MainActivity.this,"网络挂了！",Toast.LENGTH_LONG).show();
        }
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
    }

    public void onClick(View view){
        switch (view.getId()){
            case R.id.title_city_manager:
                Intent i = new Intent(this,SelectCity.class);
                //startActivity(intent);
                startActivityForResult(i,1);
                break;
            case R.id.title_update_btn:
                SharedPreferences sharedPreferences = getSharedPreferences("config",MODE_PRIVATE);
                String cityCode = sharedPreferences.getString("main_city_code","101010100");
                Log.d("myWeather",cityCode);

                //测试是否连接到网络
                if(NetUtil.getNetworkState(this) != NetUtil.NETWORN_NONE){
                    Log.d("myWeather","网络OK");
                    Toast.makeText(MainActivity.this,"网络OK！",Toast.LENGTH_LONG).show();
                    queryWeatherCode(cityCode);
                }else{
                    Log.d("myWeather","网络挂了");
                    Toast.makeText(MainActivity.this,"网络挂了！",Toast.LENGTH_LONG).show();
                }
                break;
            default:
                break;
        }

    }

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

        changeImage(todayWeather);

        Toast.makeText(MainActivity.this,"更新成功!",Toast.LENGTH_SHORT).show();
    }

    void changeImage(TodayWeather todayWeather){
        Log.d("myWeather","更新pm weather图标");
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

        String t = todayWeather.getType();

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
        }catch(Exception e){
            e.printStackTrace();
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 1 && resultCode == RESULT_OK){
            String newCityCode = data.getStringExtra("cityCode");
            Log.d("myWeather","选择的城市代码为" + newCityCode);

            //测试是否连接到网络
            if(NetUtil.getNetworkState(this) != NetUtil.NETWORN_NONE){
                Log.d("myWeather","网络OK");
                Toast.makeText(MainActivity.this,"网络OK！",Toast.LENGTH_LONG).show();
                queryWeatherCode(newCityCode);
            }else{
                Log.d("myWeather","网络挂了");
                Toast.makeText(MainActivity.this,"网络挂了！",Toast.LENGTH_LONG).show();
            }
        }
    }

    private void queryWeatherCode(String cityCode){
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
                        Log.d("myWeather",str);
                    }
                    String responseStr = response.toString();
                    Log.d("myWeather",responseStr);
                    parseXML(responseStr);

                    todayWeather = parseXML(responseStr);
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
