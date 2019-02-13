package net.minecraftforge.actuarius;

import com.electronwill.nightconfig.core.file.FileConfig;
import net.minecraftforge.actuarius.bot.BotManager;
import net.minecraftforge.actuarius.bot.discord.commands.CommandTree;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.io.IOException;
import java.security.Security;

public class Main extends CommandTree {
    
    @SuppressWarnings("null")
    public static final FileConfig config = FileConfig.builder("actuarius.toml").defaultResource("/default_config.toml").autosave().build();

    public static BotManager bot;

    public static void main(String[] unused) throws IOException
    {
        Security.addProvider(new BouncyCastleProvider());

        config.load();

        bot = new BotManager();

        bot.startBots();
    }


//        String token = config.get("discord.token");
//
//        if (token == null) {
//            throw new IllegalArgumentException("No token provided.");
//        }
//
//        Hooks.onOperatorDebug();
        
}
