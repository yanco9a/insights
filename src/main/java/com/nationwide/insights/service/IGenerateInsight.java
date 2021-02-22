package com.nationwide.insights.service;

import com.nationwide.insights.domain.Insight;

import java.util.List;

public interface IGenerateInsight {
    CustomerInsight generateInsightFrom(CustomerInsight recent, CustomerInsight past);
    List<Insight> getInsights();
}
