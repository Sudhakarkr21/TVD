package com.example.tvd.api;

import com.example.tvd.model.AttendanceSummary;
import com.example.tvd.model.LoginDetails;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;


public interface RegisterAPI {
    @POST("Retrofit_loginDetails")//2
    @FormUrlEncoded
    Call<List<LoginDetails>> getLoginDetails(@Field("username") String USERNAME, @Field("password") String PASSWORD);

    @POST("Spot_Attendance_Details")//8
    @FormUrlEncoded
    Call<List<AttendanceSummary>> attendanceInsert(@Field("EMINO") String EMINO, @Field("EMPID") String EMPID, @Field("EMPNAME") String EMPNAME, @Field("PHOTO") String PHOTO,
                                                   @Field("LOG") String LOG, @Field("LAT") String LAT, @Field("REMARK") String REMARK,
                                                   @Field("ADDRESS") String ADDRESS, @Field("Encodefile") String Encodefile);

    @POST("AttendanceSummary")//13
    @FormUrlEncoded
    Call<List<AttendanceSummary>> attendanceSummary(@Field("DATETIME") String DATETIME);

}