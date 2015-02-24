/*
 * Copyright (C) 2010-2015, Danilo Pianini and contributors
 * listed in the project's pom.xml file.
 * 
 * This file is part of Alchemist, and is distributed under the terms of
 * the GNU General Public License, with a linking exception, as described
 * in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.language.protelis.datatype;

import java.util.Objects;
import java.util.Optional;
import java.util.function.BinaryOperator;

import it.unibo.alchemist.model.interfaces.INode;

import org.danilopianini.lang.HashUtils;
import org.danilopianini.lang.Pair;

/**
 * @author Danilo Pianini
 *
 */
public abstract class AbstractField implements Field {

	private static final long serialVersionUID = 7507440716878809781L;

	@Override
	public INode<Object> reduceKeys(final BinaryOperator<INode<Object>> op, final INode<Object> exclude) {
		return reduce(nodeIterator(), op, exclude, null);
	}
	
	@Override
	public Object reduceVals(final BinaryOperator<Object> op, final INode<Object> exclude, final Object defaultVal) {
		return reduce(valIterator(), op, exclude == null ? null : getSample(exclude), defaultVal);
	}
	
	@Override
	public Pair<INode<Object>, Object> reducePairs(final BinaryOperator<Pair<INode<Object>, Object>> accumulator, final INode<Object> exclude) {
		return reduce(coupleIterator(), accumulator, exclude == null ? null : new Pair<>(exclude, getSample(exclude)), null);
	}
	
	/**
	 * Reduce the desired {@link Iterable} using the provided
	 * {@link BinaryOperator}, excluding an element and returning a default
	 * value if the {@link Iterable} is empty or only contains the value to
	 * exclude. If multiple elements to exclude are present in the iterable,
	 * only the first will be discarded.
	 * 
	 * @param c
	 *            the {@link Iterable}
	 * @param op
	 *            the {@link BinaryOperator} that selects which element should
	 *            be kept
	 * @param exclude
	 *            element not to consider (can be null)
	 * @param defaultVal
	 *            the value to return if the iterator is empty or only contains
	 *            the value to ignore
	 * @param <T>
	 *            return type
	 * @return the reduced element
	 * 
	 */
	protected static <T> T reduce(final Iterable<T> c, final BinaryOperator<T> op, final T exclude, final T defaultVal) {
		Objects.requireNonNull(c);
		Objects.requireNonNull(op);
		boolean filter = exclude != null;
		Optional<T> result = Optional.empty();
		for (final T el: c) {
			if (filter && el.equals(exclude)) {
				filter = false;
			} else {
				if (result.isPresent()) {
					result = Optional.of(op.apply(result.get(), el));
				} else {
					result = Optional.of(el);
				}
			}
		}
		return result.orElse(defaultVal);
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder("[ ");
		for (final Pair<INode<Object>, Object> entry : coupleIterator()) {
			sb.append(entry);
			sb.append(' ');
		}
		sb.append(']');
		return sb.toString();
	}
	
	@Override
	public boolean equals(final Object o) {
		if (HashUtils.pointerEquals(this, o)) {
			return true;
		}
		if (o instanceof Field) {
			final Field cmp = (Field) o;
			if (cmp.size() == size()) {
				for (final Pair<INode<Object>, Object> pv : coupleIterator()) {
					if (!pv.getSecond().equals(cmp.getSample(pv.getFirst()))) {
						return false;
					}
				}
				return true;
			}
		}
		return false;
	};
	
	@Override
	public int hashCode() {
		int[] hash = new int[size()];
		int i = 0;
		for (final Pair<INode<Object>, Object> pv : coupleIterator()) {
			hash[i++] = pv.hashCode();
		}
		return HashUtils.djb2int32(hash);
	};
	
}
