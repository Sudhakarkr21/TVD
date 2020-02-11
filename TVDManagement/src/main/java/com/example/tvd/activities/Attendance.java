package com.example.tvd.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import com.example.tvd.R;
import com.example.tvd.api.RegisterAPI;
import com.example.tvd.api.RetroClient;
import com.example.tvd.model.AttendanceSummary;
import com.example.tvd.model.LoginDetails;
import com.example.tvd.values.FunctionCall;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static androidx.constraintlayout.widget.Constraints.TAG;
import static com.example.tvd.LoginActivity.device_ID;
import static com.example.tvd.values.constants.EMPLOYEE_DETAILS_SUBMIT_FAILURE;
import static com.example.tvd.values.constants.EMPLOYEE_DETAILS_SUBMIT_SUCCESS;

public class Attendance extends Activity implements View.OnClickListener,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener {
    public static final int MEDIA_TYPE_IMAGE = 1;
    private static final int CAMERA_CAPTURE_IMAGE_REQUEST_CODE = 100;
    ImageView click_image;
    static FunctionCall functionCall;
    String cons_imageextension = "", cons_ImgAdd = "", employee_id = "", employee_name = "", file_encode = "", ImageDecode = "", address = "", remark = "";
    static String pathname = "", pathextension = "", filename = "";
    double lati = 0, longi = 0;
    private static Uri fileUri;
    Button submit;
    EditText empid, remarks, empname;
    ProgressDialog progressdialog;
    Toolbar toolbar;
    static List<LoginDetails> loginList;
    List<AttendanceSummary> summaryList;

    private GoogleApiClient mGoogleApiClient;
    private Location mLocation;
    private LocationManager mLocationManager;
    private LocationRequest mLocationRequest;
    private com.google.android.gms.location.LocationListener listener;
    private long UPDATE_INTERVAL = 2 * 1000;  /* 10 secs */
    private long FASTEST_INTERVAL = 2000; /* 2 sec */
    private LocationManager locationManager;

    private Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case EMPLOYEE_DETAILS_SUBMIT_SUCCESS:
                    progressdialog.dismiss();
                    finish();
                    functionCall.showToast(Attendance.this, summaryList.get(0).getMessage());
                    break;

