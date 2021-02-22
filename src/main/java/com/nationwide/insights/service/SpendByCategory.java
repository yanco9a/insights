package com.nationwide.insights.service;

import com.nationwide.insights.domain.Insight;
import com.nationwide.insights.domain.transactions.Transactions;

import java.math.BigDecimal;
import java.util.List;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toUnmodifiableList;

public class SpendByCategory implements ISpendByCategory, ITransform {
    public static final String EXEMPT_FROM_SPENDING_CATEGORY = "bill";

    @Override
    public List<Insight> spendByCategoryInsight(List<Transactions> transactions) {
//        return getTransactionsFromThisMth(CURRENT_MONTH, transactions)
        return transactions.stream().collect(groupingBy(Transactions::getCategory)).entrySet().stream()
                .filter(catEntry -> !catEntry.getKey().equalsIgnoreCase(EXEMPT_FROM_SPENDING_CATEGORY))
                .map(catEntry -> {
                    BigDecimal amount = catEntry.getValue().stream()
                            .map(Transactions::getAmount)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    return catEntry.getValue().stream()
                            .map(transact -> {
                                return setInsightFromTransaction(amount, transact);
                            })
                            .flatMap(insight -> insight.getInsights().stream())
                            .distinct()
                            .collect(toList());
                }).flatMap(insights -> insights.stream())
                .collect(toUnmodifiableList());
    }

    private CustomerInsight setInsightFromTransaction(BigDecimal amount, Transactions transact) {
        CustomerInsight insight = mapToInsightFrom(transact);
        insight.setSpendByCategory(amount);
        return insight;
    }

    @Override
    public CustomerInsight mapToInsightFrom(Transactions transact) {
        CustomerInsight cInsight = new CustomerInsight();
        cInsight.setAmount(transact.getAmount());
        cInsight.setDate(transact.getDate());
        cInsight.setCategory(transact.getCategory());
        cInsight.setVendor(transact.getVendor());
        return cInsight;
    }
}
