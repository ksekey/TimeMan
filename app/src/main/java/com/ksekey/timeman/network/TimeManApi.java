package com.ksekey.timeman.network;
import com.ksekey.timeman.models.Task;
import com.ksekey.timeman.models.TimeEntry;
import com.ksekey.timeman.models.Token;
import com.ksekey.timeman.models.User;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Query;

/**
 * Created by kk on 25/12/2017.
 */

public interface TimeManApi {
    @FormUrlEncoded
    @POST("/app/rest/v2/oauth/token")
    Call<Token> login(@Header("Authorization") String authorization,
                      @Field(value = "grant_type") String grantType,
                      @Field(value = "username") String username,
                      @Field(value = "password") String password);

    @GET("app/rest/v2/entities/ts$TimeEntry?view=timeEntry-full&sort=-date")
    Call<List<TimeEntry>> listTimeEntry (@Header("Authorization") String authorization);

    @POST("/app/rest/v2/entities/ts$TimeEntry")
    Call<TimeEntry> createItem(@Header("Authorization") String authorization, @Body TimeEntry item);

    @GET("/app/rest/v2/userInfo")
    Call<User> takeIdUser(@Header("Authorization") String authorization);

    @GET("/app/rest/v2/entities/ts$Task?view=task-full")
    Call<List<Task>> takeTaskNameList (@Header("Authorization") String authorization);


}
