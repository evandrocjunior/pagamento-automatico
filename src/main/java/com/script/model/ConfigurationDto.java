package com.script.model;

public record ConfigurationDto(AccountConfiguration accountConfiguration, String taxId, String accountKey) {

    @Override
    public String toString() {
        return String.join(",",taxId, accountKey, String.valueOf(accountConfiguration.isAutomaticDebit));
    }

    public record AccountConfiguration(String invoiceDueDay, boolean isAutomaticDebit ) {

    }
}
