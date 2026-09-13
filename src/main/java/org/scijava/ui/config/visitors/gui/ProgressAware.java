package org.scijava.ui.config.visitors.gui;

/**
 * Interface for receiving a {@link Progress} instance to report progress into
 * the UI. Implement this interface in your {@link Runnable} task to receive a
 * {@link Progress} instance, which you can use to report the progress of your
 * task.
 */
public interface ProgressAware
{

	/**
	 * Receives the progress instance to report into the UI. Called by the
	 * builder before the frame is shown.
	 *
	 * @param progress
	 *            the progress reporter.
	 */
	void setProgress( Progress progress );
}
