package com.max77.currencyrate.repository;

/**
 * Created by mkomarovskiy on 14/10/2017.
 * Repository request result wrapper
 */

public class RepositoryResponse<T> {
    private T mPayload;
    private boolean isCached;

    public RepositoryResponse(T payload, boolean isCached) {
        mPayload = payload;
        this.isCached = isCached;
    }

    public T getPayload() {
        return mPayload;
    }

    public boolean isCached() {
        return isCached;
    }
}
