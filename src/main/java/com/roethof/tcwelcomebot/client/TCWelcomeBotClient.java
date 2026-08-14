package com.roethof.tcwelcomebot.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.client.MinecraftClient;

import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public final class TCWelcomeBotClient implements ClientModInitializer {
    private static TCWelcomeBotConfig config;
    private static Pattern triggerPattern;
    private static long lastTriggeredAt = 0L;
    private static final Object ACTION_LOCK = new Object();

    @Override
    public void onInitializeClient() {
        Path configPath = MinecraftClient.getInstance().runDirectory.toPath()
                .resolve("config")
                .resolve("tcwelcomebot.json");

        config = TCWelcomeBotConfig.load(configPath);
        compilePattern();

        if (config.match_chat_messages) {
            ClientReceiveMessageEvents.CHAT.register((message, signedMessage, sender, params, receptionTimestamp) ->
                    handleIncoming(message.getString()));
        }

        System.out.println("[TC Welcome Bot] geladen. Config: " + configPath);
    }

    private static void compilePattern() {
        try {
            triggerPattern = Pattern.compile(config.trigger_regex, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
        } catch (PatternSyntaxException e) {
            triggerPattern = null;
            System.err.println("[TC Welcome Bot] Ongeldige trigger_regex: " + e.getMessage());
        }
    }

    private static void handleIncoming(String message) {
        if (!config.enabled || triggerPattern == null || message == null || message.isBlank()) {
            return;
        }

        Matcher matcher = triggerPattern.matcher(message.trim());
        if (!matcher.matches()) {
            return;
        }

        long now = System.currentTimeMillis();
        synchronized (ACTION_LOCK) {
            if (now - lastTriggeredAt < config.trigger_cooldown_ms) {
                return;
            }
            lastTriggeredAt = now;
        }

        String name = "";
        try {
            name = matcher.group("name").trim();
        } catch (IllegalArgumentException ignored) {
            // Optional named capture group; config can omit it.
        }

        executeWelcomeSequence(name);
    }

    private static void executeWelcomeSequence(String name) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.getNetworkHandler() == null || client.player == null) {
            return;
        }

        String welcome = replacePlaceholders(config.welcome_message, name);
        long delay = Math.max(0L, config.delay_between_actions_ms);

        Thread worker = new Thread(() -> {
            try {
                // Iedere niet-lege regel uit welcome_message wordt als een aparte chatboodschap verstuurd.
                for (String line : welcome.split("\\R", -1)) {
                    if (line.isBlank()) {
                        continue;
                    }
                    sendChatMessage(line);
                    sleep(delay);
                }

                for (String rawCommand : config.commands) {
                    if (rawCommand == null || rawCommand.isBlank()) {
                        continue;
                    }
                    sendCommand(rawCommand.trim());
                    sleep(delay);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, "tcwelcomebot-actions");

        worker.setDaemon(true);
        worker.start();
    }

    private static void sendChatMessage(String message) {
        MinecraftClient client = MinecraftClient.getInstance();
        client.execute(() -> {
            if (client.getNetworkHandler() != null) {
                client.getNetworkHandler().sendChatMessage(message);
            }
        });
    }

    private static void sendCommand(String rawCommand) {
        MinecraftClient client = MinecraftClient.getInstance();
        String command = rawCommand.startsWith("/") ? rawCommand.substring(1) : rawCommand;
        if (command.isBlank()) {
            return;
        }

        client.execute(() -> {
            if (client.getNetworkHandler() != null) {
                client.getNetworkHandler().sendChatCommand(command);
            }
        });
    }

    private static String replacePlaceholders(String text, String name) {
        if (text == null) {
            return "";
        }
        return text
                .replace("$NAAM", name)
                .replace("${name}", name)
                .replace("{name}", name)
                .replace("$NAME", name);
    }

    private static void sleep(long millis) throws InterruptedException {
        if (millis > 0) {
            Thread.sleep(millis);
        }
    }
}
