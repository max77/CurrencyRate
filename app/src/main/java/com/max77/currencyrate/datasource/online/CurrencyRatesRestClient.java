package com.max77.currencyrate.datasource.online;

import io.reactivex.Single;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by mkomarovskiy on 14/10/2017.
 */

public interface CurrencyRatesRestClient {
    @GET("/latest")
    Single<CurrencyRatesResponse> getCurrencyRate(@Query("base") String isoFrom, @Query("symbols") String isoTo);

    @GET("/latest")
    Single<CurrencyRatesResponse> getAllCurrencyRates();
}
