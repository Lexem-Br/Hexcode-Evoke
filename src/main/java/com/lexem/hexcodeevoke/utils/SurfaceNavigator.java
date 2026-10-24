package com.lexem.hexcodeevoke.utils;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.BlockMaterial;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.modules.physics.util.PhysicsMath;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.section.BlockSection;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import org.joml.Vector3d;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

public final class SurfaceNavigator {
    public static final int UP = 2;

    private static final int[][] DIRECTIONS = {{1, 0, 0}, {-1, 0, 0}, {0, 1, 0}, {0, -1, 0}, {0, 0, 1}, {0, 0, -1}};
    private static final byte UNKNOWN = 0;
    private static final byte AIR = 1;
    private static final byte SOLID = 2;
    private static final double FACE_EPSILON = 1.0E-3;
    private static final double START_HEIGHT_OFFSET = 0.05;
    private static final double DIAGONAL_COST = Math.sqrt(2);
    private static final double STEP_COST = Math.sqrt(2);
    private static final double HOP_FORWARD = 0.3;
    private static final double HOP_HEIGHT = 1.25;
    private static final double DROP_FORWARD = 0.75;
    private static final double DROP_HEIGHT = 0.15;
    private static final double STRAIGHTEN_STEP = 0.2;
    private static final int MAX_EXPANSIONS = 25000;

    private final BlockProbe probe;
    private final Map<Long, Byte> blockCache = new HashMap<>();

    public interface BlockProbe {
        @Nullable
        Boolean isSolid(int x, int y, int z);
    }

    public record Node(int x, int y, int z, int face) {
        @Nonnull
        public Vector3d normal() {
            int[] d = DIRECTIONS[face];
            return new Vector3d(d[0], d[1], d[2]);
        }

        @Nonnull
        public Vector3d position() {
            int[] d = DIRECTIONS[face];
            return new Vector3d(x + 0.5 - d[0] * 0.5, y + 0.5 - d[1] * 0.5, z + 0.5 - d[2] * 0.5);
        }

        private boolean samePlane(@Nonnull Node other) {
            if (other.face != face) return false;
            int[] d = DIRECTIONS[face];
            return d[0] != 0 ? other.x == x : d[1] != 0 ? other.y == y : other.z == z;
        }
    }

    public record Waypoint(@Nonnull Vector3d position, @Nonnull Vector3d up, @Nullable Node node) {
    }

    public SurfaceNavigator(@Nonnull World world) {
        this(worldProbe(world));
    }

    public SurfaceNavigator(@Nonnull BlockProbe probe) {
        this.probe = probe;
    }

    @Nonnull
    private static BlockProbe worldProbe(@Nonnull World world) {
        Store<ChunkStore> chunkStore = world.getChunkStore().getStore();
        return (x, y, z) -> {
            Ref<ChunkStore> section = world.getChunkStore().getChunkSectionReferenceAtBlock(x, y, z);
            BlockSection blockSection = section != null && section.isValid() ? chunkStore.getComponent(section, BlockSection.getComponentType()) : null;
            if (blockSection == null) return null;

            BlockType blockType = BlockType.getAssetMap().getAsset(blockSection.get(x, y, z));
            return blockType != null && blockType.getMaterial() == BlockMaterial.Solid;
        };
    }

    @Nullable
    public Node targetNode(@Nonnull Vector3d point) {
        for (int axis = 0; axis < 3; axis++) {
            double coordinate = axis == 0 ? point.x : axis == 1 ? point.y : point.z;
            double fraction = coordinate - Math.floor(coordinate);
            if (fraction > FACE_EPSILON && fraction < 1 - FACE_EPSILON) continue;

            int face = (int) Math.round(coordinate);
            int[] cell = {(int) Math.floor(point.x), (int) Math.floor(point.y), (int) Math.floor(point.z)};
            cell[axis] = face - 1;
            byte below = this.state(cell[0], cell[1], cell[2]);
            cell[axis] = face;
            byte above = this.state(cell[0], cell[1], cell[2]);

            if (below == SOLID && above == AIR) return new Node(cell[0], cell[1], cell[2], axis * 2);
            if (above == SOLID && below == AIR) {
                cell[axis] = face - 1;
                return new Node(cell[0], cell[1], cell[2], axis * 2 + 1);
            }
        }
        return null;
    }

    @Nullable
    public Node floorNode(@Nonnull Vector3d position) {
        int x = (int) Math.floor(position.x);
        int z = (int) Math.floor(position.z);
        int y = (int) Math.floor(position.y + START_HEIGHT_OFFSET);
        for (int cellY = y; cellY <= y + 1; cellY++) {
            Node node = new Node(x, cellY, z, UP);
            if (this.isValid(node)) return node;
        }
        return null;
    }

