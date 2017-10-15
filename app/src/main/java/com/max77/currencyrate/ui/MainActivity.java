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

import com.max77.currencyrate.R;
import com.max77.currencyrate.datamodel.Currency;

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

    private TextWatcher mAmountTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void afterTextChanged(Editable editable) {
            mPresenter.update();
        }
    };

    private AdapterView.OnItemSelectedListener mCurrencySpinnerListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
            mPresenter.update();
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
        mPresenter.restoreStateOrInitialize(savedInstanceState);
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
        mRefreshLayout.setOnRefreshListener(() -> mPresenter.forceUpdate());
    }

    @Override
    public MainState getState() {
        MainState state = new MainState();

        try {
            state.setSourceAmount(Float.valueOf(mSourceCurrencyAmountInput.getText().toString()));
            state.setTargetAmount(Float.valueOf(mTargetCurrencyAmountInput.getText().toString()));
            state.getConversionRateInfo().setSourceCurrency(getSelectedCurrency(mSourceCurrencyNameSpinner));
            state.getConversionRateInfo().setTargetCurrency(getSelectedCurrency(mTargetCurrencyNameSpinner));
        } catch (Exception e) {
        }

        return state;
    }

    @Override
    public Context getContext() {
        return this;
    }

    @Override
    protected void onDestroy() {
        mPresenter.destroy();
        super.onDestroy();
    }

    @Override
    public void setState(MainState state) {
        // blocking callbacks to avoid recursion
        mSourceCurrencyAmountInput.removeTextChangedListener(mAmountTextWatcher);
        mTargetCurrencyAmountInput.removeTextChangedListener(mAmountTextWatcher);

        mSourceCurrencyNameSpinner.setOnItemSelectedListener(null);
        mTargetCurrencyNameSpinner.setOnItemSelectedListener(null);

        // setting data
        mDateTextView.setText(Util.getPrettyDate(this, state.getConversionRateInfo().getDate()));

        mSourceCurrencyAmountInput.setText(String.valueOf(state.getSourceAmount()));
        mTargetCurrencyAmountInput.setText(String.valueOf(state.getTargetAmount()));

        mSourceCurrencyNameSpinner.setAdapter(new CurrencyNamesSpinnerAdapter(this, state.getAvailableCurrencies()));
        mTargetCurrencyNameSpinner.setAdapter(new CurrencyNamesSpinnerAdapter(this, state.getAvailableCurrencies()));

        int pos1 = ((CurrencyNamesSpinnerAdapter) mSourceCurrencyNameSpinner.getAdapter())
                .getPosition(state.getConversionRateInfo().getSourceCurrency());

        mSourceCurrencyNameSpinner.setSelection(pos1);

        int pos2 = ((CurrencyNamesSpinnerAdapter) mTargetCurrencyNameSpinner.getAdapter())
                .getPosition(state.getConversionRateInfo().getTargetCurrency());

        mTargetCurrencyNameSpinner.setSelection(pos2);

        mSourceCurrencyTextView.setText(getString(R.string.source_rate_text,
                state.getSourceAmount(),
                state.getConversionRateInfo().getSourceCurrency().getFullName(),
                state.getConversionRateInfo().getSourceCurrency().getISOCode()));
        mTargetCurrencyTextView.setText(getString(R.string.target_rate_text,
                state.getTargetAmount(),
                state.getConversionRateInfo().getTargetCurrency().getFullName(),
                state.getConversionRateInfo().getTargetCurrency().getISOCode()));

        // restoring callbacks
        mSourceCurrencyAmountInput.addTextChangedListener(mAmountTextWatcher);
        mTargetCurrencyAmountInput.addTextChangedListener(mAmountTextWatcher);

        mSourceCurrencyNameSpinner.setOnItemSelectedListener(mCurrencySpinnerListener);
        mTargetCurrencyNameSpinner.setOnItemSelectedListener(mCurrencySpinnerListener);
    }

    private Currency getSelectedCurrency(Spinner spinner) {
        return ((CurrencyNamesSpinnerAdapter) spinner.getAdapter()).getItem(spinner.getSelectedItemPosition());
    }
}
