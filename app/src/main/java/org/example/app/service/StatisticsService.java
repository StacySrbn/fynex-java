package org.example.app.service;

import org.example.app.dto.*;

public interface StatisticsService {

    FinancialStatisticsResponse getStatistics(Long userId, StatisticsFilter filter);
}
