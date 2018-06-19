package net.minecraftforge.actuarius.commands;

import discord4j.core.object.entity.MessageChannel;
import reactor.core.publisher.Mono;

@FunctionalInterface
public interface Command {
    
    Mono<?> invoke(MessageChannel channel, String... args) throws CommandException;
    
    default String description() {
        return "None";
    }
    
    default String usage() {
        return "None";
    }

}
