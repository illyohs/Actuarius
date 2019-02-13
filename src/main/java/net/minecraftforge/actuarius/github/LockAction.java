package net.minecraftforge.actuarius.github;

import net.minecraftforge.actuarius.util.GHInstallation;
import org.kohsuke.github.GHIssue;
import org.kohsuke.github.GHRepository;

import java.io.IOException;

public class LockAction implements  IGitHubAction
{
    @Override
    public void execute(GHInstallation installation, String repo, String executor, int issue, String... args) throws IOException
    {

        GHRepository repository = installation.getClient().getRepository(repo);

        GHIssue is = repository.getIssue(issue);
        is.comment("Issue was locked by " + executor);
        is.lock();
    }
}
