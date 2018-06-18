package net.minecraftforge.actuarius.hook.github;

import org.apache.commons.io.IOUtils;
import spark.Request;
import spark.Response;

import java.io.IOException;

import net.minecraftforge.actuarius.hook.IHook;

public class GitHubHook implements IHook
{
    String name;

    public GitHubHook(String name)
    {
        this.name = name;
    }

    @Override
    public String getName()
    {
        return this.name;
    }

    @Override
    public Object init(Request req, Response rep) throws IOException
    {
        String sig     = req.headers("X-Hub-Signature");
        String event   = req.headers("X-GitHub-GitEvent");
        byte[] payload = IOUtils.toByteArray(req.raw().getInputStream());
        String json    = new String(payload);
        return new GitHubEvents(json);
    }
}
