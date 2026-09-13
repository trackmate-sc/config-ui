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
package org.scijava.ui.config.visitors.gui.elements;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

/**
 * A collapsible section panel that can contain a body of components and be
 * toggled expanded or collapsed by clicking on the title arrow.
 */
public class CollapsibleSection extends JPanel
{
	private static final long serialVersionUID = 1L;

	private final ToggleTitleBorder titled;

	final JPanel body;

	private boolean expanded;

	/**
	 * Creates a new collapsible section.
	 *
	 * @param title
	 *            the title displayed in the section header.
	 * @param font
	 *            the font used for the title.
	 * @param collapsed
	 *            if <code>true</code>, the section starts in a collapsed state;
	 *            otherwise, it starts expanded.
	 */
	public CollapsibleSection( final String title, final Font font, final boolean collapsed )
	{
		super( new BorderLayout() );
		this.expanded = !collapsed;
		this.titled = new ToggleTitleBorder( title, font, expanded );
		setBorder( BorderFactory.createCompoundBorder(
				titled,
				BorderFactory.createEmptyBorder( 0, 5, 0, 5 ) ) );

		// Body
		final GridBagLayout layout = new GridBagLayout();
		layout.columnWeights = new double[] { 0., 1., 0., 0. };
		body = new JPanel( layout );
		body.setVisible( expanded );
		add( body, BorderLayout.CENTER );

		// Click on the arrow to toggle
		addMouseListener( new MouseAdapter()
		{
			@Override
			public void mouseClicked( final MouseEvent e )
			{
				if ( titled.isInToggle( e.getX(), e.getY(), CollapsibleSection.this ) )
					setExpanded( !expanded );
			}
		} );

		// Hand cursor when hovering the arrow
		addMouseMotionListener( new MouseMotionAdapter()
		{
			@Override
			public void mouseMoved( final MouseEvent e )
			{
				setCursor( titled.isInToggle( e.getX(), e.getY(), CollapsibleSection.this )
						? Cursor.getPredefinedCursor( Cursor.HAND_CURSOR )
						: Cursor.getDefaultCursor() );
			}
		} );
	}

	/**
	 * Exposes the body panel to allow adding content to the collapsible
	 * section.
	 *
	 * @return the body panel.
	 */
	public JPanel getBody()
	{
		return body;
	}

	private void setExpanded( final boolean show )
	{
		if ( this.expanded == show )
			return;
		this.expanded = show;
		titled.setExpanded( show );
		body.setVisible( show );
		revalidate();
		repaint();
	}

	/**
	 * Custom border with an invisible toggle button that is used to
	 * collapse/expand the section when clicked.
	 */
	private static class ToggleTitleBorder extends TitledBorder
	{
		private static final long serialVersionUID = 1L;

		private static final int ICON_W = 20;

		private static final int ICON_H = 10;

		// left margin before arrow
		private static final int LEFT_PAD = 6;

		private final String baseTitle;

		private boolean expanded;

		private final Rectangle toggleBounds = new Rectangle();

		ToggleTitleBorder( final String title, final Font font, final boolean expanded )
		{
			super( BorderFactory.createLineBorder( Color.LIGHT_GRAY, 1, true ),
					title, LEFT, TOP, font, null );
			this.baseTitle = title;
			this.expanded = expanded;
			setTitleColor( Color.GRAY );
			updateTitle();
		}

		void setExpanded( final boolean expanded )
		{
			if ( this.expanded != expanded )
			{
				this.expanded = expanded;
				updateTitle();
			}
		}

		boolean isInToggle( final int x, final int y, final JComponent c )
		{
			return toggleBounds.contains( x, y );
		}

		private void updateTitle()
		{
			setTitle( ( expanded ? "▼ " : "▶ " ) + baseTitle );
		}

		@Override
		public void paintBorder( final Component c, final Graphics g, final int x, final int y, final int width, final int height )
		{
			// Let the default titled border draw the line and the title
			super.paintBorder( c, g, x, y, width, height );

			final Insets ins = getBorderInsets( c );
			// Title band height
			final int band = Math.max( 0, ins.top );

			// Compute arrow position on the title band
			final int ax = x + LEFT_PAD;
			final int ay = y + Math.max( 0, ( band - ICON_H ) / 2 );

			// Remember clickable bounds for hit-testing
			toggleBounds.setBounds( ax, ay, ICON_W, ICON_H );
		}
	}
}
