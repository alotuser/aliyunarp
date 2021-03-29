package com.chileqi.threadpoolclients;

import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;

public class ThreadPoolTask implements Callable<Object>{
	
	private String taskName;
	private CountDownLatch countDownLatch;
	
	@Override
	public Object call() throws Exception {
		countDown();
		return null;
	}

	public void countDown(){
		countDownLatch.countDown();
	}

	public void setCountDownLatch(CountDownLatch countDownLatch) {
		
		if(this.countDownLatch!=countDownLatch){
			this.countDownLatch = countDownLatch;
		}
		
	}

	public void setTaskName(String taskName) {
		this.taskName = taskName;
	}

	public String getTaskName() {
		return taskName;
	}

	
	
	
	
	
}
