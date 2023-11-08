package com.script.model;

public record ConfigurationDto(AccountConfiguration accountConfiguration, String taxId, String accountKey) {
    public record AccountConfiguration(String invoiceDueDay, boolean isAutomaticDebit ) {

    }
}
