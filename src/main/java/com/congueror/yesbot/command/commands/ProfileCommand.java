package com.congueror.yesbot.command.commands;

import com.congueror.yesbot.MongoUser;
import com.congueror.yesbot.command.AbstractCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.bson.Document;

import java.awt.*;

public class ProfileCommand implements AbstractCommand {

    @Override
    public void handle(MessageReceivedEvent event) {
        String[] profile = getInput(event);
        if (check(profile)) {
            Message reference = event.getMessage();
            User player;
            if (isMention(reference)) {
                player = reference.getMentionedMembers().get(0).getUser();
            } else {
                player = reference.getAuthor();
            }

            EmbedBuilder embed = new EmbedBuilder();
            embed.setTitle("**__" + player.getAsTag() + "__**");
            embed.setThumbnail(player.getEffectiveAvatarUrl());
            embed.setColor(Color.RED);
            Document doc = MongoUser.getDocument(player.getId());
            for (var a : doc.keySet()) {
                if (a.equals("_id") || a.equals("id"))
                    continue;
                embed.addField(a + ":", doc.get(a).toString(), true);
            }
            event.getChannel().sendMessageEmbeds(embed.build()).reference(reference).queue();
        }
    }

    @Override
    public String getName() {
        return "profile";
    }

    @Override
    public String[] getArgs() {
        return new String[]{"target"};
    }

    @Override
    public String getDescription() {
        return "Displays relevant information about the player.";
    }
}
