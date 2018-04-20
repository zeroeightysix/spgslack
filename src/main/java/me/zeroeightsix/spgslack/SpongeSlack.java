package me.zeroeightsix.spgslack;

import me.zeroeightsix.spgslack.commands.EmoticonCommand;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.source.ConsoleSource;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.cause.EventContextKeys;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.event.game.state.GameStoppedServerEvent;
import org.spongepowered.api.event.message.MessageChannelEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.text.Text;

import javax.inject.Inject;

/**
 * Created by 086 on 21/02/2018.
 */

@Plugin(name = "SpongeSlack", id = "spg-slack", version = "1.1", description = "Plugin for adding slack/discord-like chat commands to your modded minecraft server")
public class SpongeSlack {

    private Logger logger;
    private PingHandler pingHandler;

    @Inject
    public SpongeSlack(Logger logger) {
        this.logger = logger;
    }

    @Listener
    public void onServerStart(GameStartedServerEvent event){
        logger.info("Initialising SpongeSlack");

        Sponge.getCommandManager().register(this, CommandSpec.builder()
                .description(Text.of("Toggle whether or not you will hear a sound when someone tags you in-game"))
                .executor((pingHandler = new PingHandler(logger, this)))
                .build(), "togglepings", "pingme");
        Sponge.getEventManager().registerListener(this, MessageChannelEvent.Chat.class, pingHandler);
        (new EmoticonCommand(Emotes.SHRUG)).createAndRegister(this, "shrug");
        (new EmoticonCommand(Emotes.DISAPPROVE)).createAndRegister(this, "disapprove");
        logger.info("SpongeSlack initialised");
    }

    @Listener
    public void onServerStop(GameStoppedServerEvent event) {
        pingHandler.save();
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
