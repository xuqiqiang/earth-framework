/*
 * Copyright (C) 2019 xuqiqiang. All rights reserved.
 * Earth Framework
 */
package com.snailstudio2010.earthframework.demo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.snailstudio2010.libutils.ScreenUtils;
import com.snailstudio2010.libutils.StatusBarUtils;

/**
 * Created by xuqiqiang on 2019/08/12.
 */
public class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ScreenUtils.initialize(this);

        StatusBarUtils.setRootViewFitsSystemWindows(this, false);
        StatusBarUtils.setTranslucentStatus(this);
        StatusBarUtils.setStatusBarDarkTheme(this, false);
    }
}
