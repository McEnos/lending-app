package org.ezra.lendingservice.scheduler;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class LoanSweepJob {
    @Scheduled(cron = "0 0 0 * * *")
    public void sweepOverdueLoans() {
        // service.findOverdueLoansAndApplyFees();
    }
}
