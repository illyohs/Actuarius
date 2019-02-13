package net.minecraftforge.actuarius.bot.discord.commands;

import net.minecraftforge.actuarius.util.GHInstallation;
import net.minecraftforge.actuarius.util.GHInstallation.NoSuchInstallationException;
import net.minecraftforge.actuarius.util.GithubUtil;
import net.minecraftforge.actuarius.util.PermissionUtil;
import org.kohsuke.github.GHRepository;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.Optional;

public class CommandLabel implements Command {

    @Override
    public Mono<?> invoke(Context ctx) throws CommandException {
        
        String[] args = ctx.getArgs();
        
        GHInstallation installation;
        try {
             installation = GHInstallation.fromConfig();
        } catch (NoSuchInstallationException e) {
            try {
                installation = GHInstallation.org(args[0]);
            } catch (NoSuchInstallationException e1) {
                try {
                    installation = GHInstallation.repo(args[0], args[1]);
                } catch (NoSuchInstallationException e2) {
                    throw new CommandException("No such repository, or no installation on that repository.", e);
                } catch (ArrayIndexOutOfBoundsException e2) {
                    throw new CommandException("Not enough arguments");
                }
            } catch (ArrayIndexOutOfBoundsException e1) {
                throw new CommandException("Not enough arguments");
            }
        }
        
        Optional<String> defaultInstallation = GithubUtil.defaultInstallation();
        String repoName;
        if (defaultInstallation.isPresent() && GithubUtil.forceDefault()) {
            repoName = defaultInstallation.get();
            if (repoName.indexOf('/') < 0) {
                repoName += args[0];
                ctx = ctx.stripArgs(1);
            }
        } else {
            repoName = args[0] + "/" + args[1];
            ctx = ctx.stripArgs(2);
        }
        
        if (!PermissionUtil.canAccess(ctx.getAuthor(), repoName)) {
            throw new CommandException("No permission to access that repository.");
        }
        
        try {
            GHRepository repo = installation.getClient().getRepository(repoName);
            repo.getIssue(1).setLabels(args);
        } catch (IOException e) {
            throw new CommandException(e);
        }
        
        return ctx.getChannel().flatMap(c -> c.createMessage(spec -> spec.setContent("Labels updated")));
    }

}
