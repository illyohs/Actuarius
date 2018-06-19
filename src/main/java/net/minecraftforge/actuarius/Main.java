package net.minecraftforge.actuarius;

import java.io.IOException;
import java.security.Security;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import com.electronwill.nightconfig.core.file.FileConfig;

import discord4j.core.ClientBuilder;
import discord4j.core.DiscordClient;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.object.util.Snowflake;
import net.minecraftforge.actuarius.commands.Command;
import net.minecraftforge.actuarius.commands.CommandException;
import net.minecraftforge.actuarius.commands.CommandRepoInfo;
import net.minecraftforge.actuarius.commands.CommandTree;
import net.minecraftforge.actuarius.util.ArgUtil;
import reactor.core.publisher.Hooks;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuples;

public class Main extends CommandTree {
    
    @SuppressWarnings("null")
    public static final FileConfig config = FileConfig.builder("actuarius.toml").defaultResource("/default_config.toml").autosave().build();

    public static void main(String[] unused) throws IOException {
        Security.addProvider(new BouncyCastleProvider());

        config.load();
        
        String token = config.get("discord.token");
       
        if (token == null) {
            throw new IllegalArgumentException("No token provided.");
        }
        
        Hooks.onOperatorDebug();
        
        DiscordClient client = new ClientBuilder(token).build();
        
        CommandTree rootCommand = new Main();
        
        rootCommand
            .withNode("help", (c, args) -> {
                String reply;
                if (args.length == 0) {
                    reply = "Available commands: " + rootCommand.getSubCommands();
                } else {
                    Command cmd = rootCommand.getSubcommand(args[0]);
                    if (cmd == null) {
                        throw new CommandException("No such command.");
                    }
                    reply = "**" + args[0] + " -- " + cmd.description() + "**\nUsage: " + args[0] + " " + cmd.usage();
                }
                
                return c.createMessage(spec -> spec.setContent(reply));
            })
            .withNode("info", new CommandRepoInfo());
        
        client.getEventDispatcher().on(MessageCreateEvent.class)
                
            // Find all messages mentioning us
            .filterWhen(e -> e.getMessage().getUserMentions().next().map(u -> client.getSelfId().map(u.getId()::equals).orElse(false)))
            
            // Filter for messages where the mention is the start of the content
            .filterWhen(e -> Mono.justOrEmpty(client.getSelfId()).flatMap(id -> isMentionFirst(id, e.getMessage())))
            
            // Create a tuple of <MessageChannel, String[]>
            .map(e -> Tuples.of(e, Mono.justOrEmpty(e.getMessage().getContent()).map(Main::getArguments)))
            
            // Unwrap the arguments and pass them to the command
            .flatMap(t -> t.getT2().flatMap(args -> t.getT1().getMessage().getChannel().flatMap(c -> {
                try {
                    return rootCommand.invoke(c, args);
                } catch (CommandException e1) {
                    // Generic error handling, can be improved
                    return c.createMessage(spec -> spec.setContent(e1.getMessage()));
                }
            })))
            .subscribe();

        // block() is required to prevent the VM exiting prematurely
        client.login().block();
    }
    
    private static Mono<Boolean> isMentionFirst(Snowflake id, Message message) {
        return Mono.just(message)
                .filterWhen(m -> m.getUserMentions()
                                    .map(u -> u.getId().equals(id)))
                .map(m -> m.getContent().map(s -> s.matches("^<!?@" + id.asLong() + ">.*$")).orElse(false));
    }
    
    private static String[] getArguments(String message) {
        String[] in = message.split("\\s+");
        return ArgUtil.withoutFirst(in);
    }
}
