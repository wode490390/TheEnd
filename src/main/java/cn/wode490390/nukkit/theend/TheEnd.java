package cn.wode490390.nukkit.theend;

import cn.nukkit.level.generator.Generator;
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.utils.Config;
import cn.wode490390.nukkit.theend.generator.TheEndGenerator;
import cn.wode490390.nukkit.theend.listener.PortalListener;
import cn.wode490390.nukkit.theend.listener.TheEndListener;
import cn.wode490390.nukkit.theend.util.MetricsLite;

public class TheEnd extends PluginBase {

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
        String node = "enable-end-portal";
        boolean portal = true;
        try {
            portal = config.getBoolean(node, portal);
        } catch (Exception e) {
            this.logConfigException(node, e);
        }

        TheEndGenerator.setConfig(config);
        Generator.addGenerator(TheEndGenerator.class, "the_end", TheEndGenerator.TYPE_THE_END);

        this.getServer().getPluginManager().registerEvents(new TheEndListener(), this);
        if (portal) {
            this.getServer().getPluginManager().registerEvents(new PortalListener(), this);
        }
    }

    private void logConfigException(String node, Throwable t) {
        this.getLogger().alert("An error occurred while reading the configuration '" + node + "'. Use the default value.", t);
    }

    public static TheEnd getInstance() {
        return INSTANCE;
    }
}
