package net.minecraftforge.actuarius.commands;

import java.io.IOException;
import java.util.Optional;

import org.kohsuke.github.GHRepository;

import discord4j.core.object.entity.MessageChannel;
import net.minecraftforge.actuarius.util.ArgUtil;
import net.minecraftforge.actuarius.util.GHInstallation;
import net.minecraftforge.actuarius.util.GHInstallation.NoSuchInstallationException;
import net.minecraftforge.actuarius.util.GithubUtil;
import reactor.core.publisher.Mono;

public class CommandLabel implements Command {

    @Override
    public Mono<?> invoke(MessageChannel channel, String... args) throws CommandException {
        
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
                    throw new CommandException("No such repository.", e);
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
                args = ArgUtil.withoutFirst(args);
            }
        } else {
            repoName = args[0] + "/" + args[1];
            args = ArgUtil.withoutFirst(ArgUtil.withoutFirst(args));
        }
        
        try {
            GHRepository repo = installation.getClient().getRepository(repoName);
            repo.getIssue(1).setLabels(args);
        } catch (IOException e) {
            throw new CommandException(e);
        }
        
        return channel.createMessage(spec -> spec.setContent("Labels updated"));
    }

}
