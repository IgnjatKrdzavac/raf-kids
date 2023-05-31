package commands;

public class NotFoundCommand implements Command{
    @Override
    public void execute() {
        System.out.println("Command not found");
    }

    @Override
    public void setArguments(String args) {

    }

    @Override
    public String getCommandName() {
        return null;
    }

    @Override
    public boolean isFatal() {
        return false;
    }
}
