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
import android.widget.Toast;

import com.example.daireker.app.MyApplication;
import com.example.daireker.bean.City;
import com.example.daireker.util.MyAdapter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by daireker on 2017/9/27.
 */

public class SelectCity extends AppCompatActivity implements View.OnClickListener{

    private ImageView mBackBtn;

    private List<City> cityList, filterDataList;

    private ListView mList;

    private EditText mSearchCity;

    private MyAdapter myAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.select_city);

        initViews();
    }

    //实现城市列表的展示
    private void initViews() {
        mBackBtn = (ImageView) findViewById(R.id.title_back);
        mBackBtn.setOnClickListener(this);

        mSearchCity = (EditText) findViewById(R.id.search_city);
        mSearchCity.addTextChangedListener(mTextWatcher);

        mList = (ListView) findViewById(R.id.title_list);

        MyApplication myApplication = (MyApplication) getApplication();
        cityList = myApplication.getCityList();
        filterDataList = new ArrayList<City>();
        for (City city : cityList) {
            filterDataList.add(city);
        }
        myAdapter = new MyAdapter(SelectCity.this,cityList);
        mList.setAdapter(myAdapter);
        mList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                City city = filterDataList.get(position);
                Intent i = new Intent();
                i.putExtra("cityCode",city.getNumber());
                setResult(RESULT_OK,i);
                finish();
            }
        });
    }

    //根据输入框中的值来过滤搜索
    TextWatcher mTextWatcher = new TextWatcher() {
        private CharSequence temp;
        private int editStart;
        private int editEnd;
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            temp = charSequence;
            Log.d("myapp","beforeTextChanged:"+temp);
        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            Log.d("myapp","onTextChanged:"+charSequence);
            //当输入框里面的值为空，更新为原来的列表，否则为过滤数据列表
            filterData(charSequence.toString());
            mList.setAdapter(myAdapter);
        }

        @Override
        public void afterTextChanged(Editable editable) {
            editStart = mSearchCity.getSelectionStart();
            editEnd = mSearchCity.getSelectionEnd();
            if(temp.length()>10){
                Toast.makeText(SelectCity.this,"你输入的字数已经超过了限制!",Toast.LENGTH_SHORT).show();
                editable.delete(editStart-1,editEnd);
                int tempSelection = editStart;
                mSearchCity.setText(editable);
                mSearchCity.setSelection(tempSelection);
            }
            Log.d("myapp","afterTextChanged:");
        }
    };

    //根据输入框中的值来过滤数据并更新ListView
    private void filterData(String filterStr){
        filterDataList = new ArrayList<City>();

        if(TextUtils.isEmpty(filterStr)){
            for (City city : cityList){
                filterDataList.add(city);
            }
        }else {
            filterDataList.clear();
            for(City city : cityList){
                if (city.getCity().indexOf(filterStr.toString()) != -1){
                    filterDataList.add(city);
                }
            }
        }
        //根据a-z进行排序
        //myAdapter.updateListView(filterDataList);
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
