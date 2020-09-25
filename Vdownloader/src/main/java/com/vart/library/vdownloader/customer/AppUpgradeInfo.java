package com.vart.library.vdownloader.customer;

public class AppUpgradeInfo {

    public int forced;//是否强制更新

    public String downloadUrl;


    public String versionLog;

    public String appVersion;

    @Override
    public String toString() {
        return "AppUpgradeInfo{" +
                "forced=" + forced +
                ", downloadUrl='" + downloadUrl + '\'' +
                ", versionLog='" + versionLog + '\'' +
                ", appVersion='" + appVersion + '\'' +
                '}';
    }
}
