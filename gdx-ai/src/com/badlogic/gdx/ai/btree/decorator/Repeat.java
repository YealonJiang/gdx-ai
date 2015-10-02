/*******************************************************************************
 * Copyright 2014 See AUTHORS file.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.badlogic.gdx.ai.btree.decorator;

import com.badlogic.gdx.ai.btree.LoopDecorator;
import com.badlogic.gdx.ai.btree.Task;
import com.badlogic.gdx.ai.btree.annotation.TaskAttribute;
import com.badlogic.gdx.ai.utils.random.ConstantIntegerDistribution;
import com.badlogic.gdx.ai.utils.random.IntegerDistribution;

/** A {@code Repeat} decorator will repeat the wrapped task a certain number of times, possibly infinite.
 * 
 * @param <E> type of the blackboard object that tasks use to read or modify game state
 * 
 * @author implicit-invocation */
public class Repeat<E> extends LoopDecorator<E> {

	/** The integer distribution that determines how many times the wrapped task must be repeated.
	 * 
	 * @see #start() */
	@TaskAttribute() public IntegerDistribution times;

	private int count;

	/** Creates an infinite repeat decorator with no child task. */
	public Repeat () {
		this(null);
	}

	/** Creates an infinite repeat decorator that wraps the given task.
	 * 
	 * @param child the task that will be wrapped */
	public Repeat (Task<E> child) {
		this(child, ConstantIntegerDistribution.NEGATIVE_ONE);
	}

	/** Creates a repeat decorator that executes the given task the number of times (possibly infinite) determined by the given
	 * distribution. The number of times is drawn from the distribution by the {@link #start()} method. Any negative value means
	 * forever.
	 * 
	 * @param child the task that will be wrapped
	 * @param times the integer distribution specifying how many times the child must be repeated. */
	public Repeat (Task<E> child, IntegerDistribution times) {
		super(child);
		this.times = times;
	}

	/** Draws a value from the distribution that determines how many times the wrapped task must be repeated. Any negative value
	 * means forever.
	 * <p>
	 * This method is called when the task is entered. */
	@Override
	public void start () {
		count = times.nextInt();
	}

	@Override
	public boolean condition () {
		return loop && count != 0;
	}

	@Override
	public void childSuccess (Task<E> runningTask) {
		if (count > 0) count--;
		if (count == 0) {
			super.childSuccess(runningTask);
			loop = false;
		} else
			loop = true;
	}

	@Override
	public void childFail (Task<E> runningTask) {
		if (count > 0) count--;
		if (count == 0) {
			super.childFail(runningTask);
			loop = false;
		} else
			loop = true;
	}

	@Override
	protected Task<E> copyTo (Task<E> task) {
		Repeat<E> repeat = (Repeat<E>)task;
		repeat.times = times; // no need to clone since it is immutable

		return super.copyTo(task);
	}
}