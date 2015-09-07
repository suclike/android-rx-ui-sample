package com.marwinxxii.reactiveui;

import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.view.View;
import android.widget.TextView;

import com.jakewharton.rxbinding.view.RxView;
import com.jakewharton.rxbinding.widget.RxAdapterView;
import com.jakewharton.rxbinding.widget.RxRadioGroup;
import com.jakewharton.rxbinding.widget.RxTextView;
import com.marwinxxii.reactiveui.entities.SearchRequest;
import com.marwinxxii.reactiveui.network.ApiStub;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;

public class RxFiltersController implements IFiltersController {
    private Subscription mSubscription;

    @Override
    public void init(FiltersView filters, TextView offersView) {
        mSubscription = Observable.combineLatest(
            RxRadioGroup.checkedChanges(filters.getDealType()),
            RxAdapterView.itemSelections(filters.getPropertyType()),

            Observable.combineLatest(
                RxTextView.textChanges(filters.getPriceFrom().getEditText())
                    .filter(filterProcessPrice(filters, filters.getPriceFrom()))
                    .map(FiltersHelper::convertPrice),

                RxTextView.textChanges(filters.getPriceTo().getEditText())
                    .filter(filterProcessPrice(filters, filters.getPriceTo()))
                    .map(FiltersHelper::convertPrice),

                (from, to) -> FiltersHelper.processPriceRange(from, to, filters)
            ).filter(pr -> pr != null),

            FiltersHelper::buildRequest
        )
            .flatMap(req -> {
                return Observable.merge(
                    ApiStub.offersCountForFilter(req.getDeal(), req.getProperty(), req.getPrice())
                        .doOnError(error -> offersView.setVisibility(View.GONE))
                        .onErrorResumeNext(Observable.empty())
                        .observeOn(AndroidSchedulers.mainThread()),

                    RxView.clicks(filters.getApplyButton()).map((Object o) -> req)
                )
                    .doOnNext(event -> {
                        if (event instanceof Integer) {
                            FiltersHelper.setOffersCount(offersView, (int) event);
                        }
                    })
                    .ofType(SearchRequest.class);
            })
            .subscribe(request -> {
                Snackbar.make(filters, R.string.filters_applied, Snackbar.LENGTH_SHORT).show();
            });
    }

    @Override
    public void onStop() {
        mSubscription.unsubscribe();
    }

    private static Func1<CharSequence, Boolean> filterProcessPrice(FiltersView filters, TextInputLayout priceView) {
        return price -> {
            boolean isError = !FiltersHelper.validatePrice(price);
            FiltersHelper.handlePriceError(isError, priceView);
            filters.getApplyButton().setEnabled(!isError);
            return !isError;
        };
    }
}
