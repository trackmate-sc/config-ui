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

import static org.scijava.ui.config.utils.GuiUtils.isLikelyUrl;
import static org.scijava.ui.config.utils.GuiUtils.openInBrowser;
import static org.scijava.ui.config.visitors.gui.elements.StyleElements.SMALL_FONT;
import static org.scijava.ui.config.visitors.gui.elements.StyleElements.booleanElement;
import static org.scijava.ui.config.visitors.gui.elements.StyleElements.boundedDoubleElement;
import static org.scijava.ui.config.visitors.gui.elements.StyleElements.doubleElement;
import static org.scijava.ui.config.visitors.gui.elements.StyleElements.enumElement;
import static org.scijava.ui.config.visitors.gui.elements.StyleElements.intElement;
import static org.scijava.ui.config.visitors.gui.elements.StyleElements.linkedCheckBox;
import static org.scijava.ui.config.visitors.gui.elements.StyleElements.linkedComboBoxEnumSelector;
import static org.scijava.ui.config.visitors.gui.elements.StyleElements.linkedComboBoxSelector;
import static org.scijava.ui.config.visitors.gui.elements.StyleElements.linkedFormattedTextField;
import static org.scijava.ui.config.visitors.gui.elements.StyleElements.linkedSliderPanel;
import static org.scijava.ui.config.visitors.gui.elements.StyleElements.linkedSpinner;
import static org.scijava.ui.config.visitors.gui.elements.StyleElements.linkedTextField;
import static org.scijava.ui.config.visitors.gui.elements.StyleElements.listElement;
import static org.scijava.ui.config.visitors.gui.elements.StyleElements.stringElement;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.FontMetrics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.DoubleSupplier;
import java.util.function.Function;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.ToolTipManager;
import javax.swing.border.Border;

import org.scijava.ui.config.Configurator;
import org.scijava.ui.config.Configurator.SelectableParameters;
import org.scijava.ui.config.ConfiguratorIterator;
import org.scijava.ui.config.ParameterVisitor;
import org.scijava.ui.config.Parameters;
import org.scijava.ui.config.Parameters.BooleanParam;
import org.scijava.ui.config.Parameters.ChoiceParam;
import org.scijava.ui.config.Parameters.DoubleParam;
import org.scijava.ui.config.Parameters.EnumParam;
import org.scijava.ui.config.Parameters.IntParam;
import org.scijava.ui.config.Parameters.Parameter;
import org.scijava.ui.config.Parameters.PathParam;
import org.scijava.ui.config.Parameters.StringParam;
import org.scijava.ui.config.utils.FileChooser;
import org.scijava.ui.config.utils.FileChooser.DialogType;
import org.scijava.ui.config.visitors.gui.elements.BoundedValue;
import org.scijava.ui.config.visitors.gui.elements.CollapsibleSection;
import org.scijava.ui.config.visitors.gui.elements.BoundedValue.UpdateListener;
import org.scijava.ui.config.visitors.gui.elements.StyleElement;
import org.scijava.ui.config.visitors.gui.elements.StyleElements.BooleanElement;
import org.scijava.ui.config.visitors.gui.elements.StyleElements.BoundedDoubleElement;
import org.scijava.ui.config.visitors.gui.elements.StyleElements.DoubleElement;
import org.scijava.ui.config.visitors.gui.elements.StyleElements.EnumElement;
import org.scijava.ui.config.visitors.gui.elements.StyleElements.IntElement;
import org.scijava.ui.config.visitors.gui.elements.StyleElements.ListElement;
import org.scijava.ui.config.visitors.gui.elements.StyleElements.StringElement;

/**
 * Creates a Swing {@link JPanel} that can edit the values of the parameters in
 * a {@link Configurator}.
 * <p>
 * A UI element is added for each {@link Parameter}, in order of addition in the
 * {@link Configurator}. Only parameters for which {@link Parameter#isVisible()}
 * is <code>true</code> are included. Groups are supported: parameters in a
 * group are added to a collapsible section with the group name as title.
 * <p>
 * The panel returned is actually a {@link ConfigPanel}, which is a subclass of
 * JPanel that lets the caller refresh the UI from the current parameter values
 * by calling {@link ConfigPanel#refresh()}.
 */
