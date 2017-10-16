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

//        Collections.sort(objects, (c1, c2) -> {
//            if (c1 == null && c2 == null)
//                return 0;
//
//            if (c1 == null)
//                return -1;
//
//            if (c2 == null)
//                return 1;
//
//            if (c1.getFullName() != null && c2.getFullName() != null)
//                return c1.getFullName().compareTo(c2.getFullName());
//
//            if (c1.getISOCode() != null && c2.getISOCode() != null)
//                return c1.getISOCode().compareTo(c2.getISOCode());
//
//            return 0;
//        });
    }

    @Nullable
    @Override
    public Currency getItem(int position) {
        Currency item = super.getItem(position);
        if (item != null)
            return new Currency(item.getISOCode(), item.getFullName(), digits) {
                @Override
                public String toString() {
                    return getContext().getString(R.string.currency_name_text, getFullName(), getISOCode());
                }
            };

        return super.getItem(position);
    }
}
