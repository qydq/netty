package com.an.app.netty;

import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.an.base.view.SuperActivity;

import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.ViewInject;

/**
 * Created by qydda on 2016/12/26.
 */

@ContentView(R.layout.sst_actiivty_tips)
public class TipsActivity extends SuperActivity {
    @ViewInject(R.id.anLlBack)
    private LinearLayout anLlBack;
    @ViewInject(R.id.anTvTitle)
    private TextView anTvTitle;
    @ViewInject(R.id.anLlRight)
    private LinearLayout anLlRight;

    @Override
    public void initView() {
        anLlRight.setVisibility(View.INVISIBLE);
        anTvTitle.setText("tips");
        anLlBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }
}
