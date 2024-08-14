package com.craftmend.openaudiomc.generic.utils;

import java.util.Objects;

import org.joml.Vector3d;
import org.joml.Vector3dc;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

/**
 * A re-implementation of Bukkit's <code>Location</code> class.
 */
public class Location {
    private World world;
    private final Vector3d position;
    private float yaw, pitch;

    public Location(World world, double x, double y, double z) {
        this(world, x, y, z, 0, 0);
    }

    public Location(World world, double x, double y, double z, float yaw, float pitch) {
        this.world = world;
        this.position = new Vector3d(x, y, z);
        this.yaw = yaw;
        this.pitch = pitch;
    }

    public static Location locationFromEntity(Entity e)
    {
        return new Location(e.getWorld(), e.getX(), e.getY(), e.getZ(), e.getYaw(), e.getPitch());
    }

    public World getWorld() {
        return world;
    }

    public void setWorld(World world) {
        this.world = world;
    }

    public BlockPos getBlockPos() {
        return new BlockPos(MathHelper.floor(position.x), MathHelper.floor(position.y), MathHelper.floor(position.z));
    }

    public ChunkSectionPos getChunkSection() {
        return ChunkSectionPos.from(getBlockPos());
    }

    public ChunkPos getChunkPos() {
        return getChunkSection().toChunkPos();
    }

    public Chunk getChunk() {
        return this.world.getChunk(getBlockPos());
    }

    public BlockState getBlock() {
        return this.world.getBlockState(getBlockPos());
    }

    public double getX() {
        return position.x;
    }

    public int getBlockX() {
        return MathHelper.floor(position.x);
    }

    public void setX(double x) {
        position.x = x;
    }

    public double getY() {
        return position.y;
    }

    public int getBlockY() {
        return MathHelper.floor(position.y);
    }

    public void setY(double y) {
        position.y = y;
    }

    public double getZ() {
        return position.z;
    }

    public int getBlockZ() {
        return MathHelper.floor(position.z);
    }

    public void setZ(double z) {
        position.z = z;
    }

    public Vector3d getPosition() {
        return position;
    }

    public void setPosition(Vector3dc position) {
        this.position.set(position);
    }

    public float getPitch() {
        return pitch;
    }

    public void setPitch(float pitch) {
        this.pitch = pitch;
    }

    public float getYaw() {
        return yaw;
    }

    public void setYaw(float yaw) {
        this.yaw = yaw;
    }

    public Vector3d getDirection(Vector3d dest) {
        dest.set(0);
        dest.rotateX(Math.toRadians(pitch));
        dest.rotateZ(Math.toRadians(yaw));
        return dest;
        // float rotX = getYaw();
        // float rotY = getPitch();

        // dest.y = -MathHelper.sin(rotY * MathHelper.RADIANS_PER_DEGREE);
        // double xz = MathHelper.cos(rotY * MathHelper.RADIANS_PER_DEGREE);
        // dest.x = (-xz * MathHelper.sin(rotX * MathHelper.RADIANS_PER_DEGREE));
        // dest.z = (xz * Math.cos(rotX * MathHelper.RADIANS_PER_DEGREE));
        // return dest;
    }

    public Vector3d getDirection() {
        return getDirection(new Vector3d());
    }

    public Location setDirection(Vector3dc vector) {
        double x = vector.x();
        double z = vector.z();
        if (x == 0 && z == 0) {
            this.pitch = vector.y() > 0 ? -90 : 90;
            return this;
        } else {
            double theta = Math.atan2(-x, z);
            // I have no idea what this does
            this.yaw = (float) Math.toDegrees((theta + Math.PI * 2) % Math.PI * 2);

            double xz = Math.sqrt(x * x + z * z);
            this.pitch = (float) Math.toDegrees(Math.atan(-vector.y() / xz));
            return this;
        }
    }

    public Location add(Location other) {
        if (other == null || other.world != world) {
            throw new IllegalArgumentException("Cannot add Locations of differing worlds");
        }

        this.position.add(other.position);
        return this;
    }

    public Location add(Vector3dc vec) {
        this.position.add(vec);
        return this;
    }

    public Location add(Vec3d vec) {
        this.position.add(vec.getX(), vec.getY(), vec.getZ());
        return this;
    }

    public Location add(double x, double y, double z) {
        this.position.add(x, y, z);
        return this;
    }

    public Location subtract(Location other) {
        if (other == null || other.world != world) {
            throw new IllegalArgumentException("Cannot subtract Locations of differing worlds");
        }

        this.position.sub(other.position);
        return this;
    }

    public Location subtract(Vector3dc vec) {
        this.position.sub(vec);
        return this;
    }

    public Location subtract(Vec3d vec) {
        this.position.sub(vec.getX(), vec.getY(), vec.getZ());
        return this;
    }

    public Location subtract(double x, double y, double z) {
        this.position.sub(x, y, z);
        return this;
    }

    public double length() {
        return position.length();
    }

    public double lengthSquared() {
        return position.lengthSquared();
    }

    public double distance(Location other) {
        return Math.sqrt(distanceSquared(other));
    }

    public double distanceSquared(Location other) {
        if (other == null) {
            throw new NullPointerException("Cannot measure distance to a null location");
        } else if (other.getWorld() != getWorld()) {
            throw new IllegalArgumentException("Cannot measure distance between " + world + " and " + other.world);
        }

        return this.position.distanceSquared(other.position);
    }

    public Location multiply(double scalar) {
        this.position.mul(scalar);
        return this;
    }

    public Location multiply(Vector3dc vec) {
        this.position.mul(vec);
        return this;
    }

    public Location zero() {
        this.position.set(0);
        pitch = 0;
        yaw = 0;
        return this;
    }

    @Override
    public String toString() {
        return "Location{world=%s, x=%d, y=%d, z=%d, yaw=%d, pitch=%d}".formatted(world, position.x, position.y, position.z, yaw, pitch);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (getClass() != obj.getClass()) {
            return false;
        }

        Location other = (Location) obj;

        if (!Objects.equals(this.world, other.world)) {
            return false;
        }

        if (!position.equals(other.position)) {
            return false;
        }

        if (Float.floatToIntBits(pitch) != Float.floatToIntBits(other.pitch)
                || Float.floatToIntBits(yaw) != Float.floatToIntBits(other.yaw)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int hash = position.hashCode();
        hash = hash * 31 + Float.floatToIntBits(pitch);
        hash = hash * 31 + Float.floatToIntBits(yaw);
        return hash;
    }

    public Location copy() {
        return new Location(world, position.x, position.y, position.z, yaw, pitch);
    }
}
