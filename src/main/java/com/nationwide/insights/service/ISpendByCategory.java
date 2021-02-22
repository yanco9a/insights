package com.nationwide.insights.service;

import com.nationwide.insights.domain.Insight;
import com.nationwide.insights.domain.transactions.Transactions;

import java.util.List;

public interface ISpendByCategory {
    List<Insight> spendByCategoryInsight(List<Transactions> transactions);
}
