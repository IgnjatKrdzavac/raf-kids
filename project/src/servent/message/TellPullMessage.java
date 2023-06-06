package servent.message;

import app.file_util.FileInfo;

public class TellPullMessage extends BasicMessage{

    private final String requesterIpAddress;
    private final int requesterId;
    private final FileInfo fileInfo;

    public TellPullMessage(String senderIpAddress, int senderPort, String receiverIpAddress, int receiverPort,
                           String requesterIpAddress, int requesterId, FileInfo fileInfo) {

        super(MessageType.TELL_PULL, senderIpAddress, senderPort, receiverIpAddress, receiverPort);

        this.requesterIpAddress = requesterIpAddress;
        this.requesterId = requesterId;
        this.fileInfo = fileInfo;

    }

    public String getRequesterIpAddress() {
        return requesterIpAddress;
    }

    public int getRequesterId() {
        return requesterId;
    }

    public FileInfo getFileInfo() {
        return fileInfo;
    }
}
