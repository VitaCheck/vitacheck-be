package com.vitacheck.service;


import com.vitacheck.domain.searchLog.SearchCategory;
import com.vitacheck.domain.searchLog.SearchLog;
import com.vitacheck.domain.user.User;
import com.vitacheck.repository.AgeGroupIngredientStatsRepository;
import com.vitacheck.repository.IngredientRepository;
import com.vitacheck.repository.SearchLogRepository;
import com.vitacheck.util.DateUtils;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class SearchLogService {

    private final SearchLogRepository searchLogRepository;
    private final AgeGroupIngredientStatsRepository ageGroupStatsRepository;
    private final IngredientRepository ingredientRepository;

    @Async
    @Transactional
    public void logSearch(User user, String keyword, String brandName, String ingredientName){

        String logKeyword = "";
        SearchCategory category = SearchCategory.KEYWORD; // 기본값

        if (StringUtils.hasText(ingredientName)) {
            logKeyword = ingredientName;
            category = SearchCategory.INGREDIENT;
        } else if (StringUtils.hasText(brandName)) {
            logKeyword = brandName;
            category = SearchCategory.BRAND;
        } else if (StringUtils.hasText(keyword)) {
            logKeyword = keyword;
            category = SearchCategory.KEYWORD;
        }

        if(!logKeyword.isEmpty()){
            SearchLog searchLog = SearchLog.builder()
                    .user(user)
                    .keyword(logKeyword)
                    .category(category)
                    .build();
            searchLogRepository.save(searchLog);


        }

    }





}