public class GuiBuilder implements ParameterVisitor
{

	private static final int tfCols = 4;

	private final ConfigPanel panel;

	private final GridBagConstraints c;

	private int topInset = 5;

	private int bottomInset = 5;

	private final Map< Parameter< ?, ? >, Function< ?, ? > > forwardUITranslators;

	private final Map< Parameter< ?, ? >, Function< ?, ? > > backwardUITranslators;

	// Foldable section routing
	private boolean inGroup = false;

	private JPanel groupBody = null; // where grouped rows are added

	private GridBagConstraints gc = null; // constraints for the group body

	private GuiBuilder( final Configurator config )
	{
		this.forwardUITranslators = config.getForwardUITranslators();
		this.backwardUITranslators = config.getBackwardUITranslators();
		this.panel = new ConfigPanel();
		final GridBagLayout layout = new GridBagLayout();
		layout.columnWeights = new double[] { 0., 1., 0., 0. };
		panel.setLayout( layout );
		panel.setBorder( BorderFactory.createEmptyBorder( 5, 5, 5, 5 ) );
		this.c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridwidth = 1;
		c.gridx = 0;
		c.gridy = 0;
	}

	private void setCurrentRadioButton( final JRadioButton radioButton )
	{
		if ( radioButton == null || ( panel.rdbtn != radioButton ) )
		{
			topInset = 5;
			bottomInset = 5;
		}
		else
		{
			topInset = 0;
			bottomInset = 0;
		}
		panel.rdbtn = radioButton;
	}

	/*
	 * Parameter VISITOR.
	 */

	@Override
	public void visit( final BooleanParam flag )
	{
		final BooleanElement element = booleanElement( flag.getName(), flag::getValue, flag::set );
		panel.elements.put( flag.getKey(), element );
		final JCheckBox checkbox = linkedCheckBox( element, "" );
		checkbox.setHorizontalAlignment( SwingConstants.LEADING );
		addToLayout(
				flag.getHelp(),
				new JLabel( element.getLabel() ),
				checkbox,
				flag );
	}

	@Override
	public void visit( final IntParam arg )
	{
		// Translate
		@SuppressWarnings( "unchecked" )
		final Function< Integer, Integer > forward = ( Function< Integer, Integer > ) forwardUITranslators.getOrDefault( arg, v -> v );
		@SuppressWarnings( "unchecked" )
		final Function< Integer, Integer > backward = ( Function< Integer, Integer > ) backwardUITranslators.getOrDefault( arg, v -> v );
		final IntSupplier valueGetter = () -> {
			final int value = arg.getValue();
			return forward.apply( value );
		};
		final Consumer< Integer > valueSetter = ( v ) -> {
			final int value = backward.apply( v );
			arg.set( value );
		};

		// Different UI if there are bounds.
		final JComponent comp;
		if ( arg.hasMin() && arg.hasMax() )
		{
			// JSlider
			final int min = forward.apply( arg.getMin() );
			final int max = forward.apply( arg.getMax() );
			final IntElement element = intElement( arg.getName(), min, max, valueGetter, valueSetter );
			panel.elements.put( arg.getKey(), element );

			final int largest = Math.max( Math.abs( min ), Math.abs( max ) );
			final String numberString = String.valueOf( largest );
			final int numberOfColumns = numberString.length() + 1;
			comp = linkedSliderPanel( element, numberOfColumns );
		}
		else
		{
			// JSpinner
			final int tmin = arg.hasMin() ? arg.getMin() : Integer.MIN_VALUE;
			final int tmax = arg.hasMax() ? arg.getMax() : Integer.MAX_VALUE;
			final int min = forward.apply( tmin );
			final int max = forward.apply( tmax );
			final IntElement element = intElement( arg.getName(), min, max, valueGetter, valueSetter );
			panel.elements.put( arg.getKey(), element );
			comp = linkedSpinner( element );
		}
		addToLayout(
				arg.getHelp(),
				new JLabel( arg.getName() ),
				comp,
				arg.getUnits(),
				arg );
	}

