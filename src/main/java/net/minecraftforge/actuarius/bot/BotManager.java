package net.minecraftforge.actuarius.bot;

import net.minecraftforge.actuarius.bot.discord.DiscordBot;

import java.util.ArrayList;
import java.util.List;

public class BotManager
{
    List<IBot> bots = new ArrayList<>();


    public BotManager()
    {
        bots.add(new DiscordBot());
    }


    public void startBots()
    {
        bots.forEach(IBot::onStart);
    }

    public void stopBots()
    {
        bots.forEach(IBot::onStop);
    }

}
