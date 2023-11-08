package com.script.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public record Invoice(LocalDate vencimento, BigDecimal valor, String statusFatura, String faturaId, String accountKey) {

    public Invoice addAccountKey(String accountKey) {
        return new Invoice(this.vencimento, this.valor, this.statusFatura, this.faturaId, accountKey);
    }

    @Override
    public String toString() {
        return accountKey + "," + vencimento.toString() + "," + valor + "," + faturaId + "\n";
    }
}
