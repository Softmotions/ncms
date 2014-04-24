package com.softmotions.ncms.media.db;

import com.softmotions.ncms.NcmsWebTest;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;

/**
 * Created by shu on 4/24/2014.
 */
public class MediaRestTest extends NcmsWebTest {

	private String basePath;

	public MediaRestTest(String basePath) {

		this.basePath = basePath;
	}

	protected ResteasyWebTarget getWebTarget(String path) {
		ResteasyClient client = new ResteasyClientBuilder().build();
		ResteasyWebTarget target = client.target(getServerAddress() + "/ncms/rs/media" + basePath + path);
		return target;
	}

}
