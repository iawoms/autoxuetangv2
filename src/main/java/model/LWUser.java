package model;

import java.io.IOException;

public class LWUser {
    String orgID;
    String userID;
    String cnName;

    public String getOrgID() {
        return orgID;
    }

    public void setOrgID(String orgID) {
        this.orgID = orgID;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getCnName() {
        return cnName;
    }

    public void setCnName(String cnName) {
        this.cnName = cnName;
    }

    public LWUser(String vararr) {
        String orgIDPrefix = "orgID\":\"";
        String userIDPrefix = "userID\":\"";
        String endFix = "\",";
        orgID = vararr.substring(vararr.indexOf(orgIDPrefix) + orgIDPrefix.length(), vararr.indexOf(endFix));
        vararr = vararr.substring(vararr.indexOf(endFix) + endFix.length());
        userID= vararr.substring(vararr.indexOf(userIDPrefix) + userIDPrefix.length(), vararr.indexOf(endFix));
    }

    @Override
    public String toString() {
        return orgID + " - " + userID;
    }
}
