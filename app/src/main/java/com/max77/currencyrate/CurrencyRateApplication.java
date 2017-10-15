package com.max77.currencyrate;

import android.app.Application;

import com.max77.currencyrate.datasource.cached.CurrenciesDataSource;
import com.max77.currencyrate.datasource.cached.CurrencyRatesCachedDataSource;
import com.max77.currencyrate.datasource.online.CurrencyRatesOnlineDataSource;
import com.max77.currencyrate.repository.RepositoryImpl;
import com.max77.currencyrate.ui.IRepository;

/**
 * Created by mkomarovskiy on 12/10/2017.
 */

public class CurrencyRateApplication extends Application {
    private IRepository mRepository;

    @Override
    public void onCreate() {
        super.onCreate();

        mRepository = new RepositoryImpl(new CurrenciesDataSource(this),
                new CurrencyRatesOnlineDataSource(BuildConfig.CURRENCY_RATES_BACKEND_URL),
                new CurrencyRatesCachedDataSource(this));
    }

    public IRepository getRepository() {
        return mRepository;
    }
}
