package com.max77.currencyrate.ui;

import android.os.Bundle;

import com.google.gson.Gson;
import com.max77.currencyrate.CurrencyRateApplication;
import com.max77.currencyrate.datamodel.ConversionRateInfo;
import com.max77.currencyrate.datamodel.Currency;

import java.util.Collections;
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

    private State mCurrentState;
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
            if (mCurrentState == null)
                throw new NullPointerException();
            updateView();
        } catch (Exception e) {
            mCurrentState = new State();
            applyCurrentState(false);
        }
    }

    void saveCurrentState(Bundle bundle) {
        try {
            bundle.putString(KEY_SAVED_STATE, new Gson().toJson(mCurrentState));
        } catch (Exception e) {

        }
    }

    void destroy() {
        mRequestDisposable.clear();
    }

    private void updateView() {
        mView.setAvailableCurrencies(mCurrentState.mAvailableCurrencies);
        mView.setSourceCurrencyIdx(mCurrentState.mSourceIdx);
        mView.setTargetCurrencyIdx(mCurrentState.mTargetIdx);
        mView.setSourceAmount(mCurrentState.mSourceAmount);
        mView.setTargetAmount(mCurrentState.mTargetAmount);
        mView.setDate(mCurrentState.mDate);
        mView.showRate(mCurrentState.mAvailableCurrencies.get(mCurrentState.mSourceIdx), mCurrentState.mSourceAmount,
                mCurrentState.mAvailableCurrencies.get(mCurrentState.mTargetIdx), mCurrentState.mTargetAmount);
    }

    void update(boolean invalidateCache) {
        int sourceIdx = mView.getSourceCurrencyIdx();
        int targetIdx = mView.getTargetCurrencyIdx();
        float sourceAmount = mView.getSourceAmount();
        float targetAmount = mView.getTargetAmount();

        if (sourceAmount <= 0) {
            mCurrentState.mSourceAmount = -1;
            updateView();
            return;
        } else if (targetAmount <= 0) {
            mCurrentState.mTargetAmount = -1;
            updateView();
            return;
        }

        // mark fields to recalculate
        if (sourceIdx != mCurrentState.mSourceIdx) {
            mCurrentState.mSourceIdx = sourceIdx;
            mCurrentState.mConversionRate = 0;
            mCurrentState.mTargetAmount = 0;
        } else if (targetIdx != mCurrentState.mTargetIdx) {
            mCurrentState.mTargetIdx = targetIdx;
            mCurrentState.mConversionRate = 0;
            mCurrentState.mSourceAmount = 0;
        } else if (sourceAmount != mCurrentState.mSourceAmount) {
            mCurrentState.mSourceAmount = sourceAmount;
            mCurrentState.mTargetAmount = 0;
        } else if (targetAmount != mCurrentState.mTargetAmount) {
            mCurrentState.mTargetAmount = targetAmount;
            mCurrentState.mSourceAmount = 0;
        }

        applyCurrentState(invalidateCache);
    }

    private void applyCurrentState(boolean invalidateCache) {
        invalidateCache |= !Util.isSameBankingDay(mCurrentState.mDate, new Date());

        mRequestDisposable.add(fillCurrentState(invalidateCache)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(disposable ->
                        mView.showProgress(true))
                .subscribe((state1, error) -> {
                    mView.showProgress(false);
                    if (error != null)
                        mView.showError(error.getLocalizedMessage());
                    else {
                        updateView();
                    }
                })
        );
    }

    private Single<State> fillCurrentState(boolean invalidateCache) {
        return Single.just(mCurrentState)
                // loading currency list if needed
                .flatMap(state ->
                        state.mAvailableCurrencies == null ?
                                getRepository().getActiveCurrencies(invalidateCache)
                                        .map(response -> {
                                            state.mAvailableCurrencies = response.getPayload();
                                            sortCurrencies(state.mAvailableCurrencies);
                                            return state;
                                        }) :
                                Single.just(state))
                // checking if source/target currency is set
                .map(state -> {
                    if (state.mSourceIdx < 0)
                        state.mSourceIdx = findCurrencyIdxByISOCode(state.mAvailableCurrencies, mDefaultSourceISO);
                    if (state.mSourceIdx < 0)
                        state.mSourceIdx = 0;

                    if (state.mTargetIdx < 0)
                        state.mTargetIdx = findCurrencyIdxByISOCode(state.mAvailableCurrencies, mDefaultTargetISO);
                    if (state.mTargetIdx < 0)
                        state.mTargetIdx = state.mAvailableCurrencies.size() - 1;

                    if (state.mSourceIdx == state.mTargetIdx)
                        state.mConversionRate = 1.0f;

                    return state;
                })
                // loading conversion rate if not set
                .flatMap(state ->
                        state.mConversionRate <= 0 ?
                                getRepository().getConversionRateInfo(state.mAvailableCurrencies.get(state.mSourceIdx),
                                        state.mAvailableCurrencies.get(state.mTargetIdx), invalidateCache)
                                        .map(response -> {
                                            ConversionRateInfo cri = response.getPayload();

                                            state.mConversionRate = cri.getConversionRate();
                                            state.mDate = cri.getDate();
                                            // a bit of paranoia
                                            state.mSourceIdx = findCurrencyIdxByISOCode(state.mAvailableCurrencies, cri.getSourceCurrency().getISOCode());
                                            state.mTargetIdx = findCurrencyIdxByISOCode(state.mAvailableCurrencies, cri.getTargetCurrency().getISOCode());
                                            state.mSourceDigits = cri.getSourceCurrency().getDigits();
                                            state.mTargetDigits = cri.getTargetCurrency().getDigits();

                                            return state;
                                        }) :
                                Single.just(state))
                // adjusting amounts
                .map(state -> {
                    if (state.mSourceAmount == 0 && state.mTargetAmount == 0) {
                        state.mSourceAmount = 1.0f;
                        state.mTargetAmount = roundToDigits(state.mConversionRate, state.mTargetDigits);
                    } else if (state.mSourceAmount == 0)
                        state.mSourceAmount = roundToDigits(state.mTargetAmount / state.mConversionRate, state.mSourceDigits);
                    else if (state.mTargetAmount == 0)
                        state.mTargetAmount = roundToDigits(state.mSourceAmount * state.mConversionRate, state.mTargetDigits);

                    return state;
                });
    }

    private void sortCurrencies(List<Currency> availableCurrencies) {
        Collections.sort(availableCurrencies, (c1, c2) -> {
            if (c1 == null && c2 == null)
                return 0;

            if (c1 == null)
                return -1;

            if (c2 == null)
                return 1;

            if (c1.getFullName() != null && c2.getFullName() != null)
                return c1.getFullName().compareTo(c2.getFullName());

            if (c1.getISOCode() != null && c2.getISOCode() != null)
                return c1.getISOCode().compareTo(c2.getISOCode());

            return 0;
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
        int mSourceIdx = -1;
        int mTargetIdx = -1;
        float mSourceAmount;
        float mTargetAmount;
        float mConversionRate;
        int mSourceDigits;
        int mTargetDigits;
        Date mDate;
    }
}
