package servent.message;

public class UpdateMessage extends BasicMessage {

	private static final long serialVersionUID = 3586102505319194978L;

	public UpdateMessage(String senderIpAddress, int senderPort, String receiverIpAddress, int receiverPort, String text) {
		super(MessageType.UPDATE, senderIpAddress, senderPort, receiverIpAddress, receiverPort, text);
	}
}
