package com.max77.currencyrate.datamodel;

/**
 * Created by mkomarovskiy on 13/10/2017.
 */

public class Currency {
    private String mISOCode = "";
    private String mFullName = "";
    private int mDigits;

    public Currency() {
    }

    public Currency(String ISOCode, String fullName, int digits) {
        mISOCode = ISOCode;
        mFullName = fullName;
        mDigits = digits;
    }

    public String getISOCode() {
        return mISOCode;
    }

    public String getFullName() {
        return mFullName;
    }

    public int getDigits() {
        return mDigits;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Currency currency = (Currency) o;

        if (!getISOCode().equals(currency.getISOCode())) return false;
        return getFullName().equals(currency.getFullName());
    }

    @Override
    public int hashCode() {
        int result = getISOCode().hashCode();
        result = 31 * result + getFullName().hashCode();
        return result;
    }
}
