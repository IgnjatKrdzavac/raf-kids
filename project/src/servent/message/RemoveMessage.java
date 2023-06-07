package servent.message;

public class RemoveMessage extends BasicMessage{

    public RemoveMessage(String senderIpAddress, int senderPort, String receiverIpAddress, int receiverPort, String path) {
        super(MessageType.REMOVE, senderIpAddress, senderPort, receiverIpAddress, receiverPort, path);
    }
}
