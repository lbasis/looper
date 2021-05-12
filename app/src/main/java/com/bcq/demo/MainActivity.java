package com.bcq.demo;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;

import com.looper.Material;
import com.looper.interfaces.IMaterial;
import com.looper.interfaces.IProcedure;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.start).setOnClickListener(this);
        findViewById(R.id.pause).setOnClickListener(this);
        findViewById(R.id.resume).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.start:
                apply();
                break;
            case R.id.pause:
                Pipeline.getPipe().pause();
                break;
            case R.id.resume:
                Pipeline.getPipe().resume();
                break;
        }
    }

    public void apply() {
        List<IMaterial> os = new ArrayList<>();
        Material material;
        for (int i = 0; i < 10; i++) {
            material = new Material("M" + i);
            os.add(material);
        }
        Pipeline.getPipe().apply(os);
    }
}