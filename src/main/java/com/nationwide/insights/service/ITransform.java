package com.nationwide.insights.service;

import com.nationwide.insights.domain.transactions.Transactions;

public interface ITransform {
    CustomerInsight mapToInsightFrom(Transactions transact);
}
