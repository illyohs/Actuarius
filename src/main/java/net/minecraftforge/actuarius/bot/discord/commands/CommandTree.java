package net.minecraftforge.actuarius.bot.discord.commands;

import reactor.core.publisher.Mono;

import java.util.*;

public class CommandTree implements Command {
    
    private final Map<String, Command> subCommands = new HashMap<>();
    
    public CommandTree withNode(String name, Command command) {
        if (getSubcommand(name) == null) {
            this.subCommands.put(name.toLowerCase(Locale.ROOT), command);
        } else {
            throw new IllegalArgumentException("This subcommand already exists: " + name);
        }
        return this;
    }
    
    @Override
    public Mono<?> invoke(Context ctx) throws CommandException {
        if (ctx.getArgs().length == 0) {
            throw new CommandException("Not enough arguments.");
        }
        Command subCommand = getSubcommand(ctx.getArgs()[0]);
        if (subCommand == null) {
            throw new CommandException("No known sub-command: " + ctx.getArgs()[0]);
        }
        return subCommand.invoke(ctx.stripArgs(1));
    }
    
    public Command getSubcommand(String name) {
        return subCommands.get(name.toLowerCase(Locale.ROOT));
    }
    
    public Collection<String> getSubCommands() {
        return Collections.unmodifiableSet(subCommands.keySet());
    }
}
