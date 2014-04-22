package com.softmotions.ncms.media.db;

import com.avaje.ebean.EbeanServer;
import com.avaje.ebean.config.ServerConfig;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Provider;
import com.softmotions.commons.weboot.WBConfiguration;
import org.apache.commons.beanutils.BeanUtils;

import javax.sql.DataSource;
import java.io.StringReader;
import java.util.Properties;



import com.avaje.ebean.EbeanServer;
import com.avaje.ebean.EbeanServerFactory;
import com.avaje.ebean.LogLevel;
import com.avaje.ebean.config.ServerConfig;
import com.avaje.ebeaninternal.server.lib.ShutdownManager;
import com.google.inject.*;
import com.softmotions.commons.weboot.WBConfiguration;
import com.softmotions.ncms.media.model.MediaFile;
import com.softmotions.ncms.media.model.MediaFolder;
import com.softmotions.ncms.media.model.Tag;
import ninja.lifecycle.Dispose;
import org.apache.commons.beanutils.BeanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.StringReader;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.Properties;



/**
 * Created by shu on 4/22/2014.
 */
public class EbeanServerProvider implements Provider<EbeanServer> {

	private static final Logger log = LoggerFactory.getLogger(MediaDbModule.class);

	@Inject
	Injector injector;

	@Inject
	WBConfiguration cfg;

	@Inject(optional = true)
	Provider<DataSource> dsProvider;

	public EbeanServer get() {
		log.info("*** Creating ebean server...");
		ServerConfig conf = new ServerConfig();
		//SubnodeConfiguration ebeanCfg = cfg.impl().configurationAt("ebean");
		String propsStr = cfg.impl().getString("ebean");
		//if (ebeanCfg.getBoolean("[@useGuiceProvidedDatasource]", false)) {
		DataSource ds = dsProvider != null ? dsProvider.get() : null;
		if (ds == null) {
			throw new RuntimeException("No Guice bound DataSource.class");
		}
		log.info("*** EBean datasource bound: : " + ds);
		conf.setDataSource(ds);
		//}

		conf.setName("test-ebean-server");

		conf.setDdlGenerate(true);
		conf.setDdlRun(true);

		if (propsStr != null) {
			log.info("*** Properties: \n" + propsStr);
			Properties cprops = new Properties();
			try {
				cprops.load(new StringReader(propsStr));
				BeanUtils.populate(conf, (Map) cprops);
			} catch (IllegalAccessException | InvocationTargetException | IOException e) {
				String msg = "Failed to load <ebean> properties";
				log.error(msg, e);
				throw new RuntimeException(msg, e);
			}
		}

		conf.setLoggingLevel(LogLevel.SQL);

		conf.addClass(MediaFile.class);
		conf.addClass(Tag.class);
		conf.addClass(MediaFolder.class);

		log.info("*** EbeanServer created: " + conf.getName() +
						"; Register: " + conf.isRegister() +
						"; Default: " + conf.isDefaultServer());

		return EbeanServerFactory.create(conf);
	}

	private static class AnnotationResolver {
		public static Class getClassWithAnnotation(Class<?> clazz, Class<? extends Annotation> annotation) {
			if (clazz.isAnnotationPresent(annotation)) {
				return clazz;
			}
			for (Class intf : clazz.getInterfaces()) {
				if (intf.isAnnotationPresent(annotation)) {
					return intf;
				}
			}
			Class superClass = clazz.getSuperclass();
			//noinspection ObjectEquality
			if (superClass != Object.class && superClass != null) {
				//noinspection TailRecursion
				return getClassWithAnnotation(superClass, annotation);
			}
			return null;
		}
	}

	public static class EbeanInitializer {
		@Dispose(order = 10)
		public void shutdown() {
			log.info("Issue ShutdownManager.shutdown()");
			ShutdownManager.shutdown();
		}
	}

}





