package net.minecraftforge.actuarius.commands;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import discord4j.core.object.entity.MessageChannel;
import net.minecraftforge.actuarius.util.ArgUtil;
import reactor.core.publisher.Mono;

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
    public Mono<?> invoke(MessageChannel channel, String... args) throws CommandException {
        if (args.length == 0) {
            throw new CommandException("Not enough arguments.");
        }
        String[] subArgs = ArgUtil.withoutFirst(args);
        Command subCommand = getSubcommand(args[0]);
        if (subCommand == null) {
            throw new CommandException("No known sub-command: " + args[0]);
        }
        return subCommand.invoke(channel, subArgs);
    }
    
    public Command getSubcommand(String name) {
        return subCommands.get(name.toLowerCase(Locale.ROOT));
    }
    
    public Collection<String> getSubCommands() {
        return Collections.unmodifiableSet(subCommands.keySet());
    }
}
