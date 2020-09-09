package net.oedu.backend.base.endpoints;

import lombok.Getter;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

@Getter
public class EndpointStorage {

    private String name;
    private Map<String, Method> methods = new HashMap<>();
    private EndpointClass object;

    public EndpointStorage(final Class<? extends EndpointClass> endpointClass) {
        try {
            object = endpointClass.getConstructor().newInstance();
            name = object.getName();
            initMethods();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    private void initMethods() {
        for (Method method: object.getClass().getDeclaredMethods()) {
            Endpoint endpoint = method.getAnnotation(Endpoint.class);
            if (endpoint == null) continue;
            String name = endpoint.value();
            methods.put(name, method);
        }
    }
}
