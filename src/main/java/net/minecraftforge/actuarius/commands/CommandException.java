package net.minecraftforge.actuarius.commands;


public class CommandException extends Exception {

    private static final long serialVersionUID = 7397025139691342944L;
    
    public CommandException(String msg) {
        super(msg);
    }

    public CommandException(String msg, Exception cause) {
        super(msg, cause);
    }
}
