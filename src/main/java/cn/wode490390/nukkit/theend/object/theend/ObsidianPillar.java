package cn.wode490390.nukkit.theend.object.theend;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ObsidianPillar {

    private final int centerX;
    private final int centerZ;
    private final int radius;
    private final int height;
    private final boolean guarded;

    public ObsidianPillar(int centerX, int centerZ, int radius, int height, boolean guarded) {
        this.centerX = centerX;
        this.centerZ = centerZ;
        this.radius = radius;
        this.height = height;
        this.guarded = guarded;
    }

    public int getCenterX() {
        return this.centerX;
    }

    public int getCenterZ() {
        return this.centerZ;
    }

    public int getRadius() {
        return this.radius;
    }

    public int getHeight() {
        return this.height;
    }

    public boolean isGuarded() {
        return this.guarded;
    }

    private static final LoadingCache<Long, ObsidianPillar[]> CACHE = CacheBuilder.newBuilder()
            .expireAfterWrite(5l, TimeUnit.MINUTES)
            .<Long, ObsidianPillar[]>build(new ObsidianPillarCacheLoader());

    public static ObsidianPillar[] getObsidianPillars(long seed) {
        return CACHE.getUnchecked(new Random(seed).nextLong() & 0xffffl);
    }

    private static class ObsidianPillarCacheLoader extends CacheLoader<Long, ObsidianPillar[]> {

        @Override
        public ObsidianPillar[] load(Long key) throws Exception {
            List<Integer> list = IntStream.range(0, 10).boxed().collect(Collectors.toList());
            Collections.shuffle(list, new Random(key));
            ObsidianPillar[] obsidianPillars = new ObsidianPillar[10];

            for (int i = 0; i < 10; ++i) {
                int pillar = list.get(i);
                obsidianPillars[i] = new ObsidianPillar((int) (42d * Math.cos(2d * (-Math.PI + (Math.PI / 10d) * i))), (int) (42d * Math.sin(2d * (-Math.PI + (Math.PI / 10d) * i))), 2 + pillar / 3, 76 + pillar * 3, pillar == 1 || pillar == 2);
            }

            return obsidianPillars;
        }
    }
}
