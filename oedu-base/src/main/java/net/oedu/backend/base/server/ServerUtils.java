package net.oedu.backend.base.server;

import net.oedu.backend.base.endpoints.Response;
import net.oedu.backend.base.endpoints.ResponseAction;
import net.oedu.backend.base.sql.models.TableModelAutoId;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ServerUtils {

    protected ServerUtils() {
        throw new RuntimeException("NOT_A_INSTANCE_CLASS");
    }

    private static final List<Action> ACTIONS = new ArrayList<>();

    public static void sendMessage(final String type, final TableModelAutoId user, final Response response, final String tag) {
        for (Action a : ACTIONS) {
            a.sendMessage(type, user, response, tag);
        }
    }

    public static void action(final TableModelAutoId session, final ResponseAction action, final Object data) {
        for (Action a : ACTIONS) {
            a.action(session, action, data);
        }
    }

    public static void sendFile(final TableModelAutoId session, final File file) {
        for (Action a : ACTIONS) {
            a.sendFile(session, file);
        }
    }

    public static void addListener(final Action action) {
        ACTIONS.add(action);
    }


    public interface Action {
        void sendMessage(String type, TableModelAutoId userOrSession, Response response, String tag);

        void action(TableModelAutoId session, ResponseAction action, Object data);

        void sendFile(TableModelAutoId session, File file);
    }
}
