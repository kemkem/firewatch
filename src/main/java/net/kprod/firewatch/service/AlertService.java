package net.kprod.firewatch.service;

import net.kprod.firewatch.data.WatchedElement;
import net.kprod.firewatch.data.WatchResult;

public interface AlertService {
    void sendCheckAlert(WatchedElement cc, WatchResult cr);
    void sendStatMessage(String body);
}