                case EMPLOYEE_DETAILS_SUBMIT_FAILURE:
                    progressdialog.dismiss();
                    functionCall.showToast(Attendance.this, "Failure");
                    break;
            }
            return false;
        }
    });


    @SuppressLint({"HardwareIds", "SetTextI18n"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attendance);

        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Attendance");
        toolbar.setTitleTextColor(Color.WHITE);
        toolbar.setNavigationIcon(R.drawable.ic_close);
        toolbar.setNavigationOnClickListener(v -> finish());

        initialize_view();
        empid.setText(loginList.get(0).getUSERID());

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        mLocationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        checkLocation(); //check whether address service is enable or not in your  phone
    }

    //-------------------------------------------------------------------------------------------------------------------------------------
    private void initialize_view() {
        Intent intent = getIntent();
        if (intent != null) {
            loginList = (ArrayList<LoginDetails>) intent.getSerializableExtra("loginList");
        }
        summaryList = new ArrayList<>();
        click_image = findViewById(R.id.im_current_read_image);
        empname = findViewById(R.id.et_name);
        empid = findViewById(R.id.edit_emp_id);
        remarks = findViewById(R.id.edit_remarks);
        submit = findViewById(R.id.btn_submit);
        click_image.setOnClickListener(this);
        submit.setOnClickListener(this);
        functionCall = new FunctionCall();
        progressdialog = new ProgressDialog(this);
    }

    //--------------------------------------------------------------------------------------------------------------------------------
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.im_current_read_image:
                captureImage();
                break;
            case R.id.btn_submit:
                submit_details();
                break;
        }
    }

    @TargetApi(24)
    private void captureImage() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        fileUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE, getApplicationContext());
        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
        startActivityForResult(intent, CAMERA_CAPTURE_IMAGE_REQUEST_CODE);
    }

    /**
     * Creating file uri to store image/video
     */
    /*@RequiresApi(api = Build.VERSION_CODES.N)*/
    public Uri getOutputMediaFileUri(int type, Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            return FileProvider.getUriForFile(context, context.getApplicationContext().getPackageName() + ".provider",
                    Objects.requireNonNull(getOutputMediaFile(type)));
        else return Uri.fromFile(getOutputMediaFile(type));
    }

    private static File getOutputMediaFile(int type) {
        File mediaStorageDir = new File(Environment.getExternalStorageDirectory(), functionCall.Appfoldername());
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d("MyCameraApp", "failed to create directory");
                return null;
            }
        }
        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator + "IMG_" + timeStamp + ".jpg");
            pathname = mediaStorageDir.getPath() + File.separator + "IMG_" + timeStamp + ".jpg";
            pathextension = loginList.get(0).getUSERID() + "_" + timeStamp + ".jpg";
            filename = loginList.get(0).getUSERID() + "_" + timeStamp + ".jpg";
        } else {
            return null;
        }

        return mediaFile;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // if the result is capturing Image
        if (requestCode == CAMERA_CAPTURE_IMAGE_REQUEST_CODE) {

            if (resultCode == RESULT_OK) {
                // successfully captured the image
                cons_ImgAdd = pathname;
                employee_id = loginList.get(0).getUSERID();
                cons_imageextension = pathextension;
                Bitmap bitmap = null;
                try {
                    bitmap = functionCall.getImage(cons_ImgAdd, getApplicationContext());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                functionCall.checkimage_and_delete("Compressed", employee_id);
                File destination = functionCall.filestorepath("Compressed", cons_imageextension);
                if (bitmap != null) {
                    saveExternalPrivateStorage(destination, timestampItAndSave(bitmap));
                }
                String destination_file = functionCall.filepath("Compressed") + File.separator + cons_imageextension;
                Bitmap bitmap1 = null;
                try {
                    bitmap1 = functionCall.getImage(destination_file, getApplicationContext());
                    ImageDecode = destination_file;
                } catch (IOException e) {
                    e.printStackTrace();
                }
                click_image.setImageBitmap(bitmap1);
            } else if (resultCode == RESULT_CANCELED) {
                functionCall.showtoast(getApplicationContext(), "User cancelled image capture");
            } else {
                functionCall.showtoast(getApplicationContext(), "Sorry! Failed to capture image");
            }
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void saveExternalPrivateStorage(File folderDir, Bitmap bitmap) {
        if (folderDir.exists()) {
            folderDir.delete();
        }
        try {
            FileOutputStream out = new FileOutputStream(folderDir);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 50, out);
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable("file_uri", fileUri);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        try {
            if ((savedInstanceState != null ? savedInstanceState.getParcelable("file_uri") : null) != null) {
                fileUri = savedInstanceState.getParcelable("file_uri");
                previewCapturedImage();
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    //----------------------------------------------------------------------------------------------------------------------------------------
    private void previewCapturedImage() {
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            final Bitmap bitmap = BitmapFactory.decodeFile(fileUri.getPath(), options);
            click_image.setImageBitmap(rotateImage(bitmap, fileUri.getPath()));
            functionCall.logStatus("Image Size: " + sizeOf(bitmap));
        } catch (NullPointerException e) {
            e.printStackTrace();
        } catch (OutOfMemoryError e) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            // downsizing image as it throws OutOfMemory Exception for larger images
            options.inSampleSize = 8;
            final Bitmap bitmap = BitmapFactory.decodeFile(fileUri.getPath(), options);
            click_image.setImageBitmap(rotateImage(bitmap, fileUri.getPath()));
            functionCall.logStatus("OME Image Size: " + sizeOf(bitmap));
        }
    }

    /****************Below code is for adding Watermark*****************************************************************************************/
    private Bitmap timestampItAndSave(Bitmap bitmap) {
        Bitmap watermarkimage = null;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        Bitmap dest = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas cs = new Canvas(dest);
        Paint tPaint = new Paint();
        tPaint.setTextSize(42);
        tPaint.setColor(Color.RED);
        tPaint.setStyle(Paint.Style.FILL);
        cs.drawBitmap(bitmap, 0f, 0f, null);
        float height = tPaint.measureText("yY");
        cs.drawText(filename, 20f, height + 15f, tPaint);
        try {
            dest.compress(Bitmap.CompressFormat.JPEG, 100, new FileOutputStream(new File(pathname)));
            watermarkimage = BitmapFactory.decodeStream(new FileInputStream(new File(pathname)));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return watermarkimage;
    }

    public static Bitmap rotateImage(Bitmap src, String Imagepath) {
        Bitmap bmp;
        // create new matrix
        Matrix matrix = new Matrix();
        ExifInterface exif = null;
        try {
            exif = new ExifInterface(Imagepath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        int orientation = 0;
        if (exif != null) {
            orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
        }
        new FunctionCall().logStatus("Orientation: " + orientation);
        if (orientation == 1) {
            bmp = src;
        } else if (orientation == 3) {
            matrix.postRotate(180);
            bmp = Bitmap.createBitmap(src, 0, 0, src.getWidth(), src.getHeight(), matrix, true);
        } else if (orientation == 8) {
            matrix.postRotate(270);
            bmp = Bitmap.createBitmap(src, 0, 0, src.getWidth(), src.getHeight(), matrix, true);
        } else {
            matrix.postRotate(90);
            bmp = Bitmap.createBitmap(src, 0, 0, src.getWidth(), src.getHeight(), matrix, true);
        }
        return bmp;
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
    protected int sizeOf(Bitmap data) {
        return data.getByteCount();
    }


    //---------------------------------------------------------------------------------------------------------------------------------------
    private void submit_details() {
        employee_id = empid.getText().toString();
        employee_name = empname.getText().toString();
        remark = remarks.getText().toString();
        file_encode = functionCall.encoded(ImageDecode);
        if (functionCall.isInternetOn(this)) {
            if (!TextUtils.isEmpty(file_encode)) {
                if (!TextUtils.isEmpty(employee_id)) {
                    if (!TextUtils.isEmpty(employee_name)) {
                        if (!TextUtils.isEmpty(remark)) {
                            functionCall.showprogressdialog("Please wait to complete", progressdialog, "Inserting Data");
                            attendanceInsert(device_ID, employee_id, employee_name, filename, longi + "", lati + "", remark, address, file_encode);
                        } else
                            Toast.makeText(getApplicationContext(), "Please Enter Remark", Toast.LENGTH_SHORT).show();
                    } else
                        Toast.makeText(getApplicationContext(), "Please Enter Name", Toast.LENGTH_SHORT).show();
                } else empid.setError("Please Enter Emp id!!");
            } else
                Toast.makeText(getApplicationContext(), "Please Capture Image and proceed", Toast.LENGTH_SHORT).show();
        } else functionCall.showToast(this, "Please Connect to Internet");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        click_image.setImageDrawable(null);
    }

    //Request or post data **********************************************************************************************************
    public void attendanceInsert(String EMINO, String EMPID, String EMPNAME, String PHOTO, String LOG, String LAT, String REMARK, String ADDRESS, String Encodefile) {
        if (TextUtils.isEmpty(EMINO)) {
            EMINO = "0";
        }
        RegisterAPI api = RetroClient.getApiService();



        api.attendanceInsert(EMINO, EMPID, EMPNAME, PHOTO, LOG, LAT, REMARK, ADDRESS, Encodefile).enqueue(new Callback<List<AttendanceSummary>>() {
            @Override
            public void onResponse(@NonNull Call<List<AttendanceSummary>> call, @NonNull Response<List<AttendanceSummary>> response) {
                if (response.isSuccessful()) {
                    assert response.body() != null;
                    summaryList.clear();
                    summaryList = response.body();
                    handler.sendEmptyMessage(EMPLOYEE_DETAILS_SUBMIT_SUCCESS);
                } else handler.sendEmptyMessage(EMPLOYEE_DETAILS_SUBMIT_FAILURE);
            }

            @Override
            public void onFailure(@NonNull Call<List<AttendanceSummary>> call, @NonNull Throwable t) {
                summaryList.get(0).setMessage(t.getMessage());
                handler.sendEmptyMessage(EMPLOYEE_DETAILS_SUBMIT_SUCCESS);
            }
        });
    }

    //--------------------------------------address code-------------------------------------------------------------------------------------------
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        startLocationUpdates();

        mLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        if (mLocation == null) {
            startLocationUpdates();
        }
        if (mLocation != null) {

            // mLatitudeTextView.setText(String.valueOf(mLocation.getLatitude()));
            //mLongitudeTextView.setText(String.valueOf(mLocation.getLongitude()));
        } else {
            Toast.makeText(this, "Location not Detected", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "Connection Suspended");
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.i(TAG, "Connection failed. Error: " + connectionResult.getErrorCode());
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    protected void startLocationUpdates() {
        // Create the address request
        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(UPDATE_INTERVAL)
                .setFastestInterval(FASTEST_INTERVAL);
        // Request address updates
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,
                mLocationRequest, this);
        Log.d("reque", "--->>>>");
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onLocationChanged(Location location) {
        longi = location.getLongitude();
        lati = location.getLatitude();
        try {
            Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            address = (addresses.get(0).getAddressLine(0) + ", " +
                    addresses.get(0).getAddressLine(1) + ", " + addresses.get(0).getAddressLine(2));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void checkLocation() {
        if (!isLocationEnabled())
            functionCall.showToast(this, "Please Enable GPS");
        isLocationEnabled();
    }

    private boolean isLocationEnabled() {
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }
}
