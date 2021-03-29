package com.chileqi.threadpoolclients;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/***
 * 线程池容器客户端 1.0
 * 
 * @author ChileQi
 * @sice 2016-11-11 11:11:11
 */
public class ThreadPoolClients {
	public class ThreadPoolEntity {
		private String key;
		private Future<Object> future;

		public ThreadPoolEntity() {}

		public ThreadPoolEntity(String key, Future<Object> future) {
			super();
			this.key = key;
			this.future = future;
		}

		public String getKey() {
			return key;
		}

		public void setKey(String key) {
			this.key = key;
		}

		public Future<Object> getFuture() {
			return future;
		}

		public void setFuture(Future<Object> future) {
			this.future = future;
		}
	}

	private final int threadsSize;// 设置运行线程个数
	private int maxThreadsSize;// 需要运行任务的个数
	private CountDownLatch countDownLatch;//
	private List<ThreadPoolTask> tasks;// 需要运行任务集合
	private ExecutorService executorService;
	/**
	 * 初始化
	 * 
	 * @param threadsSize
	 *            设置运行线程个数
	 */
	public ThreadPoolClients(int threadsSize) {
		this.threadsSize = threadsSize;
		tasks = new ArrayList<ThreadPoolTask>();
	}

	/**
	 * 新增任务
	 * 
	 * @param task
	 */
	public void add(ThreadPoolTask task) {
		tasks.add(task);
	}
	public void add(ThreadPoolTask... task) {
		this.addAll(Arrays.asList(task));
	}
	public void addAll(List<ThreadPoolTask> task) {
		tasks.addAll(task);
	}
	
	/**
	 * 线程池
	 */
	private List<ThreadPoolEntity> fixedThreadPool() {
		
		List<ThreadPoolEntity> futures = new ArrayList<ThreadPoolClients.ThreadPoolEntity>();
		executorService = Executors.newFixedThreadPool(threadsSize);
		String futkey;
		for (ThreadPoolTask threadPoolTask : tasks) {
			futkey = (null == threadPoolTask.getTaskName()) ? String.valueOf(threadPoolTask.hashCode()) : threadPoolTask.getTaskName();
			threadPoolTask.setCountDownLatch(countDownLatch);
			futures.add(new ThreadPoolEntity(futkey, executorService.submit(threadPoolTask)));
		}
		
		return futures;
	}

	/**
	 * 运行 获取结果集合
	 * 
	 * @return
	 */
	public List<ThreadPoolEntity> submits() {
		List<ThreadPoolEntity> futures=null;
		try {
			this.maxThreadsSize = tasks.size();
			this.countDownLatch = new CountDownLatch(this.maxThreadsSize);
			if (this.maxThreadsSize > 0) {
				futures = fixedThreadPool();
				countDownLatch.await();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}finally {
			if(null!=executorService)
				executorService.shutdown();
		}
		return futures;
	}

	// -----------------------------------------------public utils-------------------------------------------
	/**
	 * 运行后所有结果 -> list集合
	 * 
	 * @return
	 */
	public List<Object> submitList() {
		List<Object> poolResults = new ArrayList<Object>();
		List<ThreadPoolEntity> futures = submits();
		for (ThreadPoolEntity futobj : futures) {
			try {
				poolResults.add(futobj.getFuture().get());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return poolResults;
	}

	/**
	 * 运行后所有结果 -> map集合 根据线程name存放唯一结果
	 * 
	 * @return
	 */
	public Map<String, Object> submitMap() {
		Map<String, Object> subMaps = new HashMap<String, Object>();
		List<ThreadPoolEntity> futures = submits();
		for (ThreadPoolEntity futobj : futures) {
			try {
				subMaps.put(futobj.getKey(), futobj.getFuture().get());
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (ExecutionException e) {
				e.printStackTrace();
			}
		}
		return subMaps;
	}

	/**
	 * 运行后所有结果 ->map集合 根据线程name存放结果集合
	 * 
	 * @return
	 */
	public Map<String, List<Object>> submitMapValues() {
		@SuppressWarnings("serial")
		Map<String, List<Object>> subMaps = new HashMap<String, List<Object>>() {
			@Override
			public List<Object> put(String key, List<Object> value) {
				if (super.containsKey(key)) {
					super.get(key).addAll(value);
					return value;
				}
				return super.put(key, value);
			}
		};
		List<ThreadPoolEntity> futures = submits();
		for (ThreadPoolEntity futObj : futures) {
			try {
				subMaps.put(futObj.getKey(), new ArrayList<Object>(Arrays.asList(futObj.getFuture().get())));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return subMaps;
	}
	// -----------------------------------------------public utils-------------------------------------------





}
