package com.nationwide.insights.service;

import com.nationwide.insights.domain.Insight;
import com.nationwide.insights.domain.transactions.Transactions;

import java.util.List;
import java.util.Map;

public interface IBillTracking {
    List<Insight> billTrackingInsight(Map<String, List<Transactions>> transactionByVendor);
}
