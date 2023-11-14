package com.script.model;

import java.math.BigDecimal;

public record Credit(BigDecimal valorInvestido, BigDecimal limiteCreditoAtual, BigDecimal valorGarantia) {
}
