package com.nationwide.insights.service;

import com.nationwide.insights.domain.Insight;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import static com.nationwide.insights.service.InsightType.BILL_TRACKING;
import static com.nationwide.insights.service.InsightType.OUTGOINGS;
import static com.nationwide.insights.service.InsightType.SPEND_BY_CATEGORY;
import static java.lang.String.format;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.stream.Collectors.toList;
import static org.slf4j.LoggerFactory.getLogger;

@Component
public class CustomerInsight implements ICustomerInsight, IGenerateInsight {
    private static final Logger LOG = getLogger(CustomerInsight.class.getCanonicalName());
    public static final String MORE = "more";
    public static final String LESS = "less";
    public static final String REQUESTED_FORMAT = "%.0f";
    private BigDecimal amount;
    private String category;
    private String vendor;
    private LocalDate date;
    private List<String> insights;
    private Map<InsightType, List<String>> insightMap;
    private String insight;
    private InsightType type;

    public CustomerInsight() {
        insights = new ArrayList<>();
        insightMap = new LinkedHashMap<>();
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category.toLowerCase();
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getVendor() {
        return vendor;
    }

    public void setVendor(String vendor) {
        this.vendor = vendor;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public void setInsights(List<String> insights) {
        this.insights = insights;
    }

    public void setInsight(String insight) {
        this.insight = insight;
    }

    public InsightType getType() {
        return type;
    }

    public void setType(InsightType type) {
        this.type = type;
    }

    @Override
    public void generateHigherSpend(BigDecimal more) {
        setTracking(MORE, more);
    }

    @Override
    public void generateLowerSpend(BigDecimal less) {
        setTracking(LESS, less);
    }

    @Override
    public void setSpendByCategory(BigDecimal updatedAmount) {
        setAmount(updatedAmount);
        String insight = format(Locale.UK, "You've spent %s%.0f in %s this month", getUKPoundSign(), getAmount().abs(), getCategory());
        setInsightDetails(SPEND_BY_CATEGORY, insight);
//        LOG.info(format("category: %s | amount: %s ", this.getCategory(), updatedAmount));
    }

    private String getUKPoundSign() {
        return new String("£".getBytes(UTF_8), UTF_8);
    }

    private String setTracking(String trackingStyle, BigDecimal lessOrMore) {
        String insightStr;
        if (this.getCategory().equalsIgnoreCase("bill")) {
            insightStr = format("Your latest %s %s is %s%.0f %s than previous months",
                    this.getVendor(),
                    this.getCategory(),
                    this.getUKPoundSign(),
                    lessOrMore.abs(),
                    trackingStyle);
            setInsightDetails(BILL_TRACKING, insightStr);
//            LOG.info(format("category: %s | %s %s ", this.getCategory(), this.getAmount(), trackingStyle));
        } else {
            insightStr = format("Your latest %s spend in %s is %s%.0f %s than previous months",
                    this.getCategory(),
                    this.getVendor(),
                    this.getUKPoundSign(),
                    lessOrMore.abs(),
                    trackingStyle);
            setInsightDetails(OUTGOINGS, insightStr);
        }
//        LOG.info(format("category: %s | %s %s ", this.getCategory(), this.getAmount(), trackingStyle));
        return insightStr;
    }

    @Override
    public List<Insight> getInsights() {
        return this.insightMap.entrySet().stream()
                .flatMap(e -> e.getValue().stream().map(Insight::new))
                .collect(toList());
    }

    public void setInsightDetails(InsightType type, String insight) {
        this.type = type;
        this.setInsight(insight);
        this.insights.add(insight);
        this.setInsights(this.insights);
        this.setInsightMap(type, this.insights);
    }

    @Override
    public CustomerInsight generateInsightFrom(CustomerInsight recent, CustomerInsight next) {
        if (recent.getAmount().compareTo(next.getAmount()) == 1) {
            recent.generateHigherSpend(recent.getAmount().subtract(next.getAmount()));
        } else if (recent.getAmount().compareTo(next.getAmount()) == -1) {
            recent.generateLowerSpend(next.getAmount().subtract(recent.getAmount()));
        }
        return recent;
    }

    public void setInsightMap(InsightType type, List<String> insights) {
        this.insightMap.put(type, insights);
    }

    @Override
    public String toString() {
        return "CustomerInsight{" +
                "amount=" + amount +
                ", category='" + category + '\'' +
                ", vendor='" + vendor + '\'' +
                ", date=" + date +
                ", insights=" + insights +
                ", insightMap=" + insightMap +
                ", insight='" + insight + '\'' +
                ", type=" + type +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CustomerInsight)) return false;
        CustomerInsight insight1 = (CustomerInsight) o;
        return Objects.equals(amount, insight1.amount) &&
                Objects.equals(category, insight1.category) &&
                Objects.equals(vendor, insight1.vendor) &&
                Objects.equals(date, insight1.date) &&
                Objects.equals(insights, insight1.insights) &&
                Objects.equals(insightMap, insight1.insightMap) &&
                Objects.equals(insight, insight1.insight) &&
                type == insight1.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(amount, category, vendor, date, insights, insightMap, insight, type);
    }
}