	@Override
	public void visit( final DoubleParam arg )
	{
		// Translate
		@SuppressWarnings( "unchecked" )
		final Function< Double, Double > forward = ( Function< Double, Double > ) forwardUITranslators.getOrDefault( arg, v -> v );
		@SuppressWarnings( "unchecked" )
		final Function< Double, Double > backward = ( Function< Double, Double > ) backwardUITranslators.getOrDefault( arg, v -> v );
		final DoubleSupplier valueGetter = () -> {
			final double value = arg.getValue();
			return forward.apply( value );
		};
		final Consumer< Double > valueSetter = ( v ) -> {
			final double value = backward.apply( v );
			// Clamp
			if ( arg.hasMax() && value > arg.getMax() )
				arg.set( arg.getMax() );
			else if ( arg.hasMin() && value < arg.getMin() )
				arg.set( arg.getMin() );
			else
				arg.set( value );
		};

		if ( arg.hasMin() && arg.hasMax() )
		{
			final double min = forward.apply( arg.getMin() );
			final double max = forward.apply( arg.getMax() );
			final BoundedDoubleElement element = boundedDoubleElement( arg.getName(), min, max, valueGetter, valueSetter );
			panel.elements.put( arg.getKey(), element );
			addToLayout(
					arg.getHelp(),
					new JLabel( element.getLabel() ),
					linkedSliderPanel( element, tfCols, ( arg.getMax() - arg.getMin() ) / 50 ),
					arg.getUnits(),
					arg );
		}
		else
		{
			final DoubleElement element = doubleElement( arg.getName(), valueGetter, valueSetter );
			panel.elements.put( arg.getKey(), element );
			final Double min = arg.hasMin() ? forward.apply( arg.getMin() ) : null;
			final Double max = arg.hasMax() ? forward.apply( arg.getMax() ) : null;
			addToLayout(
					arg.getHelp(),
					new JLabel( element.getLabel() ),
					linkedFormattedTextField( element, min, max ),
					arg.getUnits(),
					arg );
		}
	}

	@Override
	public void visit( final StringParam arg )
	{
		// Translate
		@SuppressWarnings( "unchecked" )
		final Function< String, String > forward = ( Function< String, String > ) forwardUITranslators.getOrDefault( arg, v -> v );
		@SuppressWarnings( "unchecked" )
		final Function< String, String > backward = ( Function< String, String > ) backwardUITranslators.getOrDefault( arg, v -> v );
		final Supplier< String > valueGetter = () -> {
			final String value = arg.getValue();
			return forward.apply( value );
		};
		final Consumer< String > valueSetter = ( v ) -> {
			final String value = backward.apply( v );
			arg.set( value );
		};

		final StringElement element = stringElement( arg.getName(), valueGetter, valueSetter );
		panel.elements.put( arg.getKey(), element );
		addToLayoutTwoLines(
				arg.getHelp(),
				new JLabel( element.getLabel() ),
				linkedTextField( element ) );
	}

	@Override
	public void visit( final PathParam arg )
	{
		// Translate
		@SuppressWarnings( "unchecked" )
		final Function< String, String > forward = ( Function< String, String > ) forwardUITranslators.getOrDefault( arg, v -> v );
		@SuppressWarnings( "unchecked" )
		final Function< String, String > backward = ( Function< String, String > ) backwardUITranslators.getOrDefault( arg, v -> v );
		final Supplier< String > valueGetter = () -> {
			final String value = arg.getValue();
			return forward.apply( value );
		};
		final Consumer< String > valueSetter = ( v ) -> {
			final String value = backward.apply( v );
			arg.set( value );
		};

		final StringElement element = stringElement( arg.getName(), valueGetter, valueSetter );
		panel.elements.put( arg.getKey(), element );
		addPathToLayout(
				arg.getHelp(),
				new JLabel( element.getLabel() ),
				linkedTextField( element ) );
	}

