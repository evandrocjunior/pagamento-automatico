package com.script.model;

import java.math.BigDecimal;

public record InvoicePayment(String faturaId, BigDecimal valor) {
}
