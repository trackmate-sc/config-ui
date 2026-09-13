/*-
 * #%L
 * A Java library to facilitate building user-interfaces configuring algorithms.
 * %%
 * Copyright (C) 2026 Institut Pasteur
 * %%
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * 3. Neither the name of the Institut Pasteur nor the names of its contributors
 *    may be used to endorse or promote products derived from this software without
 *    specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */
package org.scijava.ui.config.visitors.gui;

/**
 * Interface for reporting a task progress, when it is run from the
 * {@link FrameBuilder} UI.
 * <p>
 * When creating a {@link Runnable} task to be run from the {@link FrameBuilder}
 * UI, you can implement the {@link ProgressAware} interface to receive a
 * {@link Progress} instance, which you can use to report the progress of your
 * task.
 */
public interface Progress
{

	/**
	 * Sets the progress fraction.
	 *
	 * @param fraction
	 *            the progress fraction (0 to 1).
	 */
	void set( double fraction );

	/**
	 * Sets the progress fraction with a status message.
	 *
	 * @param fraction
	 *            the progress fraction (0 to 1).
	 * @param text
	 *            the status message.
	 */
	void set( double fraction, String text );

	/**
	 * Sets the progress bar to indeterminate mode.
	 *
	 * @param on
	 *            if {@code true}, show indeterminate progress.
	 * @param text
	 *            the status message.
	 */
	void indeterminate( boolean on, String text );

	/**
	 * Sets a status message without changing the progress value.
	 *
	 * @param text
	 *            the status message.
	 */
	void message( String text );

	/**
	 * Clears the progress indicator and resets to the initial state.
	 */
	void clear();

	/**
	 * Checks whether the task has been canceled.
	 *
	 * @return {@code true} if canceled, {@code false} otherwise.
	 */
	boolean isCanceled();
}
