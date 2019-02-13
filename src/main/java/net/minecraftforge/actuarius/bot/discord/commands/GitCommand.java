package net.minecraftforge.actuarius.bot.discord.commands;

import net.minecraftforge.actuarius.util.GHInstallation;
import net.minecraftforge.actuarius.util.GithubUtil;
import reactor.core.publisher.Mono;

import java.util.Optional;

public abstract class GitCommand implements Command
{

    Context context;
    String args[];

    @Override
    public Mono<?> invoke(Context ctx) throws CommandException
    {
        String[] args = ctx.getArgs();

        GHInstallation installation;
        try {
            installation = GHInstallation.fromConfig();
        } catch (GHInstallation.NoSuchInstallationException e) {
            try {
                installation = GHInstallation.org(args[0]);
            } catch (GHInstallation.NoSuchInstallationException e1) {
                try {
                    installation = GHInstallation.repo(args[0], args[1]);
                } catch (GHInstallation.NoSuchInstallationException e2) {
                    throw new CommandException("No such repository, or no installation on that repository.", e);
                } catch (ArrayIndexOutOfBoundsException e2) {
                    throw new CommandException("Not enough arguments");
                }
            } catch (ArrayIndexOutOfBoundsException e1) {
                throw new CommandException("Not enough arguments");
            }
        }


        return gitCommand(installation, ctx);
    }

    public abstract Mono<?> gitCommand(GHInstallation installation, Context ctx);

    public String getRepo() {

        Optional<String> defualtInstall = GithubUtil.defaultInstallation();
        String repo;

        if (defualtInstall.isPresent() && GithubUtil.forceDefault()) {
            repo = defualtInstall.get();
            if (repo.indexOf('/') < 0) {
//                repo += context.
            }
        }
        return  null;
    }
}
