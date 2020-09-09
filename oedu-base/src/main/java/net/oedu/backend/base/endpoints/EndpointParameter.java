package net.oedu.backend.base.endpoints;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface EndpointParameter {

    String value();
    boolean optional() default false;
    EndpointParameterType type() default EndpointParameterType.NORMAL;
}
