package servent.message;

import app.file_util.FileInfo;

public class AskPullMessage extends BasicMessage{

    private final FileInfo fileInfo;
    private final int requesterId;

    public AskPullMessage(String senderIpAddress, int senderPort,String receiverIpAddress, int receiverPort, int requesterId, FileInfo fileInfo) {
        super(MessageType.ASK_PULL, senderIpAddress, senderPort, receiverIpAddress, receiverPort);
        this.fileInfo = fileInfo;
        this.requesterId = requesterId;
    }

    public FileInfo getFileInfo() {
        return fileInfo;
    }

    public int getRequesterId() {
        return requesterId;
    }
}
