package com.marwinxxii.reactiveui;

import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.text.TextUtils;
import com.marwinxxii.reactiveui.network.SearchRequest;
import com.marwinxxii.reactiveui.network.SearchRequest.DealType;
import com.marwinxxii.reactiveui.network.SearchRequest.PropertyType;

public final class FiltersHelper {
    private FiltersHelper() {
    }

    public static SearchRequest buildRequest(int dealTypeId, int propertyTypeId, PriceRange price) {
        return new SearchRequest(
            R.id.deal_type_buy == dealTypeId ? DealType.BUY : DealType.RENT,
            getProperty(propertyTypeId),
            price
        );
    }

    public static boolean validatePrice(CharSequence price) {
        if (!TextUtils.isEmpty(price)) {
            String s = price.toString();
            try {
                int value = Integer.parseInt(s, 10);
                if (value <= 0) {
                    return false;
                }
            } catch (NumberFormatException e) {
                return false;
            }
        }
        return true;
    }

    public static Integer convertPrice(CharSequence value) {
        return TextUtils.isEmpty(value) ? null : Integer.parseInt(value.toString(), 10);
    }

    @Nullable
    public static PriceRange processPriceRange(Integer from, Integer to, FiltersView filters) {
        if (from == null || to == null || from <= to) {
            handlePriceError(false, filters.getPriceFrom());
            handlePriceError(false, filters.getPriceTo());
            filters.getApplyButton().setEnabled(true);
            return new PriceRange(from, to);
        } else {
            boolean fromGreater = from > to;
            handlePriceError(fromGreater, filters.getPriceFrom());
            handlePriceError(!fromGreater, filters.getPriceTo());
            filters.getApplyButton().setEnabled(false);
            return null;
        }
    }
    
    public static void handlePriceError(boolean isError, TextInputLayout layout) {
        layout.setError(isError ? layout.getContext().getString(R.string.price_error) : null);
    }

    private static PropertyType getProperty(int propertyIndex) {
        switch (propertyIndex) {
            case 2:
                return PropertyType.HOUSE;
            case 1:
                return PropertyType.ROOM;
            case 0:
            default:
                return PropertyType.APT;
        }
    }
}
