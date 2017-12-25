package com.ksekey.timeman;

import android.content.Context;
import android.content.SharedPreferences;

import com.ksekey.timeman.models.Token;

/**
 * Created by kk on 25/12/2017.
 */

/**
 * класс для загрузки и хранения токена
 */
public class LoginHelper {

    private Context context;

    public LoginHelper(Context context) {
        this.context = context;
    }

    public void save(Token token) {
        SharedPreferences sharedPref = context.getSharedPreferences("login",Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("key", token.getAccess_token());
        editor.commit();
    }

    /**
     * Если токена нет, вернуть null
     * @return
     */
    public Token loadToken(){
        SharedPreferences sharedPref = context.getSharedPreferences("login",Context.MODE_PRIVATE);
        String tokenString = sharedPref.getString("key", null);
        if (tokenString == null) {
            return null;
        }
        Token token = new Token();
        token.setAccess_token(tokenString);
        return token;
    }
}
