package com.roethof.tcwelcomebot.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class TCWelcomeBotConfig {
    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .create();

    public boolean enabled = true;
    public String trigger_regex = "\\[TC\\]\\s+Hoi\\s+(?<name>[^,]+),\\s+welkom\\s+op\\s+Survival!";
    public String welcome_message = "Welkom $NAAM!\nJe kunt met /pw userdorp een dorp joinen.\nType /regels voor de serverregels.\nType /uitleg voor meer informatie.";
    public List<String> commands = new ArrayList<>();
    public long delay_between_actions_ms = 1000L;
    public long trigger_cooldown_ms = 10000L;
    public boolean match_chat_messages = true;

    public static TCWelcomeBotConfig load(Path path) {
        if (!Files.exists(path)) {
            TCWelcomeBotConfig config = new TCWelcomeBotConfig();
            config.save(path);
            return config;
        }

        try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            TCWelcomeBotConfig config = GSON.fromJson(reader, TCWelcomeBotConfig.class);
            if (config == null) {
                config = new TCWelcomeBotConfig();
            }
            config.sanitize();
            return config;
        } catch (Exception e) {
            System.err.println("[TC Welcome Bot] Kon config niet lezen: " + e.getMessage());
            System.err.println("[TC Welcome Bot] Er wordt een veilige standaardconfig gebruikt.");
            return new TCWelcomeBotConfig();
        }
    }

    public void save(Path path) {
        try {
            Files.createDirectories(path.getParent());
            try (Writer writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
                GSON.toJson(this, writer);
            }
        } catch (IOException e) {
            System.err.println("[TC Welcome Bot] Kon config niet opslaan: " + e.getMessage());
        }
    }

    public void sanitize() {
        if (trigger_regex == null || trigger_regex.isBlank()) {
            trigger_regex = "\\[TC\\]\\s+Hoi\\s+(?<name>[^,]+),\\s+welkom\\s+op\\s+Survival!";
        }
        if (welcome_message == null) {
            welcome_message = "";
        }
        if (commands == null) {
            commands = new ArrayList<>();
        }
        if (delay_between_actions_ms < 0) {
            delay_between_actions_ms = 0;
        }
        if (trigger_cooldown_ms < 0) {
            trigger_cooldown_ms = 0;
        }
    }
}
