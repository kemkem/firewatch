package net.kprod.firewatch.service;

import net.kprod.firewatch.data.WatchedElement;

public interface WatchService {
    void watchElement(WatchedElement cc);
    void checkUrl(WatchedElement cc, boolean boot);
}
