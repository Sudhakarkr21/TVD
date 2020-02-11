package com.example.tvd.api;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetroClient {
    private static final String BASE_URL = "http://bc_service2.hescomtrm.com/TicketingService.asmx/";

    private static Retrofit getRetrofitInstance() {
        return new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    public static RegisterAPI getApiService() {
        return getRetrofitInstance().create(RegisterAPI.class);
    }
}