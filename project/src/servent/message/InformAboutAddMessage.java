package servent.message;

import app.file_util.FileInfo;

public class InformAboutAddMessage extends BasicMessage{

    private final String requesterIpAddress;
    private final int requesterPort;
    private final FileInfo fileInfo;

    public InformAboutAddMessage(String senderIpAddress, int senderPort, String receiverIpAddress, int receiverPort,
                            String requesterIpAddress, int requesterPort, FileInfo fileInfo) {
        super(MessageType.ADD_INFORM, senderIpAddress, senderPort, receiverIpAddress, receiverPort);

        this.requesterIpAddress = requesterIpAddress;
        this.requesterPort = requesterPort;
        this.fileInfo = fileInfo;
    }
    @Override
    public String getSenderIpAddress() {
        return null;
    }

    @Override
    public int getSenderPort() {
        return 0;
    }

    @Override
    public int getReceiverPort() {
        return 0;
    }

    @Override
    public String getReceiverIpAddress() {
        return null;
    }

    @Override
    public MessageType getMessageType() {
        return null;
    }

    @Override
    public String getMessageText() {
        return null;
    }

    @Override
    public int getMessageId() {
        return 0;
    }

    public String getRequesterIpAddress() {
        return requesterIpAddress;
    }

    public int getRequesterPort() {
        return requesterPort;
    }

    public FileInfo getFileInfo() {
        return fileInfo;
    }
}
