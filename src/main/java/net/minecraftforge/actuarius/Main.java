package net.minecraftforge.actuarius;

import discord4j.core.ClientBuilder;
import discord4j.core.DiscordClient;
import discord4j.core.event.domain.message.MessageCreateEvent;

public class Main {

    public static void main(String[] args) {
        
        String token = args.length > 0 ? args[0] : null;
       
        if (token == null) {
            throw new IllegalArgumentException("No token provided.");
        }
        
        DiscordClient client = new ClientBuilder(token).build();
        
        client.getEventDispatcher().on(MessageCreateEvent.class)
            // Find messages where the first mention is ourselves
            .filterWhen(e -> e.getMessage().getUserMentions().next().map(u -> client.getSelfId().map(u.getId()::equals).orElse(false)))
            .subscribe(e -> {
                e.getMessage().getChannel()
                    .flatMap(channel -> channel.createMessage(spec -> spec.setContent("Hello world, I am Actuarius!")))
                    .subscribe();
            });
        
        // block() is required to prevent the VM exiting prematurely
        client.login().block();
    }
}
