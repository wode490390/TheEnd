package cn.wode490390.nukkit.theend;

import cn.nukkit.level.generator.Generator;
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.utils.Config;
import cn.wode490390.nukkit.theend.generator.TheEndGenerator;
import cn.wode490390.nukkit.theend.listener.PortalListener;
import cn.wode490390.nukkit.theend.listener.TheEndListener;

public class TheEnd extends PluginBase {

    private static final String CONFIG_ACTIVATED = "exit-portal-activated";
    private static final String CONFIG_PORTAL = "enable-end-portal";

    public static Config config;
    public static boolean activated;

    @Override
    public void onEnable() {
        try {
            new MetricsLite(this);
        } catch (Exception ignore) {

        }
        this.saveDefaultConfig();
        config = this.getConfig();
        String node = CONFIG_ACTIVATED;
        try {
            activated = config.getBoolean(node, true);
        } catch (Exception e) {
            activated = true;
            this.logLoadException(node);
        }
        node = CONFIG_PORTAL;
        boolean portal;
        try {
            portal = config.getBoolean(node, true);
        } catch (Exception e) {
            portal = true;
            this.logLoadException(node);
        }

        Generator.addGenerator(TheEndGenerator.class, "the_end", TheEndGenerator.TYPE_THE_END);
        this.getServer().getPluginManager().registerEvents(new TheEndListener(), this);
        if (portal) {
            this.getServer().getPluginManager().registerEvents(new PortalListener(), this);
        }
    }

    private void logLoadException(String node) {
        this.getLogger().alert("An error occurred while reading the configuration '" + node + "'. Use the default value.");
    }
}
