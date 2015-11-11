package controllers;

import actors.CheckActor;
import dtos.CometUser;
import dtos.User;
import play.libs.Comet;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.index;

public class Application extends Controller {

    public Result index() {
        return ok(index.render("Comet × Akka Actor でブラウザpush"));
    }

    /**
     * Cometを作成してActorを呼ぶ
     * @return
     */
    public Result count() {
        User user = new User("testuser", 0);
        return ok(new Comet("parent.cometMessage") {
            @Override
            public void onConnected() {
                CheckActor.instance.tell(new CometUser(this, user), null);
            }
        });
    }
}
