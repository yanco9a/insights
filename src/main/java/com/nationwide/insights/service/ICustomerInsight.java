package com.nationwide.insights.service;

import java.math.BigDecimal;

public interface ICustomerInsight {
    void setSpendByCategory(BigDecimal amount);
    void generateHigherSpend(BigDecimal more);
    void generateLowerSpend(BigDecimal less);
}
