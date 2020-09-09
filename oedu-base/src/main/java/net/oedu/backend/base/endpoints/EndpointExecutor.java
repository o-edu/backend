package net.oedu.backend.base.endpoints;

import net.oedu.backend.base.sql.models.TableModelAutoId;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;

public class EndpointExecutor {

    private final Map<String, EndpointStorage> endpoints = new HashMap<>();
    private final AnnotationConfigApplicationContext applicationContext;

    private TableModelAutoId user;
    private TableModelAutoId session;

    public EndpointExecutor(final AnnotationConfigApplicationContext applicationContext, final Set<Class<? extends EndpointClass>> endpoints) {
        this.applicationContext = applicationContext;
        setup(endpoints);
    }

    private void setup(final Set<Class<? extends EndpointClass>> classes) {
        for (Class<? extends EndpointClass> clazz : classes) {
            EndpointStorage s = new EndpointStorage(clazz);
            endpoints.put(s.getName(), s);
            for (Method m : clazz.getMethods()) {
                EndpointSetup anno = m.getAnnotation(EndpointSetup.class);
                if (anno != null) {
                    execute(s.getObject(), m, new HashMap<>());
                    break;
                }
            }
        }
    }

    /**
     * @param req     the req from the user
     * @param user    the user id
     * @param session the session
     * @return a response
     */
    public Response execute(final Map<?, ?> req, final TableModelAutoId session, final TableModelAutoId user) {
        return execute((String) req.get("endpoint"), (Map<?, ?>) req.get("data"), session, user);
    }

    /**
     * executes an endpoint method.
     *
     * @param endpoint the string of the endpoint
     * @param data     the data as map
     * @param user     the user the req came from
     * @param session  the session id
     * @return the response
     */
    public Response execute(final String endpoint, final Map<?, ?> data, final TableModelAutoId session, final TableModelAutoId user) {
        this.user = user;
        this.session = session;
        int index = endpoint.lastIndexOf('/');
        String endpointBegin = endpoint.substring(0, index);
        String endpointEnd = endpoint.substring(index + 1);
        EndpointStorage endpointStorage = endpoints.get(endpointBegin);
        if (endpointStorage == null) {
            return new Response(500, "INVALID_ENDPOINT");
        }
        Method end = endpointStorage.getMethods().get(endpointEnd);
        if (end == null) {
            return new Response(500, "INVALID_ENDPOINT");
        }
        return execute(endpointStorage.getObject(), end, data);
    }

    private Response execute(final Object object, final Method end, final Map<?, ?> data) {
        List<Object> params = new ArrayList<>();
        for (Parameter p : end.getParameters()) {
            EndpointParameter param = p.getAnnotation(EndpointParameter.class);
            Object arg = null;
            switch (param.type()) {
                case NORMAL:
                    arg = data.getOrDefault(param.value(), null);
                    break;
                case REPOSITORY:
                    arg = applicationContext.getBean(param.value() + "Repository");
                    break;
                case USER:
                    arg = user;
                    break;
                case SESSION:
                    arg = session;
                default:
                    break;
            }
            if (!param.optional() && arg == null) {
                if (param.type() != EndpointParameterType.NORMAL) {
                    return new Response(400, "NOT_LOGGED_IN");
                }
                return new Response(500, "INVALID_ARGUMENTS");
            }
            params.add(arg);
        }
        System.out.println(params);
        try {
            return (Response) end.invoke(object, params.toArray());
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
            return new Response(500, "ERROR_WITH_ARGUMENTS");
        }
    }
}
