package io.github.qihuan92.activitystarter.activity;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import io.github.qihuan92.activitystarter.R;
import io.github.qihuan92.activitystarter.annotation.Builder;
import io.github.qihuan92.activitystarter.annotation.Extra;
import io.github.qihuan92.activitystarter.annotation.ResultField;

@Builder(resultFields = @ResultField(name = "color", type = String.class))
public class ColorSelectActivity extends AppCompatActivity {

    @Extra
    String currColor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_color_select);

        initView();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        ColorSelectActivityBuilder.processNewIntent(this, intent);
    }

    private void initView() {

    }
}