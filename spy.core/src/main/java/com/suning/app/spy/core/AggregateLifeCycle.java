package com.suning.app.spy.core;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 类AggregateLifeCycle.java的实现描述:
 * 
 * @author karry 2014-10-27 上午11:35:37
 */
public class AggregateLifeCycle extends AbstractLifeCycle implements Dumpable {

	private final List<Bean> _beans = new CopyOnWriteArrayList<Bean>();

	private class Bean {
		Bean(Object b) {
			_bean = b;
		}

		final Object _bean;

		public String toString() {
			return "{" + _bean + "}";
		}
	}

	@Override
	protected void doStart(Configuration conf) throws Exception {
		for (Bean b : _beans) {
			if (b._bean instanceof LifeCycle) {
				LifeCycle l = (LifeCycle) b._bean;
				if (!l.isStarting()) {
					l.start(conf);
					if (!l.isStarting()) {
						setFailed("AggregateLifeCycle start");
						stop();
						return;
					}
				}

			}
		}
		super.doStart(conf);
	}

	@Override
	protected void doStop() throws Exception {
		super.doStop();
		for (Bean b : _beans) {
			if (b._bean instanceof LifeCycle) {
				LifeCycle l = (LifeCycle) b._bean;
				if (l.isStarting())
					l.stop();
			}
		}
	}

	public boolean contains(Object bean) {
		for (Bean b : _beans)
			if (b._bean == bean)
				return true;
		return false;
	}

	public boolean addBean(Object o) {
		if (contains(o))
			return false;

		Bean b = new Bean(o);
		_beans.add(b);
		return true;
	}

	public Collection<Object> getBeans() {
		return getBeans(Object.class);
	}

	@SuppressWarnings("unchecked")
	public <T> List<T> getBeans(Class<T> clazz) {
		ArrayList<T> beans = new ArrayList<T>();
		for (Bean b : _beans) {
			if (clazz.isInstance(b._bean))
				beans.add((T) (b._bean));
		}
		return beans;
	}

	@SuppressWarnings("unchecked")
	public <T> T getBean(Class<T> clazz) {
		for (Bean b : _beans) {
			if (clazz.isInstance(b._bean))
				return (T) b._bean;
		}

		return null;
	}

	public void removeBeans() {
		_beans.clear();
	}

	public boolean removeBean(Object o) {
		Iterator<Bean> i = _beans.iterator();
		while (i.hasNext()) {
			Bean b = i.next();
			if (b._bean == o) {
				_beans.remove(b);
				return true;
			}
		}
		return false;
	}

	@Override
	public void dump(Appendable out, String indent) throws IOException {

	}

}
