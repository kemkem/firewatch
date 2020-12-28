package net.kprod.firewatch.sched;

import net.kprod.firewatch.data.CheckContext;
import net.kprod.firewatch.service.CheckService;
import org.springframework.context.ApplicationContext;

public class RunnableTask implements Runnable{
	private ApplicationContext ctx;
	private CheckContext checkContext;

	public RunnableTask(ApplicationContext ctx, CheckContext checkContext){
		this.ctx = ctx;
		this.checkContext = checkContext;
	}

	@Override
	public void run() {
		CheckService checkService = ctx.getBean(CheckService.class);
		checkService.checkUrl(checkContext);
	}
}