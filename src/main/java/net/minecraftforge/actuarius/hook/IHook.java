package net.minecraftforge.actuarius.hook;

import spark.Request;
import spark.Response;

import java.io.IOException;

public interface IHook
{
    String getName();

    Object init(Request req, Response rep) throws IOException;
}
