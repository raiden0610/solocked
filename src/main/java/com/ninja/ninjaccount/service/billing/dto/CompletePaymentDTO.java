package com.ninja.ninjaccount.service.billing.dto;

import javax.validation.constraints.NotNull;

public class CompletePaymentDTO {

    @NotNull
    private String paymentId;

    @NotNull
    private String payerId;

    @NotNull
    private String token;

    public String getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(String paymentId) {
        this.paymentId = paymentId;
    }

    public String getPayerId() {
        return payerId;
    }

    public void setPayerId(String payerId) {
        this.payerId = payerId;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    @Override
    public String toString() {
        return "CompletePaymentDTO{" +
            "paymentId='" + paymentId + '\'' +
            ", payerId='" + payerId + '\'' +
            '}';
    }
}
