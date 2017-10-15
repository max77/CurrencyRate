package com.max77.currencyrate.repository;


import com.max77.currencyrate.datamodel.ConversionRateInfo;
import com.max77.currencyrate.datamodel.Currency;

import java.util.List;

import io.reactivex.Single;

/**
 * Created by mkomarovskiy on 14/10/2017.
 */

public interface ICurrencyRateCachedDataSource {
    Single<ConversionRateInfo> getCurrencyRate(Currency from, Currency to);

    Single<ConversionRateInfo> putCurrencyRate(ConversionRateInfo rate);

    Single<List<Currency>> getActiveCurrencies();

    Single<List<Currency>> putActiveCurrencies(List<Currency> currencies);

    Single<Void> invalidate();
}
