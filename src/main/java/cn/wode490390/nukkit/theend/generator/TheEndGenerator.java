package cn.wode490390.nukkit.theend.generator;

import cn.nukkit.level.ChunkManager;
import cn.nukkit.level.Level;
import cn.nukkit.level.Position;
import cn.nukkit.level.biome.Biome;
import cn.nukkit.level.format.generic.BaseFullChunk;
import cn.nukkit.level.generator.Generator;
import cn.nukkit.level.generator.populator.type.Populator;
import cn.nukkit.math.NukkitRandom;
import cn.nukkit.math.Vector3;
import cn.nukkit.utils.Config;
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

    protected static double coordinateScale;
    protected static double heightScale;
    protected static double detailNoiseScaleX; // mainNoiseScaleX
    protected static double detailNoiseScaleY; // mainNoiseScaleY
    protected static double detailNoiseScaleZ; // mainNoiseScaleZ
    protected static boolean activated;
    protected static boolean spawnDragon;

    public static void setConfig(Config config) {
        coordinateScale = getConfig(config, "generator.end.coordinate-scale", 684.412d);
        heightScale = getConfig(config, "generator.end.height.scale", 1368.824d);
        detailNoiseScaleX = getConfig(config, "generator.end.detail.noise-scale.x", 80d);
        detailNoiseScaleY = getConfig(config, "generator.end.detail.noise-scale.y", 160d);
        detailNoiseScaleZ = getConfig(config, "generator.end.detail.noise-scale.z", 80d);

        activated = getConfig(config, "exit-portal-activated", true);
        spawnDragon = getConfig(config, "spawn-ender-dragon", true);
    }

    protected final double[][][] density = new double[3][3][33];

    protected ChunkManager level;
    protected NukkitRandom nukkitRandom;
    protected final List<Populator> populators = Lists.newArrayList();
    protected List<Populator> generationPopulators = Lists.newArrayList();

    protected long localSeed1;
    protected long localSeed2;

    protected PerlinOctaveGenerator roughnessNoise;
    protected PerlinOctaveGenerator roughness2Noise;
    protected PerlinOctaveGenerator detailNoise;
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

        this.roughnessNoise = new PerlinOctaveGenerator(this.nukkitRandom, 16, 3, 33, 3);
        this.roughnessNoise.setXScale(coordinateScale);
        this.roughnessNoise.setYScale(heightScale);
        this.roughnessNoise.setZScale(coordinateScale);

        this.roughness2Noise = new PerlinOctaveGenerator(this.nukkitRandom, 16, 3, 33, 3);
        this.roughness2Noise.setXScale(coordinateScale);
        this.roughness2Noise.setYScale(heightScale);
        this.roughness2Noise.setZScale(coordinateScale);

        this.detailNoise = new PerlinOctaveGenerator(this.nukkitRandom, 8, 3, 33, 3);
        this.detailNoise.setXScale(coordinateScale / detailNoiseScaleX);
        this.detailNoise.setYScale(heightScale / detailNoiseScaleY);
        this.detailNoise.setZScale(coordinateScale / detailNoiseScaleZ);

        this.islandNoise = new SimplexNoise(this.nukkitRandom);

        for (ObsidianPillar obsidianPillar : ObsidianPillar.getObsidianPillars(this.level.getSeed())) {
            PopulatorObsidianPillar populator = new PopulatorObsidianPillar(obsidianPillar);
            populator.setAmount(1);
            this.populators.add(populator);
        }

        this.populators.add(new PopulatorPodium(activated, spawnDragon));
        this.populators.add(new PopulatorEndIsland(this));
        this.populators.add(new PopulatorChorusPlant(this));
        this.populators.add(new PopulatorEndGateway(this));
    }

    @Override
    public void generateChunk(int chunkX, int chunkZ) {
        this.nukkitRandom.setSeed(chunkX * localSeed1 ^ chunkZ * localSeed2 ^ this.level.getSeed());

        BaseFullChunk chunk = this.level.getChunk(chunkX, chunkZ);

        int densityX = chunkX << 1;
        int densityZ = chunkZ << 1;

        double[] roughnessNoise = this.roughnessNoise.getFractalBrownianMotion(densityX, 0, densityZ, 0.5d, 2d);
        double[] roughnessNoise2 = this.roughness2Noise.getFractalBrownianMotion(densityX, 0, densityZ, 0.5d, 2d);
        double[] detailNoise = this.detailNoise.getFractalBrownianMotion(densityX, 0, densityZ, 0.5d, 2d);

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

        this.generationPopulators.forEach(populator -> populator.populate(this.level, chunkX, chunkZ, this.nukkitRandom, chunk));
    }

    @Override
    public void populateChunk(int chunkX, int chunkZ) {
        BaseFullChunk chunk = level.getChunk(chunkX, chunkZ);
        this.nukkitRandom.setSeed(0xdeadbeef ^ (chunkX << 8) ^ chunkZ ^ this.level.getSeed());
        this.populators.forEach(populator -> populator.populate(this.level, chunkX, chunkZ, this.nukkitRandom, chunk));
        Biome.getBiome(chunk.getBiomeId(7, 7)).populateChunk(this.level, chunkX, chunkZ, this.nukkitRandom);
    }

    @Override
    public Vector3 getSpawn() {
        return new Vector3(100.5, 49, 0.5);
    }

    public float getIslandHeight(int chunkX, int chunkZ) {
        float xx = (chunkX << 1) + 1;
        float zz = (chunkZ << 1) + 1;
        float height = (float) (100 - Math.sqrt(Math.pow(xx, 2) + Math.pow(zz, 2)) * 8f);
        if (height > 80) {
            height = 80;
        }
        if (height < -100) {
            height = -100;
        }

        for (int cx = -12; cx <= 12; ++cx) {
            for (int cz = -12; cz <= 12; ++cz) {
                long x = chunkX + cx;
                long z = chunkZ + cz;

                if (Math.pow(x, 2) + Math.pow(z, 2) > 4096 && this.islandNoise.noise(x, z) < -0.8999999761581421) { // 0.9f / 1.0d
                    xx = 1 - (cx << 1);
                    zz = 1 - (cz << 1);
                    float height2 = (float) (100 - Math.sqrt(Math.pow(xx, 2) + Math.pow(zz, 2)) * ((Math.abs(x) * 3439 + Math.abs(z) * 147) % 13 + 9));
                    if (height2 > 80) {
                        height2 = 80;
                    }
                    if (height2 < -100) {
                        height2 = -100;
                    }
                    if (height2 > height) {
                        height = height2;
                    }
                }
            }
        }

        return height;
    }

    public static void spawnPlatform(Position pos) {
        Level level = pos.getLevel();
        int x = pos.getFloorX();
        int y = pos.getFloorY();
        int z = pos.getFloorZ();
        for (int xx = x - 2; xx < x + 3; xx++) {
            for (int zz = z - 2; zz < z + 3; zz++)  {
                level.setBlockAt(xx, y - 1, zz, OBSIDIAN);
                for (int yy = y; yy < y + 4; yy++) {
                    level.setBlockAt(xx, yy, zz, AIR);
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    protected static <T> T getConfig(Config config, String variable, T defaultValue) {
        Object value = config.get(variable);
        return value == null ? defaultValue : (T) value;
    }
}
