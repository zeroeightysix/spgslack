package me.zeroeightsix.spgslack.commands;

import me.zeroeightsix.spgslack.SpongeSlack;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.text.Text;

/**
 * Created by 086 on 19/04/2018.
 */
public class EmoticonCommand implements CommandExecutor {

    private final String EMOTICON;

    public EmoticonCommand(String emoticon) {
        this.EMOTICON = emoticon;
    }

    public CommandSpec createAndRegister(Object plugin, String name) {
        CommandSpec spec = CommandSpec.builder()
                .description(Text.of(String.format("Sends a %s emoticon", name)))
                .executor(this)
                .arguments(
                        GenericArguments.optional(
                                GenericArguments.remainingRawJoinedStrings(Text.of("message"))
                        )
                )
                .build();
        Sponge.getCommandManager().register(plugin, spec, name);
        return spec;
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        SpongeSlack.sendChat(src, EMOTICON + (args.hasAny("message") ? " " : "") + args.<String>getOne("message").orElse(""));
        return CommandResult.success();
    }

}
