package com.max77.currencyrate.ui;

import com.max77.currencyrate.datamodel.ConversionRateInfo;
import com.max77.currencyrate.datamodel.Currency;
import com.max77.currencyrate.repository.RepositoryResponse;

import java.util.List;

import io.reactivex.Single;

/**
 * Created by mkomarovskiy on 13/10/2017.
 */

public interface IRepository {
    Single<RepositoryResponse<List<Currency>>> getActiveCurrencies(boolean forceCacheRefresh);

    Single<RepositoryResponse<ConversionRateInfo>> getConversionRateInfo(Currency from, Currency to, boolean forceCacheRefresh);
}