	@Override
	public void visit( final ChoiceParam arg )
	{
		final List< String > displays = arg.getDisplays();
		final Supplier< String > supplier = () -> {
			return displays.get( arg.getSelectedIndex() );
		};
		final Consumer< String > consumer = ( s ) -> arg.set( displays.indexOf( s ) );

		final ListElement< String > element = listElement( arg.getName(), displays, supplier, consumer );
		panel.elements.put( arg.getKey(), element );
		final JComboBox< String > comboBox = linkedComboBoxSelector( element );
		comboBox.setSelectedIndex( arg.getSelectedIndex() );
		addToLayout(
				arg.getHelp(),
				new JLabel( element.getLabel() ),
				comboBox,
				arg.getUnits(),
				arg );
	}

	@Override
	public < E extends Enum< E > > void visit( final EnumParam< E > param )
	{
		final Class< E > enumClass = param.getEnumClass();
		final E[] enumConstants = enumClass.getEnumConstants();
		final Supplier< E > supplier = () -> param.getValue();
		final Consumer< E > consumer = ( s ) -> param.set( s );

		final EnumElement< E > element = enumElement( param.getName(), enumConstants, supplier, consumer );
		panel.elements.put( param.getKey(), element );
		final JComboBox< E > comboBox = linkedComboBoxEnumSelector( element );
		comboBox.setSelectedItem( param.getValue() );
		addToLayout(
				param.getHelp(),
				new JLabel( element.getLabel() ),
				comboBox,
				param.getUnits(),
				param );
	}

	/*
	 * HELPER METHODS.
	 */

	private void startGroup( final String title, final boolean collapsed )
	{
		// Create a collapsible section with its own grid
		final CollapsibleSection section = new CollapsibleSection( title, SMALL_FONT, collapsed );

		// Full width.
		c.gridx = 0;
		c.gridwidth = 4;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = new Insets( topInset, 0, bottomInset, 0 );
		panel.add( section, c );

		// advance main grid row and restore defaults
		c.gridy++;
		c.gridx = 0;
		c.gridwidth = 1;
		c.fill = GridBagConstraints.NONE;

		// Route subsequent rows to the group's body
		inGroup = true;
		groupBody = section.getBody();

		// Give the group body its own 4-column grid matching the root
		final GridBagLayout gbl = new GridBagLayout();
		gbl.columnWeights = new double[] { 0., 1., 0., 0. };
		groupBody.setLayout( gbl );

		// Constraints used inside the group body
		gc = new GridBagConstraints();
		gc.fill = GridBagConstraints.HORIZONTAL;
		gc.gridwidth = 1;
		gc.gridx = 0;
		gc.gridy = 0;
		gc.anchor = GridBagConstraints.LINE_START;
		gc.insets = new Insets( 5, 0, 5, 0 );
	}

	private void endGroup()
	{
		inGroup = false;
		groupBody = null;
		gc = null;
	}

