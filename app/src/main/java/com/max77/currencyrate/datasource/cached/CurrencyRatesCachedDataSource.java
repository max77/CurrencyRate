package com.max77.currencyrate.datasource.cached;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.max77.currencyrate.datamodel.ConversionRateInfo;
import com.max77.currencyrate.datamodel.Currency;
import com.max77.currencyrate.repository.ICurrencyRateCachedDataSource;

import java.util.List;

import io.reactivex.Single;

/**
 * Created by mkomarovskiy on 14/10/2017.
 * <p>
 * Using a DB is an overkill for the app, so I stick to shared prefs for caching ;-)
 */

public class CurrencyRatesCachedDataSource implements ICurrencyRateCachedDataSource {

    private static final String CACHE_PREF_NAME = "rates_cache_prefs";
    private static final String KEY_ACTIVE_CURRENCIES = "active_currencies";

    private SharedPreferences mPreferences;

    public CurrencyRatesCachedDataSource(Context context) {
        mPreferences = context.getSharedPreferences(CACHE_PREF_NAME, Context.MODE_PRIVATE);
    }

    @Override
    public Single<ConversionRateInfo> getCurrencyRate(Currency from, Currency to) {
        return Single.fromCallable(() -> {
                    String json = mPreferences.getString(getRateCacheKey(from, to), null);
                    return new Gson().fromJson(json, ConversionRateInfo.class);
                }
        );
    }

    @Override
    public Single<ConversionRateInfo> putCurrencyRate(ConversionRateInfo rate) {
        return Single.fromCallable(() ->
        {
            String json = new Gson().toJson(rate);
            mPreferences
                    .edit()
                    .putString(getRateCacheKey(rate.getSourceCurrency(), rate.getTargetCurrency()), json)
                    .apply();

            return rate;
        });
    }

    @Override
    public Single<List<Currency>> getActiveCurrencies() {
        return Single.fromCallable(() ->
                new Gson().fromJson(mPreferences.getString(KEY_ACTIVE_CURRENCIES, null),
                        new TypeToken<List<Currency>>() {
                        }.getType())
        );
    }

    @Override
    public Single<List<Currency>> putActiveCurrencies(List<Currency> currencies) {
        return Single.fromCallable(() -> {
            String json = new Gson().toJson(currencies);
            mPreferences
                    .edit()
                    .putString(KEY_ACTIVE_CURRENCIES, json)
                    .apply();

            return currencies;
        });
    }

    @Override
    public Single<Void> invalidate() {
        return Single.fromCallable(() -> {
            mPreferences
                    .edit()
                    .clear()
                    .apply();

            return null;
        });
    }

    private String getRateCacheKey(Currency from, Currency to) {
        return from.getISOCode() + to.getISOCode();
    }
}
