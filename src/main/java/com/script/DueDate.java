package com.script;

import com.script.model.ConfigurationDto;
import com.script.model.Invoice;
import com.script.model.InvoicePayment;
import com.script.model.Paid;
import feign.FeignException;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.time.Month;
import java.time.Year;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class DueDate {
    private static final Logger LOGGER = LoggerFactory.getLogger(DueDate.class);
    public static final String DELIMITED = ",";
    private final HttpClient httpClient;

    Path pathClientsFile = Paths.get("src", "main", "resources", "files", "clients");
    Path pathAccountKeys = Paths.get("src", "main", "resources", "files", "accountKeys");
    Path pathNotFoundFile = Paths.get("src", "main", "resources", "files", "cpfNotFound");
    Path automaticDebitFile = Paths.get("src", "main", "resources", "files", "automaticDebit");
    Path invoicePaid = Paths.get("src", "main", "resources", "files", "invoicePaid");
    Path creditAccountPath = Paths.get("src", "main", "resources", "files", "creditAccount");
    Path creditAccountNotFoundPath = Paths.get("src", "main", "resources", "files", "creditAccountNotFound");

    public DueDate(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public void performInvoicePayment(LocalDate date) throws IOException {
        getAccounts();
        filterAutomaticDebit(date);
        paymentInvoice();
    }


    public void getAccounts() throws IOException {
        List<String> clients = Files.readAllLines(pathClientsFile);

        if (!Files.exists(pathAccountKeys)) {
            Files.createFile(pathAccountKeys);
            Files.createFile(pathNotFoundFile);
        }

        clients.forEach(client -> {
            try {
                ConfigurationDto configurationData =
                        httpClient.getConfigurationData(client.replaceAll(DELIMITED, "#"), "status#configuration-completed");
                Files.writeString(pathAccountKeys, configurationData.toString() + System.lineSeparator(), StandardOpenOption.APPEND);
            } catch (FeignException.NotFound exception) {
                try {
                    Files.writeString(pathNotFoundFile, client + "\n", StandardOpenOption.APPEND);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        LOGGER.info("PROCESS FINISH");
    }

    public void filterAutomaticDebit(LocalDate date) throws IOException {
        List<String> clients = Files.readAllLines(pathAccountKeys);

        if (!Files.exists(automaticDebitFile)) {
            Files.createFile(automaticDebitFile);
        }

        clients.forEach(client -> {
            String accountKey = client.split(DELIMITED)[1];
            boolean automaticDebit = Boolean.parseBoolean(client.split(DELIMITED)[2]);
            if (automaticDebit) {
                try {
                    List<Invoice> invoices = httpClient.getInvoice(accountKey);
                    List<Invoice> invoicesFilter = invoices.stream()
                            .filter(invoice -> (invoice.vencimento().isEqual(date))).toList();
                    if (invoicesFilter.isEmpty()) {
                        LOGGER.error(client);
                    }
                    List<Invoice> list = invoicesFilter.stream().map(invoice -> invoice.addAccountKey(accountKey)).toList();
                    list.forEach(invoice -> {
                        try {
                            Files.writeString(automaticDebitFile, invoice.toString(), StandardOpenOption.APPEND);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });
                } catch (FeignException.NotFound notFound) {
                    LOGGER.error(client);
                }
            }
        });

        LOGGER.info("PROCESS FINISH");
        System.exit(0);
    }

    public void paymentInvoice() throws IOException {
        List<String> clients = Files.readAllLines(automaticDebitFile);

        if (!Files.exists(invoicePaid)) {
            Files.createFile(invoicePaid);
        }

        clients.forEach(client -> {
            String accountKey = client.split(DELIMITED)[0];
            BigDecimal valor = new BigDecimal(client.split(DELIMITED)[2]);
            String faturaId = client.split(DELIMITED)[3];
            if (valor.compareTo(BigDecimal.ZERO) > 0) {
                try {
                    Paid payment = httpClient.payment(accountKey, new InvoicePayment(faturaId, valor));
                    LOGGER.info(payment.pagamentoId());
                    try {
                        Files.writeString(invoicePaid, String
                                        .join(DELIMITED, accountKey, faturaId, valor.toString(), payment.pagamentoId() + System.lineSeparator()),
                                StandardOpenOption.APPEND);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                } catch (FeignException exception) {
                    LOGGER.error(accountKey);
                    LOGGER.error(exception.getMessage());
                }
            } else {
                LOGGER.info("accountKey %s com valor %s não será paga".formatted(accountKey, valor));
            }
        });
        LOGGER.info("#### Finalizado pagamentos de fatuas em debito automático ####");
        System.exit(0);
    }

    public void getCredit() throws IOException {
        List<String> clients = Files.readAllLines(pathAccountKeys);

        if (!Files.exists(creditAccountPath)) {
            Files.createFile(creditAccountPath);
            Files.createFile(creditAccountNotFoundPath);
        }
        Files.writeString(creditAccountPath, "accountKey,valorInvestido,limiteCreditoAtual,valorGarantia" + "\n", StandardOpenOption.APPEND);

        clients.forEach(client -> {
            try {
                String accountKey = client.split(DELIMITED)[1];
                var credit = httpClient.getCredit(accountKey);

                Files.writeString(creditAccountPath, accountKey + DELIMITED
                        + credit.valorInvestido() + DELIMITED
                        + credit.limiteCreditoAtual() + DELIMITED
                        + credit.valorGarantia() + "\n", StandardOpenOption.APPEND);
            } catch (FeignException.NotFound exception) {
                try {
                    Files.writeString(creditAccountNotFoundPath, client + "\n", StandardOpenOption.APPEND);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        LOGGER.info("PROCESS FINISH");
    }
}
