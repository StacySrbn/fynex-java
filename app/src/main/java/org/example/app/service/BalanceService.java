package org.example.app.service;

import org.example.app.dto.*;

public interface BalanceService {

    BalanceResponse calculateBalance(Long userId);
}
