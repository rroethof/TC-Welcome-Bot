# TC Welcome Bot — Minecraft 1.21.11

Een client-side Fabric-mod voor Minecraft 1.21.11 die luistert naar een specifieke welkomstmelding in de **gewone chat** en daarop automatisch reageert.

## Wat doet de mod?

De mod luistert standaard naar een bericht zoals:

```text
[TC] Hoi Ronny, welkom op Survivial!
```

Uit dit bericht wordt de naam gehaald. Die naam kun je vervolgens in je eigen welkomsttekst gebruiken met `$NAAM`.

De standaardtrigger is:

```text
[TC] Hoi $NAAM, welkom op Survivial!
```

> Let op: de naam in de configuratie is alleen een aanduiding. De daadwerkelijke naam wordt uit het ontvangen chatbericht gehaald.

## Belangrijk: alleen gewone chat

De mod luistert **uitsluitend naar gewone chatberichten** via Fabric's `CHAT` event.

Game messages, actionbar-berichten en andere systeemmeldingen worden bewust niet gebruikt, omdat de `[TC]`-welkomstmelding altijd in de gewone chat verschijnt.

## Automatische reactie

De welkomstreactie staat volledig in de configuratie.

Je kunt bijvoorbeeld instellen:

```json
"welcome_message": "Welkom $NAAM!\nJe kunt met /pw userdorp een dorp joinen.\nType /regels voor de serverregels.\nType /uitleg voor meer informatie."
```

Elke regel tussen `\n` wordt als een afzonderlijk chatbericht verstuurd.

De mod ondersteunt deze naamvarianten in de tekst:

```text
$NAAM
$NAME
${name}
{name}
```

## Commands

Automatische commands zijn optioneel.

Gebruik bijvoorbeeld:

```json
"commands": [
  "/kit starter",
  "/regels",
  "/uitleg"
]
```

Wil je **geen commands automatisch uitvoeren**, laat de lijst leeg:

```json
"commands": []
```

De mod voert de commands uit in de volgorde waarin ze in de configuratie staan.

## Configuratie

Na de eerste keer starten wordt de configuratie aangemaakt in de Minecraft-instance:

```text
config/tcwelcomebot.json
```

Bij een standaard Minecraft-installatie is dat meestal:

```text
~/.minecraft/config/tcwelcomebot.json
```

Gebruik je bijvoorbeeld Prism Launcher, dan staat het bestand in de `config`-directory van de betreffende instance.

### Voorbeeldconfiguratie

```json
{
  "enabled": true,
  "trigger_regex": "^\\[TC\\]\\s+Hoi\\s+(?<name>[^,]+),\\s+welkom\\s+op\\s+Survivial!.*$",
  "welcome_message": "Welkom $NAAM!\\nJe kunt met /pw userdorp een dorp joinen.\\nType /regels voor de serverregels.\\nType /uitleg voor meer informatie.",
  "commands": [],
  "delay_between_actions_ms": 1000,
  "trigger_cooldown_ms": 10000,
  "match_chat_messages": true
}
```

### Betekenis van de instellingen

`enabled`
: Zet de mod aan of uit.

`trigger_regex`
: De regex waarmee het binnenkomende chatbericht wordt herkend. De groep `(?<name>...)` wordt gebruikt om de spelersnaam uit het bericht te halen.

`welcome_message`
: De tekst die automatisch wordt verstuurd. Gebruik `\\n` voor een nieuwe regel. `$NAAM` wordt vervangen door de naam uit het welkomstbericht.

`commands`
: Een lijst met commands die na de welkomsttekst automatisch worden uitgevoerd. De lijst mag leeg zijn.

`delay_between_actions_ms`
: Wachttijd in milliseconden tussen afzonderlijke chatregels en commands. Bijvoorbeeld `1000` = 1 seconde.

`trigger_cooldown_ms`
: Minimale tijd tussen twee triggers. Dit voorkomt dat een dubbele of opnieuw ontvangen welkomstmelding direct nogmaals een volledige reactie start.

`match_chat_messages`
: Als dit `false` is, luistert de mod niet naar gewone chatberichten.

## De standaardinstellingen aanpassen

De standaardconfiguratie in de mod is alleen bedoeld als startpunt. Je kunt na de eerste start het bestand `tcwelcomebot.json` gewoon met een teksteditor aanpassen.

De mod gebruikt de configuratie op het moment dat Minecraft start. Start Minecraft opnieuw na wijzigingen voor een betrouwbare reload.

## Bouwen vanaf broncode

Benodigd:

- Minecraft 1.21.11
- Java 21
- Fabric Loader
- Fabric API

Bouwen:

```bash
./gradlew clean build
```

De gebouwde JAR komt in:

```text
build/libs/
```

## Opmerking

Dit is een **client-side** mod. Hij draait dus op jouw Minecraft-client en vereist geen plugin-installatie op de server.
