package net.minecraftforge.actuarius.hook;

import spark.Spark;

import java.util.ArrayList;
import java.util.List;

import net.minecraftforge.actuarius.hook.github.GitHubHook;

public class HookLoader
{
    List<IHook> hookRegistry = new ArrayList<>();

    public HookLoader()
    {
        hookRegistry.add(new GitHubHook("github"));
    }

    public void startHooks()
    {
        Spark.init();

        hookRegistry.forEach(iHook -> Spark.post("/" + iHook.getName(), iHook::init));
    }

    public void shutdownHooks()
    {
        Spark.stop();
    }

    public void restartHooks()
    {
        shutdownHooks();
        startHooks();
    }
}
