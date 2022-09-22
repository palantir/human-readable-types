<p align="right">
<a href="https://autorelease.general.dmz.palantir.tech/palantir/human-readable-types"><img src="https://img.shields.io/badge/Perform%20an-Autorelease-success.svg" alt="Autorelease"></a>
</p>

# Human Readable Types

[![Build Status](https://circleci.com/gh/palantir/human-readable-types.svg?style=shield)](https://circleci.com/gh/palantir/human-readable-types)

This repository provides a collection of useful types that can be deserialized from human-readable strings. These types
can be particularly useful for use in POJOs deserialized from configuration files where legibility is important.

human-readable-types
--------------------

The following types are currently provided:
* [HumanReadableByteCount](human-readable-types/src/main/java/com/palantir/humanreadabletypes/HumanReadableByteCount.java)
* [HumanReadableDuration](human-readable-types/src/main/java/com/palantir/humanreadabletypes/HumanReadableDuration.java)

### Example Usage

Maven artifacts are published to JCenter. Example Gradle dependency configuration:

```groovy
repositories {
    jcenter()
}

dependencies {
    compile "com.palantir.human-readable-types:human-readable-types:$version"
}
```

Using these types alongside [Jackson](https://github.com/FasterXML/jackson) and [Immutables](https://github.com/immutables/immutables)
might look something like:

```java
package com.palantir.example;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.palantir.humanreadabletypes.HumanReadableByteCount;
import com.palantir.humanreadabletypes.HumanReadableDuration;
import org.immutables.value.Value;

@Value.Immutable
@JsonDeserialize(as = ImmutableExampleConfiguration.class)
public abstract class ExampleConfiguration {
    
    @JsonProperty("maximum-connect-timeout")
    public abstract HumanReadableDuration getMaximumConnectTimeout();
    
    @JsonProperty("maximum-file-size")
    public abstract HumanReadableByteCount getMaximumFileSize();
}

```

If this class were deserialized from some YAML file this may look something like:

```yaml
# example.yml
maximum-connect-timeout: 2 minutes
maximum-file-size: 10 mibibytes
```

License
-------
This repository is made available under the [Apache 2.0 License](http://www.apache.org/licenses/LICENSE-2.0). 
