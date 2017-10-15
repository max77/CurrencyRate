package com.max77.currencyrate.datasource.online;

import android.util.Log;

import com.max77.currencyrate.BuildConfig;
import com.max77.currencyrate.datamodel.ConversionRateInfo;
import com.max77.currencyrate.datamodel.Currency;
import com.max77.currencyrate.repository.ICurrencyRateOnlineDataSource;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by mkomarovskiy on 14/10/2017.
 */

public class CurrencyRatesOnlineDataSource implements ICurrencyRateOnlineDataSource {
    private static final String TAG = "CURRATE:REST";
    private CurrencyRatesRestClient mRestClient;

    public CurrencyRatesOnlineDataSource(String baseUrl) {
        OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder();

        if (BuildConfig.DEBUG) {
            Interceptor loggingInterceptor =
                    new HttpLoggingInterceptor(message -> Log.d(TAG, message))
                            .setLevel(HttpLoggingInterceptor.Level.BODY);
            clientBuilder.addInterceptor(loggingInterceptor);
        }

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.createWithScheduler(Schedulers.io()))
                .addConverterFactory(GsonConverterFactory.create())
                .client(clientBuilder.build())
                .build();

        mRestClient = retrofit.create(CurrencyRatesRestClient.class);
    }

    @Override
    public Single<ConversionRateInfo> getCurrencyRate(Currency from, Currency to) {
        return mRestClient
                .getCurrencyRate(from.getISOCode(), to.getISOCode())
                .map(ratesResponse -> new ConversionRateInfo(from, to,
                        ratesResponse.getRatesMap().get(to.getISOCode()), ratesResponse.getDate()));
    }

    @Override
    public Single<List<String>> getActiveCurrencyISOCodes() {
        return mRestClient
                .getAllCurrencyRates()
                .map(ratesResponse -> {
                    List<String> result = new ArrayList<>(ratesResponse.getRatesMap().keySet());
                    result.add(ratesResponse.getBase());
                    return result;
                });
    }
}
