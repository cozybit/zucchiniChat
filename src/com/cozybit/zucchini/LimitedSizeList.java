package com.cozybit.zucchini;

import java.util.LinkedList;

public class LimitedSizeList<E> extends LinkedList<E> {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -5418008081151200962L;
	
	int maxSize;
	
	public LimitedSizeList(int size) {
		super();
		maxSize = size;
	}

	@Override
	public boolean add(E o) {
		if (this.size() >= maxSize)
			this.poll();
		return super.add(o);
	}
	
	@Override
	public boolean offer(E o) {
		if (this.size() >= maxSize)
			this.poll();
		return super.offer(o);
	}
	
}
