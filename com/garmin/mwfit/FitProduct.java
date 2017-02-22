package com.garmin.mwfit;

/**
 * Created by Jimmy on 2017/2/22.
 */

public class FitProduct {

    private String name;
    private String unitId;
    private String productId;
    private String versionMajor;
    private String versionMinor;

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setUnitId(String unitId) {
        this.unitId = unitId;
    }

    public String getUnitId() {
        return unitId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getProductId() {
        return productId;
    }

    public void setVersionMajor(String versionMajor) {
        this.versionMajor = versionMajor;
    }

    public String getVersionMajor() {
        return versionMajor;
    }

    public void setVersionMinor(String versionMinor) {
        this.versionMinor = versionMinor;
    }

    public String getVersionMinor() {
        return versionMinor;
    }
}
