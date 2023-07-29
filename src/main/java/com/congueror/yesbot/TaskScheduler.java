package com.congueror.yesbot;

import com.congueror.yesbot.command.announcements.Announcement;
import com.congueror.yesbot.mongodb.Mongo;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.bson.Document;

import java.awt.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static com.congueror.yesbot.Constants.*;

public class TaskScheduler {

    private static final ScheduledExecutorService SCHEDULER = Executors.newScheduledThreadPool(1);
    private static final List<ScheduledFuture<?>> SCHEDULES = new ArrayList<>();

    public static void refresh(JDA jda) {
        for (ScheduledFuture<?> schedule : SCHEDULES) {
            schedule.cancel(true);
        }
        SCHEDULES.clear();

        initialize(jda);
    }

    static void initialize(JDA jda) {
        SCHEDULES.add(SCHEDULER.scheduleWithFixedDelay(WebInterface.PERIODIC_PING, 0, 50, TimeUnit.SECONDS));
        //SCHEDULES.add(SCHEDULER.scheduleWithFixedDelay(() -> YESBot.LOG.info("test"), 0, 50, TimeUnit.SECONDS));

        if (jda == null || jda.getStatus().equals(JDA.Status.SHUTDOWN))
            return;

        SCHEDULES.add(SCHEDULER.scheduleWithFixedDelay(() -> {
            try {
                var guilds = jda.getGuilds();
                for (Guild guild : guilds) {
                    if (Mongo.hasGuildDocument(guild.getId())) {
                        Announcement.update(guild, jda);
                    }
                }
            } catch (Exception e) {
                Constants.LOG.error("An exception occurred while updating announcements", e);
            }
        }, 0, 120, TimeUnit.MINUTES));

        SCHEDULES.add(SCHEDULER.scheduleWithFixedDelay(() -> {
            String ip;
            try {
                ProcessBuilder builder = new ProcessBuilder("cmd.exe", "/c", "curl ifconfig.me");
                Process p = builder.start();
                BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
                ip = r.readLine();
            } catch (Exception e) {
                ip = "";
            }

            var guild = jda.getGuildById("741704736406765599");
            var channel = guild.getTextChannelById("1081556794079453244");
            var latest = channel.getLatestMessageId();
            boolean flag = true;
            if (!latest.equals("0")) {
                var latest_msg = channel.retrieveMessageById(latest).complete();
                String msg = latest_msg.getContentRaw();
                if (msg.equals(ip))
                    flag = false;
            }
            if (flag)
                channel.sendMessage(ip).queue();
        }, 0, 1, TimeUnit.DAYS));
    }
}
