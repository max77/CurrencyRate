package com.max77.currencyrate.ui;

import android.os.Bundle;

import com.google.gson.Gson;
import com.max77.currencyrate.CurrencyRateApplication;
import com.max77.currencyrate.datamodel.Currency;

import java.util.Date;
import java.util.List;

import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by mkomarovskiy on 13/10/2017.
 */

class MainPresenter {
    private static final String KEY_SAVED_STATE = "saved_state";

    private MainState mCurrentState = new MainState();
    private IMainView mView;
    private CompositeDisposable mRequestDisposable = new CompositeDisposable();
    private String mDefaultSourceISO;
    private String mDefaultTargetISO;

    public MainPresenter(IMainView view, String defaultSourceISO, String defaultTargetISO) {
        mView = view;
        mDefaultSourceISO = defaultSourceISO;
        mDefaultTargetISO = defaultTargetISO;
    }

    private IRepository getRepository() {
        return ((CurrencyRateApplication) mView.getContext().getApplicationContext()).getRepository();
    }

    void restoreStateOrInitialize(Bundle bundle) {
        boolean shouldReload = false;

        try {
            mCurrentState = new Gson().fromJson(bundle.getString(KEY_SAVED_STATE), MainState.class);
            if (!Util.areSameDay(mCurrentState.getConversionRateInfo().getDate(), new Date()))
                shouldReload = true;
        } catch (Exception e) {
            mCurrentState = new MainState();
            shouldReload = true;
        }

        if (shouldReload)
            forceUpdate();
        else
            update();
    }

    void update() {
        MainState newState = mView.getState();

        // current rate is more than one day old -> refresh
        if (!Util.areSameDay(mCurrentState.getConversionRateInfo().getDate(), new Date())) {
            mCurrentState.setSourceAmount(newState.getSourceAmount());
            mCurrentState.setTargetAmount(newState.getTargetAmount());
            mCurrentState.setConversionRateInfo(newState.getConversionRateInfo());
            forceUpdate();
            return;
        }

        // source currency changed
        if (!newState.getConversionRateInfo().getSourceCurrency().equals(mCurrentState.getConversionRateInfo().getSourceCurrency())) {
            mCurrentState.getConversionRateInfo().setSourceCurrency(newState.getConversionRateInfo().getSourceCurrency());
            mCurrentState.setTargetAmount(0);   // mark the amount to be recalculated
            loadData(false, true);
            return;
        }

        // target currency changed
        if (!newState.getConversionRateInfo().getTargetCurrency().equals(mCurrentState.getConversionRateInfo().getTargetCurrency())) {
            mCurrentState.getConversionRateInfo().setTargetCurrency(newState.getConversionRateInfo().getTargetCurrency());
            mCurrentState.setSourceAmount(0);   // mark the amount to be recalculated
            loadData(false, true);
            return;
        }

        // source amount changed
        if (newState.getSourceAmount() != mCurrentState.getSourceAmount()) {
            mCurrentState.setSourceAmount(newState.getSourceAmount());
            mCurrentState.setTargetAmount(0);   // mark the amount to be recalculated
        }

        // target amount changed
        if (newState.getTargetAmount() != mCurrentState.getTargetAmount()) {
            mCurrentState.setTargetAmount(newState.getTargetAmount());
            mCurrentState.setSourceAmount(0);   // mark the amount to be recalculated
        }

        mView.setState(mCurrentState);
    }

    void forceUpdate() {
        loadData(true, true);
    }

    private void loadData(boolean reloadCurrencies, boolean refreshCache) {
        mRequestDisposable.add(Single.just(new MainState(mCurrentState))
                .flatMap(state ->
                        reloadCurrencies ?
                                getRepository().getActiveCurrencies(refreshCache)
                                        .map(response ->
                                                state.setAvailableCurrencies(response.getPayload())) :
                                Single.just(state))
                .map(state ->
                        fixState(state, state.getAvailableCurrencies()))
                .flatMap(state ->
                        getRepository().getConversionRateInfo(state.getConversionRateInfo().getSourceCurrency(),
                                state.getConversionRateInfo().getTargetCurrency(), refreshCache)
                                .map(response ->
                                        state.setConversionRateInfo(response.getPayload())))
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSuccess(state -> mView.setState(mCurrentState = state))
                .subscribeOn(Schedulers.io())
                .subscribe());
    }

    private MainState fixState(MainState state, List<Currency> currencies) {
        MainState result = new MainState(state);

        if (state.getSourceAmount() == 0 && state.getTargetAmount() == 0)
            result.setSourceAmount(1.0f);

        int sourceIdx = findCurrencyIdxByISOTag(currencies,
                state.getConversionRateInfo().getSourceCurrency().getISOCode());

        if (sourceIdx == -1) {
            sourceIdx = findCurrencyIdxByISOTag(currencies, mDefaultSourceISO);
            if (sourceIdx == -1)
                sourceIdx = 0;
        }

        result.getConversionRateInfo().setSourceCurrency(currencies.get(sourceIdx));

        int targetIdx = findCurrencyIdxByISOTag(currencies,
                state.getConversionRateInfo().getTargetCurrency().getISOCode());

        if (targetIdx == -1) {
            targetIdx = findCurrencyIdxByISOTag(currencies, mDefaultTargetISO);
            if (targetIdx == -1)
                targetIdx = sourceIdx + 1;
        }

        result.getConversionRateInfo().setTargetCurrency(currencies.get(targetIdx));

        return result;
    }

    private int findCurrencyIdxByISOTag(List<Currency> currencies, String iso) {
        for (int i = 0; i < currencies.size(); i++) {
            if (currencies.get(i).getISOCode().equals(iso))
                return i;
        }

        return -1;
    }

    void destroy() {
        mRequestDisposable.clear();
    }
}
