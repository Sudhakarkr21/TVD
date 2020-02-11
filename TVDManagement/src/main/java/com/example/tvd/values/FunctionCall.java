package com.example.tvd.values;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import com.example.tvd.R;
import com.google.android.material.snackbar.Snackbar;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;

public class FunctionCall {
    public void logStatus(String message) {
        Log.d("debug", message);
    }

    public void showToast(Context context, String Message) {
        Toast toast = Toast.makeText(context, Message, Toast.LENGTH_SHORT);
        toast.show();
    }

    //-----------------------------------------method for show progress dialog----------------------------------------------------//
    public void showprogressdialog(String Message, ProgressDialog dialog, String Title) {
        dialog.setTitle(Title);
        dialog.setMessage(Message);
        dialog.setCancelable(false);
        dialog.show();
    }


    public File filestorepath(String value, String file) {
        File dir = new File(android.os.Environment.getExternalStorageDirectory(), Appfoldername()
                + File.separator + value);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return new File(dir, File.separator + file);
    }

    public String Appfoldername() {
        return "Transvision";
    }

    public Bitmap getImage(String path, Context con) throws IOException {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);
        int srcWidth = options.outWidth;
        int srcHeight = options.outHeight;
        int[] newWH = new int[2];
        newWH[0] = srcWidth / 2;
        newWH[1] = (newWH[0] * srcHeight) / srcWidth;

        int inSampleSize = 1;
        while (srcWidth / 2 >= newWH[0]) {
            srcWidth /= 2;
            srcHeight /= 2;
            inSampleSize *= 2;
        }

