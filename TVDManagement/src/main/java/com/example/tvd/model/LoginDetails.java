package com.example.tvd.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class LoginDetails implements Serializable {
    public String getSUBDIVCODE() {
        return SUBDIVCODE;
    }

    public void setSUBDIVCODE(String SUBDIVCODE) {
        this.SUBDIVCODE = SUBDIVCODE;
    }

    public String getUSERNAME() {
        return USERNAME;
    }

    public void setUSERNAME(String USERNAME) {
        this.USERNAME = USERNAME;
    }

    public String getTIC_VERSION() {
        return TIC_VERSION;
    }

    public void setTIC_VERSION(String TIC_VERSION) {
        this.TIC_VERSION = TIC_VERSION;
    }

        @SerializedName("SUBDIVCODE")
        @Expose
        private String SUBDIVCODE;

    public String getUSERID() {
        return USERID;
    }

    public void setUSERID(String USERID) {
        this.USERID = USERID;
    }

    @SerializedName("USERID")
    @Expose
    private String USERID;
    @SerializedName("USERNAME")
    @Expose
    private String USERNAME;
    @SerializedName("TIC_VERSION")
    @Expose
    private String TIC_VERSION;

    public String getCOMPANY_LEVEL_ID() {
        return COMPANY_LEVEL_ID;
    }

    public void setCOMPANY_LEVEL_ID(String COMPANY_LEVEL_ID) {
        this.COMPANY_LEVEL_ID = COMPANY_LEVEL_ID;
    }

    @SerializedName("COMPANY_LEVEL_ID")
    @Expose
    private String COMPANY_LEVEL_ID;

    public String getGROUPS() {
        return GROUPS;
    }

    public void setGROUPS(String GROUPS) {
        this.GROUPS = GROUPS;
    }

    @SerializedName("GROUPS")
    @Expose
    private String GROUPS;

    public String getEMP_ATTENDENCE() {
        return EMP_ATTENDENCE;
    }

    public void setEMP_ATTENDENCE(String EMP_ATTENDENCE) {
        this.EMP_ATTENDENCE = EMP_ATTENDENCE;
    }

    @SerializedName("EMP_ATTENDENCE")
    @Expose
    private String EMP_ATTENDENCE;

    public String getMOBILE_NO() {
        return MOBILE_NO;
    }

    @SerializedName("MOBILE_NO")
    @Expose
    private String MOBILE_NO;
}
