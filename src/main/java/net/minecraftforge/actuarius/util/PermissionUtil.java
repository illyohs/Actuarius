package net.minecraftforge.actuarius.util;

import java.util.List;

import com.electronwill.nightconfig.core.Config;

import discord4j.core.object.entity.Member;
import discord4j.core.object.util.Snowflake;
import net.minecraftforge.actuarius.Main;
import reactor.util.annotation.Nullable;

public class PermissionUtil {
    
    public static synchronized boolean canAccess(Member member, String repo) {
        return canAccess(member.getId(), repo) || member.getRoleIds().stream().anyMatch(id -> canAccess(id, repo));
    }
    
    public static synchronized boolean canAccess(Snowflake id, String repo) {
        List<String> repos = getRepos(id);
        return repos != null && (repos.size() == 0 || repos.contains(repo));
    }
    
    private static @Nullable List<String> getRepos(Snowflake id) {
        
        List<Config> grants = Main.config.<List<Config>>get("grants");
        
        for (Config grant : grants) {
            if (grant.<Long>get("id") == id.asLong()) {
                return grant.<List<String>>get("repos");
            }
        }
        return null;
    }

}
