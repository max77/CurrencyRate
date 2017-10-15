package com.max77.currencyrate.datamodel;

import java.util.Date;

/**
 * Created by mkomarovskiy on 13/10/2017.
 */

public class ConversionRateInfo {
    private Currency mSourceCurrency = new Currency();
    private Currency mTargetCurrency = new Currency();
    private float mConversionRate;
    private Date mDate = new Date(0);

    public ConversionRateInfo() {
    }

    public ConversionRateInfo(Currency sourceCurrency, Currency targetCurrency, float conversionRate, Date date) {
        mSourceCurrency = sourceCurrency;
        mTargetCurrency = targetCurrency;
        mConversionRate = conversionRate;
        mDate = date;
    }

    public Currency getSourceCurrency() {
        return mSourceCurrency;
    }

    public ConversionRateInfo setSourceCurrency(Currency sourceCurrency) {
        mSourceCurrency = sourceCurrency;
        return this;
    }

    public Currency getTargetCurrency() {
        return mTargetCurrency;
    }

    public ConversionRateInfo setTargetCurrency(Currency targetCurrency) {
        mTargetCurrency = targetCurrency;
        return this;
    }

    public float getConversionRate() {
        return mConversionRate;
    }

    public ConversionRateInfo setConversionRate(float conversionRate) {
        mConversionRate = conversionRate;
        return this;
    }

    public Date getDate() {
        return mDate;
    }

    public ConversionRateInfo setDate(Date date) {
        mDate = date;
        return this;
    }
}
