package com.example.daireker.miniweather;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;

import com.example.daireker.app.MyApplication;
import com.example.daireker.bean.City;

import java.util.List;

/**
 * Created by daireker on 2017/9/27.
 */

public class SelectCity extends AppCompatActivity implements View.OnClickListener{

    private ImageView mBackBtn;

    private ClearEditText mClearEditText;
    private ListView mList;
    private List<City> cityList;
    private String citycode;

    //private SimpleAdapter adapter;
    private ArrayAdapter<String> adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.select_city);

        //为选择城市界面的返回(ImageView)设置点击事件
        /*
        mBackBtn = (ImageView) findViewById(R.id.title_back);
        mBackBtn.setOnClickListener(this);
        */

        //调用initView方法
        initViews();

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.title_back:
                //在finish之前传递数据
                Intent i = new Intent();
                //i.putExtra("cityCode", "101160101");
                i.putExtra("cityCode", citycode);
                setResult(RESULT_OK, i);
                finish();
                break;
            default:
                break;
        }
    }

    //城市列表展示
    private void initViews() {
        //为mBackBtn设置监听事件
        mBackBtn = (ImageView) findViewById(R.id.title_back);
        mBackBtn.setOnClickListener(this);

        mList = (ListView) findViewById(R.id.title_list);
        MyApplication myApplication = (MyApplication) getApplication();
        cityList = myApplication.getCityList();



        //将数组作为数据源，填充的是ArrayAdapter
        String[] data = new String[cityList.size()];
        int i = 0;
        for(City city : cityList){
            data[i] = city.getCity();
            data[i] += "  ";
            data[i] += city.getNumber();
            System.out.println(data[i]);
            i++;
            //cityList.add(city);
        }
        adapter = new ArrayAdapter<String>(SelectCity.this, android.R.layout.simple_list_item_1, data);


/*
        //将List作为数据源，填充SimpleAdapter
        //生成动态数组并转载数据
        ArrayList<HashMap<String, String>> mylist = new ArrayList<HashMap<String, String>>();
        for (City city : cityList) {
            HashMap<String, String> map = new HashMap<String, String>();
            map.put("City", city.getCity());
            map.put("Number", city.getNumber());
            mylist.add(map);
        }
        //生成适配器
        //参数分别对应this，mylist，数据来源（item的xml实现），item的xml文件中的两个textview id
        adapter = new SimpleAdapter(this, mylist, R.layout.item, new String[]{"City", "Number"}, new int[]{R.id.list_city_name, R.id.list_city_number});
*/

        //城市搜索
        mClearEditText = (ClearEditText) findViewById(R.id.search_city);
        //根据输入框输入的值的改变来过滤搜索
        mClearEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                //当输入框里面的值为空时，更新为原来的列表，否则为过滤数据列表
                //filterData(charSequence.toString());
                //mList.setAdapter(adapter);
                //adapter.getFilter().filter(charSequence);
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                adapter.getFilter().filter(charSequence);

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        //添加并显示
        mList.setAdapter(adapter);

        // 设置ListView条目点击事件
        mList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                //修改为从适配器中获取被点击的条目
                //City city = cityList.get(position);
                //citycode = city.getNumber();
                String namecode = adapter.getItem(position);
                citycode = namecode.substring(namecode.length()-9,namecode.length());
                Intent i = new Intent();
                i.putExtra("cityCode", citycode);
                setResult(RESULT_OK, i);
                finish();
            }
        });


    }
}
