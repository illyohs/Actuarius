package net.minecraftforge.actuarius.bot.discord;

import discord4j.core.DiscordClient;
import discord4j.core.DiscordClientBuilder;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.object.util.Snowflake;
import net.minecraftforge.actuarius.Main;
import net.minecraftforge.actuarius.bot.IBot;
import net.minecraftforge.actuarius.bot.discord.commands.*;
import reactor.core.publisher.Hooks;
import reactor.core.publisher.Mono;

public class DiscordBot extends CommandTree implements IBot
{

    DiscordClient client = null;

    @Override
    public void onStart()
    {
        Hooks.onOperatorDebug();
        client = new DiscordClientBuilder(getToken()).build();
        client.login();

        CommandTree rootCommand = new DiscordBot();

        rootCommand
                .withNode("help", (ctx) -> {
                    String reply;
                    if (ctx.getArgs().length == 0) {
                        reply = "Available commands: " + rootCommand.getSubCommands();
                    } else {
                        String cmdName = ctx.getArgs()[0];
                        Command cmd = rootCommand.getSubcommand(cmdName);
                        if (cmd == null) {
                            throw new CommandException("No such command.");
                        }
                        reply = "**" + cmdName + " -- " + cmd.description() + "**\nUsage: " + cmdName + " " + cmd.usage();
                    }

                    return ctx.getChannel().flatMap(c -> c.createMessage(spec -> spec.setContent(reply)));
                })
                .withNode("info", new CommandRepoInfo())
                .withNode("label", new CommandLabel());

        client.getEventDispatcher().on(MessageCreateEvent.class)

                // Find all messages mentioning us
                .filterWhen(e -> e.getMessage().getUserMentions().next().map(u -> client.getSelfId().map(u.getId()::equals).orElse(false)))

                // Filter for messages where the mention is the start of the content
                .filterWhen(e -> Mono.justOrEmpty(client.getSelfId()).flatMap(id -> isMentionFirst(id, e.getMessage())))
                .filter(e -> e.getMessage().getContent().isPresent())
                .filter(e -> e.getGuildId().isPresent())
                .map(Context::new)
                .flatMap(ctx -> {
                    try {
                        return rootCommand.invoke(ctx);
                    } catch (Exception e1) {
                        // Generic error handling, can be improved
                        return ctx.getChannel().map(c -> c.createMessage(spec -> spec.setContent(e1.getMessage())));
                    }
                })
                .subscribe();

    }

    @Override
    public void onStop()
    {
        client.logout();
    }


    public String getToken()
    {
       if (Main.config.get("discord.token") == null)
       {
           try
           {
               throw new IllegalAccessException("Not token provided");
           } catch (IllegalAccessException e)
           {
               e.printStackTrace();
           }
       }

        return Main.config.get("discord.token");
    }

    private static Mono<Boolean> isMentionFirst(Snowflake id, Message message) {
        return Mono.just(message)
                .filterWhen(m -> m.getUserMentions()
                        .map(u -> u.getId().equals(id)))
                .map(m -> m.getContent().map(s -> s.matches("^<!?@" + id.asLong() + ">.*$")).orElse(false));
    }
}
