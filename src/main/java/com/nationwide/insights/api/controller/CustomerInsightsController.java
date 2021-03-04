package com.nationwide.insights.api.controller;

import com.nationwide.insights.domain.Insight;
import com.nationwide.insights.service.CustomerInsightsService;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.Positive;
import java.util.List;

import static java.lang.String.format;
import static org.slf4j.LoggerFactory.getLogger;
import static org.springframework.http.ResponseEntity.ok;

@Validated
@RestController
@RequestMapping(value = "/api/v1/customer/insights", produces = "application/json;charset=UTF-8", consumes = "application/json;charset=UTF-8")
public class CustomerInsightsController {
    private static final Logger LOG = getLogger(CustomerInsightsController.class.getCanonicalName());
    private final CustomerInsightsService service;

    @Autowired
    public CustomerInsightsController(CustomerInsightsService service) {
        this.service = service;
    }

    @GetMapping("{id}")
    public ResponseEntity<List<Insight>> customerInsightsById(@PathVariable("id")
                                                              @Positive(message = "Customer id must be greater than 0") Long id) {
        LOG.info(format("preparing to return customer insight(s) by id: %s", id));
        List<Insight> insights = service.customerInsightsById(id);
        LOG.info(format("insights retrieved. insights: %s", insights));
        return ok(insights);
    }
}
