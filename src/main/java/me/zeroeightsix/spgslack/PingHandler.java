package me.zeroeightsix.spgslack;

import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.effect.sound.SoundType;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.EventListener;
import org.spongepowered.api.event.message.MessageChannelEvent;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by 086 on 19/04/2018.
 */
public class PingHandler implements CommandExecutor, EventListener<MessageChannelEvent.Chat> {

    private Path configurationFile;
    private static List<String> notificationList = new ArrayList<>(); // A list of all players who have notifications enabled

    // Regex for matching @ followed by 1-16 acceptable name characters, followed by either a space or the end of our text.
    private static final Pattern TAG_PATTERN = Pattern.compile("(@)([a-zA-Z0-9_]{1,15})(?=( |\\Z))");

    public PingHandler(Logger logger, SpongeSlack plugin) {
        configurationFile = Sponge.getConfigManager().getPluginConfig(plugin).getConfigPath();
        if (!Files.exists(configurationFile)) {
            try {
                Files.createFile(configurationFile);
            } catch (IOException e) {
                e.printStackTrace();
                configurationFile = null;
            }
        }else{
            try (BufferedReader reader = Files.newBufferedReader(configurationFile)) {
                String line = reader.readLine();
                while ((line != null)) {
                    notificationList.add(line);
                    line = reader.readLine();
                }
            } catch (IOException e) {
                e.printStackTrace();
                logger.error(String.format("SpongeSlack could not load its configuration file :( (%s)", e.getMessage()));
            }
        }
    }

    private boolean shouldPing(String name) {
        return notificationList.contains(name);
    }

    public void save() {
        if (configurationFile == null) return;
        try (BufferedWriter writer = Files.newBufferedWriter(configurationFile)) {
            notificationList.forEach(s -> {
                // The above try doesn't 'handle' this?? IntelliJ are you drunk or have I never known of this?
                try {
                    writer.write(s + System.lineSeparator());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        if (src instanceof Player) {
            String name = src.getName();
            boolean flag = notificationList.contains(name);
            if (flag) notificationList.remove(name);
            else notificationList.add(name);
            src.sendMessage(Text.builder(flag ? "You will no longer receive pings." : "You will now receive pings.")
                    .color(TextColors.GREEN)
                    .build());
        }else throw new CommandException(Text.builder("You must be a player to execute this command.")
                .color(TextColors.RED)
                .build());
        return CommandResult.success();
    }

    @Override
    public void handle(MessageChannelEvent.Chat event) {
        String message = event.getOriginalMessage().toPlain();
        Matcher m = TAG_PATTERN.matcher(message);
        while (m.find()) {
            String name = m.group(2); // We take the second group which is the player name. The first being the @ and the third group is either a space or end of string, which we have no need for.
            Optional<Player> opt = Sponge.getServer().getPlayer(name);
            if (opt.isPresent() && shouldPing(name)) { // getPlayer would return null is the specified player is nonexistent or not online - So we already know we the player is online, and they have notifications enabled. Lovely! Let's get started waking them up.
                Player p = opt.get();
                p.playSound(SoundType.of("entity.firework.twinkle"), p.getLocation().getPosition(), 2d); // BANG!
                // TODO: Is it possible to modify the chat message for this specific player to colour their name?
            }
        }
    }
}
