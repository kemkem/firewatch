package net.kprod.firewatch.service;

import net.kprod.firewatch.data.CheckContext;

public interface CheckService {
    void setCheckContextItem(CheckContext cc);
    void checkUrl(CheckContext cc);
}
