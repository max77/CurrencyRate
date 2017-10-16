package com.max77.currencyrate.ui;

import android.os.Bundle;

import com.google.gson.Gson;
import com.max77.currencyrate.CurrencyRateApplication;
import com.max77.currencyrate.datamodel.ConversionRateInfo;
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
    private static final String TAG = "CURRATE::LOGIC";
    private static final String KEY_SAVED_STATE = "saved_state";

    private State mCurrentState = new State();
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

    void init(Bundle bundle) {
        try {
            mCurrentState = new Gson().fromJson(bundle.getString(KEY_SAVED_STATE), State.class);
        } catch (Exception e) {
            mCurrentState = new State();
        }

        update(false);
    }

    void destroy() {
        mRequestDisposable.clear();
    }

    void update(boolean invalidateCache) {
        State newState = new State(mCurrentState);
        newState.mSourceCurrencyIdx = mView.getCurrencyIdx(false);
        newState.mTargetCurrencyIdx = mView.getCurrencyIdx(true);
        newState.mSourceAmount = mView.getAmount(false);
        newState.mTargetAmount = mView.getAmount(true);

        // mark fields to recalculate
        if (newState.mSourceCurrencyIdx != mCurrentState.mSourceCurrencyIdx) {
            newState.mConversionRate = 0;
            newState.mTargetAmount = 0;
        } else if (newState.mTargetCurrencyIdx != mCurrentState.mTargetCurrencyIdx) {
            newState.mConversionRate = 0;
            newState.mSourceAmount = 0;
        } else if (newState.mSourceAmount != mCurrentState.mSourceAmount)
            newState.mTargetAmount = 0;
        else if (newState.mTargetAmount != mCurrentState.mTargetAmount)
            newState.mSourceAmount = 0;

        invalidateCache |= !Util.isSameBankingDay(mCurrentState.mDate, new Date());

        mRequestDisposable.add(fillState(newState, invalidateCache)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(disposable ->
                        mView.showProgress(true))
                .subscribe((state, error) -> {
                    mView.showProgress(false);
                    if (error != null)
                        mView.showError(error.getLocalizedMessage());
                    else {
                        updateView(state);
                        mCurrentState = state;
                    }
                })
        );
    }

    private void updateView(State newState) {
        if (newState.mAvailableCurrencies != mCurrentState.mAvailableCurrencies)
            mView.setAvailableCurrencies(newState.mAvailableCurrencies);

        if (newState.mSourceCurrencyIdx != mCurrentState.mSourceCurrencyIdx)
            mView.setCurrencyIdx(false, newState.mSourceCurrencyIdx);
        if (newState.mTargetCurrencyIdx != mCurrentState.mTargetCurrencyIdx)
            mView.setCurrencyIdx(true, newState.mTargetCurrencyIdx);

        if (newState.mSourceAmount != mCurrentState.mSourceAmount)
            mView.setAmount(false, newState.mSourceAmount);
        if (newState.mTargetAmount != mCurrentState.mTargetAmount)
            mView.setAmount(true, newState.mTargetAmount);

        mView.setDate(newState.mDate);
    }

    private Single<State> fillState(State stateToFill, boolean invalidateCache) {
        return Single.just(stateToFill)
                // loading currency list if needed
                .flatMap(state ->
                        state.mAvailableCurrencies == null ?
                                getRepository().getActiveCurrencies(invalidateCache)
                                        .map(response -> {
                                            state.mAvailableCurrencies = response.getPayload();
                                            return state;
                                        }) :
                                Single.just(state))
                // checking if source/target currency is set
                .map(state -> {
                    if (state.mSourceCurrencyIdx < 0)
                        state.mSourceCurrencyIdx = findCurrencyIdxByISOCode(state.mAvailableCurrencies, mDefaultSourceISO);
                    if (state.mSourceCurrencyIdx < 0)
                        state.mSourceCurrencyIdx = 0;

                    if (state.mTargetCurrencyIdx < 0)
                        state.mTargetCurrencyIdx = findCurrencyIdxByISOCode(state.mAvailableCurrencies, mDefaultTargetISO);
                    if (state.mTargetCurrencyIdx < 0)
                        state.mTargetCurrencyIdx = state.mAvailableCurrencies.size() - 1;

                    return state;
                })
                // loading conversion rate if not set
                .flatMap(state ->
                        state.mConversionRate <= 0 ?
                                getRepository().getConversionRateInfo(state.mAvailableCurrencies.get(state.mSourceCurrencyIdx),
                                        state.mAvailableCurrencies.get(state.mTargetCurrencyIdx), invalidateCache)
                                        .map(response -> {
                                            ConversionRateInfo cri = response.getPayload();

                                            state.mConversionRate = cri.getConversionRate();
                                            state.mDate = cri.getDate();
                                            // a bit of paranoia
                                            state.mSourceCurrencyIdx = findCurrencyIdxByISOCode(state.mAvailableCurrencies, cri.getSourceCurrency().getISOCode());
                                            state.mTargetCurrencyIdx = findCurrencyIdxByISOCode(state.mAvailableCurrencies, cri.getTargetCurrency().getISOCode());

                                            return state;
                                        }) :
                                Single.just(state))
                // adjusting amounts
                .map(state -> {
                    if (state.mSourceAmount == 0 && state.mTargetAmount == 0) {
                        state.mSourceAmount = 1.0f;
                        state.mTargetAmount = roundToDigits(state.mConversionRate, state.;
                    } else if (state.mSourceAmount == 0)
                        state.mSourceAmount = state.mTargetAmount / state.mConversionRate;
                    else if (state.mTargetAmount == 0)
                        state.mTargetAmount = state.mSourceAmount * state.mConversionRate;

                    return state;
                });
    }

    private int findCurrencyIdxByISOCode(List<Currency> currencies, String iso) {
        for (int i = 0; i < currencies.size(); i++) {
            if (currencies.get(i).getISOCode().equals(iso))
                return i;
        }

        return -1;
    }

    private float roundToDigits(float x, int digits) {
        double m = Math.pow(10, digits);
        return (float) (Math.round(x * m) / m);
    }

    private static class State {
        List<Currency> mAvailableCurrencies;
        int mSourceCurrencyIdx;
        int mTargetCurrencyIdx;
        float mSourceAmount;
        float mTargetAmount;
        float mConversionRate;
        Date mDate;

        State() {
        }

        State(State other) {
            this.mAvailableCurrencies = other.mAvailableCurrencies;
            this.mSourceCurrencyIdx = other.mSourceCurrencyIdx;
            this.mTargetCurrencyIdx = other.mTargetCurrencyIdx;
            this.mSourceAmount = other.mSourceAmount;
            this.mTargetAmount = other.mTargetAmount;
            this.mConversionRate = other.mConversionRate;
            this.mDate = other.mDate;
        }
    }
}
