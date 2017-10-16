package com.max77.currencyrate.ui;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.widget.ArrayAdapter;

import com.max77.currencyrate.R;
import com.max77.currencyrate.datamodel.Currency;

import java.util.List;

/**
 * Created by mkomarovskiy on 13/10/2017.
 */

class CurrencyNamesSpinnerAdapter extends ArrayAdapter<Currency> {
    CurrencyNamesSpinnerAdapter(@NonNull Context context, @NonNull List<Currency> objects) {
        super(context, android.R.layout.simple_spinner_item, objects);
    }

    @Nullable
    @Override
    public Currency getItem(int position) {
        Currency item = super.getItem(position);
        if (item != null)
            return new Currency(item.getISOCode(), item.getFullName(), item.getDigits()) {
                @Override
                public String toString() {
                    return getContext().getString(R.string.currency_name_text, getFullName(), getISOCode());
                }
            };

        return super.getItem(position);
    }
}
