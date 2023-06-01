package servent.message;

import app.ServentInfo;

import java.io.Serial;

public class TokenMessage extends BasicMessage{

    @Serial
    private static final long serialVersionUID = 2084490973699262440L;
    public TokenMessage(String senderIpAddress, int senderPort, String receiverIpAddress, int receiverPort) {
        super(MessageType.TOKEN, senderIpAddress, senderPort, receiverIpAddress, receiverPort);
    }

}
