package actors;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import com.fasterxml.jackson.databind.JsonNode;
import dtos.CometUser;
import dtos.User;
import play.Logger;
import play.libs.Akka;
import play.libs.Comet;
import play.libs.F;
import play.libs.Json;
import scala.concurrent.duration.Duration;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
/**
 * Created by yukihirai on 2015/11/05.
 */
public class CheckActor extends UntypedActor {
    /**
     * singleton
     */
    public final static ActorRef instance = Akka.system().actorOf(Props.create(CheckActor.class));

    final static public HashMap<Comet, User> sockets = new HashMap<Comet, User>();

    /**
     * スケジュール処理（定期処理）
     * 一定間隔でPUSHを行う
     */
    static {
        //定期的にComet経由の通知を行う
        Akka.system().scheduler().schedule(
                Duration.Zero(),
                Duration.create(1000, TimeUnit.MILLISECONDS),
                instance,
                "PLUS_COUNT",
                Akka.system().dispatcher(),
                null
        );
    }

    /**
     * Mapからコメットを取り出してviewにpush
     * @param user
     */
    public void sentMessage(User user) {
        JsonNode message = Json.toJson(user);
        for (Map.Entry<Comet, User> ck : sockets.entrySet()) {
            ck.getKey().sendMessage(message);
        }
    }

    public void onReceive(Object message) {

        if (message instanceof CometUser) {
            CometUser data = (CometUser) message;
            Comet cometObj = data.comet;
            User user = data.user;
            if (cometObj instanceof Comet) {
                final Comet cometSocket = (Comet) cometObj;
                cometSocket.onDisconnected(new F.Callback0() {
                    //通信が途絶えた場合の処理
                    public void invoke() {
                        if (sockets.containsKey(cometSocket)) {
                            sockets.remove(cometSocket);
                            Logger.info("Browser disconnected (" + sockets.size() + " browsers currently connected)");
                        }
                    }
                });
                // 接続中
                sockets.put(cometSocket, user);
                Logger.info("New browser connected (" + sockets.size() + " browsers currently connected)");
                instance.tell(user, null);
            }

        } else if (message instanceof User) {

            this.sentMessage((User) message);

        } else if (message instanceof String) {
            if ("PLUS_COUNT".equals(message)) {
                for (Map.Entry<Comet, User> ck : sockets.entrySet()) {
                    User user = (User) ck.getValue();
                    user.count += 1;
                    sentMessage(user);
                }
            }
        }
    }
}
