package com.example.tvd;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.tvd.api.RegisterAPI;
import com.example.tvd.api.RetroClient;
import com.example.tvd.invoke.FTPAPI;
import com.example.tvd.model.LoginDetails;
import com.example.tvd.values.CustomizedExceptionHandler;
import com.example.tvd.values.FunctionCall;
import com.example.tvd.values.SharedPref;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.credentials.Credential;
import com.google.android.gms.auth.api.credentials.CredentialPickerConfig;
import com.google.android.gms.auth.api.credentials.HintRequest;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.io.Serializable;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.example.tvd.values.constants.APK_FILE_DOWNLOADED;
import static com.example.tvd.values.constants.APK_FILE_NOT_FOUND;
import static com.example.tvd.values.constants.DIR_CRASH_REPORT;
import static com.example.tvd.values.constants.KEY_PHONE;
import static com.example.tvd.values.constants.LOGIN_FAILURE;
import static com.example.tvd.values.constants.LOGIN_SUCCESS;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private static final int DLG_APK_UPDATE_SUCCESS = 1;
    Button login;
    EditText userName, password;
    FunctionCall functionCall;
    ProgressDialog progressDialog;
    List<LoginDetails> loginList;
    String main_curr_version = "";
    FTPAPI ftpapi;
    LinearLayout layout;
    TextView tv_version;
    public static String device_ID = "", sim_sl_no = "",dummy = "aaaassa";
    private static final int RC_HINT = 1000;
    SharedPref sharedPref;
    String servertextpath;
    int upload_text_length;

    @SuppressLint("SetTextI18n")
    private Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message message) {
            switch (message.what) {
                case LOGIN_SUCCESS:
                    progressDialog.dismiss();
                    if (functionCall.compare(main_curr_version, loginList.get(0).getEMP_ATTENDENCE()))
                        show_Dialog(DLG_APK_UPDATE_SUCCESS);
                    else {
                        upload_text_length = new File(functionCall.filepath(DIR_CRASH_REPORT)).listFiles().length;
                        if(upload_text_length > 0){
                            ftpapi.new UploadText(functionCall.filepath(DIR_CRASH_REPORT),
                                    getLastModified(functionCall.filepath(DIR_CRASH_REPORT)),servertextpath).execute();
                        }

                        moveToNext();
                    }

                    break;

                case LOGIN_FAILURE:
                    progressDialog.dismiss();
                    functionCall.showToast(LoginActivity.this, "Login Failure");
                    break;

                case APK_FILE_DOWNLOADED:
                    progressDialog.dismiss();
                    functionCall.updateApp(LoginActivity.this, new File(functionCall.filepath("ApkFolder") +
                            File.separator + "TVDManagement_" + loginList.get(0).getEMP_ATTENDENCE() + ".apk"));
                    break;

                case APK_FILE_NOT_FOUND:
                    progressDialog.dismiss();
                    functionCall.setSnackBar(LoginActivity.this, layout, "APK Not Found");
                    break;
            }
            return false;
        }
    });


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Window w = getWindow(); // in Activity's onCreate() for instance
        w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        setContentView(R.layout.activity_login);
        initialize();
        Thread.setDefaultUncaughtExceptionHandler(new CustomizedExceptionHandler(functionCall.filepath(DIR_CRASH_REPORT),
                sharedPref.getUsername(),device_ID));
        onLocation();
    }

    //--------------------------------------------------------------------------------------------
    @SuppressLint({"SetTextI18n", "HardwareIds"})
    public void initialize() {

        //----------------------------------------------------------------------------------------------------------------------------------
        PackageInfo pInfo = null;
        try {
            pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        if (pInfo != null) {
            main_curr_version = pInfo.versionName;
        }
        try {
            TelephonyManager tm = (TelephonyManager) getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);
            if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            if (tm != null) {
                device_ID = tm.getDeviceId();
                sim_sl_no = tm.getSimSerialNumber();
//                Toast.makeText(this, device_ID + "", Toast.LENGTH_SHORT).show();
            }
        } catch (SecurityException e) {
            device_ID = "0";
        }



        functionCall = new FunctionCall();
        servertextpath = "/Android/crash_report/"+functionCall.getMonthYear()+"/"+functionCall.getonlyDate()+"/";
        sharedPref = new SharedPref(this);
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        loginList = new ArrayList<>();
        progressDialog = new ProgressDialog(this);
        userName = findViewById(R.id.et_user_name);
        userName.setText("");
        password = findViewById(R.id.et_password);
        password.setText("");
        login = findViewById(R.id.btn_login);
        login.setOnClickListener(this);
        ftpapi = new FTPAPI();
        layout = findViewById(R.id.lin_login);
        tv_version = findViewById(R.id.txt_version);
        tv_version.setText("Version :" + main_curr_version);

//        showHint();
//        getData();
//        userName.setText(String.valueOf(f(1) )+String.valueOf(f(2))+String.valueOf(f(3)));
    }

    //--------------------------------------------------------------------------------------------
    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_login) {
            if (functionCall.isInternetOn(this)) {
                userLogin();
            } else snackBar();
        }
    }

    //--------------------------------------------------------------------------------------------
    public void userLogin() {
       /* if (TextUtils.isEmpty(sharedPreferences.getString(KEY_PHONE, ""))) {
            functionCall.showToast(LoginActivity.this, "Please Select Mobile Number to login");
            return;
        }*/
        if (TextUtils.isEmpty(userName.getText())) {
            userName.setError("Please Enter UserName");
            return;
        }

        if (TextUtils.isEmpty(password.getText())) {
            password.setError("Please Enter Password");
            return;
        }

        sharedPref.saveLoginDetails(password.getText().toString(),userName.getText().toString());
        functionCall.showprogressdialog("Please wait to complete...", progressDialog, "Login");
        loginDetails(userName.getText().toString(), password.getText().toString());
    }

    //--------------------------------------------------------------------------------------------
    public void snackBar() {
        final LinearLayout linearLayout = findViewById(R.id.lin_login);
        Snackbar snackbar = Snackbar.make(linearLayout, "Please turn on internet & proceed.", Snackbar.LENGTH_LONG);
        snackbar.show();
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------
    public void loginDetails(String USERNAME, String PASSWORD) {
        RegisterAPI api = RetroClient.getApiService();
        api.getLoginDetails(USERNAME, PASSWORD).enqueue(new Callback<List<LoginDetails>>() {
            @Override
            public void onResponse(@NonNull Call<List<LoginDetails>> call, @NonNull Response<List<LoginDetails>> response) {
                if (response.isSuccessful()) {
                    loginList = response.body();
                    handler.sendEmptyMessage(LOGIN_SUCCESS);
                } else
                    handler.sendEmptyMessage(LOGIN_FAILURE);
            }

            @Override
            public void onFailure(@NonNull Call<List<LoginDetails>> call, @NonNull Throwable t) {
                handler.sendEmptyMessage(LOGIN_FAILURE);
            }
        });
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------
    private void moveToNext() {
//        if (Objects.requireNonNull(sharedPreferences.getString(KEY_PHONE, "")).contains(loginList.get(0).getMOBILE_NO())) {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.putExtra("loginList", (Serializable) loginList);
        startActivity(intent);
        finish();
//        } else functionCall.showToast(LoginActivity.this, "Verification Failed");
    }

    //------------------------------------------------------------------------------------------------------------------------------
    private void show_Dialog(int id) {
        Dialog dialog;
        if (id == DLG_APK_UPDATE_SUCCESS) {
            AlertDialog.Builder appupdate = new AlertDialog.Builder(this);
            appupdate.setTitle("App Updates");
            appupdate.setCancelable(false);
            appupdate.setMessage("Your current version number : " + main_curr_version + "\n" + "\n" +
                    "New version is available : " + loginList.get(0).getEMP_ATTENDENCE() + "\n");
            appupdate.setPositiveButton("UPDATE", (dialog1, which) -> {
                FTPAPI.Download_apk downloadApk = ftpapi.new Download_apk(handler, progressDialog, loginList.get(0).getEMP_ATTENDENCE());
                downloadApk.execute();
            });
            dialog = appupdate.create();
            dialog.show();
        }
    }

    //------------------------------------------------------------------------------------------------------------------------------
    private void onLocation() {
        LocationManager lm = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (!Objects.requireNonNull(lm).isProviderEnabled(LocationManager.GPS_PROVIDER) || !lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Alert");
            builder.setMessage("Please enable GPS");
            builder.setPositiveButton("OK", (dialogInterface, i) -> {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            });
            Dialog alertDialog = builder.create();
            alertDialog.setCanceledOnTouchOutside(false);
            alertDialog.show();
        }
    }

    //----------------------------------------------------------------------------------------------------------------------------------
    private void showHint() {
        GoogleApiClient mCredentialsApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .enableAutoManage(this, this)
                .addApi(Auth.CREDENTIALS_API)
                .build();
        HintRequest hintRequest = new HintRequest.Builder().setHintPickerConfig(new CredentialPickerConfig.Builder()
                .setShowCancelButton(false)
                .build())
                .setPhoneNumberIdentifierSupported(true)
                .build();

        PendingIntent intent = Auth.CredentialsApi.getHintPickerIntent(mCredentialsApiClient, hintRequest);
        try {
            startIntentSenderForResult(intent.getIntentSender(), RC_HINT, null, 0, 0, 0);
        } catch (IntentSender.SendIntentException e) {
            Log.e("debug", "Could not start hint picker Intent", e);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_HINT) {
            if (resultCode == RESULT_OK) {
                Credential cred = data.getParcelableExtra(Credential.EXTRA_KEY);
                SavePreferences(KEY_PHONE, cred.getId());
            } else functionCall.showToast(LoginActivity.this, "Select Mobile Number");
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d("debug", "Connected");
    }

    @Override
    public void onConnectionSuspended(int cause) {
        Log.d("debug", "GoogleApiClient is suspended with cause code: " + cause);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d("debug", "GoogleApiClient failed to connect: " + connectionResult);
    }

    //----------------------------------------------------------------------------------------------------------------------------------
    private void SavePreferences(String key, String value) {
        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("MY_SHARED_PREF", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, value);
        editor.apply();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        SavePreferences(KEY_PHONE, "");
    }

    private void getData() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    Activity#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for Activity#requestPermissions for more details.
                return;
            }
            List<SubscriptionInfo> subscription = SubscriptionManager.from(getApplicationContext()).getActiveSubscriptionInfoList();
            for (int i = 0; i < subscription.size(); i++) {
                SubscriptionInfo info = subscription.get(i);
                Log.d("debug", "number " + info.getNumber());
                Log.d("debug", "network name : " + info.getCarrierName());
                Log.d("debug", "country iso " + info.getCountryIso());
            }
        }
    }

    public static String getLastModified(String directoryFilePath) {
        File directory = new File(directoryFilePath);
        File[] files = directory.listFiles(File::isFile);
        long lastModifiedTime = Long.MIN_VALUE;
        File chosenFile = null;

        if (files != null)
        {
            for (File file : files)
            {
                if (file.lastModified() > lastModifiedTime)
                {
                    chosenFile = file;
                    lastModifiedTime = file.lastModified();
                }
            }
        }

        return Objects.requireNonNull(chosenFile).getName();
    }

}
