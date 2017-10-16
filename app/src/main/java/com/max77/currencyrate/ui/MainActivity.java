package com.max77.currencyrate.ui;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.max77.currencyrate.R;
import com.max77.currencyrate.datamodel.Currency;

import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity implements IMainView {

    private SwipeRefreshLayout mRefreshLayout;
    private TextView mDateTextView;
    private TextView mSourceCurrencyTextView;
    private TextView mTargetCurrencyTextView;
    private EditText mSourceAmountInput;
    private EditText mTargetAmountInput;
    private Spinner mSourceNameSpinner;
    private Spinner mTargetNameSpinner;

    private MainPresenter mPresenter;

    private AmountTextWatcher mSourceAmountWatcher = new AmountTextWatcher();
    private AmountTextWatcher mTargetAmountWatcher = new AmountTextWatcher();
    private OneShotSpinnerListener mSourceListener = new OneShotSpinnerListener();
    private OneShotSpinnerListener mTargetListener = new OneShotSpinnerListener();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        preventKeypadFromHiding();
        initControls();

        mPresenter = new MainPresenter(this, "SEK", "USD");
        mPresenter.init();
    }

    private void preventKeypadFromHiding() {
        InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
    }

    private void initControls() {
        mDateTextView = findViewById(R.id.date);
        mSourceCurrencyTextView = findViewById(R.id.source_text);
        mTargetCurrencyTextView = findViewById(R.id.target_text);
        mSourceAmountInput = findViewById(R.id.source_amount);
        mTargetAmountInput = findViewById(R.id.target_amount);
        mSourceNameSpinner = findViewById(R.id.source_currency);
        mTargetNameSpinner = findViewById(R.id.target_currency);
        mRefreshLayout = findViewById(R.id.refreshLayout);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mRefreshLayout.setOnRefreshListener(() -> mPresenter.update(true));
        mSourceAmountInput.addTextChangedListener(mSourceAmountWatcher);
        mTargetAmountInput.addTextChangedListener(mTargetAmountWatcher);
        mSourceNameSpinner.setOnItemSelectedListener(mSourceListener);
        mTargetNameSpinner.setOnItemSelectedListener(mTargetListener);
    }

    @Override
    public void setDate(Date date) {
        mDateTextView.setText(Util.getPrettyDate(this, date));
    }

    @Override
    public void setSourceAmount(float amount) {
        if (amount <= 0) {
            mSourceAmountWatcher.lockForUpdate();
            mSourceAmountInput.setText("");
        } else if (amount != getSourceAmount()) {
            mSourceAmountWatcher.lockForUpdate();
            mSourceAmountInput.setText(String.valueOf(amount));
        }
    }

    @Override
    public void setTargetAmount(float amount) {
        if (amount <= 0) {
            mTargetAmountWatcher.lockForUpdate();
            mTargetAmountInput.setText("");
        } else if (amount != getTargetAmount()) {
            mTargetAmountWatcher.lockForUpdate();
            mTargetAmountInput.setText(String.valueOf(amount));
        }
    }

    @Override
    public float getSourceAmount() {
        try {
            return Float.valueOf(mSourceAmountInput.getText().toString());
        } catch (Exception e) {
            return -1;
        }
    }

    @Override
    public float getTargetAmount() {
        try {
            return Float.valueOf(mTargetAmountInput.getText().toString());
        } catch (Exception e) {
            return -1;
        }
    }

    @Override
    public void setAvailableCurrencies(List<Currency> currencies) {
        mSourceNameSpinner.setAdapter(new CurrencyNamesSpinnerAdapter(this, currencies));
        mTargetNameSpinner.setAdapter(new CurrencyNamesSpinnerAdapter(this, currencies));
    }

    @Override
    public void setSourceCurrencyIdx(int idx) {
        if (mSourceNameSpinner.getSelectedItemPosition() != idx) {
            mSourceListener.lockForUpdate();
            mSourceNameSpinner.setSelection(idx);
        }
    }

    @Override
    public void setTargetCurrencyIdx(int idx) {
        if (mTargetNameSpinner.getSelectedItemPosition() != idx) {
            mTargetListener.lockForUpdate();
            mTargetNameSpinner.setSelection(idx);
        }
    }

    @Override
    public int getSourceCurrencyIdx() {
        return mSourceNameSpinner.getSelectedItemPosition();
    }

    @Override
    public int getTargetCurrencyIdx() {
        return mTargetNameSpinner.getSelectedItemPosition();
    }

    @Override
    public Context getContext() {
        return this;
    }

    @Override
    public void showProgress(boolean show) {
        mRefreshLayout.setRefreshing(show);
    }

    @Override
    public void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onDestroy() {
        mPresenter.destroy();
        super.onDestroy();
    }

    @Override
    public void showRate(Currency source, float sourceAmount, Currency target, float targetAmount) {
        mSourceCurrencyTextView.setText(getString(R.string.source_rate_text,
                Util.roundToDigits(sourceAmount, source.getDigits()), source.getFullName(), source.getISOCode()));
        mTargetCurrencyTextView.setText(getString(R.string.target_rate_text,
                Util.roundToDigits(targetAmount, target.getDigits()), target.getFullName(), target.getISOCode()));
    }

    private class AmountTextWatcher implements TextWatcher {
        private boolean isUpdating;

        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void afterTextChanged(Editable editable) {
            if (isUpdating) {
                isUpdating = false;
                return;
            }

            mPresenter.update(false);
        }

        void lockForUpdate() {
            isUpdating = true;
        }
    }

    private class OneShotSpinnerListener implements AdapterView.OnItemSelectedListener {
        private boolean isUpdating;

        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
            if (isUpdating) {
                isUpdating = false;
                return;
            }

            mPresenter.update(false);
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {
            isUpdating = false;
        }

        void lockForUpdate() {
            isUpdating = true;
        }
    }
}
