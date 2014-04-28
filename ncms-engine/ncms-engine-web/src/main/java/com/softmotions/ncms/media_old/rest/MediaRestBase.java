package com.softmotions.ncms.media_old.rest;

import com.softmotions.ncms.jaxrs.NcmsRSException;
import com.softmotions.ncms.media_old.db.MediaDataManager;

import com.avaje.ebean.EbeanServer;
import com.google.inject.Inject;

import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.Response;

/**
 * Created by shu on 4/25/2014.
 */
public class MediaRestBase {

	@Inject
	protected MediaDataManager manager;

	@Inject
	protected EbeanServer ebean;

	protected Response response(int code, String message) {
		switch (code) {
			case 404:
				throw new NotFoundException(message);
			case 500:
				throw new NcmsRSException(message);
			default:
				throw new NcmsRSException("Error [: " + code + "] " + message);
		}
	}

	protected Response ok(Object entity) {
		return Response.status(200).entity(entity).build();
	}

}
