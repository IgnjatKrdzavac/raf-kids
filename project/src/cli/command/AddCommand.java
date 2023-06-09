package cli.command;

import app.AppConfig;
import app.file_util.FileInfo;
import app.file_util.FileUtils;
import mutex.TokenMutex;

import java.util.List;

public class AddCommand implements CLICommand{
    @Override
    public String commandName() {
        return "add";
    }

    @Override
    public void execute(String args) {

        if (args == null || args.isEmpty()) {
            AppConfig.timestampedStandardPrint("Invalid argument for add command. Should be add path.");
            return;
        }

        String path = args.replace('/' , '\\');
        TokenMutex.lock();

        if (FileUtils.isPathFile(AppConfig.ROOT_DIR, path)) {
            FileInfo fileInfo = FileUtils.getFileInfoFromPath(AppConfig.ROOT_DIR, path);
            if (fileInfo != null) {
                AppConfig.chordState.addToStorage(fileInfo, AppConfig.myServentInfo.getIpAddress(), AppConfig.myServentInfo.getListenerPort());
            }
        }
        else {
            List<FileInfo> fileInfoList = FileUtils.getDirectoryInfoFromPath(AppConfig.ROOT_DIR, path);
            if (!fileInfoList.isEmpty()) {
                for (FileInfo fileInfo : fileInfoList) {
                    AppConfig.chordState.addToStorage(fileInfo, AppConfig.myServentInfo.getIpAddress(), AppConfig.myServentInfo.getListenerPort());
                }
            }
        }

        TokenMutex.unlock();
    }


}
