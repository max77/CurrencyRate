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

    void setAmount(boolean target, float amount);

    float getAmount(boolean target);

    void setAvailableCurrencies(List<Currency> currencies);

    void setCurrencyIdx(boolean target, int idx);

    int getCurrencyIdx(boolean target);

    Context getContext();

    void showProgress(boolean show);

    void showError(String message);
}
