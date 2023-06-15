package cli.command;

import app.AppConfig;
import mutex.TokenMutex;

public class RemoveCommand implements CLICommand{
    @Override
    public String commandName() {
        return "remove";
    }

    @Override
    public void execute(String args) {

        if (args == null || args.isEmpty()) {
            AppConfig.timestampedStandardPrint("Invalid argument for remove command. Should be remove path.");
            return;
        }

        String path = args.replace('/' , '\\');


        if (AppConfig.chordState.getStorageMap().containsKey(path)){
            if (AppConfig.chordState.getStorageMap().get(path).getOgNode() == AppConfig.myServentInfo.getChordId()){
                TokenMutex.lock();
                AppConfig.chordState.removeFileFromStorage(path);
                TokenMutex.unlock();
            } else AppConfig.timestampedErrorPrint("This node is not the main holder of the file, can not delete selected file");
        } else AppConfig.timestampedErrorPrint("Nonexistent path " + path);

    }
}
