package com.congueror.yesbot;

import com.congueror.yesbot.command.Command;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.managers.AudioManager;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class BotListenerAdapter extends ListenerAdapter {
    public static boolean locked = false;
    public static boolean shouldStop = false;
    public static final ArrayList<Command> COMMANDS = new ArrayList<>();
    public static final String PREFIX = "!";

    public BotListenerAdapter() {
    }

    @SuppressWarnings({"ConstantConditions"})
    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        System.out.printf("[%s | %s]: %s\n", event.getAuthor().getName(), event.getGuild().getName(), event.getMessage().getContentDisplay());

        if (event.getAuthor().isBot() && event.isWebhookMessage()) {
            return;
        }

        if (locked)
            return;

        //Secret Commands
        String cases = event.getMessage().getContentRaw();
        if (cases.equalsIgnoreCase("im alone") || cases.equalsIgnoreCase("i'm alone") || cases.equalsIgnoreCase("im lonely") || cases.equalsIgnoreCase("i'm lonely") || cases.equalsIgnoreCase("i am lonely") || cases.equalsIgnoreCase("i am alone")) {
            AudioChannel connectedChannel = event.getMember().getVoiceState().getChannel();
            if (connectedChannel == null)
                return;

            AudioManager audioManager = event.getGuild().getAudioManager();
            audioManager.openAudioConnection(connectedChannel);
            event.getChannel().sendMessage("Not anymore!").queue();
        }

        if (cases.equalsIgnoreCase("what have I done")) {
            event.getChannel().sendMessage("https://tenor.com/view/star-wars-anakin-skywalker-what-have-i-done-confused-sad-gif-3575836").queue();
        }

        if (event.getMessage().getContentRaw().equals("|shutdown") && event.getAuthor().getId().equals(Config.get("owner_id"))) {
            event.getChannel().sendMessage("Shutting down!").queue();
            event.getJDA().shutdown();
            return;
        }

        //Handle Commands
        String cmd = event.getMessage().getContentRaw().split(" ")[0];
        if (Command.getCommand(cmd.toLowerCase()) != null) {
            Command.getCommand(cmd.toLowerCase()).handle(event);
        }
    }

    @Override
    public void onMessageReactionAdd(@NotNull MessageReactionAddEvent event) {
        for (Command cmd : COMMANDS) {
            cmd.handleMessageReaction(event);
        }
    }

    @Override
    public void onGuildVoiceUpdate(@NotNull GuildVoiceUpdateEvent event) {
        if (event.getChannelJoined() == null) {
            AudioChannel memberLeaveVC = event.getChannelLeft();
            List<Member> members = memberLeaveVC.getMembers();
            if (members.contains(event.getGuild().getMember(Objects.requireNonNull(event.getJDA().getUserById("727830791664697395"))))) {
                if (memberLeaveVC.getMembers().toArray().length == 1) {
                    AudioManager audioManager = event.getGuild().getAudioManager();
                    audioManager.closeAudioConnection();
                }
            }
        }
    }
}
