package com.max77.currencyrate.datasource.online;

import com.google.gson.annotations.SerializedName;

import java.util.Date;
import java.util.Map;

/**
 * Created by mkomarovskiy on 14/10/2017.
 */

class CurrencyRatesResponse {
    @SerializedName("error")
    private String mError;

    @SerializedName("base")
    private String mBase;

    @SerializedName("date")
    private Date mDate;

    @SerializedName("rates")
    private Map<String, Float> mRatesMap;

    public String getError() {
        return mError;
    }

    public String getBase() {
        return mBase;
    }

    public Date getDate() {
        return mDate;
    }

    public Map<String, Float> getRatesMap() {
        return mRatesMap;
    }
}
