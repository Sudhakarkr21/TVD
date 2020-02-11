package com.example.tvd;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.tvd.activities.Attendance;
import com.example.tvd.activities.AttendanceReport;
import com.example.tvd.model.LoginDetails;
import com.example.tvd.values.FunctionCall;
import com.google.android.material.snackbar.Snackbar;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    Button attendance, att_report;
    FunctionCall functionCall;
    List<LoginDetails> loginList;
    String main_curr_version = null;
    Toolbar toolbar;
    private boolean isFirstBackPressed = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initialize();
    }

    //------------------------------------------------------------------------------------------------------------------------------
    private void initialize() {
        toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(getResources().getDrawable(R.drawable.ic_person));
        toolbar.setTitle(getResources().getString(R.string.app_name));
        toolbar.setTitleTextColor(getResources().getColor(R.color.white));
        setSupportActionBar(toolbar);
        Intent intent = getIntent();
        if (intent != null) {
            loginList = (ArrayList<LoginDetails>) intent.getSerializableExtra("loginList");
        }
        attendance = findViewById(R.id.btn_attendance);
        attendance.setOnClickListener(this);
        att_report = findViewById(R.id.btn_att_report);
        att_report.setOnClickListener(this);
        functionCall = new FunctionCall();

        //*****************************set app version to drawer******************************************************************************
        PackageInfo pInfo = null;
        try {
            pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        if (pInfo != null) {
            main_curr_version = pInfo.versionName;
        }
    }

    //----------------------------------------------------------------------------------------------------------------------------------
    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.btn_attendance) {
            if (functionCall.isInternetOn(this)) {
                Intent intent = new Intent(MainActivity.this, Attendance.class);
                intent.putExtra("loginList", (Serializable) loginList);
                startActivity(intent);
            } else snackBar();
        }
        if (view.getId() == R.id.btn_att_report) {
            if (functionCall.isInternetOn(this)) {
                Intent intent = new Intent(MainActivity.this, AttendanceReport.class);
                intent.putExtra("loginList", (Serializable) loginList);
                startActivity(intent);
            } else snackBar();
        }
    }

    //--------------------------------------------------------------------------------------------------------------------------
    public void snackBar() {
        final LinearLayout linearLayout = findViewById(R.id.lin_main);
        Snackbar snackbar = Snackbar.make(linearLayout, "Please turn on internet & proceed.", Snackbar.LENGTH_LONG);
        snackbar.show();
    }

    //--------------------------------------------------------------------------------------------------------------------------------------------
    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() != 0) {
            super.onBackPressed();
        } else {
            if (isFirstBackPressed) {
                super.onBackPressed();
            } else {
                isFirstBackPressed = true;
                Toast.makeText(this, "Press again to close app", Toast.LENGTH_SHORT).show();
                new Handler().postDelayed(() -> isFirstBackPressed = false, 2000);
            }
        }
    }
}