	private JComponent buildHelpCell( final String help )
	{
		if ( help == null || help.isBlank() )
			return new JLabel();

		final JButton b = new JButton( " ? " );
		b.setBorderPainted( true );
		b.setContentAreaFilled( false );
		b.setFocusPainted( false );
		b.setOpaque( false );
		b.setForeground( Color.LIGHT_GRAY );
		final Border line = BorderFactory.createLineBorder( Color.LIGHT_GRAY, 1, true );
		b.setBorder( BorderFactory.createCompoundBorder(
				BorderFactory.createEmptyBorder( 0, 5, 0, 0 ), // left margin
				line ) );
		b.setCursor( Cursor.getPredefinedCursor( Cursor.HAND_CURSOR ) );

		if ( isLikelyUrl( help ) )
		{
			b.setToolTipText( "<html>Open help in browser: " + help + "</html>" );
			b.addActionListener( e -> openInBrowser( help, b ) );
		}
		else
		{
			b.setToolTipText( formatToolTip( help, b ) );
			// Click to make the tooltip appear.
			b.addMouseListener( new MouseAdapter()
			{
				@Override
				public void mouseClicked( final MouseEvent e )
				{
					final JComponent c = ( JComponent ) e.getSource();
					final ToolTipManager ttm = ToolTipManager.sharedInstance();

					final int oldInit = ttm.getInitialDelay();
					final int oldDismiss = ttm.getDismissDelay();

					// Show immediately, hide after 3s.
					ttm.setInitialDelay( 0 );
					ttm.setDismissDelay( 3000 );

					// Fake mouse-move at the click location to show the tooltip
					final MouseEvent mv = new MouseEvent(
							c, MouseEvent.MOUSE_MOVED, System.currentTimeMillis(),
							0, e.getX(), e.getY(), 0, false );
					ttm.mouseMoved( mv );

					// Restore delays shortly after.
					final Timer restore = new Timer( 0, ev -> {
						ttm.setInitialDelay( oldInit );
						ttm.setDismissDelay( oldDismiss );
					} );
					restore.setRepeats( false );
					restore.start();
				}
			} );
		}
		return b;
	}

	/*
	 * UI STUFF.
	 */

	private void addToLayoutTwoLines( final String help, final JLabel lbl, final JComponent comp )
	{
		final JPanel target = inGroup ? groupBody : panel;
		final GridBagConstraints CC = inGroup ? gc : c;

		lbl.setText( lbl.getText() + " " );
		lbl.setFont( SMALL_FONT );
		comp.setFont( SMALL_FONT );

		final JComponent item;
		if ( panel.rdbtn != null )
		{
			final JRadioButton btn = panel.rdbtn;
			btn.addItemListener( e -> comp.setEnabled( btn.isSelected() ) );
			comp.setEnabled( btn.isSelected() );
			item = new JPanel();
			item.setLayout( new BoxLayout( item, BoxLayout.LINE_AXIS ) );
			item.add( btn );
			item.add( Box.createHorizontalGlue() );
			item.add( lbl );
		}
		else
		{
			item = lbl;
		}

		// First row: header spans columns 0-2
		CC.insets = new Insets( 5, 0, 0, 0 );
		CC.gridx = 0;
		CC.gridwidth = 3;
		target.add( item, CC );

		// Help icon at column 3 on the same row
		final int savedGridY = CC.gridy;
		CC.gridx = 3;
		CC.gridwidth = 1;
		CC.anchor = inGroup ? GridBagConstraints.LINE_START : GridBagConstraints.LINE_END;
		final JComponent helpCell = buildHelpCell( help );
		target.add( helpCell, CC );

		// Second row: component spans columns 0-2
		CC.gridx = 0;
		CC.gridy = savedGridY + 1;
		CC.gridwidth = 3;
		CC.anchor = GridBagConstraints.LINE_START;
		CC.insets = new Insets( 0, 0, 5, 0 );
		target.add( comp, CC );

		// Advance
		CC.gridy++;

		if ( help != null )
		{
			final String tt = formatToolTip( help, comp );
			lbl.setToolTipText( tt );
			comp.setToolTipText( tt );
		}
	}

