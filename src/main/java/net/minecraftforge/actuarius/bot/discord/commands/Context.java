package net.minecraftforge.actuarius.bot.discord.commands;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.MessageChannel;
import discord4j.core.object.util.Snowflake;
import reactor.core.publisher.Mono;

import java.util.Optional;

public class Context {
    
    private final Message message;
    private final Member author;
    private final Optional<Snowflake> guildId;
    
    private final String[] args;

    public Context(MessageCreateEvent event) {
        this(event.getMessage(), event.getMember().orElseThrow(() -> new IllegalArgumentException("Message must be sent by a user.")), event.getGuildId(), parseArgs(event.getMessage().getContent()));
    }

    public Context(Message message, Member author, Optional<Snowflake> guildId, String... args) {
        this.message = message;
        this.author = author;
        this.guildId = guildId;
        this.args = args;
    }
    
    private static final String[] parseArgs(Optional<String> content) {
        String[] in = content.orElseThrow(() -> new IllegalArgumentException("Message must have content.")).split("\\s+");
        return withoutFirstN(in, 1);
    }
    
    private static String[] withoutFirstN(String[] in, int n) {
        if (in.length == 0) {
            return in;
        }
        String[] out = new String[in.length - n];
        System.arraycopy(in, n, out, 0, out.length);
        return out;
    }
    
    public Context stripArgs(int amount) {
        return new Context(getMessage(), getAuthor(), getGuildId(), withoutFirstN(args, amount));
    }

    public Message getMessage() {
        return message;
    }
    
    public Member getAuthor() {
        return author;
    }
    
    public Optional<Snowflake> getGuildId() {
        return guildId;
    }

    public String[] getArgs() {
        return args;
    }
    
    public Mono<Guild> getGuild() {
        return getMessage().getGuild();
    }
    
    public Mono<MessageChannel> getChannel() {
        return getMessage().getChannel();
    }
}
