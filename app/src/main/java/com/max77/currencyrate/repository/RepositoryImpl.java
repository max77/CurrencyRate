package com.max77.currencyrate.repository;

import android.util.Log;

import com.max77.currencyrate.datamodel.ConversionRateInfo;
import com.max77.currencyrate.datamodel.Currency;
import com.max77.currencyrate.ui.IRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.reactivex.Single;

/**
 * Created by mkomarovskiy on 14/10/2017.
 */

public class RepositoryImpl implements IRepository {
    private static final String TAG = "CURRATE:REPO";

    // for simplicity assume that currencies list is locally available (no online/cached stuff)
    private ICurrenciesDataSource mCurrenciesDataSource;
    private ICurrencyRateOnlineDataSource mCurrencyRateOnlineDataSource;
    private ICurrencyRateCachedDataSource mCurrencyRateCachedDataSource;

    public RepositoryImpl(ICurrenciesDataSource currenciesDataSource,
                          ICurrencyRateOnlineDataSource currencyRateOnlineDataSource,
                          ICurrencyRateCachedDataSource currencyRateCachedDataSource) {
        mCurrenciesDataSource = currenciesDataSource;
        mCurrencyRateOnlineDataSource = currencyRateOnlineDataSource;
        mCurrencyRateCachedDataSource = currencyRateCachedDataSource;
    }

    @Override
    public Single<RepositoryResponse<List<Currency>>> getActiveCurrencies(boolean forceCacheRefresh) {
        return retrieveData(mCurrenciesDataSource.getCurrencies()
                        .zipWith(mCurrencyRateOnlineDataSource.getActiveCurrencyISOCodes(),
                                this::filterActiveCurrencies)
                        .flatMap(activeCurrencies ->
                                mCurrencyRateCachedDataSource.putActiveCurrencies(activeCurrencies)),
                mCurrencyRateCachedDataSource
                        .getActiveCurrencies(),
                forceCacheRefresh,
                false);
    }

    /**
     * returns full info for currencies that the bank works with
     */
    private List<Currency> filterActiveCurrencies(List<Currency> allCurrencies, List<String> isoCodes) throws Exception {
        Set<String> isoCodesSet = new HashSet<>(isoCodes);
        Map<String, Currency> result = new HashMap<>();

        for (Currency currency : allCurrencies)
            if (currency.getISOCode() != null && isoCodesSet.contains(currency.getISOCode()))
                result.put(currency.getISOCode(), currency);

        if (result.isEmpty())
            throw new Exception("empty active currency list");

        return new ArrayList<>(result.values());
    }

    @Override
    public Single<RepositoryResponse<ConversionRateInfo>> getConversionRateInfo(Currency from, Currency to, boolean forceCacheRefresh) {
        return retrieveData(
                mCurrencyRateOnlineDataSource
                        .getCurrencyRate(from, to)
                        .flatMap(rate -> mCurrencyRateCachedDataSource.putCurrencyRate(rate)),
                mCurrencyRateCachedDataSource
                        .getCurrencyRate(from, to),
                forceCacheRefresh,
                false);
    }

    private <T> Single<RepositoryResponse<T>> retrieveData(Single<T> readOnlineAndSaveToCacheSingle,
                                                           Single<T> readCacheSingle,
                                                           boolean forceCacheRefresh,
                                                           boolean recursive) {
        if (forceCacheRefresh)
            return readOnlineAndSaveToCacheSingle
                    .map(data -> {
                        Log.d(TAG, "Received online data");

                        return new RepositoryResponse<>(data, false);
                    })
                    .compose(s ->
                            recursive ?
                                    s :
                                    s.onErrorResumeNext(throwable -> {
                                        Log.w(TAG, "Error receiving online data, trying cache!" + "(" + throwable + ")");

                                        return retrieveData(readOnlineAndSaveToCacheSingle,
                                                readCacheSingle,
                                                false,
                                                true);
                                    })
                    );
        else
            // attempting to use cached data
            return readCacheSingle
                    .map(data -> {
                        Log.d(TAG, "Cache read success");

                        return new RepositoryResponse<>(data, true);
                    })
                    .compose(s ->
                            recursive ?
                                    s :
                                    s.onErrorResumeNext(throwable -> {
                                        Log.w(TAG, "Error receiving cached data, trying online!" + "(" + throwable + ")");

                                        return retrieveData(readOnlineAndSaveToCacheSingle,
                                                readCacheSingle,
                                                true,
                                                true);
                                    })
                    );
    }
}