	private void addPathToLayout( final String help, final JLabel lbl, final JTextField tf )
	{
		final JPanel target = inGroup ? groupBody : panel;
		final GridBagConstraints CC = inGroup ? gc : c;

		final JPanel p = new JPanel();
		final BoxLayout bl = new BoxLayout( p, BoxLayout.LINE_AXIS );
		p.setLayout( bl );

		tf.setColumns( 10 ); // Avoid long paths deforming new panels.

		lbl.setText( lbl.getText() + " " );
		lbl.setFont( SMALL_FONT );
		tf.setFont( SMALL_FONT );
		final JButton browseButton = new JButton( "browse" );
		browseButton.setFont( SMALL_FONT );
		browseButton.addActionListener( e -> {
			final File file = FileChooser.chooseFile( p, tf.getText(), DialogType.LOAD );
			if ( file == null )
				return;
			tf.setText( file.getAbsolutePath() );
			tf.postActionEvent();
		} );

		if ( panel.rdbtn != null )
		{
			final JRadioButton btn = panel.rdbtn;
			btn.addItemListener( e -> {
				tf.setEnabled( btn.isSelected() );
				browseButton.setEnabled( btn.isSelected() );
			} );

			tf.setEnabled( btn.isSelected() );
			browseButton.setEnabled( btn.isSelected() );
			p.add( btn );
		}
		p.add( lbl );
		p.add( Box.createHorizontalGlue() );
		p.add( browseButton );

		// First row: header panel spans columns 0-2
		CC.insets = new Insets( topInset, 0, 0, 0 );
		CC.gridx = 0;
		CC.gridwidth = 3;
		target.add( p, CC );

		// Help icon at column 3 on the same row
		final int savedGridY = CC.gridy;
		CC.gridx = 3;
		CC.gridwidth = 1;
		CC.anchor = inGroup ? GridBagConstraints.LINE_START : GridBagConstraints.LINE_END;
		final JComponent helpCell = buildHelpCell( help );
		target.add( helpCell, CC );

		// Second row: text field spans columns 0-2
		CC.gridx = 0;
		CC.gridy = savedGridY + 1;
		CC.gridwidth = 3;
		CC.anchor = GridBagConstraints.LINE_START;
		CC.insets = new Insets( 0, 0, bottomInset, 0 );
		target.add( tf, CC );

		// Advance
		CC.gridy++;

		if ( help != null )
		{
			final String tt = formatToolTip( help, tf );
			lbl.setToolTipText( tt );
			tf.setToolTipText( tt );
			browseButton.setToolTipText( tt );
		}
	}

	private void addToLayout( final String help, final JLabel lbl, final JComponent comp, final Parameter< ?, ? > arg )
	{
		final JPanel target = inGroup ? groupBody : panel;
		final GridBagConstraints CC = inGroup ? gc : c;

		lbl.setText( lbl.getText() + " " );
		lbl.setFont( SMALL_FONT );
		lbl.setHorizontalAlignment( JLabel.RIGHT );

		comp.setFont( SMALL_FONT );

		final JComponent header;
		if ( arg != null && panel.rdbtn != null )
		{
			final JRadioButton btn = panel.rdbtn;
			btn.addItemListener( e -> comp.setEnabled( btn.isSelected() ) );
			comp.setEnabled( btn.isSelected() );
			header = new JPanel();
			header.setLayout( new BoxLayout( header, BoxLayout.LINE_AXIS ) );
			header.add( btn );
			header.add( Box.createHorizontalGlue() );
			header.add( lbl );
		}
		else
		{
			header = lbl;
		}

		// Label/header at column 0
		CC.gridx = 0;
		CC.gridwidth = 1;
		CC.anchor = GridBagConstraints.LINE_END;
		target.add( header, CC );

		// Component spans columns 1-2
		CC.gridx++;
		CC.gridwidth = 2;
		CC.anchor = GridBagConstraints.LINE_START;
		target.add( comp, CC );

		// Help icon at column 3
		CC.gridx++;
		CC.gridx++;
		CC.gridwidth = 1;
		CC.anchor = inGroup ? GridBagConstraints.LINE_START : GridBagConstraints.LINE_END;
		final JComponent helpCell = buildHelpCell( help );
		target.add( helpCell, CC );

		// Advance row
		CC.gridx = 0;
		CC.gridy++;
		CC.insets = new Insets( topInset, 0, bottomInset, 0 );

		if ( help != null )
		{
			final String tt = formatToolTip( help, comp );
			lbl.setToolTipText( tt );
			comp.setToolTipText( tt );
		}
	}

