package me.miquiis.wardrobe.common;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.io.WritingMode;
import net.minecraftforge.common.ForgeConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

import java.io.File;

public class Configs {

    public static class DatabaseConfig {

        private static final String DEFAULT_HOST = "localhost";
        private static final String DEFAULT_PORT = "3306";
        private static final String DEFAULT_DATABASE = "wardrobe";
        private static final String DEFAULT_USERNAME = "root";
        private static final String DEFAULT_PASSWORD = "";

        public final ForgeConfigSpec.ConfigValue<String> host;
        public final ForgeConfigSpec.ConfigValue<String> port;
        public final ForgeConfigSpec.ConfigValue<String> database;
        public final ForgeConfigSpec.ConfigValue<String> username;
        public final ForgeConfigSpec.ConfigValue<String> password;

        public DatabaseConfig(ForgeConfigSpec.Builder builder)
        {
            builder.push("server");
            this.host = builder
                    .comment("Database Host IP")
                    .define("host", DEFAULT_HOST);
            this.port = builder
                    .comment("Database Port [Default: 3306]")
                    .define("port", DEFAULT_PORT);
            this.database = builder
                    .comment("Database Name")
                    .define("database", DEFAULT_DATABASE);
            this.username = builder
                    .comment("Database Username")
                    .define("username", DEFAULT_USERNAME);
            this.password = builder
                    .comment("Database Password")
                    .define("password", DEFAULT_PASSWORD);
            builder.pop();
        }
    }

    public static final DatabaseConfig DATABASE_CONFIG;
    public static final ForgeConfigSpec DATABASE_CONFIG_SPEC;

    static {
        Pair<DatabaseConfig, ForgeConfigSpec> serverSpecPair = new ForgeConfigSpec.Builder().configure(DatabaseConfig::new);
        DATABASE_CONFIG = serverSpecPair.getLeft();
        DATABASE_CONFIG_SPEC = serverSpecPair.getRight();
    }

    public static void loadConfig(ForgeConfigSpec config, String path) {
        final CommentedFileConfig file = CommentedFileConfig.builder(new File(path)).sync().autosave().writingMode(WritingMode.REPLACE).build();
        file.load();
        config.setConfig(file);
    }

}
