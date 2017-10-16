package com.max77.currencyrate.ui;

import android.content.Context;

import com.max77.currencyrate.datamodel.Currency;

import java.util.Date;
import java.util.List;

/**
 * Created by mkomarovskiy on 13/10/2017.
 */

interface IMainView {
    void setDate(Date date);

    void setSourceAmount(float amount);

    void setTargetAmount(float amount);

    float getSourceAmount();

    float getTargetAmount();

    void setAvailableCurrencies(List<Currency> currencies);

    void setSourceCurrencyIdx(int idx);

    void setTargetCurrencyIdx(int idx);

    int getSourceCurrencyIdx();

    int getTargetCurrencyIdx();

    Context getContext();

    void showProgress(boolean show);

    void showError(String message);

    void showRate(Currency source, float sourceAmount, Currency target, float targetAmount);
}
