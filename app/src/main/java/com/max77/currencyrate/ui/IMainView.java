package com.max77.currencyrate.ui;

import android.content.Context;

/**
 * Created by mkomarovskiy on 13/10/2017.
 */

interface IMainView {
    void setState(MainState state);

    MainState getState();

    Context getContext();
}
