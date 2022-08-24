package io.github.qihuan92.activitystarter.activity;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import io.github.qihuan92.activitystarter.R;
import io.github.qihuan92.activitystarter.adapter.ColorAdapter;
import io.github.qihuan92.activitystarter.annotation.Builder;
import io.github.qihuan92.activitystarter.annotation.Extra;
import io.github.qihuan92.activitystarter.annotation.ResultField;
import io.github.qihuan92.activitystarter.entity.ColorItem;

@Builder(resultFields = @ResultField(name = "color", type = String.class))
public class ColorSelectActivity extends AppCompatActivity {

    @Extra
    String currentColor;

    @Extra(required = false)
    String title;

    private Toolbar toolbar;
    private RecyclerView rvList;
    private ColorAdapter colorAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_color_select);

        bindView();
        initView();
        initData();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        ColorSelectActivityBuilder.processNewIntent(this, intent);
    }

    private void bindView() {
        toolbar = findViewById(R.id.toolbar);
        rvList = findViewById(R.id.rv_list);
    }

    private void initView() {
        toolbar.setNavigationOnClickListener(view -> onBackPressed());
        colorAdapter = new ColorAdapter(colorItem -> ColorSelectActivityBuilder.finish(this, colorItem.getColor()));
        rvList.setAdapter(colorAdapter);
    }

    private void initData() {
        List<ColorItem> colorItemList = new ArrayList<>();
        colorItemList.add(new ColorItem("#FFBB86FC"));
        colorItemList.add(new ColorItem("#FF6200EE"));
        colorItemList.add(new ColorItem("#FF3700B3"));
        colorItemList.add(new ColorItem("#FF03DAC5"));
        colorItemList.add(new ColorItem("#FF018786"));

        for (ColorItem colorItem : colorItemList) {
            colorItem.setSelected(colorItem.getColor().equals(currentColor));
        }

        colorAdapter.submitList(colorItemList);
    }
}