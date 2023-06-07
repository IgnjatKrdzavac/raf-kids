package servent.handler;

import servent.message.Message;

public class RemoveHandler implements MessageHandler{

    private final Message clientMessage;

    public RemoveHandler(Message clientMessage){
        this.clientMessage = clientMessage;
    }

    @Override
    public void run() {

    }
}
