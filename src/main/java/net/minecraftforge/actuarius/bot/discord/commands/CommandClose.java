package net.minecraftforge.actuarius.bot.discord.commands;

import net.minecraftforge.actuarius.github.CloseAction;
import net.minecraftforge.actuarius.github.IGitHubAction;
import net.minecraftforge.actuarius.util.GHInstallation;
import reactor.core.publisher.Mono;

public class CommandClose extends GitCommand
{
    IGitHubAction action;

    @Override
    public Mono<?> gitCommand(GHInstallation installation, Context ctx)
    {
        String[] args = ctx.getArgs();
        int issue = Integer.valueOf(ctx.getArgs()[0]);

        action = new CloseAction();

        action.execute(installation, issue, ctx.getAuthor().getDisplayName(), issue, args);

        return null;
    }
}
