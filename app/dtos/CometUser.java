package dtos; /**
 * @author Yuki Hirai
 */

import play.libs.Comet;

/**
 * アクターに渡す用
 */
public class CometUser {
    public Comet comet;
    public User user;

    public CometUser(Comet comet, User user) {
        this.comet = comet;
        this.user = user;
    }
}
