package com.nationwide.insights.service;

import com.nationwide.insights.api.exception.TransactionNotFoundException;
import com.nationwide.insights.domain.Insight;
import com.nationwide.insights.domain.transactions.TransactionRepository;
import com.nationwide.insights.domain.transactions.Transactions;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static java.time.LocalDate.now;
import static java.time.temporal.ChronoUnit.MONTHS;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import static org.slf4j.LoggerFactory.getLogger;

@Service
public class CustomerInsightsService implements IInsightType {
    private static final Logger LOG = getLogger(CustomerInsightsService.class.getCanonicalName());
    public static final int LAST_12_MONTHS = 12;
    public static final int CURRENT_MONTH = 0;
    private final TransactionRepository repository;
    private List<Transactions> transactions;
    private IGenerateInsight cInsight;


    @Autowired
    public CustomerInsightsService(TransactionRepository repository, IGenerateInsight cInsight) {
        this.repository = repository;
        this.cInsight = cInsight;
        this.transactions = new ArrayList<>();
    }

    public List<Insight> customerInsightsById(Long id) {
        transactions = repository.findAllByCustomerId(id);
        if (transactions.isEmpty()) {
            throw new TransactionNotFoundException(id);
        }
        return Stream.of(billTrackingInsight(transactions),
                spendByCategoryInsight(transactions))
                .flatMap(Collection::stream)
                .collect(toList());
    }

    private List<Insight> spendByCategoryInsight(List<Transactions> transactions) {
        spendByCategory = new SpendByCategory();
        List<Transactions> transactionsFromThisMth = getTransactionsFromThisMth(CURRENT_MONTH, transactions);
        return spendByCategory.spendByCategoryInsight(transactionsFromThisMth);
    }

    private List<Insight> billTrackingInsight(List<Transactions> transactions) {
        billTracking = new BillTracking(cInsight);
        Map<String, List<Transactions>> transactionByVendor =
                groupByVendor(getTransactionsFromThisMth(LAST_12_MONTHS, transactions));
        return billTracking.billTrackingInsight(transactionByVendor);
    }

    private Map<String, List<Transactions>> groupByVendor(List<Transactions> transactions) {
        return transactions.stream().collect(groupingBy(Transactions::getVendor));
    }

    private List<Transactions> getTransactionsFromThisMth(long noOfMthsBack, List<Transactions> transactions) {
        return transactions.stream()
                .filter(transact -> {
                    long noOfMths = MONTHS.between(transact.getDate(), LocalDate.now().plusMonths(1));
                    return noOfMths >= 0 && noOfMths <= noOfMthsBack + 1;
                }).collect(toList());
    }
}