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
    private EditText mSourceCurrencyAmountInput;
    private EditText mTargetCurrencyAmountInput;
    private Spinner mSourceCurrencyNameSpinner;
    private Spinner mTargetCurrencyNameSpinner;

    private MainPresenter mPresenter;

    private boolean isAmountUpdating;

    private TextWatcher mAmountTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void afterTextChanged(Editable editable) {
            if (isAmountUpdating) {
                isAmountUpdating = false;
                return;
            }
            mPresenter.update(false);
        }
    };

    private boolean isCurrencyUpdating;

    private AdapterView.OnItemSelectedListener mCurrencySpinnerListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
            if (isCurrencyUpdating) {
                isCurrencyUpdating = false;
                return;
            }

            mPresenter.update(false);
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        preventKeypadFromHiding();
        initControls();

        mPresenter = new MainPresenter(this, "SEK", "USD");
        mPresenter.init(savedInstanceState);
    }

    private void preventKeypadFromHiding() {
        InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
    }

    private void initControls() {
        mDateTextView = findViewById(R.id.date);
        mSourceCurrencyTextView = findViewById(R.id.source_text);
        mTargetCurrencyTextView = findViewById(R.id.target_text);
        mSourceCurrencyAmountInput = findViewById(R.id.source_amount);
        mTargetCurrencyAmountInput = findViewById(R.id.target_amount);
        mSourceCurrencyNameSpinner = findViewById(R.id.source_currency);
        mTargetCurrencyNameSpinner = findViewById(R.id.target_currency);

        mRefreshLayout = findViewById(R.id.refreshLayout);
        mRefreshLayout.setOnRefreshListener(() -> mPresenter.update(true));

        mSourceCurrencyAmountInput.addTextChangedListener(mAmountTextWatcher);
        mTargetCurrencyAmountInput.addTextChangedListener(mAmountTextWatcher);

        mSourceCurrencyNameSpinner.setOnItemSelectedListener(mCurrencySpinnerListener);
        mTargetCurrencyNameSpinner.setOnItemSelectedListener(mCurrencySpinnerListener);
    }

    @Override
    public void setDate(Date date) {
        mDateTextView.setText(Util.getPrettyDate(this, date));
    }

    @Override
    public void setAmount(boolean target, float amount) {
        isAmountUpdating = true;
        (target ? mTargetCurrencyAmountInput : mSourceCurrencyAmountInput)
                .setText(amount <= 0 ? "" : String.valueOf(amount));
    }

    @Override
    public float getAmount(boolean target) {
        try {
            return Float.valueOf((target ? mTargetCurrencyAmountInput : mSourceCurrencyAmountInput)
                    .getText().toString());
        } catch (Exception e) {
            return 0;
        }
    }

    @Override
    public void setAvailableCurrencies(List<Currency> currencies) {
        mSourceCurrencyNameSpinner.setAdapter(new CurrencyNamesSpinnerAdapter(this, currencies));
        mTargetCurrencyNameSpinner.setAdapter(new CurrencyNamesSpinnerAdapter(this, currencies));
    }

    @Override
    public void setCurrencyIdx(boolean target, int idx) {
        isCurrencyUpdating = true;
        (target ? mTargetCurrencyNameSpinner : mSourceCurrencyNameSpinner).setSelection(idx);
    }

    @Override
    public int getCurrencyIdx(boolean target) {
        return (target ? mTargetCurrencyNameSpinner : mSourceCurrencyNameSpinner).getSelectedItemPosition();
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

//        mSourceCurrencyTextView.setText(getString(R.string.source_rate_text,
//                state.getSourceAmount(),
//                state.getConversionRateInfo().getSourceCurrency().getFullName(),
//                state.getConversionRateInfo().getSourceCurrency().getISOCode()));
//        mTargetCurrencyTextView.setText(getString(R.string.target_rate_text,
//                state.getTargetAmount(),
//                state.getConversionRateInfo().getTargetCurrency().getFullName(),
//                state.getConversionRateInfo().getTargetCurrency().getISOCode()));
}