    public boolean isValid(@Nonnull Node node) {
        int[] d = DIRECTIONS[node.face];
        return this.state(node.x, node.y, node.z) == AIR && this.state(node.x - d[0], node.y - d[1], node.z - d[2]) == SOLID;
    }

    @Nullable
    public List<Waypoint> findPath(@Nonnull Node start, @Nonnull Vector3d startPosition, @Nonnull Node goal, @Nonnull Vector3d goalPoint) {
        List<Node> nodes = this.search(start, goal);
        if (nodes == null) return null;

        nodes = this.straighten(nodes);
        List<Waypoint> waypoints = new ArrayList<>();
        waypoints.add(new Waypoint(new Vector3d(startPosition), start.normal(), start));
        for (int i = 1; i < nodes.size(); i++) {
            Node previous = nodes.get(i - 1);
            Node node = nodes.get(i);
            if (previous.face != node.face) {
                waypoints.add(new Waypoint(edgePoint(previous, node), new Vector3d(previous.normal()).add(node.normal()).normalize(), null));
            } else if (previous.face == UP && previous.y != node.y) {
                waypoints.add(new Waypoint(stepPoint(previous, node), new Vector3d(0, 1, 0), null));
            }
            Vector3d position = i == nodes.size() - 1 ? new Vector3d(goalPoint) : node.position();
            waypoints.add(new Waypoint(position, node.normal(), node));
        }
        if (nodes.size() == 1) {
            waypoints.add(new Waypoint(new Vector3d(goalPoint), goal.normal(), goal));
        }
        return waypoints;
    }

    @Nonnull
    private static Vector3d edgePoint(@Nonnull Node from, @Nonnull Node to) {
        boolean concave = from.x == to.x && from.y == to.y && from.z == to.z;
        return from.position().add(to.normal().mul(concave ? -0.5 : 0.5));
    }

    @Nonnull
    private static Vector3d stepPoint(@Nonnull Node from, @Nonnull Node to) {
        boolean hop = to.y > from.y;
        double forward = hop ? HOP_FORWARD : DROP_FORWARD;
        return from.position().add((to.x - from.x) * forward, hop ? HOP_HEIGHT : DROP_HEIGHT, (to.z - from.z) * forward);
    }

    @Nullable
    private List<Node> search(@Nonnull Node start, @Nonnull Node goal) {
        record Entry(Node node, double estimate) {
        }

        Vector3d goalPosition = goal.position();
        Map<Node, Double> cost = new HashMap<>();
        Map<Node, Node> cameFrom = new HashMap<>();
        PriorityQueue<Entry> open = new PriorityQueue<>((a, b) -> Double.compare(a.estimate, b.estimate));
        cost.put(start, 0.0);
        open.add(new Entry(start, start.position().distance(goalPosition)));

        int expansions = 0;
        List<Node> neighbors = new ArrayList<>(8);
        List<Double> stepCosts = new ArrayList<>(8);
        while (!open.isEmpty() && expansions < MAX_EXPANSIONS) {
            Node current = open.poll().node;
            if (current.equals(goal)) return rebuild(cameFrom, current);
            expansions++;

            double currentCost = cost.get(current);
            this.neighbors(current, neighbors, stepCosts);
            for (int i = 0; i < neighbors.size(); i++) {
                Node next = neighbors.get(i);
                double nextCost = currentCost + stepCosts.get(i);
                Double known = cost.get(next);
                if (known != null && known <= nextCost) continue;

                cost.put(next, nextCost);
                cameFrom.put(next, current);
                open.add(new Entry(next, nextCost + next.position().distance(goalPosition)));
            }
        }
        return null;
    }

    @Nonnull
    private static List<Node> rebuild(@Nonnull Map<Node, Node> cameFrom, @Nonnull Node end) {
        List<Node> path = new ArrayList<>();
        for (Node node = end; node != null; node = cameFrom.get(node)) {
            path.add(node);
        }
        Collections.reverse(path);
        return path;
    }

