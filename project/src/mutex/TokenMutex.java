package mutex;

import app.AppConfig;
import servent.message.Message;
import servent.message.TokenMessage;
import servent.message.util.MessageUtil;

public class TokenMutex{

    private static volatile boolean haveToken = false;
    private static volatile boolean wantLock = false;

    public static void init() {
        haveToken = true;
    }

    public static void lock() {
        wantLock = true;

        while (!haveToken) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

    public static void unlock() {
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
        String nextNodeIp = AppConfig.chordState.getNextNodeIp();
        int nextNodePort = AppConfig.chordState.getNextNodePort();

        Message tokenMessage = new TokenMessage(AppConfig.myServentInfo.getIpAddress(), AppConfig.myServentInfo.getListenerPort(),
                nextNodeIp, nextNodePort);
        MessageUtil.sendMessage(tokenMessage);
    }


}
