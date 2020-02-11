package com.example.tvd.invoke;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Handler;
import com.example.tvd.values.FunctionCall;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPConnectionClosedException;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static com.example.tvd.values.constants.APK_FILE_DOWNLOADED;
import static com.example.tvd.values.constants.APK_FILE_NOT_FOUND;
import static com.example.tvd.values.constants.FILE_UPLOAD_SUCCESS;
import static com.example.tvd.values.constants.FTP_HOST;
import static com.example.tvd.values.constants.FTP_PASS;
import static com.example.tvd.values.constants.FTP_PORT;
import static com.example.tvd.values.constants.FTP_USER;

public class FTPAPI {

    private FunctionCall fcall = new FunctionCall();
    @SuppressLint("StaticFieldLeak")
    public class Download_apk  extends AsyncTask<String, Integer, String> {
        boolean downloadapk=false, file_found=false;
        Handler handler;
        ProgressDialog progressDialog;
        String mobilepath = fcall.filepath("ApkFolder") + File.separator;
        String update_version;

        public Download_apk(Handler handler, ProgressDialog progressDialog, String update_version) {
            this.handler = handler;
            this.progressDialog = progressDialog;
            this.update_version = update_version;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            fcall.showprogressdialog("Please wait to complete...", progressDialog, "Downloading APK");
        }

