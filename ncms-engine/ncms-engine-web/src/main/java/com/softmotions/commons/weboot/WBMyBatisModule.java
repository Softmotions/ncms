package com.softmotions.commons.weboot;

import com.google.inject.Inject;
import com.google.inject.Provider;

import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.SubnodeConfiguration;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.guice.XMLMyBatisModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.util.List;
import java.util.Properties;

/**
 * @author Adamansky Anton (adamansky@gmail.com)
 */
public class WBMyBatisModule extends XMLMyBatisModule {

    private static final Logger log = LoggerFactory.getLogger(WBMyBatisModule.class);

    final WBConfiguration cfg;

    public WBMyBatisModule(WBConfiguration cfg) {
        this.cfg = cfg;
    }

    protected void initialize() {
        XMLConfiguration xcfg = cfg.impl();
        setEnvironmentId(cfg.getDBEnvironmentType());
        SubnodeConfiguration mbCfg = xcfg.configurationAt("mybatis");
        String cfgLocation = mbCfg.getString("[@config]");
        if (cfgLocation == null) {
            throw new RuntimeException("Missing required 'config' attribute in <mybatis> element");
        }
        setClassPathResource(cfgLocation);

        Properties props = new Properties();
        List<HierarchicalConfiguration> fields = mbCfg.configurationsAt("property");
        for (HierarchicalConfiguration sub : fields) {
            String pkey = sub.getString("[@name]");
            String pval = sub.getString("[@value]");
            props.setProperty(pkey, pval);
        }
        addProperties(props);

        for (String k : props.stringPropertyNames()) {
            if (k.toLowerCase().contains("passw")) {
                props.setProperty(k, "********");
            }
        }
        log.info("MyBatis environment type: " + cfg.getDBEnvironmentType());
        log.info("MyBatis properties: " + props);
        log.info("MyBatis config: " + cfgLocation);

        bind(DataSource.class).toProvider(DataSourceProvider.class);
    }

    static class DataSourceProvider implements Provider<DataSource> {
        final Provider<SqlSessionFactory> sessionFactoryProvider;

        @Inject
        DataSourceProvider(Provider<SqlSessionFactory> sessionFactoryProvider) {
            this.sessionFactoryProvider = sessionFactoryProvider;
        }

        public DataSource get() {
            SqlSessionFactory sf = sessionFactoryProvider.get();
            Environment env = sf.getConfiguration().getEnvironment();
            return env.getDataSource();
        }
    }
}
