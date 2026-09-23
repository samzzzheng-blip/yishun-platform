package com.techtron.onebook.module.app.controller.app.auction.vo;

import jakarta.validation.Validation;
import org.hibernate.validator.messageinterpolation.ParameterMessageInterpolator;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

class AppAuctionCreateReqVOTest {
    @Test
    void onlyOneYuanStartingPriceIsAccepted() {
        try (var factory = Validation.byDefaultProvider().configure()
                .messageInterpolator(new ParameterMessageInterpolator()).buildValidatorFactory()) {
            var request = new AppAuctionCreateReqVO();
            for (Integer price : new Integer[]{null, -1, 0, 1, 99, 100, 101, 100000000}) {
                request.setStartPrice(price);
                assertEquals(Integer.valueOf(100).equals(price),
                        factory.getValidator().validateProperty(request, "startPrice").isEmpty(),
                        "starting price in cents: " + price);
            }
        }
    }
}
