package servent.handler;

import app.AppConfig;
import app.ChordState;
import app.ServentInfo;
import app.file_util.FileInfo;
import servent.message.InformAboutAddMessage;
import servent.message.Message;
import servent.message.MessageType;
import servent.message.util.MessageUtil;

public class InformAboutAddHandler implements MessageHandler {

    private final Message clientMessage;

    public InformAboutAddHandler(Message clientMessage) { this.clientMessage = clientMessage; }
    @Override
    public void run() {
        if (clientMessage.getMessageType() == MessageType.ADD_INFORM) {
            InformAboutAddMessage additionInfoMsg = (InformAboutAddMessage) clientMessage;

            String requesterNode = additionInfoMsg.getReceiverIpAddress() + ":" + additionInfoMsg.getReceiverPort();
            int key = ChordState.chordHash(requesterNode);

            if (key == AppConfig.myServentInfo.getChordId()) {
                FileInfo fileInfo = additionInfoMsg.getFileInfo();
                AppConfig.chordState.addToStorage(fileInfo, additionInfoMsg.getSenderIpAddress(), additionInfoMsg.getSenderPort());
            }
            else {
                ServentInfo nextNode = AppConfig.chordState.getNextNodeForKey(key);
                Message nextSuccessMessage = new InformAboutAddMessage(
                        additionInfoMsg.getSenderIpAddress(), additionInfoMsg.getSenderPort(),
                        nextNode.getIpAddress(), nextNode.getListenerPort(),
                        additionInfoMsg.getRequesterIpAddress(), additionInfoMsg.getRequesterPort(),
                        additionInfoMsg.getFileInfo());
                MessageUtil.sendMessage(nextSuccessMessage);
            }
        } else {
            AppConfig.timestampedErrorPrint("Add success handler got message that's not of type ADD_SUCCESS.");
        }
    }
}