        @Override
        protected String doInBackground(String... params) {
            int count;
            long read = 0;

            fcall.logStatus("Main_Apk 1");
            FTPClient ftp_1 = new FTPClient();
            fcall.logStatus("Main_Apk 2");
            try {
                fcall.logStatus("Main_Apk 3");
                ftp_1.connect(FTP_HOST, FTP_PORT);
                fcall.logStatus("Main_Apk 4");
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                fcall.logStatus("Main_Apk 5");
                ftp_1.login(FTP_USER, FTP_PASS);
                downloadapk = ftp_1.login(FTP_USER, FTP_PASS);
                fcall.logStatus("Main_Apk 6");
            } catch (FTPConnectionClosedException e) {
                e.printStackTrace();
                try {
                    downloadapk = false;
                    ftp_1.disconnect();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (downloadapk) {
                fcall.logStatus("Apk download billing_file true");
                try {
                    fcall.logStatus("Main_Apk 7");
                    ftp_1.setFileType(FTP.BINARY_FILE_TYPE);
                    ftp_1.enterLocalPassiveMode();
                    fcall.logStatus("Main_Apk 8");
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    fcall.logStatus("Main_Apk 9");
                    ftp_1.changeWorkingDirectory("/Android/Apk/");
                    fcall.logStatus("Main_Apk 10");
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    fcall.logStatus("Main_Apk 11");
                    FTPFile[] ftpFiles = ftp_1.listFiles("/Android/Apk/");
                    fcall.logStatus("Main_Apk 12");
                    int length = ftpFiles.length;
                    fcall.logStatus("Main_Apk 13");
                    fcall.logStatus("Main_Apk_length = " + length);
                    String namefile;
                    long filelength = 0;
                    for (FTPFile ftpFile : ftpFiles) {
                        namefile = ftpFile.getName();
                        fcall.logStatus("Main_Apk_namefile : " + namefile);
                        boolean isFile = ftpFile.isFile();
                        if (isFile) {
                            fcall.logStatus("Main_Apk_File: " + "TVDManagement_" + update_version + ".apk");
                            if (namefile.equals("TVDManagement_" + update_version + ".apk")) {
                                fcall.logStatus("Main_Apk File found to download");
                                filelength = ftpFile.getSize();
                                file_found = true;
                                break;
                            }
                        }
                    }
                    if (file_found) {
                        File file = new File(mobilepath + "TVDManagement_"+update_version+".apk");
                        fcall.logStatus("FTP File length: "+filelength);
                        OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(file));
                        InputStream inputStream = ftp_1.retrieveFileStream("/Android/Apk/" + "TVDManagement_"+update_version+".apk");
                        byte[] bytesIn = new byte[1024];
                        while ((count = inputStream.read(bytesIn)) != -1) {
                            read += count;
                            publishProgress((int)((read*100)/filelength));
                            outputStream.write(bytesIn, 0, count);
                        }
                        inputStream.close();
                        outputStream.close();

                        if (ftp_1.completePendingCommand()) {
                            fcall.logStatus("Apk file Download successfully.");
                            handler.sendEmptyMessage(APK_FILE_DOWNLOADED);
                        }
                    } else handler.sendEmptyMessage(APK_FILE_NOT_FOUND);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            try {
                ftp_1.logout();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            progressDialog.setProgress(values[0]);
        }
    }

    public class UploadText extends AsyncTask<String,Void,Void>{

        FileInputStream fis = null;
//        Handler handler;
        String mobileFilePathUplod, file_name_UP, serverUploadFilePath;

        public UploadText(String mobileFilePathUplod, String file_name_UP,
                          String serverUploadFilePath) {
//            this.handler = handler;
            this.mobileFilePathUplod = mobileFilePathUplod;
            this.file_name_UP = file_name_UP;
            this.serverUploadFilePath = serverUploadFilePath;
        }

        @Override
        protected Void doInBackground(String... strings) {

            fcall.logStatus("Text Upload Started");
            FTPClient client = new FTPClient();
            fcall.logStatus("Text_Upload 1");
            try {
                fcall.logStatus("Text_Upload 2");
                client.connect(FTP_HOST, FTP_PORT);
                fcall.logStatus("Text_Upload 3");
                int reply_from_server = client.getReplyCode();
                fcall.logStatus("Text_Upload 4");
                //noinspection ResultOfMethodCallIgnored
                FTPReply.isPositiveCompletion(reply_from_server);
            } catch (IOException | NullPointerException e1) {
                e1.printStackTrace();
            }

            try {
                fcall.logStatus("Text_Upload 5");
                client.login(FTP_USER, FTP_PASS);
                fcall.logStatus("Text_Upload 6");
            } catch (IOException | NullPointerException e1) {
                e1.printStackTrace();
            }
            try {
                fcall.logStatus("Text_Upload 7");
                client.setFileType(FTP.BINARY_FILE_TYPE);
                fcall.logStatus("Text_Upload 8");
                client.enterLocalPassiveMode();
                fcall.logStatus("Text_Upload 9");
            } catch (IOException | NullPointerException e1) {
                e1.printStackTrace();
                fcall.logStatus("Text_Upload setFileType Null pointer exception");
            }
            try {
                fcall.logStatus("Text_Upload 10");
                client.changeWorkingDirectory(serverUploadFilePath);
                fcall.logStatus("Text_Upload 11");
            } catch (IOException | NullPointerException e1) {
                e1.printStackTrace();
            }
            try {
                fcall.logStatus("Text_Upload 12");
                File file = new File(mobileFilePathUplod + File.separator + file_name_UP);
                fcall.logStatus("Text_Upload 13");
                String testName = file.getName();
                fcall.logStatus("Upload Testname: "+testName);
                fcall.logStatus("Text_Upload 14");
                fis = new FileInputStream(file);
                fcall.logStatus("Text_Upload 15");
                long filesize = file.length();
                fcall.logStatus("File size: "+filesize);
                if (client.storeFile(testName, fis)) {
                    fcall.logStatus("Text Uploaded Successfully");
                    move_files(mobileFilePathUplod,file_name_UP,fcall.filepath("BACKUP"),file_name_UP);
//                    handler.sendEmptyMessage(FILE_UPLOAD_SUCCESS);
                }
                fis.close();
                fcall.logStatus("Text_Upload 16");
                client.logout();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    public void move_files(String source_path, String source_file, String destination_path, String destination_file) {
        File fromfile = new File(source_path + File.separator + source_file);
        File tofile = new File(destination_path + File.separator + destination_file);
        if (fromfile.exists())
            //noinspection ResultOfMethodCallIgnored
            fromfile.renameTo(tofile);
    }

}
