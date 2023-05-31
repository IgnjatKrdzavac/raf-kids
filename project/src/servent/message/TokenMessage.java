package servent.message;

import app.ServentInfo;

import java.io.Serial;

public class TokenMessage extends BasicMessage{

    @Serial
    private static final long serialVersionUID = -4345742380547149474L;

    public TokenMessage(int senderPort, int receiverPort) {
        super(MessageType.TOKEN, senderPort, receiverPort);
    }


}
