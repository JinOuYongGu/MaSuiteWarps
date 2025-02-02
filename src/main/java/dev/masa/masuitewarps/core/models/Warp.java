package dev.masa.masuitewarps.core.models;

import com.google.gson.Gson;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import dev.masa.masuitecore.core.objects.Location;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Table;
import java.util.UUID;


/**
 * @author Masa
 */
@NoArgsConstructor
@Data
@Table(name = "masuite_warps")
public class Warp {

    /**
     * ID of the Warp Point
     */
    @DatabaseField(generatedId = true)
    private int id;

    /**
     * Name of the Warp Point
     */
    @DatabaseField(unique = true, canBeNull = false)
    private String name;

    /**
     * Owner (Creator) of the Warp Point
     */
    @DatabaseField(dataType = DataType.UUID, canBeNull = false)
    private UUID owner;

    /**
     * Location
     */
    @DatabaseField
    private String server;
    @DatabaseField
    private String world;
    @DatabaseField
    private Double x;
    @DatabaseField
    private Double y;
    @DatabaseField
    private Double z;
    @DatabaseField
    private Float yaw = 0.0F;
    @DatabaseField
    private Float pitch = 0.0F;

    public Warp(String name, Location location, UUID ownerId) {
        this.name = name;
        this.setLocation(location);
        this.owner = ownerId;
    }

    public static Warp deserialize(String json) {
        return new Gson().fromJson(json, Warp.class);
    }

    public Location getLocation() {
        return new Location(server, world, x, y, z, yaw, pitch);
    }

    public void setLocation(Location loc) {
        this.server = loc.getServer();
        this.world = loc.getWorld();
        this.x = loc.getX();
        this.y = loc.getY();
        this.z = loc.getZ();
        this.yaw = loc.getYaw();
        this.pitch = loc.getPitch();
    }

    public String serialize() {
        return new Gson().toJson(this);
    }
}
