package com.nationwide.insights;

import com.nationwide.insights.domain.Insight;
import com.nationwide.insights.service.CustomerInsightsService;
import org.slf4j.Logger;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.List;

import static org.slf4j.LoggerFactory.getLogger;


@Configuration
@Profile("demo")
public class CustomerInsightsDemoConfig {
    private static final Logger LOG = getLogger(CustomerInsightsDemoConfig.class.getCanonicalName());

    @Bean
    public CommandLineRunner demo(CustomerInsightsService service) {
        return (args) -> {
            LOG.info("******************************************");
            LOG.info("Find Customer Insights(s) by Identifier: '1'");
            LOG.info("******************************************");

            List<Insight> insightsOne = service.customerInsightsById(1L);
            insightsOne.forEach(insight -> LOG.info("insight: "+insight.toString()));

            LOG.info("******************************************");
            LOG.info("Find Customer Insights(s) by Identifier: '2'");
            LOG.info("******************************************");

            List<Insight> insightsTwo = service.customerInsightsById(2L);
            insightsTwo.forEach(insight -> LOG.info("insight: "+insight.toString()));
        };
    }
}