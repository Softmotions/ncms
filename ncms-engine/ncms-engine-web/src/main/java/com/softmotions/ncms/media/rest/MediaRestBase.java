package com.softmotions.ncms.media.rest;

import com.avaje.ebean.EbeanServer;
import com.google.inject.Inject;
import com.softmotions.ncms.media.db.MediaDataManager;

import javax.ws.rs.core.Response;

/**
 * Created by shu on 4/25/2014.
 */
public class MediaRestBase {

	@Inject
	protected MediaDataManager manager;

	@Inject
	protected EbeanServer ebean;

	protected Response response(int code, Object entity) {
		return Response.status(code).entity(entity).build();
	}

	protected Response ok(Object entity) {
		return response(200, entity);
	}

}
