package net.kprod.firewatch.sched;

import net.kprod.firewatch.data.WatchedElement;
import net.kprod.firewatch.service.WatchService;
import org.springframework.context.ApplicationContext;

public class RunnableTask implements Runnable{
	private ApplicationContext ctx;
	private WatchedElement watchedElement;

	public RunnableTask(ApplicationContext ctx, WatchedElement watchedElement){
		this.ctx = ctx;
		this.watchedElement = watchedElement;
	}

	@Override
	public void run() {
		WatchService watchService = ctx.getBean(WatchService.class);
		watchService.checkUrl(watchedElement, false);
	}
}