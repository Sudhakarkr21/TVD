package com.example.tvd.values;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPref {
    private Context context;
    public SharedPref(Context context) {
        this.context = context;
    }

    public void saveLoginDetails(String password,String username) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("LoginDetails", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("Password", password);
        editor.putString("UserName",username);
        editor.apply();
    }

    public String getUsername(){
        SharedPreferences sharedPreferences = context.getSharedPreferences("LoginDetails", Context.MODE_PRIVATE);
        return sharedPreferences.getString("UserName", "NA");
    }
}
