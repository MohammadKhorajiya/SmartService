package com.smartservice.config;

import org.springframework.core.MethodParameter;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.util.ArrayList;
import java.util.List;

public class CustomPageableHandlerMethodArgumentResolver extends PageableHandlerMethodArgumentResolver {

    @Override
    public Pageable resolveArgument(
            @NonNull MethodParameter methodParameter,
            ModelAndViewContainer mavContainer,
            @NonNull NativeWebRequest webRequest,
            WebDataBinderFactory binderFactory) {

        Pageable defaultPageable = super.resolveArgument(methodParameter, mavContainer, webRequest, binderFactory);

        String[] rawSortParams = webRequest.getParameterValues("sort");
        if (rawSortParams == null || rawSortParams.length == 0) {
            return defaultPageable;
        }

        Sort cleanSort = parseSort(rawSortParams);
        if (cleanSort.isUnsorted()) {
            return defaultPageable;
        }

        return PageRequest.of(defaultPageable.getPageNumber(), defaultPageable.getPageSize(), cleanSort);
    }

    public static Sort parseSort(String[] sortValues) {
        if (sortValues == null || sortValues.length == 0) {
            return Sort.unsorted();
        }

        List<Sort.Order> orders = new ArrayList<>();
        List<String> tokens = new ArrayList<>();

        for (String val : sortValues) {
            if (val == null) continue;
            String cleaned = val.replaceAll("[\\[\\]\"']", "").trim();
            if (cleaned.isEmpty()) continue;

            String[] parts = cleaned.split(",");
            for (String p : parts) {
                String trimmed = p.trim();
                if (!trimmed.isEmpty()) {
                    tokens.add(trimmed);
                }
            }
        }

        if (tokens.isEmpty()) {
            return Sort.unsorted();
        }

        int i = 0;
        while (i < tokens.size()) {
            String propOrDir = tokens.get(i);

            if ("asc".equalsIgnoreCase(propOrDir) || "desc".equalsIgnoreCase(propOrDir)) {
                i++;
                continue;
            }

            String property = propOrDir;
            Sort.Direction direction = Sort.Direction.ASC;

            if (i + 1 < tokens.size()) {
                String nextToken = tokens.get(i + 1);
                if ("asc".equalsIgnoreCase(nextToken)) {
                    direction = Sort.Direction.ASC;
                    i++;
                } else if ("desc".equalsIgnoreCase(nextToken)) {
                    direction = Sort.Direction.DESC;
                    i++;
                }
            }

            orders.add(new Sort.Order(direction, property));
            i++;
        }

        return orders.isEmpty() ? Sort.unsorted() : Sort.by(orders);
    }
}
