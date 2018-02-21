package me.zeroeightsix.spgslack;

import me.zeroeightsix.spgslack.commands.ShrugCommand;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.source.ConsoleSource;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.cause.EventContextKeys;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.text.Text;

import javax.inject.Inject;

/**
 * Created by 086 on 21/02/2018.
 */

@Plugin(name = "SpongeSlack", id = "spg-slack", version = "1.0", description = "Plugin for adding slack/discord-like chat commands to your modded minecraft server")
public class SpongeSlack {

    private CommandSpec shrugSpec = CommandSpec.builder() // Our beloved /shrug command
            .description(Text.of("Sends a shrug emoticon"))
            .executor(new ShrugCommand())
            .arguments(
                    GenericArguments.optional(
                        GenericArguments.remainingRawJoinedStrings(Text.of("message"))
                    )
            )
            .build();

    private Logger logger;

    @Inject
    public SpongeSlack(Logger logger) {
        this.logger = logger;
    }

    @Listener
    public void onServerStart(GameStartedServerEvent event){
        logger.info("SpongeSlack initialising..");
        Sponge.getCommandManager().register(this, shrugSpec, "shrug"); // Register our beloved /shrug command so we can actually use our beloved /shrug command
        // We'll hopefully have more here later
        logger.info("SpongeSlack initialised");
    }

    /**
     * Simulate a chat message from the specified source
     * @param source
     * @param message
     */
    public static void sendChat(CommandSource source, String message) {
        if (source instanceof Player) {
            // This is required for nucleus to work correctly with a simulated player message
            CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame();
            Player pl = (Player) source;
            frame.pushCause(pl);
            frame.addContext(EventContextKeys.PLAYER_SIMULATED, pl.getProfile());

            // Send a chat message from the specified player
            pl.simulateChat(Text.of(message), Sponge.getCauseStackManager().getCurrentCause());

            // Pop the frame so we don't have any bad bad overflows
            Sponge.getCauseStackManager().popCauseFrame(frame);
        }else if (source instanceof ConsoleSource) {
            Sponge.getCommandManager().process(source, "say " + message); // We simulate the say using /say <message> (cheap, but it works)
        }else{
            source.sendMessage(Text.of(message)); // Anything else is just sent back to whatever the fuck issued this command
        }
    }

}
