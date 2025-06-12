package websocket.server;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonObject;
import websocket.commands.*;
import websocket.messages.*;

public class GsonFactory {

    public static GsonBuilder websocketBuilder() {
        GsonBuilder builder = new GsonBuilder();

        builder.registerTypeAdapter(UserGameCommand.class, (JsonDeserializer<UserGameCommand>)
                (json, typeOfT, context) -> {
                    JsonObject object = json.getAsJsonObject();
                    UserGameCommand.CommandType kind =
                            UserGameCommand.CommandType.valueOf(object.get("commandType").getAsString());

                    return switch (kind) {
                        case CONNECT   -> context.deserialize(object, UserGameCommand.ConnectCommand.class);
                        case MAKE_MOVE -> context.deserialize(object, UserGameCommand.MakeMoveCommand.class);
                        case LEAVE     -> context.deserialize(object, UserGameCommand.LeaveCommand.class);
                        case RESIGN    -> context.deserialize(object, UserGameCommand.ResignCommand.class);
                    };
                });

        builder.registerTypeAdapter(ServerMessage .class, (JsonDeserializer<ServerMessage>)
                (json, typeOfT, context) -> {
                    JsonObject object = json.getAsJsonObject();
                    ServerMessage.ServerMessageType kind =
                            ServerMessage.ServerMessageType.valueOf(object.get("serverMessageType").getAsString());

                    return switch (kind) {
                        case LOAD_GAME    -> context.deserialize(object, ServerMessage.LoadGame.class);
                        case ERROR        -> context.deserialize(object, ServerMessage.Error.class);
                        case NOTIFICATION -> context.deserialize(object, ServerMessage.Notification.class);
                    };
                });

        return builder;
    }

}
