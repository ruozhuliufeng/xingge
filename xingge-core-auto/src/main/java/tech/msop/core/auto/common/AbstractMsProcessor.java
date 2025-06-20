package tech.msop.core.auto.common;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Set;

public abstract class AbstractMsProcessor extends AbstractProcessor {

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    /**
     * AutoService 注解处理器
     *
     * @param annotations 注解 getSupportedAnnotationTypes
     * @param roundEnv    扫描到的注解
     * @return 是否完成
     */
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        try {
            return processImpl(annotations, roundEnv);
        } catch (Exception e) {
            fatalError(e);
            return false;
        }
    }

    protected abstract boolean processImpl(Set<? extends TypeElement> annotations, RoundEnvironment roundEnvironment);

    protected void log(String msg) {
        if (processingEnv.getOptions().containsKey("debug")) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, msg);
        }
    }

    protected void error(String msg, Element element, AnnotationMirror annotation) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, msg, element, annotation);
    }

    protected void fatalError(Exception e) {
        StringWriter writer = new StringWriter();
        e.printStackTrace(new PrintWriter(writer));
        fatalError(writer.toString());
    }

    protected void fatalError(String msg) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "FATAL ERROR：" + msg);
    }
}
