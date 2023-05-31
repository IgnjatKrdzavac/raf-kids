package mutex;

import app.AppConfig;
import servent.message.TokenMessage;
import servent.message.util.MessageUtil;

public class TokenMutex{

    private static volatile boolean haveToken = false;
    private static volatile boolean wantLock = false;

    public void lock() {
        wantLock = true;

        while (!haveToken) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

    public void unlock() {
        haveToken = false;
        wantLock = false;
        sendTokenForward();
    }

    public static void receiveToken() {
        if (wantLock) {
            haveToken = true;
        } else {
            sendTokenForward();
        }
    }

    public static void sendTokenForward() {
//        int nextNodeId = (AppConfig.myServentInfo.getId() + 1) % AppConfig.getServentCount();
//
//        MessageUtil.sendMessage(new TokenMessage(AppConfig.myServentInfo, AppConfig.getInfoById(nextNodeId)));
    }


}
