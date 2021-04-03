package cn.wode490390.nukkit.theend;

import cn.nukkit.Server;
import cn.nukkit.level.generator.Generator;
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.utils.Config;
import cn.wode490390.nukkit.theend.generator.TheEndGenerator;
import cn.wode490390.nukkit.theend.listener.PortalListener;
import cn.wode490390.nukkit.theend.listener.ResummonListener;
import cn.wode490390.nukkit.theend.listener.TheEndListener;
import cn.wode490390.nukkit.theend.util.MetricsLite;

public class TheEnd extends PluginBase {

    public static final String THE_END_LEVEL_NAME = "the_end";

    private static TheEnd INSTANCE;

    @Override
    public void onLoad() {
        INSTANCE = this;
    }

    @Override
    public void onEnable() {
        try {
            new MetricsLite(this, 4882);
        } catch (Throwable ignore) {

        }

        this.saveDefaultConfig();
        Config config = this.getConfig();
        String key = "enable-end-portal";
        boolean portal = true;
        try {
            portal = config.getBoolean(key, portal);
        } catch (Exception e) {
            this.logConfigException(key, e);
        }
        key = "allow-resummon-ender-dragon";
        boolean resummon = true;
        try {
            resummon = config.getBoolean(key, resummon);
        } catch (Exception e) {
            this.logConfigException(key, e);
        }

        TheEndGenerator.setConfig(config);
        Generator.addGenerator(TheEndGenerator.class, TheEndGenerator.THE_END_GENERATOR_NAME, TheEndGenerator.TYPE_THE_END);

        boolean builtin = false;
        try {
            String[] version = this.getServer().getApiVersion().split("\\.");
            int majorVersion = Integer.parseInt(version[0]);
            int minorVersion = Integer.parseInt(version[1]);
            int revisionVersion = Integer.parseInt(version[2]);
            builtin = majorVersion > 1 || majorVersion == 1 && (minorVersion > 0 || revisionVersion >= 11);
        } catch (Exception ignored) {

        }

        if (!builtin) {
            this.getServer().getPluginManager().registerEvents(new TheEndListener(), this);
        }
        if (portal) {
            this.getServer().getPluginManager().registerEvents(new PortalListener(), this);
        }
        if (resummon) {
            this.getServer().getPluginManager().registerEvents(new ResummonListener(), this);
        }

        this.getServer().getScheduler().scheduleTask(this, TheEnd::loadTheEndLevel);
    }

    public static void loadTheEndLevel() {
        Server server = Server.getInstance();
        if (!server.loadLevel(THE_END_LEVEL_NAME)) {
            server.getLogger().info("No level called 'the_end' found, creating default the end level.");
            server.generateLevel(THE_END_LEVEL_NAME, System.currentTimeMillis(), Generator.getGenerator(TheEndGenerator.THE_END_GENERATOR_NAME));
            if (!server.isLevelLoaded(THE_END_LEVEL_NAME)) {
                server.loadLevel(THE_END_LEVEL_NAME);
            }
        }
    }

    private void logConfigException(String key, Throwable t) {
        this.getLogger().alert("An error occurred while reading the configuration '" + key + "'. Use the default value.", t);
    }

    public static TheEnd getInstance() {
        return INSTANCE;
    }
}
