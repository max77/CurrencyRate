package com.max77.currencyrate.repository;


import com.max77.currencyrate.datamodel.ConversionRateInfo;
import com.max77.currencyrate.datamodel.Currency;

import java.util.List;

import io.reactivex.Single;

/**
 * Created by mkomarovskiy on 14/10/2017.
 */

public interface ICurrencyRateOnlineDataSource {
    Single<ConversionRateInfo> getCurrencyRate(Currency from, Currency to);

    Single<List<String>> getActiveCurrencyISOCodes();
}
