# messagebundle-maven-plugin
A Maven plugin that generates enumerations for message bundles

## Usage

	<build>
		[...]
		<plugins>
			[...]
			<plugin>
				<groupId>com.github.maxfichtelmann</groupId>
				<artifactId>messagebundle-maven-plugin</artifactId>
				<version>1.1.0</version>
				<executions>
					<execution>
						<phase>generate-sources</phase>
						<goals>
							<goal>generate</goal>
						</goals>
					</execution>
				</executions>
			</plugin>
		</plugins>
	</build>


## Configuration

    <configuration>
        <!-- fileset which property files should be used -->
        <fileset>
            <!-- directory where to find the properties; default: src/main/resources -->
            <directory>src/main/bundles</directory>
            <!-- list of include glob patterns; default: messages/**/*.properties -->
            <includes>
                <include>foo/*.properties</include>
            </includes>
            <!-- list of exclude glob patterns -->
            <excludes>
                <exclude>foo/version.properties</exclude>
            </excludes>
        </fileset>
        <!-- folder where to put the generated java files; default: target/generated-sources/message-bundles -->
        <outputDirectory>target/bundle-enums</outputDirectory>
        <!-- package in which the generated enums will be located -->
        <packageName>foo.bar</packageName>
    </configuration>

## Sample

Using above configuration, a [ResourceBundle](https://docs.oracle.com/javase/7/docs/api/java/util/ResourceBundle.html) property-file `src/main/bundles/Messages.properties` with the following content:

    foo=some text
    bar=some other text


will be transformed this enum:

```java
package foo.bar;

import java.text.MessageFormat;
import java.util.ResourceBundle;


/**
 * A generated enum that wraps the {@link ResourceBundle} for Messages.
 * 
 */
public enum Messages {


    /**
     * Property 'bar'. Does not require format parameters.
     * 
     */
    BAR("bar"),

    /**
     * Property 'foo'. Does not require format parameters.
     * 
     */
    FOO("foo");
    private String propertyName;
    private final static ResourceBundle _BUNDLE = (ResourceBundle.getBundle("messages/Foo"));

    /**
     * Create the enum constant with the given property key.
     * 
     * @param propertyName
     *     the name of the property the created enum constant refers to
     */
    private Foo(String propertyName) {
        this.propertyName = propertyName;
    }

    /**
     * Returns the property name in the {@link ResourceBundle} this refers to.
     * 
     * @return
     *     the property name in the {@link ResourceBundle} this refers to.
     */
    public String getPropertyName() {
        return propertyName;
    }

    /**
     * Render the message by turning the value in the {@link ResourceBundle} into a {@link MessageFormat} and formatting it using the provided parameters.
     * 
     * @param parameters
     *     the parameters that should be used during formatting
     * @return
     *     the formatted String.
     */
    public String render(Object... parameters) {
        return MessageFormat.format(_BUNDLE.getString(propertyName), parameters);
    }

}
```
