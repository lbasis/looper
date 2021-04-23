package com.bcq.demo;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;

import com.looper.Material;
import com.looper.PipelineQueue;
import com.looper.interfaces.IMaterial;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.text).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                testApply();
            }
        });
    }

    public void testApply() {
        List<IMaterial> os = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            os.add(new Material("Queue_" + i,2));
        }
        PipelineQueue.getQueue().apply(os);
    }
}