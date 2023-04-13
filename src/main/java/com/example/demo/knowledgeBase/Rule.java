package com.example.demo.knowledgeBase;

import com.example.demo.restAPI.RuleNamespace;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class Rule {
    RuleNamespace ruleNamespace;
    String ruleId;
    String condition;
    String action;
    Integer priority;
    String description;
}
