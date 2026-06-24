**ModSync**
Secure Client-Server Synchronisation & Compliance Enforcement
ModSync is a lightweight, two-part system designed for server administrators who need absolute transparency over what their players are running. By bridging the gap between the server (Spigot/Paper) and the client (Fabric), ModSync allows you to enforce mod and resource pack requirements instantly, maintain historical logs, and verify the integrity of every connection.

🛡️ Core Security
ModSync doesn't just "ask" the client for a list; it uses a secure challenge-response handshake:

Dynamic Hashing: Every handshake is secured with SHA-512 using a server-side pepper and a unique challenge UUID to prevent packet spoofing.

Auto-Authorization: Link player usernames to unique client hardware IDs to prevent account sharing or spoofing on "cracked" or insecure servers.

Privacy-First: Players are prompted to agree to data reporting on their first join, ensuring compliance with privacy standards.

⚙️ Technical Features
Categorized Resource Reporting: Unlike basic list-reporters, ModSync distinguishes between Active (enabled), Inactive (in folder), and Deleted resource packs.

Intelligent Mod Consolidation: Automatically collapses library dependencies (like fabric-api sub-modules) into single entries to keep admin modlists readable.

Historical Audit Logs: Every player gets a dedicated .yml log tracking their mod/pack history across sessions—including timestamps of when a mod was removed.

Remote Diagnostics: Admins can request remote screenshots or even "dump" specific mod/pack files directly from a client to verify suspicious files.

Bedrock Support: Native compatibility with Geyser/Floodgate to skip verification for Bedrock players while maintaining strict security for Java players.

💻 Commands
/modsync modlist <player> – View all installed mods (filtered to remove library noise).

/modsync packlist <player> – View active vs. inactive resource packs.

/modsync screenshot <player> – Request a secure remote screenshot.

/modsync dump <player> <mod|pack> <id> – Download a specific file from the player's client for inspection.

/modsync authreset <player> – Reset the hardware ID link for a player.

/modsync reload – Hot-reload all configurations and banned/required lists.

🚀 Setup
Install the Plugin on your Spigot/Paper server.
Install the Mod on your Fabric client.
Configure your required-mods and banned-mods in the server's config.yml.
Developed by Dark_Yoddha — Built for Integrity.
