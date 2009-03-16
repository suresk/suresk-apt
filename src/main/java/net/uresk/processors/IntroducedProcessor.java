package net.uresk.processors;

import java.util.List;
import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVisitor;
import javax.lang.model.util.SimpleTypeVisitor6;
import javax.tools.Diagnostic.Kind;

/**
 * @author suresk
 */
@SupportedAnnotationTypes("net.uresk.processors.Introduced")
@SupportedSourceVersion(SourceVersion.RELEASE_6)
public class IntroducedProcessor extends AbstractProcessor {

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment env) {
        for(TypeElement t : annotations){
            processMethodsMarkedIntroduced(t, env);
        }
        return true;
    }

    private void processMethodsMarkedIntroduced(TypeElement type, RoundEnvironment env){
        for(Element element : env.getElementsAnnotatedWith(type)){
            processMethod(element);
        }
    }

    private void processMethod(Element element){
        if(doesOverrideMethod(element)){
            this.processingEnv.getMessager().printMessage(Kind.ERROR, "Cannot override a method", element);
        }
    }

    private boolean doesOverrideMethod(Element element){
        boolean foundMatchingMethod = false;
        ExecutableElement method = (ExecutableElement)element;
        TypeElement te = (TypeElement)element.getEnclosingElement();
        TypeMirror mirror = te.getSuperclass();
        Element superclass = this.processingEnv.getTypeUtils().asElement(mirror);
        for(Element e : superclass.getEnclosedElements()){
            if(e.asType().getKind() == TypeKind.EXECUTABLE  && e.getModifiers().contains(Modifier.PUBLIC)){
                if(method.getSimpleName().equals(e.getSimpleName()) && e.asType().accept(methodVisitor, method.getParameters())){
                    foundMatchingMethod = true;
                }
            }
        }
        return foundMatchingMethod;
    }

    private static final TypeVisitor<Boolean, List <? extends VariableElement>> methodVisitor =
            new SimpleTypeVisitor6<Boolean, List <? extends VariableElement>>(){

        @Override
        public Boolean visitExecutable(ExecutableType t, List <? extends VariableElement> v) {

            //If they don't have the same number of params, we can assume they are different
            if(t.getTypeVariables().size() != v.size()){
                return false;
            }

            //If they both have 0, then they are the same
            if(t.getTypeVariables().size() == 0){
                return true;
            }

            return true;
        }

   };
}
