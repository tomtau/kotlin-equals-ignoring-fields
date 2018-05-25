# kotlin-equals-ignoring-fields
Compares two data cases classes excluding specific field names.

## Example
```
@GenEqualsIgnore(
    [EqualsIgnore(["id", "count"]),
    (EqualsIgnore(["id"], false))]
)
data class Measurement(val id: Long, val date: Long, val count: Int, val test: ByteArray = ByteArray(1))

...
    val m1 = Measurement(1, 1131, 3)
    val m2 = Measurement(2, 1131, 2)
    println(m1 == m2) // false
    println(m1.equalsIgnore_id(m2)) // false
    println(m1.equalsIgnore_id_count(m2)) // true
```

## Usage
Put the `@GenEqualsIgnore` annotation on the data classes where
equals methods excluding certain fields are desired. Each
`EqualsIgnore` inside `GenEqualsIgnore` will generate one method.
The first argument of `EqualsIgnore` is the array of field names
that should be excluded from the equality comparison.
The second argument of `EqualsIgnore` indicates whether the 
arrays should be compared using the structural equality (`Arrays.equals`)
rather than the referential equality.
The second argument is optional (the default value is true).
The third argument of `EqualsIgnore` is an optional method name: if it's not set, the
corresponding method names are auto-generated as shown above (`equalsIgnore_<fields>`).

build.gradle example:
```
buildscript {
    ext.kotlin_version = '1.2.41'
    ext.ignore_equals_version = '0.3'

    repositories {
        mavenCentral()
    }
    dependencies {
        classpath "org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlin_version"
    }
}


apply plugin: 'kotlin'
apply plugin: 'kotlin-kapt'

kapt {
    generateStubs = true
}

repositories {
    mavenCentral()
    maven {
        url  "https://dl.bintray.com/tomastauber/equals-ignoring-fields"
    }
}

sourceSets {
    main {
        java {
            srcDir "${buildDir.absolutePath}/tmp/kapt/main/kotlinGenerated/"
        }
    }
}

dependencies {
    kapt 'io.cryptoblk:equals-ignoring-fields:$ignore_equals_version'
    compileOnly 'io.cryptoblk:equals-ignoring-fields:$ignore_equals_version'
    compile "org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kotlin_version"
}


```