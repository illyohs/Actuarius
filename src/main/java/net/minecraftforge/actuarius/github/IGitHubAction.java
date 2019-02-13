package net.minecraftforge.actuarius.github;

import net.minecraftforge.actuarius.util.GHInstallation;

import java.io.IOException;

public interface IGitHubAction
{
    void execute(GHInstallation installation, String repo, String executor, int issue, String... args) throws IOException;
}