        options.inJustDecodeBounds = false;
        options.inDither = false;
        options.inSampleSize = inSampleSize;
        options.inScaled = false;
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        Bitmap sampledSrcBitmap = BitmapFactory.decodeFile(path, options);
        ExifInterface exif = new ExifInterface(path);
        String s = exif.getAttribute(ExifInterface.TAG_ORIENTATION);
        System.out.println("Orientation>>>>>>>>>>>>>>>>>>>>" + s);
        Matrix matrix = new Matrix();
        float rotation = rotationForImage(con, Uri.fromFile(new File(path)));
        if (rotation != 0f) {
            matrix.preRotate(rotation);
        }
        return Bitmap.createBitmap(
                sampledSrcBitmap, 0, 0, sampledSrcBitmap.getWidth(), sampledSrcBitmap.getHeight(), matrix, true);
    }


    private float rotationForImage(Context context, Uri uri) {
        if (Objects.requireNonNull(uri.getScheme()).equals("content")) {
            String[] projection = {MediaStore.Images.ImageColumns.ORIENTATION};
            @SuppressLint("Recycle") Cursor c = context.getContentResolver().query(
                    uri, projection, null, null, null);
            if (c != null && c.moveToFirst()) {
                return c.getInt(0);
            }
        } else if (uri.getScheme().equals("file")) {
            try {
                ExifInterface exif = new ExifInterface(uri.getPath());
                return (int) exifOrientationToDegrees(
                        exif.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                                ExifInterface.ORIENTATION_NORMAL));
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        return 0f;
    }

    private static float exifOrientationToDegrees(int exifOrientation) {
        if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_90) {
            return 90;
        } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_180) {
            return 180;
        } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_270) {
            return 270;
        }
        return 0;
    }
    public String filepath(String value) {
        File dir = new File(android.os.Environment.getExternalStorageDirectory(), Appfoldername()
                + File.separator + value);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return dir.toString();
    }
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public String encoded(String filepath) {
        File originalFile = new File(filepath);
        String encodedBase64;
        try {
            byte[] bytes = FileUtils.readFileToByteArray(originalFile);
            encodedBase64 = Base64.encodeToString(bytes, Base64.DEFAULT);
        } catch (IOException | OutOfMemoryError e) {
            e.printStackTrace();
            byte[] bytes = new byte[0];
            try {
                bytes = FileUtils.readFileToByteArray(originalFile);
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            encodedBase64 = Base64.encodeToString(bytes, Base64.DEFAULT);
        }
        return encodedBase64;
    }
    //--------------------------------------CHECKING INTERNET IS ON OR NOT-------------------------------------------------------//
    public final boolean isInternetOn(Activity activity) {
        ConnectivityManager connect = (ConnectivityManager) activity.getSystemService(activity.getBaseContext().CONNECTIVITY_SERVICE);
        if (connect.getNetworkInfo(0).getState() == NetworkInfo.State.CONNECTED ||
                connect.getNetworkInfo(0).getState() == NetworkInfo.State.CONNECTING ||
                connect.getNetworkInfo(1).getState() == NetworkInfo.State.CONNECTING ||
                connect.getNetworkInfo(1).getState() == NetworkInfo.State.CONNECTED) {
            return true;
        } else if (connect.getNetworkInfo(0).getState() == NetworkInfo.State.DISCONNECTED ||
                connect.getNetworkInfo(1).getState() == NetworkInfo.State.DISCONNECTED) {
            return false;
        }
        return false;
    }

    public void checkimage_and_delete(String foldername, String ConsumerID) {
        String folderpath = filepath(foldername);
        int considlength = ConsumerID.length();
        File imagefiledir = new File(folderpath);
        File[] files = imagefiledir.listFiles();
        for (File file1 : files) {
            String filepath = file1.getName();
            String findimage = filepath.substring(0, considlength);
            if (findimage.equals(ConsumerID)) {
                File file = new File(folderpath + File.separator + filepath);
                if (file.isFile()) {
                    file.delete();
                }
            }
        }
    }
    public void showtoast(Context context, String Message) {
        Toast.makeText(context, Message, Toast.LENGTH_SHORT).show();
    }

    public boolean isDeviceSupportCamera(Context context) {
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            // this device has a camera
            return true;
        } else {
            // no camera on this device
            return false;
        }
    }

    //***********************************compare version************************************************************************
    public boolean compare(String v1, String v2) {
        String s1 = normalisedVersion(v1);
        String s2 = normalisedVersion(v2);
        int cmp = s1.compareTo(s2);
        String cmpStr = cmp < 0 ? "<" : cmp > 0 ? ">" : "==";
        return cmpStr.equals("<");
    }

    //**********************************************************************************************************
    public String normalisedVersion(String version) {
        return normalisedVersion(version, ".", 4);
    }

    private String normalisedVersion(String version, String sep, int maxWidth) {
        String[] split = Pattern.compile(sep, Pattern.LITERAL).split(version);
        StringBuilder sb = new StringBuilder();
        for (String s : split) {
            sb.append(String.format("%" + maxWidth + 's', s));
        }
        return sb.toString();
    }

    //---------------------------------------------------------------------------------------------------------------------------------
    public void setSnackBar(Context context, View root, String snackTitle) {
        Snackbar snackbar = Snackbar.make(root, snackTitle, Snackbar.LENGTH_LONG);
        snackbar.show();
        View view = snackbar.getView();
        view.setBackgroundColor(context.getResources().getColor(R.color.red));
        TextView txtv = view.findViewById(com.google.android.material.R.id.snackbar_text);
        txtv.setTextColor(context.getResources().getColor(R.color.white));
        txtv.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        txtv.setGravity(Gravity.CENTER);
    }

    //****************************************update App*****************************************************************
    public void updateApp(Context context, File Apkfile) {
        Uri path;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            path = FileProvider.getUriForFile(context, context.getApplicationContext().getPackageName()
                    + ".provider", Apkfile);
        } else path = Uri.fromFile(Apkfile);
        Intent objIntent = new Intent(Intent.ACTION_VIEW);
        objIntent.setDataAndType(path, "application/vnd.android.package-archive");
        objIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        objIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        context.startActivity(objIntent);
    }

    public String decimalroundoff(double value) {
        BigDecimal a = new BigDecimal(value);
        BigDecimal roundOff = a.setScale(2, BigDecimal.ROUND_HALF_EVEN);
        return "" + roundOff;
    }

    //-----------------------------------------------------------------------------------------------------------------------------------
    public String getCurrentDate(){
        Date c = Calendar.getInstance().getTime();
        @SuppressLint("SimpleDateFormat") SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        return df.format(c);
    }

    //-----------------------------------------------------------------------------------------------------------------------------------
    public String Parse_Date4(String time) {
        String input = "yyyy-MM-d";
        String output = "yyyy-MM-dd";
        @SuppressLint("SimpleDateFormat") SimpleDateFormat inputFormat = new SimpleDateFormat(input);
        @SuppressLint("SimpleDateFormat") SimpleDateFormat outputFormat = new SimpleDateFormat(output);

        Date date = null;
        String str = null;
        try {
            date = inputFormat.parse(time);
            str = outputFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return str;
    }

    public String getMonthYear() {
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int mnth2 = month + 1;
        String present_date1;
        if (String.valueOf(mnth2).length() == 1)
            present_date1 = "0"+mnth2 + ""+year;
        else present_date1 = mnth2 + ""+year;
        Date date = null;
        try {
            date = new SimpleDateFormat("MMyyyy", Locale.US).parse(present_date1);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        SimpleDateFormat sdf = new SimpleDateFormat("MMyyyy", Locale.US);
        c.setTime(date);
        return sdf.format(c.getTime());
    }

    public String getonlyDate() {
        return dateSet().substring(0, 2);
    }

    public String dateSet() {
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);
        int mnth2 = month + 1;
        String present_date1 = day + "/" + mnth2 + "/" + "" + year;
        Date date = null;
        try {
            date = new SimpleDateFormat("dd/MM/yyyy", Locale.US).parse(present_date1);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.US);
        c.setTime(date);
        return sdf.format(c.getTime());
    }
}

