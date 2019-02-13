package net.minecraftforge.actuarius.github;

import net.minecraftforge.actuarius.util.GHInstallation;
import org.kohsuke.github.GHIssue;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GHUser;

import java.io.IOException;

public class AssignAction implements IGitHubAction
{
    @Override
    public void execute(GHInstallation installation, String repo, String executor, int issue, String... args) throws IOException
    {
        GHRepository repository = installation.getClient().getRepository(repo);
        GHIssue is = repository.getIssue(issue);
        GHUser user = installation.getClient().getUser(args[0]);
        is.assignTo(user);
    }
}
