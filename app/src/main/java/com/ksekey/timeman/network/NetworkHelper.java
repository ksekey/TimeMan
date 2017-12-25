package com.ksekey.timeman.network;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by kk on 25/12/2017.
 *
 * Используется для запросов к серверу
 */

public class NetworkHelper {

    //singletone
    private static NetworkHelper instance;
    public static NetworkHelper getInstance(){
        if (instance == null) {
            instance = new NetworkHelper();
        }
        return instance;
    }

    private TimeManApi api;

    //ifconfig|grep inet
    private NetworkHelper() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://192.168.0.101:8080/") //Базовая часть адреса
                .addConverterFactory(GsonConverterFactory.create()) //Конвертер, необходимый для преобразования JSON'а в объекты
                .build();
        api = retrofit.create(TimeManApi.class); //Создаем объект, при помощи которого будем выполнять запросы
    }

    public TimeManApi getApi() {
        return api;
    }
}
