package com.example.demo.dslResolver;

public interface DSLResolver {
    String getResolverKeyword();

    Object resolveValue(String keyword);
}
