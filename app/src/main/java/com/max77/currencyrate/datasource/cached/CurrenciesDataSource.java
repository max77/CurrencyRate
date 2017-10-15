package com.max77.currencyrate.datasource.cached;

import android.content.Context;
import android.util.Log;

import com.max77.currencyrate.BuildConfig;
import com.max77.currencyrate.datamodel.Currency;
import com.max77.currencyrate.repository.ICurrenciesDataSource;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.Single;

/**
 * Created by mkomarovskiy on 13/10/2017.
 */

public class CurrenciesDataSource implements ICurrenciesDataSource {
    private static final String TAG = "CURRATE:ISO4217";
    private Context mContext;

    public CurrenciesDataSource(Context context) {
        mContext = context;
    }

    @Override
    public Single<List<Currency>> getCurrencies() {
        // Get all currencies from ISO4217 asset provided
        return Single.fromCallable(() -> {
            InputStream is = mContext.getAssets().open(BuildConfig.ISO_4217_XML_FILENAME);
            Reader reader = new InputStreamReader(is);
            Serializer serializer = new Persister();
            CurrencyTable table = serializer.read(CurrencyTable.class, reader);
            reader.close();

            List<Currency> result = new ArrayList<>();
            for (CurrencyInfo info : table.infos)
                result.add(new Currency(info.mCurrencyCode, info.mCurrencyName));

            Log.d(TAG, "Currency list read success");

            return result;
        });
    }

    @Root(name = "ISO_4217", strict = false)
    private static class CurrencyTable {
        @ElementList(name = "CcyTbl")
        List<CurrencyInfo> infos;
    }

    @Root(name = "CcyNtry", strict = false)
    private static class CurrencyInfo {
        @Element(name = "CcyNm", required = false)
        String mCurrencyName;

        @Element(name = "Ccy", required = false)
        String mCurrencyCode;
    }
}