    private void neighbors(@Nonnull Node node, @Nonnull List<Node> out, @Nonnull List<Double> costs) {
        out.clear();
        costs.clear();
        int[] n = DIRECTIONS[node.face];
        boolean[] flat = new boolean[6];

        for (int tangent = 0; tangent < 6; tangent++) {
            int[] t = DIRECTIONS[tangent];
            if (t[0] * n[0] + t[1] * n[1] + t[2] * n[2] != 0) continue;

            int bx = node.x + t[0], by = node.y + t[1], bz = node.z + t[2];
            byte ahead = this.state(bx, by, bz);
            if (ahead == SOLID) {
                out.add(new Node(node.x, node.y, node.z, tangent ^ 1));
                costs.add(1.0);
                if (node.face == UP && this.state(bx, by + 1, bz) == AIR && this.state(node.x, node.y + 1, node.z) == AIR) {
                    out.add(new Node(bx, by + 1, bz, UP));
                    costs.add(STEP_COST);
                }
            } else if (ahead == AIR) {
                byte support = this.state(bx - n[0], by - n[1], bz - n[2]);
                if (support == SOLID) {
                    out.add(new Node(bx, by, bz, node.face));
                    costs.add(1.0);
                    flat[tangent] = true;
                } else if (support == AIR) {
                    out.add(new Node(bx - n[0], by - n[1], bz - n[2], tangent));
                    costs.add(1.0);
                    if (node.face == UP && this.state(bx, by - 2, bz) == SOLID) {
                        out.add(new Node(bx, by - 1, bz, UP));
                        costs.add(STEP_COST);
                    }
                }
            }
        }

        for (int first = 0; first < 6; first += 2) {
            for (int second = first + 2; second < 6; second += 2) {
                for (int a = first; a <= first + 1; a++) {
                    for (int b = second; b <= second + 1; b++) {
                        if (!flat[a] || !flat[b]) continue;
                        int[] ta = DIRECTIONS[a], tb = DIRECTIONS[b];
                        Node diagonal = new Node(node.x + ta[0] + tb[0], node.y + ta[1] + tb[1], node.z + ta[2] + tb[2], node.face);
                        if (this.isValid(diagonal)) {
                            out.add(diagonal);
                            costs.add(DIAGONAL_COST);
                        }
                    }
                }
            }
        }
    }

    @Nonnull
    private List<Node> straighten(@Nonnull List<Node> nodes) {
        List<Node> result = new ArrayList<>();
        int i = 0;
        result.add(nodes.get(0));
        while (i < nodes.size() - 1) {
            int j = i + 1;
            while (j + 1 < nodes.size() && nodes.get(i).samePlane(nodes.get(j + 1)) && this.isClear(nodes.get(i), nodes.get(j + 1))) {
                j++;
            }
            result.add(nodes.get(j));
            i = j;
        }
        return result;
    }

    private boolean isClear(@Nonnull Node from, @Nonnull Node to) {
        Vector3d start = from.position();
        Vector3d end = to.position();
        Vector3d normal = from.normal();
        double length = start.distance(end);
        int steps = (int) Math.ceil(length / STRAIGHTEN_STEP);
        Vector3d sample = new Vector3d();
        for (int step = 1; step < steps; step++) {
            start.lerp(end, (double) step / steps, sample).add(normal.x * 0.5, normal.y * 0.5, normal.z * 0.5);
            Node node = new Node((int) Math.floor(sample.x), (int) Math.floor(sample.y), (int) Math.floor(sample.z), from.face);
            if (!this.isValid(node)) return false;
        }
        return true;
    }

    public static boolean orient(@Nonnull Vector3d forward, @Nonnull Vector3d up, @Nonnull float[] out) {
        Vector3d u = new Vector3d(up).normalize();
        Vector3d f = new Vector3d(forward).sub(new Vector3d(u).mul(forward.dot(u)));
        if (f.lengthSquared() < 1.0E-8) return false;
        f.normalize();

        double horizontal = Math.sqrt(f.x * f.x + f.z * f.z);
        if (horizontal > 1.0E-3) {
            float yaw = PhysicsMath.headingFromDirection(f.x, f.z);
            float pitch = (float) Math.atan2(f.y, horizontal);
            Vector3d heading = new Vector3d(PhysicsMath.headingX(yaw), 0, PhysicsMath.headingZ(yaw));
            Vector3d baseUp = new Vector3d(0, Math.cos(pitch), 0).sub(heading.mul(Math.sin(pitch)));
            Vector3d left = new Vector3d(baseUp).cross(f);
            out[0] = yaw;
            out[1] = pitch;
            out[2] = (float) Math.atan2(u.dot(left), u.dot(baseUp));
        } else {
            boolean ascending = f.y > 0;
            out[0] = ascending ? PhysicsMath.headingFromDirection(-u.x, -u.z) : PhysicsMath.headingFromDirection(u.x, u.z);
            out[1] = (float) (ascending ? Math.PI / 2 : -Math.PI / 2);
            out[2] = 0;
        }
        return true;
    }

    private byte state(int x, int y, int z) {
        long key = ((long) (x & 0x3FFFFFF) << 38) | ((long) (z & 0x3FFFFFF) << 12) | (y & 0xFFF);
        Byte cached = this.blockCache.get(key);
        if (cached != null) return cached;

        Boolean solid = this.probe.isSolid(x, y, z);
        byte state = solid == null ? UNKNOWN : solid ? SOLID : AIR;
        this.blockCache.put(key, state);
        return state;
    }
}
