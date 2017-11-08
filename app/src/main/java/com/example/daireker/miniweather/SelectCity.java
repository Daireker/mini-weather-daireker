package com.example.daireker.miniweather;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;

import com.example.daireker.app.MyApplication;
import com.example.daireker.bean.City;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by daireker on 2017/9/27.
 */

public class SelectCity extends AppCompatActivity implements View.OnClickListener{

    private ImageView mBackBtn;

    private EditText mClearEditText;

    private ListView mList;

    private List<City> cityList, filterDateList;

    private Myadapter myadapter;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.select_city);

        initViews();
    }

    //实现城市列表的展示
    private void initViews(){
        mBackBtn = (ImageView) findViewById(R.id.title_back);
        mBackBtn.setOnClickListener(this);

        mClearEditText = (EditText) findViewById(R.id.search_city);

        //根据输入框输入值的改变来过滤搜索
        mClearEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int before, int count) {
                //当输入框里面为空，更新为原来的列表，否则为过滤数据列表
                filterData(s.toString());
                mList.setAdapter(myadapter);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        filterDateList = new ArrayList<City>();
        mList = (ListView) findViewById(R.id.title_list);
        MyApplication myApplication = (MyApplication) getApplication();
        cityList = myApplication.getCityList();
        for(City city : cityList){
            filterDateList.add(city);
        }
        myadapter = new Myadapter(SelectCity.this, cityList);
        mList.setAdapter(myadapter);
        mList.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                City city = filterDateList.get(position);
                Intent i = new Intent();
                i.putExtra("cityCode",city.getNumber());
                setResult(RESULT_OK,i);
                finish();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    /*根据输入框中的值来过滤数据并更新ListView*/
    private void filterData(String filterStr){
        filterDateList = new ArrayList<City>();

        Log.d("Filter",filterStr);

        if(TextUtils.isEmpty(filterStr)){
            for(City city : cityList){
                filterDateList.add(city);
            }
        }else {
            filterDateList.clear();
            for (City city : cityList){
                if(city.getCity().indexOf(filterStr.toString()) != -1){
                    filterDateList.add(city);
                }
            }
        }

        //根据a-z进行排序
        //myadapter.updateListView(filterDateList);
    }

    public class Myadapter extends BaseAdapter{
        private List<City> list;
        public Myadapter(Context context, List<City> list){
            this.list = list;
        }

        public int getCount(){
            return list.size();
        }

        public Object getItem(int position){
            return list.get(position);
        }

        public long getItemId(int position){
            return position;
        }

        public View getView(int position, View convertView, ViewGroup parent){
            //ViewHolder holder;
            return convertView;
        }
    }

    public void onClick(View view){
        switch (view.getId()){
            case R.id.title_back:
                Intent i = new Intent();
                i.putExtra("cityCode","101190101");
                setResult(RESULT_OK,i);
                finish();
                break;
            default:
                break;
        }

    }
}
