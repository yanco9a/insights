package com.nationwide.insights.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Objects;

public class Insight {
    @JsonProperty
    private String insight;

    public Insight() {
    }

    @Autowired
    public Insight(String insight) {
        setInsight(insight);
    }

    public void setInsight(String customerInsight) {
        this.insight = customerInsight;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Insight)) return false;
        Insight insight = (Insight) o;
        return this.insight.equals(insight.insight);
    }

    @Override
    public int hashCode() {
        return Objects.hash(insight);
    }

    @Override
    public String toString() {
        return insight;
    }
}
