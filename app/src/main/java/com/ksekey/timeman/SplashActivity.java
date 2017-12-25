package com.ksekey.timeman;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

/**
 * Если авторизован, то открывает записи, иначе возвращает к экрану авторизации
 */

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TokenHelper tokenHelper = new TokenHelper(this);
        if (tokenHelper.loadToken() == null) {
            //запускаем экран авторизации
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);

            //закрываем SplashActivity
            finish();
        }
    }
}
