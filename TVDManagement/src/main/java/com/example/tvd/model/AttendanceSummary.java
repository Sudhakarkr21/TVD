package com.example.tvd.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class AttendanceSummary implements Serializable {
    @SerializedName("USERNAME")
    @Expose
    private String USERNAME;
    @SerializedName("ADDRESS")
    @Expose
    private String ADDRESS;
    @SerializedName("SUBDIVCODE")
    @Expose
    private String SUBDIVCODE;

    public String getREMARK() {
        return REMARK;
    }

    @SerializedName("REMARK")
    @Expose
    private String REMARK;

    public String getUSERNAME() {
        return USERNAME;
    }

    public String getADDRESS() {
        return ADDRESS;
    }

    public String getSUBDIVCODE() {
        return SUBDIVCODE;
    }

    public String getDATETIME() {
        return DATETIME;
    }

    @SerializedName("DATETIME")
    @Expose
    private String DATETIME;

    public String getMessage() {
        return Message;
    }

    public void setMessage(String message) {
        Message = message;
    }

    @SerializedName("Message")
    @Expose
    private String Message;
}
