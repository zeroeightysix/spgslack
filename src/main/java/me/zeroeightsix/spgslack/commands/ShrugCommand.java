package me.zeroeightsix.spgslack.commands;

import me.zeroeightsix.spgslack.SpongeSlack;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;

/**
 * Created by 086 on 21/02/2018.
 */
public class ShrugCommand implements CommandExecutor {

    private static final String SHRUG = "\u00AF\\_(\u30C4)_/\u00AF"; // This is our beloved shrug emoticon

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) {
        SpongeSlack.sendChat(src, SHRUG + (args.hasAny("message") ? " " : "") + args.<String>getOne("message").orElse(""));
        return CommandResult.success();
    }
}
