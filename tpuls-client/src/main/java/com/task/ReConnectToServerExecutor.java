package com.task;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ReConnectToServerExecutor {
	private static ExecutorService executorService = Executors.newFixedThreadPool(5);
	
	public static void commitTask(Runnable task) {
		executorService.submit(task);
	}
}
