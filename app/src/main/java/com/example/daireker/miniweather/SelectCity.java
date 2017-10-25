package com.example.daireker.miniweather;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;

/**
 * Created by daireker on 2017/9/27.
 */

public class SelectCity extends AppCompatActivity implements View.OnClickListener{

    private ImageView mBackBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.select_city);

        mBackBtn = (ImageView) findViewById(R.id.title_back);
        mBackBtn.setOnClickListener(this);

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
