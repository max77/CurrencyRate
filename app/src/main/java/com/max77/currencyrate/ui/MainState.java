package com.max77.currencyrate.ui;

import com.max77.currencyrate.datamodel.ConversionRateInfo;
import com.max77.currencyrate.datamodel.Currency;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mkomarovskiy on 13/10/2017.
 */

class MainState {
    private float mSourceAmount;
    private float mTargetAmount;
    private ConversionRateInfo mConversionRateInfo = new ConversionRateInfo();
    private List<Currency> mAvailableCurrencies = new ArrayList<>();

    public MainState() {
    }

    public MainState(MainState other) {
        this.mSourceAmount = other.mSourceAmount;
        this.mTargetAmount = other.mTargetAmount;
        this.mConversionRateInfo = other.mConversionRateInfo;
        this.mAvailableCurrencies = other.mAvailableCurrencies;
    }

    public float getSourceAmount() {
        return mSourceAmount;
    }

    public MainState setSourceAmount(float sourceAmount) {
        mSourceAmount = sourceAmount;
        return this;
    }

    public float getTargetAmount() {
        return mTargetAmount;
    }

    public MainState setTargetAmount(float targetAmount) {
        mTargetAmount = targetAmount;
        return this;
    }

    public ConversionRateInfo getConversionRateInfo() {
        return mConversionRateInfo;
    }

    public MainState setConversionRateInfo(ConversionRateInfo conversionRateInfo) {
        mConversionRateInfo = conversionRateInfo;
        return this;
    }

    public List<Currency> getAvailableCurrencies() {
        return mAvailableCurrencies;
    }

    public MainState setAvailableCurrencies(List<Currency> availableCurrencies) {
        mAvailableCurrencies = availableCurrencies;
        return this;
    }
}
