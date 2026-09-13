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
