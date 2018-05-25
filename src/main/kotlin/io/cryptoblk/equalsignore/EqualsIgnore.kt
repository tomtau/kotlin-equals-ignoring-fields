package io.cryptoblk.equalsignore

@Target(AnnotationTarget.CLASS)
annotation class GenEqualsIgnore(val annotations: Array<EqualsIgnore>)

@Target(AnnotationTarget.CLASS)
annotation class EqualsIgnore(val fields: Array<String>, val arraysContentEquals: Boolean = true, val customMethodName: String = "")