package cn.wode490390.nukkit.theend.generator;

import cn.nukkit.block.Block;
import cn.nukkit.level.ChunkManager;
import cn.nukkit.level.Level;
import cn.nukkit.level.Position;
import cn.nukkit.level.biome.EnumBiome;
import cn.nukkit.level.format.generic.BaseFullChunk;
import cn.nukkit.level.generator.Generator;
import cn.nukkit.level.generator.populator.type.Populator;
import cn.nukkit.math.NukkitRandom;
import cn.nukkit.math.Vector3;
import cn.wode490390.nukkit.theend.TheEnd;
import cn.wode490390.nukkit.theend.noise.OctaveGenerator;
import cn.wode490390.nukkit.theend.noise.PerlinOctaveGenerator;
import cn.wode490390.nukkit.theend.noise.SimplexNoise;
import cn.wode490390.nukkit.theend.object.theend.ObsidianPillar;
import cn.wode490390.nukkit.theend.populator.theend.PopulatorChorusPlant;
import cn.wode490390.nukkit.theend.populator.theend.PopulatorEndGateway;
import cn.wode490390.nukkit.theend.populator.theend.PopulatorEndIsland;
import cn.wode490390.nukkit.theend.populator.theend.PopulatorObsidianPillar;
import cn.wode490390.nukkit.theend.populator.theend.PopulatorPodium;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class TheEndGenerator extends Generator {

    public static final int TYPE_THE_END = 4;

    protected static double coordinateScale = getConfig("end.coordinate-scale", 684.412d);
    protected static double heightScale = getConfig("end.height.scale", 1368.824d);
    protected static double detailNoiseScaleX = getConfig("end.detail.noise-scale.x", 80d);  // mainNoiseScaleX
    protected static double detailNoiseScaleY = getConfig("end.detail.noise-scale.y", 160d); // mainNoiseScaleY
    protected static double detailNoiseScaleZ = getConfig("end.detail.noise-scale.z", 80d);  // mainNoiseScaleZ

    protected final Map<String, Map<String, OctaveGenerator>> octaveCache = Maps.newHashMap();
    protected final double[][][] density = new double[3][3][33];

    protected ChunkManager level;
    protected NukkitRandom nukkitRandom;
    protected final List<Populator> populators = Lists.newArrayList();
    protected List<Populator> generationPopulators = Lists.newArrayList();

    protected long localSeed1;
    protected long localSeed2;

    protected SimplexNoise islandNoise;

    public TheEndGenerator() {

    }

    public TheEndGenerator(Map<String, Object> options) {

    }

    @Override
    public int getId() {
        return TYPE_THE_END;
    }

    @Override
    public int getDimension() {
        return Level.DIMENSION_THE_END;
    }

    @Override
    public String getName() {
        return "the_end";
    }

    @Override
    public Map<String, Object> getSettings() {
        return Maps.newHashMap();
    }

    @Override
    public ChunkManager getChunkManager() {
        return this.level;
    }

    @Override
    public void init(ChunkManager level, NukkitRandom random) {
        this.level = level;
        this.nukkitRandom = random;
        this.nukkitRandom.setSeed(this.level.getSeed());
        this.localSeed1 = ThreadLocalRandom.current().nextLong();
        this.localSeed2 = ThreadLocalRandom.current().nextLong();

        this.islandNoise = new SimplexNoise(this.nukkitRandom);

        for (ObsidianPillar obsidianPillar : ObsidianPillar.getObsidianPillars(this.level.getSeed())) {
            PopulatorObsidianPillar populator = new PopulatorObsidianPillar(obsidianPillar);
            populator.setAmount(1);
            this.populators.add(populator);
        }
        this.populators.add(new PopulatorPodium(TheEnd.activated));
        this.populators.add(new PopulatorEndIsland(this));
        this.populators.add(new PopulatorChorusPlant(this));
        this.populators.add(new PopulatorEndGateway(this));
    }

    @Override
    public void generateChunk(int chunkX, int chunkZ) {
        this.nukkitRandom.setSeed(chunkX * localSeed1 ^ chunkZ * localSeed2 ^ this.level.getSeed());

        BaseFullChunk chunk = level.getChunk(chunkX, chunkZ);

        int densityX = chunkX << 1;
        int densityZ = chunkZ << 1;

        Map<String, OctaveGenerator> octaves = this.getWorldOctaves();
        double[] roughnessNoise = ((PerlinOctaveGenerator) octaves.get("roughness")).getFractalBrownianMotion(densityX, 0, densityZ, 0.5d, 2d);
        double[] roughnessNoise2 = ((PerlinOctaveGenerator) octaves.get("roughness2")).getFractalBrownianMotion(densityX, 0, densityZ, 0.5d, 2d);
        double[] detailNoise = ((PerlinOctaveGenerator) octaves.get("detail")).getFractalBrownianMotion(densityX, 0, densityZ, 0.5d, 2d);

        int index = 0;

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                double noiseHeight = 100d - Math.sqrt((densityX + i) * (densityX + i) + (densityZ + j) * (densityZ + j)) * 8d;
                noiseHeight = Math.max(-100d, Math.min(80d, noiseHeight));
                for (int k = 0; k < 33; k++) {
                    double noiseR = roughnessNoise[index] / 512d;
                    double noiseR2 = roughnessNoise2[index] / 512d;
                    double noiseD = (detailNoise[index] / 10d + 1d) / 2d;
                    // linear interpolation
                    double dens = noiseD < 0 ? noiseR : noiseD > 1 ? noiseR2 : noiseR + (noiseR2 - noiseR) * noiseD;
                    dens = dens - 8d + noiseHeight;
                    index++;
                    if (k < 8) {
                        double lowering = (8 - k) / 7;
                        dens = dens * (1d - lowering) + lowering * -30d;
                    } else if (k > 33 / 2 - 2) {
                        double lowering = (k - (33 / 2 - 2)) / 64d;
                        lowering = Math.max(0d, Math.min(1d, lowering));
                        dens = dens * (1d - lowering) + lowering * -3000d;
                    }
                    this.density[i][j][k] = dens;
                }
            }
        }

        for (int i = 0; i < 3 - 1; i++) {
            for (int j = 0; j < 3 - 1; j++) {
                for (int k = 0; k < 33 - 1; k++) {
                    double d1 = this.density[i][j][k];
                    double d2 = this.density[i + 1][j][k];
                    double d3 = this.density[i][j + 1][k];
                    double d4 = this.density[i + 1][j + 1][k];
                    double d5 = (this.density[i][j][k + 1] - d1) / 4;
                    double d6 = (this.density[i + 1][j][k + 1] - d2) / 4;
                    double d7 = (this.density[i][j + 1][k + 1] - d3) / 4;
                    double d8 = (this.density[i + 1][j + 1][k + 1] - d4) / 4;

                    for (int l = 0; l < 4; l++) {
                        double d9 = d1;
                        double d10 = d3;
                        for (int m = 0; m < 8; m++) {
                            double dens = d9;
                            for (int n = 0; n < 8; n++) {
                                // any density higher than 0 is ground, any density lower or equal to 0 is air.
                                if (dens > 0) {
                                    chunk.setBlock(m + (i << 3), l + (k << 2), n + (j << 3), END_STONE);
                                }
                                // interpolation along z
                                dens += (d10 - d9) / 8;
                            }
                            // interpolation along x
                            d9 += (d2 - d1) / 8;
                            // interpolate along z
                            d10 += (d4 - d3) / 8;
                        }
                        // interpolation along y
                        d1 += d5;
                        d3 += d7;
                        d2 += d6;
                        d4 += d8;
                    }
                }
            }
        }

        for (int x = 0; x < 16; ++x) {
            for (int z = 0; z < 16; ++z) {
                chunk.setBiomeId(x, z, 9);//EnumBiome.THE_END.biome.getId()
            }
        }

        for (Populator populator : this.generationPopulators) {
            populator.populate(this.level, chunkX, chunkZ, this.nukkitRandom, chunk);
        }
    }

    @Override
    public void populateChunk(int chunkX, int chunkZ) {
        BaseFullChunk chunk = level.getChunk(chunkX, chunkZ);
        this.nukkitRandom.setSeed(0xdeadbeef ^ (chunkX << 8) ^ chunkZ ^ this.level.getSeed());
        for (Populator populator : this.populators) {
            populator.populate(this.level, chunkX, chunkZ, this.nukkitRandom, chunk);
        }
        EnumBiome.getBiome(chunk.getBiomeId(7, 7)).populateChunk(this.level, chunkX, chunkZ, this.nukkitRandom);
    }

    @Override
    public Vector3 getSpawn() {
        return new Vector3(100.5, 49, 0.5);
    }

    /**
     * Returns the {@link OctaveGenerator} instances for the world, which are
     * either newly created or retrieved from the cache.
     *
     * @return A map of {@link OctaveGenerator}s
     */
    protected final Map<String, OctaveGenerator> getWorldOctaves() {
        if (this.octaveCache.get(this.getName()) == null) {
            Map<String, OctaveGenerator> octaves = Maps.newHashMap();
            NukkitRandom seed = new NukkitRandom(this.level.getSeed());

            OctaveGenerator gen = new PerlinOctaveGenerator(seed, 16, 3, 33, 3);
            gen.setXScale(coordinateScale);
            gen.setYScale(heightScale);
            gen.setZScale(coordinateScale);
            octaves.put("roughness", gen);

            gen = new PerlinOctaveGenerator(seed, 16, 3, 33, 3);
            gen.setXScale(coordinateScale);
            gen.setYScale(heightScale);
            gen.setZScale(coordinateScale);
            octaves.put("roughness2", gen);

            gen = new PerlinOctaveGenerator(seed, 8, 3, 33, 3);
            gen.setXScale(coordinateScale / detailNoiseScaleX);
            gen.setYScale(heightScale / detailNoiseScaleY);
            gen.setZScale(coordinateScale / detailNoiseScaleZ);
            octaves.put("detail", gen);

            this.octaveCache.put(this.getName(), octaves);
            return octaves;
        }
        return this.octaveCache.get(this.getName());
    }

    public float getIslandHeight(int chunkX, int chunkZ) {
        float f0 = (chunkX << 1) + 1;
        float f1 = (chunkZ << 1) + 1;
        float f = (float) (100 - Math.sqrt(Math.pow(f0, 2) + Math.pow(f1, 2)) * 8f);
        if (f > 80) {
            f = 80;
        }
        if (f < -100) {
            f = -100;
        }

        for (int i = -12; i <= 12; ++i) {
            for (int j = -12; j <= 12; ++j) {
                long x = chunkX + i;
                long z = chunkZ + j;

                if (Math.pow(x, 2) + Math.pow(z, 2) > 4096 && this.islandNoise.noise(x, z) < -0.8999999761581421) { // 0.9f / 1.0d
                    f0 = 1 - (i << 1);
                    f1 = 1 - (j << 1);
                    float t = (float) (100 - Math.sqrt(Math.pow(f0, 2) + Math.pow(f1, 2)) * ((Math.abs(x) * 3439 + Math.abs(z) * 147) % 13 + 9));
                    if (t > 80) {
                        t = 80;
                    }
                    if (t < -100) {
                        t = -100;
                    }
                    if (t > f) {
                        f = t;
                    }
                }
            }
        }

        return f;
    }

    public static void spawnPlatform(Position pos) {
        Level level = pos.getLevel();
        int x = pos.getFloorX();
        int y = pos.getFloorY();
        int z = pos.getFloorZ();
        for (int xx = x - 2; xx < x + 3; xx++) {
            for (int zz = z - 2; zz < z + 3; zz++)  {
                level.setBlockAt(xx, y - 1, zz, Block.OBSIDIAN);
                for (int yy = y; yy < y + 4; yy++) {
                    level.setBlockAt(xx, yy, zz, Block.AIR);
                }
            }
        }
    }

    protected static <T> T getConfig(String variable) {
        return getConfig(variable, null);
    }

    @SuppressWarnings("unchecked")
    protected static <T> T getConfig(String variable, T defaultValue) {
        Object value = TheEnd.config.get("generator." + variable);
        return value == null ? defaultValue : (T) value;
    }
}
