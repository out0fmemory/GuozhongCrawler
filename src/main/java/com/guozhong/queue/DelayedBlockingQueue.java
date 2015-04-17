package com.guozhong.queue;

import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import com.guozhong.Request;

/**
 * URL请求延迟队列，设置好延迟时间
 * @author Administrator
 *
 */
public class DelayedBlockingQueue implements BlockingQueue<Request>,Serializable {

	private final BlockingQueue<Request> queue;
	private final int delayInMilliseconds;
	private volatile long lastSuccesfullPop;

	/**
	 * 
	 * @param delayInMilliseconds
	 */
	public DelayedBlockingQueue(final int delayInMilliseconds) {
		this.delayInMilliseconds = delayInMilliseconds;
		queue = new LinkedBlockingQueue<Request>();
		lastSuccesfullPop = System.currentTimeMillis() - delayInMilliseconds;
	}

	public Request poll() {
		synchronized (queue) {
			while ((System.currentTimeMillis() - lastSuccesfullPop <= delayInMilliseconds) && !queue.isEmpty()) {
				sleep();
			}
			lastSuccesfullPop = System.currentTimeMillis();
			return queue.poll();
		}
	}

	public Request poll(final long timeout, final TimeUnit unit) throws InterruptedException {
		synchronized (queue) {
			while ((System.currentTimeMillis() - lastSuccesfullPop <= delayInMilliseconds) && !queue.isEmpty()) {
				sleep();
			}
			lastSuccesfullPop = System.currentTimeMillis();
			return queue.poll(timeout, unit);
		}
	}

	public Request take() throws InterruptedException {
		synchronized (queue) {
			while ((System.currentTimeMillis() - lastSuccesfullPop <= delayInMilliseconds) && !queue.isEmpty()) {
				sleep();
			}
			lastSuccesfullPop = System.currentTimeMillis();
			return queue.take();
		}
	}

	public Request remove() {
		return queue.remove();
	}

	private void sleep() {
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	// Delegate Methods. Java is just soooo fun sometimes...

	public boolean add(final Request e) {
		return queue.add(e);
	}

	public boolean addAll(final Collection<? extends Request> c) {
		return queue.addAll(c);
	}

	public void clear() {
		queue.clear();
	}

	public boolean contains(final Object o) {
		return queue.contains(o);
	}

	public boolean containsAll(final Collection<?> c) {
		return queue.containsAll(c);
	}

	public int drainTo(final Collection<? super Request> c, final int maxElements) {
		return queue.drainTo(c, maxElements);
	}

	public int drainTo(final Collection<? super Request> c) {
		return queue.drainTo(c);
	}

	public Request element() {
		return queue.element();
	}

	@Override
	public boolean equals(final Object o) {
		return queue.equals(o);
	}

	@Override
	public int hashCode() {
		return queue.hashCode();
	}

	public boolean isEmpty() {
		return queue.isEmpty();
	}

	public Iterator<Request> iterator() {
		return queue.iterator();
	}

	public boolean offer(final Request e, final long timeout, final TimeUnit unit) throws InterruptedException {
		return queue.offer(e, timeout, unit);
	}

	public boolean offer(final Request e) {
		return queue.offer(e);
	}

	public Request peek() {
		return queue.peek();
	}

	public void put(final Request e) throws InterruptedException {
		queue.put(e);
	}

	public int remainingCapacity() {
		return queue.remainingCapacity();
	}

	public boolean remove(final Object o) {
		return queue.remove(o);
	}

	public boolean removeAll(final Collection<?> c) {
		return queue.removeAll(c);
	}

	public boolean retainAll(final Collection<?> c) {
		return queue.retainAll(c);
	}

	public int size() {
		return queue.size();
	}

	public Object[] toArray() {
		return queue.toArray();
	}

	public <T> T[] toArray(final T[] a) {
		return queue.toArray(a);
	}


}
