package fr.WarzouMc.MonaiServGroup.HubCenter.npc;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import net.minecraft.server.v1_10_R1.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_10_R1.CraftServer;
import org.bukkit.craftbukkit.v1_10_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_10_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.UUID;

public class NPC {

    public void createNPC(String npcName, String npcSkin, Location location) {

        String[] a = getFromName(npcSkin);

        byte mask = (0x01 | 0x02 | 0x04 | 0x08 | 0x10 | 0x20 | 0x40);

        MinecraftServer nmsServer = ((CraftServer) Bukkit.getServer()).getServer();
        WorldServer nmsWorld = (( CraftWorld ) location.getWorld()).getHandle();
        GameProfile gameProfile = new GameProfile(UUID.randomUUID(), npcName.replace("&","§"));
        gameProfile.getProperties().put("textures", new Property("textures", a[0], a[1]));

        EntityPlayer npc = new EntityPlayer(nmsServer, nmsWorld, gameProfile, new PlayerInteractManager(nmsWorld));
        Player npcPlayer = npc.getBukkitEntity().getPlayer();

        DataWatcher watcher = npc.getDataWatcher();
        watcher.set(DataWatcherRegistry.a.a(13), mask);

        npc.setLocation(location.getX()+0.5, location.getY()+1, location.getZ()+0.5, 90.0f, 0.0f);

        for(Player player : Bukkit.getOnlinePlayers()){
            PlayerConnection connection = (( CraftPlayer ) player).getHandle().playerConnection;
            connection.sendPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER, npc));
            connection.sendPacket(new PacketPlayOutNamedEntitySpawn(npc));
            new BukkitRunnable() {
                @Override
                public void run() {
                    connection.sendPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER, npc));
                }
            }.runTaskLater(main, 50);
        }

    }

    public String[] getFromName(String name) {
        try {
            URL url_0 = new URL("https://api.mojang.com/users/profiles/minecraft/" + name);
            InputStreamReader reader_0 = new InputStreamReader(url_0.openStream());
            String uuid = new JsonParser().parse(reader_0).getAsJsonObject().get("id").getAsString();

            URL url_1 = new URL("https://sessionserver.mojang.com/session/minecraft/profile/" + uuid + "?unsigned=false");
            InputStreamReader reader_1 = new InputStreamReader(url_1.openStream());
            JsonObject textureProperty = new JsonParser().parse(reader_1).getAsJsonObject().get("properties").getAsJsonArray().get(0).getAsJsonObject();
            String texture = textureProperty.get("value").getAsString();
            String signature = textureProperty.get("signature").getAsString();

            return new String[] {texture, signature};
        } catch (IOException e) {
            System.err.println("Could not get skin data from session servers!");
            e.printStackTrace();
            return null;
        }
    }

}
