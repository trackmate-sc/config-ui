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
package org.scijava.ui.config;

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.scijava.Cancelable;
import org.scijava.command.Previewable;
import org.scijava.ui.config.visitors.Maps;
import org.scijava.ui.config.visitors.Strings;
import org.scijava.ui.config.visitors.gui.FrameBuilder;
import org.scijava.ui.config.visitors.gui.FrameBuilder.ConfigFrame;
import org.scijava.ui.config.visitors.gui.Progress;
import org.scijava.ui.config.visitors.gui.ProgressAware;

/**
 * Demo with a UI that would configure Cellpose 3.
 */
public class Demo
{

	public static void main( final String[] args )
	{
		final int nChannels = 3;
		final double pixelSize = 0.2;
		final String units = "µm";

		final Cellpose3Config config = new Cellpose3Config( nChannels, pixelSize, units );

		config.builtinModel.set( Cellpose3BuiltinModels.CYTO3 );
		config.builtinOrCustom.select( config.builtinModel );
		config.chan1.set( 2 );
		config.chan2.set( 1 );
		config.diameter.set( 40. );

		System.out.println( "------------------------------" );
		System.out.println( "Original config" );
		System.out.println( "------------------------------" );
		System.out.println( config );
		System.out.println( "------------------------------" );

		System.out.println();
		System.out.println( "------------------------------" );
		System.out.println( "As a map:" );
		System.out.println( "------------------------------" );
		final Map< String, Object > map = Maps.toMap( config );
		map.forEach( ( k, v ) -> System.out.println( " - " + k + " -> " + v ) );
		System.out.println( "------------------------------" );

		// Modify the map.
		map.put( "CUSTOM_MODEL_PATH", "Trololo" );
		map.put( "BUILTIN_OR_CUSTOM", "CUSTOM_MODEL_PATH" );
		map.put( "CHAN2", 0 );

		// Re-read the map into a new config.
		final Cellpose3Config config2 = new Cellpose3Config( nChannels, pixelSize, units );
		Maps.fromMap( map, config2 );
		System.out.println();
		System.out.println( "------------------------------" );
		System.out.println( "After modifying the map" );
		System.out.println( "------------------------------" );
		System.out.println( Strings.echo( config2 ) );
		System.out.println( "------------------------------" );

		/*
		 * GUI
		 */

		final DummyRunner dummyRunner = new DummyRunner( config2 );
		final Configurator defaultValues = new Cellpose3Config( nChannels, pixelSize, units );

		final ConfigFrame frame = FrameBuilder.build( config2, dummyRunner, defaultValues );

		frame.setVisible( true );
	}

	private static class DummyRunner implements Runnable, ProgressAware, Cancelable, Previewable
	{

		private final Cellpose3Config config;

		private final AtomicBoolean cancelRequested = new AtomicBoolean( false );

		private String cancelReason;

		private Progress p;

		public DummyRunner( final Cellpose3Config config )
		{
			this.config = config;
		}

		@Override
		public void setProgress( final Progress progress )
		{
			this.p = progress;
		}

		@Override
		public void run()
		{
			cancelRequested.set( false );
			p.indeterminate( false, "Preparing..." );
			try
			{
				Thread.sleep( 500 );
				final int steps = 20;
				for ( int i = 1; i <= steps; i++ )
				{
					if ( isCanceled() )
					{
						p.message( "Canceled:" + getCancelReason() );
						return;
					}
					Thread.sleep( 100 );
					p.set( i / ( double ) steps, "Running " + config.builtinModel.getValue() );
				}
				p.message( "Model run finished." );
			}
			catch ( final InterruptedException e )
			{
				e.printStackTrace();
			}
		}

		@Override
		public boolean isCanceled()
		{
			return cancelRequested.get();
		}

		@Override
		public void cancel( final String reason )
		{
			this.cancelReason = reason;
			cancelRequested.set( true );
		}

		@Override
		public String getCancelReason()
		{
			return cancelReason;
		}

		@Override
		public void preview()
		{
			cancelRequested.set( false );
			p.indeterminate( false, "Previewing..." );
			try
			{
				Thread.sleep( 2500 );
				p.message( "Preview done." );
			}
			catch ( final InterruptedException e )
			{
				e.printStackTrace();
			}
			p.clear();
		}

		@Override
		public void cancel()
		{
			cancelRequested.set( true );
			p.message( "Canceling preview..." );
		}
	}
}
