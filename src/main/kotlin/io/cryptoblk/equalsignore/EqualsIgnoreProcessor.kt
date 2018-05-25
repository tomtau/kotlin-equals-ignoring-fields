package io.cryptoblk.equalsignore

import com.google.auto.service.AutoService
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.asTypeName
import java.io.File
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.Processor
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.SourceVersion
import javax.lang.model.element.Element
import javax.lang.model.element.ElementKind
import javax.lang.model.element.Modifier
import javax.lang.model.element.TypeElement
import javax.lang.model.type.TypeKind

@AutoService(Processor::class)
class EqualsIgnoreProcessor : AbstractProcessor() {
    override fun getSupportedAnnotationTypes(): MutableSet<String> = mutableSetOf(GenEqualsIgnore::class.java.name)

    override fun getSupportedSourceVersion(): SourceVersion = SourceVersion.latest()

    override fun process(set: MutableSet<out TypeElement>, roundEnv: RoundEnvironment): Boolean {
        roundEnv.getElementsAnnotatedWith(GenEqualsIgnore::class.java)
            .forEach {
                val gen = it.getAnnotation(GenEqualsIgnore::class.java)
                val pack = processingEnv.elementUtils.getPackageOf(it).toString()
                generateClass(it, pack, gen.annotations)
            }
        return true
    }

    private fun generateClass(klass: Element, pack: String, annotations: Array<EqualsIgnore>) {
        val fileName = "${klass.simpleName}EqualsIgnore"
        val file = FileSpec.builder(pack, fileName)
        file.addStaticImport("java.util", "Arrays")
        annotations.forEach {
            val name = if (it.customMethodName.isEmpty()) "equalsIgnore_${it.fields.joinToString("_")}" else it.customMethodName
            val ignored = it.fields.toSet()
            val arrayEquals = it.arraysContentEquals
            val string = StringBuilder("if (this === other) return true\n")
            klass.enclosedElements.forEach {
                if (it.kind == ElementKind.FIELD && it.toString() !in ignored && Modifier.STATIC !in it.modifiers) {
                    if (it.asType().kind == TypeKind.ARRAY && arrayEquals) {
                        string.appendln("if (!Arrays.equals(this.$it, other.$it)) return false")
                    } else {
                        string.appendln("if (this.$it != other.$it) return false")
                    }
                }
            }
            string.appendln("return true")
            file.addFunction(FunSpec.builder(name)
                .returns(Boolean::class)
                .receiver(klass.asType().asTypeName())
                .addCode(string.toString())
                .addParameter("other", klass.asType().asTypeName())
                .build())
        }

        val bfile = file.build()

        val kaptKotlinGeneratedDir = processingEnv.options[KAPT_KOTLIN_GENERATED_OPTION_NAME]
        bfile.writeTo(File(kaptKotlinGeneratedDir, "$fileName.kt"))
    }

    companion object {
        const val KAPT_KOTLIN_GENERATED_OPTION_NAME = "kapt.kotlin.generated"
    }
}