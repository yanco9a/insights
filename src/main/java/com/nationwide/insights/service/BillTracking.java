package com.nationwide.insights.service;

import com.nationwide.insights.domain.Insight;
import com.nationwide.insights.domain.transactions.Transactions;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static java.math.RoundingMode.CEILING;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toUnmodifiableList;

public class BillTracking implements IBillTracking, ITransform {
    private final IGenerateInsight cInsight;

    public BillTracking(IGenerateInsight cInsight) {
        this.cInsight = cInsight;
    }

    @Override
    public List<Insight> billTrackingInsight(Map<String, List<Transactions>> transactionByVendor) {

        Map<String, List<CustomerInsight>> mapInsightsByVendor = transformToInsightGroup(transactionByVendor);

        return mapInsightsByVendor.entrySet().stream()
                .map(Map.Entry::getValue)
                .map(this::combineLikenedInsights)
                .map(this::generateThisRecentInsight)
                .filter(Objects::nonNull)
                .flatMap(Collection::stream)
                .flatMap(ci -> ci.getInsights().stream())
                .collect(toUnmodifiableList());
    }

    private Map<String, List<CustomerInsight>> transformToInsightGroup(Map<String, List<Transactions>> transactionByVendor) {
        Map<String, List<CustomerInsight>> mapInsightsByVendor =
                transactionByVendor.entrySet().stream()
                        .flatMap(entry -> entry.getValue().stream()
                                .map(this::mapToInsightFrom))
                        .sorted(comparing(CustomerInsight::getDate).reversed())
                        .collect(groupingBy(CustomerInsight::getVendor, LinkedHashMap::new, toUnmodifiableList()));
        return mapInsightsByVendor;
    }

    private List<CustomerInsight> combineLikenedInsights(List<CustomerInsight> dateOrderedVendorList) {
        Map<String, List<CustomerInsight>> sameBill =
                dateOrderedVendorList.stream()
                        .sorted(comparing(CustomerInsight::getDate).reversed())
                        .collect(groupingBy(CustomerInsight::getVendor, LinkedHashMap::new, toUnmodifiableList()));

        return sameBill.values().stream()
                .flatMap(catEntry -> {
                    return catEntry.stream()
                            .skip(1)
                            .reduce((mostRecent, next) -> {
                                BigDecimal add = mostRecent.getAmount().add(next.getAmount());
                                // not sure about precision here as the spec omits precision
                                mostRecent.setAmount(add.divide(BigDecimal.valueOf(2L), CEILING));
                                return mostRecent;
                            }).stream().map(mostRecent -> {
                                ArrayList<CustomerInsight> insights = new ArrayList<>();
                                insights.add(catEntry.get(0));
                                insights.add(mostRecent);
                                return insights;
                            }).flatMap(Collection::stream);
                })
                .sorted(comparing(CustomerInsight::getDate).reversed())
                .collect(toUnmodifiableList());
    }

    public List<CustomerInsight> generateThisRecentInsight(List<CustomerInsight> list) {
        return list.stream()
                .sorted(comparing(CustomerInsight::getDate).reversed())
                .reduce((recent, past) -> generateThisInsight(recent, past))
                .stream()
                .collect(toUnmodifiableList());
    }

    public CustomerInsight generateThisInsight(CustomerInsight recent, CustomerInsight past) {
        return cInsight.generateInsightFrom(recent, past);
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
