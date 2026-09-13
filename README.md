[![Build Status](https://github.com/tinevez/config-ui/actions/workflows/build.yml/badge.svg)](https://github.com/tinevez/config-ui/actions/workflows/build.yml)

# Config-UI

A Java library to facilitate building user interfaces for configuring algorithms.

**Config-UI** provides a declarative way to define algorithm parameters, and automatically generates Swing-based configuration dialogs from a single configuration class.

## Origin

This library was initially developed to support the extension of [TrackMate](https://github.com/trackmate-sc/TrackMate), a Fiji plugin for tracking particles in microscopy images. TrackMate allows third-party developers to contribute modules for spot detection, object segmentation, and tracking algorithms, each requiring custom configuration parameters. 

The development bottleneck was consistently the creation of custom Swing configuration panels. Config-UI automates this step: define your parameters once, and the library generates the UI, handles serialization, and provides utilities for validation and persistence.

## Overview

The library is built around two core concepts:

1. **`Configurator`** – A base class you extend to declare your algorithm's parameters using a fluent builder API.
2. **`ParameterVisitor`s** – Utilities that consume a `Configurator` to produce different outputs: Swing GUIs, JSON files, maps, string representations, or persistent preferences.

## Dependencies

Add the following dependency to your `pom.xml`:

```xml
<dependency>
    <groupId>org.scijava</groupId>
    <artifactId>config-ui</artifactId>
    <version>0.0.1</version>
</dependency>
```

The library depends on:
- **SciJava commons** – For `Cancelable` and `Previewable` interfaces
- **Gson** – For JSON serialization
- **FontChooser** – For font selection dialogs

## Quick Start

### 1. Define Your Configuration Class

Extend `Configurator` and declare parameters in the constructor:

```java
import org.scijava.ui.config.Configurator;
import org.scijava.ui.config.Parameters.*;

public class MyAlgorithmConfig extends Configurator {

    public final DoubleParam threshold;
    public final IntParam maxIterations;
    public final BooleanParam useAdvancedMode;
    public final ChoiceParam method;

    public MyAlgorithmConfig() {
        super("My Algorithm", "Configure the parameters for my algorithm.");

        // Bounded double parameter with slider
        this.threshold = addDoubleParameter()
                .key("THRESHOLD")
                .name("Threshold")
                .help("The detection threshold. Higher values are more strict.")
                .defaultValue(0.5)
                .min(0.0)
                .max(1.0)
                .get();

        // Integer parameter with bounds
        this.maxIterations = addIntParameter()
                .key("MAX_ITER")
                .name("Max iterations")
                .help("Maximum number of iterations.")
                .defaultValue(100)
                .min(1)
                .max(1000)
                .units("iterations")
                .get();

        // Boolean flag
        this.useAdvancedMode = addBooleanParameter()
                .key("ADVANCED")
                .name("Advanced mode")
                .help("Enable advanced processing options.")
                .defaultValue(false)
                .get();

        // Choice from discrete values
        this.method = addChoiceParameter()
                .key("METHOD")
                .name("Processing method")
                .help("Select the algorithm to use.")
                .addChoice("FAST", "Fast but less accurate")
                .addChoice("ACCURATE", "Slower but more accurate")
                .addChoice("BALANCED", "A balance between speed and accuracy")
                .defaultValue("BALANCED")
                .get();
    }
}
```

### 2. Build and Show the GUI

```java
import org.scijava.ui.config.visitors.gui.FrameBuilder;
import org.scijava.ui.config.visitors.gui.FrameBuilder.ConfigFrame;
import org.scijava.ui.config.visitors.gui.Progress;
import org.scijava.ui.config.visitors.gui.ProgressAware;

// Create config and default values instances
MyAlgorithmConfig config = new MyAlgorithmConfig();
MyAlgorithmConfig defaultValues = new MyAlgorithmConfig();

// Define the task to execute. Implementing ProgressAware lets the frame
// inject its Progress instance into the task before it is shown.
class MyTask implements Runnable, ProgressAware {

    private Progress progress;

    @Override
    public void setProgress(Progress progress) {
        this.progress = progress;
    }

    @Override
    public void run() {
        progress.indeterminate(false, "Processing...");
        for (int i = 0; i <= config.maxIterations.getValue(); i++) {
            if (progress.isCanceled()) return;
            // Do work...
            progress.set((double) i / config.maxIterations.getValue(), "Step " + i);
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        progress.message("Done!");
    }
}

// Build and show the frame
ConfigFrame frame = FrameBuilder.build(config, new MyTask(), defaultValues);
frame.setVisible(true);
```

## Parameter Types

| Type | Builder Method | Description |
|------|----------------|-------------|
| `BooleanParam` | `addBooleanParameter()` | A checkbox flag |
| `IntParam` | `addIntParameter()` | Integer with optional min/max bounds (uses slider if bounded, spinner otherwise) |
| `DoubleParam` | `addDoubleParameter()` | Double with optional min/max bounds (uses slider if bounded, text field otherwise) |
| `StringParam` | `addStringParameter()` | Free text input |
| `PathParam` | `addPathParameter()` | File or directory path (with browse button in GUI) |
| `ChoiceParam` | `addChoiceParameter()` | Dropdown selection from discrete choices |
| `EnumParam<E>` | `addEnumParameter(Class<E>)` | Dropdown selection from Java enum values |

### Parameter Builder Options

All parameter builders support these methods:

| Method | Description |
|--------|-------------|
| `.key(String)` | **Required.** Unique identifier for serialization and map conversion |
| `.name(String)` | Display name in the UI |
| `.help(String)` | Help text shown via the help button (can be a URL) |
| `.defaultValue(T)` | Default value when not set |
| `.units(String)` | Physical units (displayed next to the input in GUI) |
| `.visible(boolean)` | Whether to show in GUI (default: `true`) |
| `.updateListener(UpdateListener)` | Callback when value changes |

Bounded parameters (`IntParam`, `DoubleParam`) additionally support:

| Method | Description |
|--------|-------------|
| `.min(T)` | Minimum allowed value |
| `.max(T)` | Maximum allowed value |

### ChoiceParam: Internal Value vs Display String

The `addChoiceParameter()` builder accepts pairs of values:

```java
.addChoice("FAST", "Fast but less accurate")
```

- **First argument** (`"FAST"`) – The internal value stored in the parameter and used in serialization
- **Second argument** (`"Fast but less accurate"`) – The human-readable string displayed in the GUI dropdown

If only one argument is provided, it is used for both:

```java
.addChoice("Simple Choice")
```

## Building a Configuration GUI

### Using GuiBuilder Directly

For simple use cases where you only need the parameter panel:

```java
import org.scijava.ui.config.visitors.gui.GuiBuilder;
import org.scijava.ui.config.visitors.gui.GuiBuilder.ConfigPanel;

ConfigPanel panel = GuiBuilder.build(config);
JFrame frame = new JFrame("Algorithm Configuration");
frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
frame.getContentPane().add(panel);
frame.pack();
frame.setLocationRelativeTo(null);
frame.setVisible(true);
```

<img src="doc/PanelShowingConfig.png" alt="PanelShowingConfig" width="600" />

The gray `?` buttons will display the help text of a parameter when clicked. 

### Using FrameBuilder for a Complete Dialog

`FrameBuilder` creates a full-featured dialog with action buttons, but you need to provide a `Runnable` task that will be executed when the user presses the run button. To report progress, implement the `ProgressAware` capability interface so the frame injects its `Progress` instance into the task before it is shown (see the previous section):

```java
		class MyTask implements Runnable, ProgressAware
		{

			/* We will use it later in the cancelable example. */
			protected final AtomicBoolean cancelRequested = new AtomicBoolean( false );

			private Progress progress;

			@Override
			public void setProgress( final Progress progress )
			{
				this.progress = progress;
			}

			@Override
			public void run()
			{
				cancelRequested.set( false );
				progress.indeterminate( false, "Processing..." );
				for ( int i = 0; i <= config.maxIterations.getValue(); i++ )
				{
					if ( cancelRequested.get() )
					{
						System.out.println( "I have been canceled!" );
						return;
					}
					// Do work...
					progress.set( ( double ) i / config.maxIterations.getValue(), "Step " + i );
					try
					{
						Thread.sleep( 50 );
					}
					catch ( final InterruptedException e )
					{
						e.printStackTrace();
					}
				}
				progress.message( "Done!" );
			}
		};

ConfigFrame frame = FrameBuilder.build(config, new MyTask(), defaultValues);
frame.setVisible(true);
```

<img src="doc/GuiWithUserTask.png" alt="GuiWithUserTask" width="600" />

The frame includes:

- **Run button** <img src="src/main/resources/icons/play_circle_filled_white.png" alt="play_circle_filled_white" width="24" /> – Executes the task in a background thread with progress reporting 
- **Stop button** <img src="src/main/resources/icons/stop.png" alt="stop" width="24" /> – Appears during execution for cancelable tasks, and calls the cancelation method (see below)
- **Preview button** <img src="src/main/resources/icons/preview.png" alt="preview" width="24" /> – For previewable tasks (quick preview without full execution)
- **Store button** <img src="src/main/resources/icons/turned_in_not.png" alt="turned_in_not" width="24" /> – Saves configuration to preferences
- **Reload button** <img src="src/main/resources/icons/cached.png" alt="cached" width="24" /> – Loads configuration from preferences
- **Reset button** <img src="src/main/resources/icons/settings_backup_restore.png" alt="settings_backup_restore" width="24" /> – Restores default values
- **Help button** <img src="src/main/resources/icons/help_outline.png" alt="help_outline" width="24" /> – Opens the help URL or shows help text
- **Text button** <img src="src/main/resources/icons/comment.png" alt="comment" width="24" /> - Outputs the current config to text
- **A Progress bar** – Shows task progress with status messages

### Integrating Cancelable and Previewable

If you provide a user task that implements `Cancelable` and/or `Previewable`, new buttons and behaviors will appear on the UI:

```java
import org.scijava.Cancelable;
import org.scijava.command.Previewable;
import java.util.concurrent.atomic.AtomicBoolean;

		class MyCancelableAndPreviewableTask extends MyTask implements Cancelable, Previewable
		{

			@Override
			public void preview()
			{
				// Implement preview logic here, e.g., update a preview display
				// based on current config values.
				System.out.println( "Previewing with threshold: " + config.threshold.getValue() +
						", max iterations: " + config.maxIterations.getValue() +
						", advanced mode: " + config.useAdvancedMode.getValue() +
						", method: " + config.method.getValue() );
			}

			@Override
			public void cancel( final String reason )
			{
				cancelRequested.set( true );
			}

			@Override
			public boolean isCanceled()
			{
				return cancelRequested.get();
			}

			@Override
			public void cancel()
			{
				System.out.println( "Canceling preview" );
			}

			@Override
			public String getCancelReason()
			{
				return "User requested cancellation.";
			}
		}
```

<img src="doc/GuiWithCancelablePreviewableTask.png" alt="GuiWithCancelablePreviewableTask" width="600" />

## Advanced Features

### Parameter Groups and Collapsible Sections

Organize parameters into collapsible groups for cleaner UIs:

```java
public class MyAdvancedConfig extends MyAlgorithmConfig {
    
    public final BooleanParam enableLogging;
    public final IntParam logLevel;

    public MyAdvancedConfig() {
        super("My Advanced Algorithm", "Configure with logging options.");

        this.enableLogging = addBooleanParameter()
                .key("ENABLE_LOGGING")
                .name("Enable Logging")
                .help("Enable detailed logging.")
                .defaultValue(false)
                .get();

        this.logLevel = addIntParameter()
                .key("LOG_LEVEL")
                .name("Log Level")
                .help("Set the logging level (0-5).")
                .defaultValue(3)
                .min(0)
                .max(5)
                .get();

        // Create a collapsible group
        addGroup("Logging Options")
                .add(enableLogging)
                .add(logLevel)
                .collapsed(false)  // Start expanded
                .get();
    }
}
```

<img src="doc/GuiWithParameterGroup.png" alt="GuiWithParameterGroup" width="600" />

### Mutually Exclusive Parameters

Use `SelectableParameters` to create radio-button groups where only one option can be active at a time:

```java
public class MyModelConfig extends Configurator {

    public final EnumParam<ModelType> builtinModel;
    public final PathParam customModelPath;
    public final SelectableParameters modelSelection;

    enum ModelType {
        MODEL_A, MODEL_B, MODEL_C
    }

    public MyModelConfig() {
        super("Model Selection", "Choose a model to use.");

        // Step 1: Define the parameters
        this.builtinModel = addEnumParameter(ModelType.class)
                .key("BUILTIN_MODEL")
                .name("Built-in model")
                .get();

        this.customModelPath = addPathParameter()
                .key("CUSTOM_PATH")
                .name("Custom model path")
                .get();

        // Step 2: Group them as mutually exclusive
        this.modelSelection = addSelectableParameters()
                .key("MODEL_SOURCE")
                .add(builtinModel)
                .add(customModelPath)
                .get();
    }
}
```

<img src="doc/GuiWithSelectableParameters.png" alt="GuiWithSelectableParameters" width="600" />

In the GUI, this creates radio buttons. Only the selected parameter's value is used when iterating over parameters or serializing.

### Display Translators

Transform values for display while storing the original internally. This is useful for showing physical units when the parameter stores pixels:

```java
public class MyMeasurementConfig extends Configurator {
    
    public final DoubleParam diameter;

    public MyMeasurementConfig() {
        super("Measurement Config", "Configure measurements.");

        this.diameter = addDoubleParameter()
                .key("DIAMETER")
                .name("Diameter")
                .units("µm")
                .defaultValue(30.0)
                .get();

        // Store pixels internally, display in physical units
        double pixelSize = 0.2; // µm/pixel
        setDisplayTranslator(
                diameter,
                v -> v * pixelSize,  // Display: pixels -> µm
                v -> v / pixelSize   // Store: µm -> pixels
        );
    }
}
```

<img src="doc/GuiWithTranslation.png" alt="GuiWithTranslation" width="600" />

When the user enters `13 µm`, the parameter internally stores `65.0` (pixels). The table representation shows the translated value:

```
┌────────────────────────────┐
│  My translated algorithm   │
├────────────────────────────┤
│Threshold         │ 0.5     │
│Max iterations    │ 100     │
│Advanced mode     │ false   │
│Processing method │ BALANCED│
│Diameter          │ 65.0    │ <- properly stored in pixel units
└────────────────────────────┘
```

## Visitor Utilities

The `visitors` package provides utilities to consume configurations in different ways:

### JSON Serialization

Save and load configurations to/from JSON files:

```java
import org.scijava.ui.config.visitors.JSon;

// Save to file
JSon.serialize("/path/to/config.json", config);

// Load from file
JSon.deserialize("/path/to/config.json", config);

// Get as JSON string
String json = JSon.toJson(config);
```

JSON format:
```json
{
  "MyAlgorithmConfig": {
    "THRESHOLD": 0.5,
    "MAX_ITER": 100,
    "ADVANCED": false,
    "METHOD": "BALANCED"
  }
}
```

### Map Conversion

Convert to/from a `Map<String, Object>` for programmatic manipulation:

```java
import org.scijava.ui.config.visitors.Maps;

// Config → Map
Map<String, Object> map = Maps.toMap(config);

// Map → Config (populates existing instance)
Maps.fromMap(map, config);

// Modify values programmatically
map.put("THRESHOLD", 0.75);
Maps.fromMap(map, config);  // config now has threshold = 0.75
```

### String Representation

Pretty-print configurations for logging or debugging:

```java
import org.scijava.ui.config.visitors.Strings;

// Simple list format
String list = Strings.toString(config);
// Output:
// My Algorithm:
// Threshold=0.5
// Max iterations=100
// ...

// Boxed table format (recommended)
String table = Strings.echo(config);
// Output:
// ┌───────────────────────────┐
// │     My Algorithm          │
// ├───────────────────────────┤
// │ Threshold        │ 0.5    │
// │ Max iterations   │ 100    │
// │ Advanced mode    │ false  │
// │ Processing method│BALANCED│
// └───────────────────────────┘
```

### Preferences Persistence

Save and reload configurations from user preferences:

```java
import org.scijava.ui.config.visitors.Prefs;

// Store in preferences
Prefs.serialize(config);

// Reload from preferences
Prefs.deserialize(config);
```

## Architecture

```
Configurator (your class)
    │
    ├── Parameter<T, O> – Base type for all parameters
    │   ├── BooleanParam
    │   ├── IntParam
    │   ├── DoubleParam
    │   ├── StringParam
    │   ├── PathParam
    │   ├── ChoiceParam
    │   └── EnumParam<E>
    │
    └── ParameterVisitor – Consumes parameters
        ├── GuiBuilder – Swing UI generation
        │   └── FrameBuilder – Complete dialog with buttons
        ├── JSon – JSON (de)serialization
        ├── Maps – Map conversion
        ├── Strings – Text representation
        └── Prefs – Preferences storage
```

## Complete Examples

See the demo classes in `src/test/java`:

- **`DemoSimple.java`** – Minimal example with a single integer parameter
- **`DemoDoc.java`** – Example used in this README
- **`Demo.java`** – Full-featured Cellpose 3 configuration (real-world example)
- **`Cellpose3Config.java`** – Example standalone configuration class for Cellpose 3

## License

BSD-3-Clause. See the `LICENSE` file for details.

## Authors

Jean-Yves Tinevez, based on what Tobias Pietzsch taught me while working on Mastodon, and code he created there (notably the `StyleElements`).