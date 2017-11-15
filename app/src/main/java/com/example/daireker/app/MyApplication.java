package com.example.daireker.app;

import android.app.Application;
import android.os.Environment;
import android.util.Log;

import com.example.daireker.bean.City;
import com.example.daireker.db.CityDB;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by daireker on 2017/9/28.
 */

public class MyApplication extends Application{
    private static final String TAG = "MyAPP";

    private static MyApplication myApplication;
    private CityDB myCityDB;
    private List<City> myCityList;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG,"MyApplication->OnCreate");

        myApplication = this;
        myCityDB = openCityDB();   //打开数据库
        initCityList();
    }

    public static MyApplication getInstance(){
        return myApplication;
    }

    //初始化城市信息列表
    private void initCityList(){
        myCityList = new ArrayList<City>();
        new Thread(new Runnable() {
            @Override
            public void run() {
                prepareCityList();
            }
        }).start();
    }

    //初始化城市信息列表
    private boolean prepareCityList(){
        myCityList = myCityDB.getAllCity();
        int i = 0;
        for(City city : myCityList){
            i++;
            String cityName = city.getCity();
            String cityCode = city.getNumber();
            Log.d(TAG,cityCode + ":" + cityName);
        }
        Log.d(TAG,"i="+i);
        return true;
    }

    public List<City> getCityList(){
        return myCityList;
    }

    //创建打开数据库的方法
    private CityDB openCityDB(){
        String path = "/data"
                + Environment.getDataDirectory().getAbsolutePath()
                + File.separator + getPackageName()
                + File.separator + "databases1"
                + File.separator
                + CityDB.CITY_DB_NAME;
        File db = new File(path);
        Log.d(TAG,path);
        if (!db.exists()){
            String pathfolder = "/data"
                    + Environment.getDataDirectory().getAbsolutePath()
                    + File.separator + getPackageName()
                    + File.separator + "databases1"
                    + File.separator;
            File dirFirstFolder = new File(pathfolder);
            if(!dirFirstFolder.exists()){
                dirFirstFolder.mkdirs();
                Log.i("MyAPP","mkdirs");
            }
            Log.i("MyAPP","db is not exists");
            try {
                InputStream inputStream = getAssets().open("city.db");
                FileOutputStream fileOutputStream = new FileOutputStream(db);
                int len = -1;
                byte[] buffer = new byte[1024];
                while ((len = inputStream.read(buffer)) != -1){
                    fileOutputStream.write(buffer,0,len);
                    fileOutputStream.flush();
                }
                fileOutputStream.close();
                inputStream.close();
            }catch (IOException e){
                e.printStackTrace();
                System.exit(0);
            }
        }
        return new CityDB(this, path);
    }
}