	private void addToLayout( final String help, final JLabel lbl, final JComponent comp, final String units, final Parameter< ?, ? > arg )
	{
		if ( units == null )
		{
			addToLayout( help, lbl, comp, arg );
			return;
		}

		final JPanel target = inGroup ? groupBody : panel;
		final GridBagConstraints CC = inGroup ? gc : c;

		lbl.setText( lbl.getText() + " " );
		lbl.setFont( SMALL_FONT );
		lbl.setHorizontalAlignment( JLabel.RIGHT );
		comp.setFont( SMALL_FONT );

		final JComponent header;
		if ( panel.rdbtn != null )
		{
			final JRadioButton btn = panel.rdbtn;
			btn.addItemListener( e -> comp.setEnabled( btn.isSelected() ) );
			header = new JPanel();
			header.setLayout( new BoxLayout( header, BoxLayout.LINE_AXIS ) );
			header.add( btn );
			header.add( Box.createHorizontalGlue() );
			header.add( lbl );
		}
		else
		{
			header = lbl;
		}

		// Label/header at column 0
		CC.gridwidth = 1;
		CC.anchor = GridBagConstraints.LINE_END;
		target.add( header, CC );

		// Component at column 1
		CC.gridx++;
		CC.anchor = GridBagConstraints.LINE_START;
		target.add( comp, CC );

		// Units at column 2
		final JLabel lblUnits = new JLabel( " " + units );
		lblUnits.setFont( SMALL_FONT );
		CC.gridx++;
		CC.insets = new Insets( topInset, 0, bottomInset, 0 );
		target.add( lblUnits, CC );

		// Help icon at column 3
		CC.gridx++;
		CC.gridwidth = 1;
		CC.anchor = GridBagConstraints.LINE_END;
		final JComponent helpCell = buildHelpCell( help );
		target.add( helpCell, CC );

		// Advance row
		CC.gridx = 0;
		CC.gridy++;

		if ( help != null )
		{
			final String tt = formatToolTip( help, comp );
			lbl.setToolTipText( tt );
			comp.setToolTipText( tt );
			lblUnits.setToolTipText( tt );
		}
	}

	private static String formatToolTip( final String help, final Component c )
	{
		final FontMetrics fontMetrics = c.getFontMetrics( c.getFont() );
		final int length = fontMetrics.stringWidth( help );
		if ( length < 350 )
			return help;
		return "<html><p width=\"350px\">" + help + "</p></html>\"";
	}

	private void addLastRow()
	{
		c.gridx = 0;
		c.gridy++;
		c.weighty = 1.;
		panel.add( new JLabel(), c );
	}

	/**
	 * Creates and returns a basic {@link ConfigPanel} for the specified
	 * {@link Configurator}. The panel UI elements will <i>modify</i> the values
	 * of the parameters in the config.
	 *
	 * <p>
	 * The panel will contain only the UI elements corresponding to the
	 * {@link Parameters} in the Configurator that are
	 * {@link Parameter#isVisible()}, using the default Swing look and feel.
	 * There is no title nor help.
	 *
	 * @param config
	 *            the configurator to build a panel for
	 * @return the built panel
	 */
	public static ConfigPanel build( final Configurator config )
	{
		final GuiBuilder builder = createBuilder( config );
		return build( config, builder );
	}

	private static GuiBuilder createBuilder( final Configurator config )
	{
		return new GuiBuilder( config );
	}

