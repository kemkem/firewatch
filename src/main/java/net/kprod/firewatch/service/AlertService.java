package net.kprod.firewatch.service;

import net.kprod.firewatch.data.CheckContext;
import net.kprod.firewatch.data.CheckResult;

public interface AlertService {
    void sendCheckAlert(CheckContext cc, CheckResult cr);
    void sendStatMessage(String body);
}
