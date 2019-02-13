package net.minecraftforge.actuarius.bot.discord.commands;

import net.minecraftforge.actuarius.util.GithubUtil;
import org.kohsuke.github.GHPerson;
import org.kohsuke.github.GHRepository;
import reactor.core.publisher.Mono;

import java.io.IOException;

public class CommandRepoInfo implements Command {

    @Override
    public Mono<?> invoke(Context ctx) throws CommandException {
        String[] args = ctx.getArgs();
        String owner = "MinecraftForge", repo;
        if (args.length >= 2) {
            owner = args[0];
            repo = args[1];
        } else if (args.length == 1) {
            repo = args[0];
        } else {
            throw new CommandException("Wrong number of arguments.");
        }
        
        try {
            
            GHPerson org;
            try {
                org = GithubUtil.getUnauthorizedClient().getOrganization(owner);
            } catch (IOException e) {
                org = GithubUtil.getUnauthorizedClient().getUser(owner);
            }
            
            if (org == null) {
                throw new CommandException("No such user.");
            }
            GHRepository repository = org.getRepository(repo);
            if (repository == null) {
                throw new CommandException("No such repository.");
            }
            
            return ctx.getChannel().flatMap(c -> c.createMessage(spec -> spec.setEmbed(embed -> {
                embed.setTitle(repository.getOwnerName() + "/" + repository.getName());
                String desc = repository.getDescription();
                if (desc != null) {
                    embed.setDescription(repository.getDescription());
                }
                embed.addField("Open Issues/PRs", String.valueOf(repository.getOpenIssueCount()), true);
                embed.addField("Stars", String.valueOf(repository.getStargazersCount()), true);
            })));
        } catch (IOException e) {
            e.printStackTrace();
            throw new CommandException("Error getting repository info.", e);
        }
    }

    @Override
    public String description() {
        return "Get information on a repository.";
    }
    
    @Override
    public String usage() {
        return "[owner] <repository>";
    }
}
