package com.financecoach;

import com.financecoach.entity.CategorizationRule;
import com.financecoach.repository.CategorizationRuleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.List;

@SpringBootApplication
public class FinanceCoachApplication {

    public static void main(String[] args) {
        SpringApplication.run(FinanceCoachApplication.class, args);
    }

    @Bean
    CommandLineRunner initDefaultCategorizationRules(CategorizationRuleRepository ruleRepository) {
        return args -> {
            if (ruleRepository.count() == 0) {
                List<CategorizationRule> defaultRules = List.of(
                    new CategorizationRule(null, "netflix", "Entertainment", false),
                    new CategorizationRule(null, "spotify", "Entertainment", false),
                    new CategorizationRule(null, "prime video", "Entertainment", false),
                    new CategorizationRule(null, "disney", "Entertainment", false),
                    new CategorizationRule(null, "cinema", "Entertainment", false),
                    new CategorizationRule(null, "uber", "Transport", false),
                    new CategorizationRule(null, "ola", "Transport", false),
                    new CategorizationRule(null, "metro", "Transport", false),
                    new CategorizationRule(null, "fuel", "Transport", false),
                    new CategorizationRule(null, "petrol", "Transport", false),
                    new CategorizationRule(null, "amazon", "Shopping", false),
                    new CategorizationRule(null, "flipkart", "Shopping", false),
                    new CategorizationRule(null, "myntra", "Shopping", false),
                    new CategorizationRule(null, "swiggy", "Food & Dining", false),
                    new CategorizationRule(null, "zomato", "Food & Dining", false),
                    new CategorizationRule(null, "starbucks", "Food & Dining", false),
                    new CategorizationRule(null, "mcdonald", "Food & Dining", false),
                    new CategorizationRule(null, "restaurant", "Food & Dining", false),
                    new CategorizationRule(null, "rent", "Housing", false),
                    new CategorizationRule(null, "maintenance", "Housing", false),
                    new CategorizationRule(null, "electricity", "Utilities", false),
                    new CategorizationRule(null, "water bill", "Utilities", false),
                    new CategorizationRule(null, "wifi", "Utilities", false),
                    new CategorizationRule(null, "broadband", "Utilities", false),
                    new CategorizationRule(null, "salary", "Salary", false),
                    new CategorizationRule(null, "freelance", "Income", false),
                    new CategorizationRule(null, "dividend", "Investments", false),
                    new CategorizationRule(null, "pharmacy", "Healthcare", false),
                    new CategorizationRule(null, "hospital", "Healthcare", false),
                    new CategorizationRule(null, "doctor", "Healthcare", false)
                );
                ruleRepository.saveAll(defaultRules);
            }
        };
    }
}

