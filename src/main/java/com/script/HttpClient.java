package com.script;

import com.script.model.ConfigurationDto;
import com.script.model.Invoice;
import com.script.model.InvoicePayment;
import com.script.model.Paid;
import feign.Body;
import java.util.List;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "httpClients", url = "https://nifi.jazztech.com.br/reserve-credit/v1.0")
public interface HttpClient {


    @GetMapping(path = "/flow")
    ConfigurationDto getConfigurationData(@RequestParam("hashKey") String hashKey, @RequestParam("sortKey") String sortKey);

    @GetMapping(path = "/account-key/{accountKey}/invoices")
    List<Invoice> getInvoice(@PathVariable("accountKey") String accountKey);

    @PostMapping(path = "/account-key/{accountKey}/payment/account-debit")
    Paid payment(@PathVariable("accountKey") String accountKey, InvoicePayment invoicePayment);

}