	private static ConfigPanel build( final Configurator config, final GuiBuilder builder )
	{
		// Map a selectable group to a button group in the GUI
		final Map< Parameter< ?, ? >, JRadioButton > buttons = new HashMap<>();
		for ( final SelectableParameters selectable : config.getSelectables() )
		{
			final List< Parameter< ?, ? > > args = selectable.getParameters();
			final int nItems = args.size();
			final String label = selectable.getKey();
			final IntSupplier get = selectable::getSelected;
			final Consumer< Integer > set = selectable::select;
			final IntElement element = intElement( label, 0, nItems - 1, get, set );
			builder.panel.elements.put( selectable.getKey(), element );
			final ButtonGroup buttonGroup = linkedButtonGroup( element );
			// Link radio buttons to Parameters.
			final Enumeration< AbstractButton > enumeration = buttonGroup.getElements();
			final Iterator< Parameter< ?, ? > > it = args.iterator();
			while ( enumeration.hasMoreElements() )
			{
				final JRadioButton btn = ( JRadioButton ) enumeration.nextElement();
				final Parameter< ?, ? > arg = it.next();
				buttons.put( arg, btn );
				btn.setSelected( selectable.getSelection().equals( arg ) );
			}
		}

		// Collect all parameters that appear inside any group (identity
		// semantics)
		final Set< Parameter< ?, ? > > paramsInAnyGroup = Collections.newSetFromMap( new IdentityHashMap<>() );
		final ConfiguratorIterator probe = config.iterator();
		while ( probe.hasNext() )
		{
			final Parameter< ?, ? > p = probe.next();
			if ( probe.inGroup() )
				paramsInAnyGroup.add( p );
		}

		// Iterate over Parameters, taking care of selectable group.
		final ConfiguratorIterator it = config.iterator();
		while ( it.hasNext() )
		{
			final Parameter< ?, ? > arg = it.next();

			// Open a collapsible section if we just entered a group
			if ( it.groupEntered() && it.getCurrentGroup() != null )
				builder.startGroup( it.getCurrentGroup().getName(), it.getCurrentGroup().isCollapsed() );

			// Skip standalone duplicates of grouped parameters
			if ( !it.inGroup() && paramsInAnyGroup.contains( arg ) )
			{
				if ( it.groupExited() )
					builder.endGroup(); // defensive: close if needed
				continue;
			}

			if ( !arg.isVisible() )
			{
				if ( it.groupExited() )
					builder.endGroup();
				continue;
			}

			builder.setCurrentRadioButton( buttons.get( arg ) );
			arg.accept( builder );

			// Close the section if this was the group's last parameter
			if ( it.groupExited() )
				builder.endGroup();
		}

		/*
		 * Last row.
		 */

		builder.addLastRow();
		return builder.panel;
	}

	private static ButtonGroup linkedButtonGroup( final IntElement element )
	{
		final BoundedValue value = element.getValue();
		final ButtonGroup buttonGroup = new ButtonGroup();
		final List< JRadioButton > buttons = new ArrayList<>();
		for ( int i = 0; i <= value.getRangeMax(); i++ )
		{
			final JRadioButton btn = new JRadioButton();
			buttons.add( btn );
			final int selected = i;
			btn.addItemListener( new ItemListener()
			{

				@Override
				public void itemStateChanged( final ItemEvent e )
				{
					if ( btn.isSelected() )
						element.set( selected );
				}
			} );
			buttonGroup.add( btn );
		}
		element.getValue().setUpdateListener( new UpdateListener()
		{

			@Override
			public void update()
			{
				final int selected = element.get();
				buttons.get( selected ).setSelected( true );
			}
		} );
		return buttonGroup;
	}

	/*
	 * INNER CLASSES.
	 */

	/**
	 * Panel containing the configured UI elements.
	 */
	public class ConfigPanel extends JPanel
	{

		/**
		 * Map of StyleElements that are created by this builder. The keys are
		 * the corresponding Parameter keys.
		 */
		final Map< String, StyleElement > elements = new LinkedHashMap<>();

		private JRadioButton rdbtn;

		private static final long serialVersionUID = 1L;

		/** Refreshes all UI elements in the panel. */
		public void refresh()
		{
			elements.values().forEach( e -> e.update() );
		}

		/**
		 * Returns the style element for the given key.
		 *
		 * @param key
		 *            the parameter key.
		 * @return the style element, or null if not found.
		 */
		public StyleElement getStyleElement( final String key )
		{
			return elements.get( key );
		}
	}
}
