package com.example.tvd.activities;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.example.tvd.R;
import com.example.tvd.adapter.AttendanceRepAdapter;
import com.example.tvd.api.RegisterAPI;
import com.example.tvd.api.RetroClient;
import com.example.tvd.model.AttendanceSummary;
import com.example.tvd.values.FunctionCall;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.example.tvd.values.constants.REQUEST_RESULT_FAILURE;
import static com.example.tvd.values.constants.REQUEST_RESULT_SUCCESS;


public class AttendanceReport extends AppCompatActivity implements View.OnClickListener {
    ProgressDialog progressdialog;
    Toolbar toolbar;
    List<AttendanceSummary> attendanceSummaryList;
    RecyclerView recyclerView;
    AttendanceRepAdapter adapter;
    FunctionCall functionCall;
    LinearLayout layout;
    EditText edt_date;
    ImageView select_date;
    private int day, month, year;
    private Calendar mcalender;
    String dd, date;

    private Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case REQUEST_RESULT_SUCCESS:
                    progressdialog.dismiss();
                    recyclerView.setVisibility(View.VISIBLE);
                    break;

                case REQUEST_RESULT_FAILURE:
                    progressdialog.dismiss();
                    functionCall.setSnackBar(AttendanceReport.this, layout, "No Data Found");
                    break;
            }
            return false;
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attendance_report);
        initialize();
    }

    //-------------------------------------------------------------------------------------------------------------
    private void initialize() {
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        layout = findViewById(R.id.lin_main);
        attendanceSummaryList = new ArrayList<>();
        functionCall = new FunctionCall();
        recyclerView = findViewById(R.id.rec_att_report);
        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Attendance Report");
        toolbar.setTitleTextColor(Color.WHITE);
        toolbar.setNavigationIcon(R.drawable.ic_close);
        toolbar.setNavigationOnClickListener(v -> finish());
        progressdialog = new ProgressDialog(this);
        edt_date = findViewById(R.id.et_date);
        select_date = findViewById(R.id.img_sel_date);
        select_date.setOnClickListener(this);

    }

    //Request or post data **********************************************************************************************************
    public void attendanceSummary(String DATE) {
        RegisterAPI api = RetroClient.getApiService();
        api.attendanceSummary(DATE).enqueue(new Callback<List<AttendanceSummary>>() {
            @Override
            public void onResponse(@NonNull Call<List<AttendanceSummary>> call, @NonNull Response<List<AttendanceSummary>> response) {
                if (response.isSuccessful()) {
                    assert response.body() != null;
                    attendanceSummaryList = response.body();
                    adapter = new AttendanceRepAdapter(AttendanceReport.this, attendanceSummaryList);
                    recyclerView.setHasFixedSize(true);
                    recyclerView.setLayoutManager(new LinearLayoutManager(AttendanceReport.this));
                    recyclerView.setAdapter(adapter);
                    handler.sendEmptyMessage(REQUEST_RESULT_SUCCESS);
                } else handler.sendEmptyMessage(REQUEST_RESULT_FAILURE);
            }

            @Override
            public void onFailure(@NonNull Call<List<AttendanceSummary>> call, @NonNull Throwable t) {
                handler.sendEmptyMessage(REQUEST_RESULT_FAILURE);
            }
        });
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.img_sel_date) {
            recyclerView.setVisibility(View.GONE);
            DateDialog();
        }
    }

    //------------------------------------------------------------------------------------------------------------------------------
    private void DateDialog() {
        mcalender = Calendar.getInstance();
        day = mcalender.get(Calendar.DAY_OF_MONTH);
        year = mcalender.get(Calendar.YEAR);
        month = mcalender.get(Calendar.MONTH);

        DatePickerDialog.OnDateSetListener listener = (view, year, month, dayOfMonth) -> {
            dd = (year + "-" + (month + 1) + "-" + dayOfMonth);
            date = functionCall.Parse_Date4(dd);
            edt_date.setText(date);
            functionCall.showprogressdialog("Please wait to complete", progressdialog, "Data Loading");
            attendanceSummary(date);
        };
        DatePickerDialog dpdialog = new DatePickerDialog(this, listener, year, month, day);
        mcalender.add(Calendar.MONTH, -1);
        dpdialog.show();
    }
}
