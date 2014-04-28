package com.softmotions.ncms.media.rest;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import com.google.inject.Inject;
import com.softmotions.commons.weboot.WBConfiguration;
import org.apache.commons.configuration.SubnodeConfiguration;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;

@Path("/media/file")
public class FileUploadRS extends MediaRestBase {

	//private static final String SERVER_UPLOAD_LOCATION_FOLDER = "media-data/";
	//private static final String SERVER_UPLOAD_LOCATION_FOLDER = "C://Users/nikos/Desktop/Upload_Files/";

	@Inject
	WBConfiguration cfg;

	@POST
	@Path("/{id}/upload")
	@Consumes("multipart/form-data")
	public Response uploadFile(@PathParam("id") Long id, MultipartFormDataInput input) {
		System.out.println("File ID: " + id);
		String fileName = "";
		Map<String, List<InputPart>> formParts = input.getFormDataMap();
		System.out.println("PARTS:\n" + formParts);
		List<InputPart> inPart = formParts.get("file");
		String storageDirPath = getStorageDir();
		if(storageDirPath == null) response(500, "Storare dir is not configured");
		File basedir = new File(storageDirPath);
		for (InputPart inputPart : inPart) {
			try {
				// Retrieve headers, read the Content-Disposition header to obtain the original name of the file
				MultivaluedMap<String, String> headers = inputPart.getHeaders();
				fileName = parseFileName(headers);
				// Handle the body of that part with an InputStream
				InputStream istream = inputPart.getBody(InputStream.class, null);
				File file = new File(basedir, fileName);
				saveFile(istream, file);
			} catch (IOException e) {
				e.printStackTrace();
				return response(500, e.getMessage());
			}
		}
		return ok("File saved: " + fileName);
	}

	private void saveFile(InputStream uploadedInputStream, File file) {
		try {
			int read = 0;
			byte[] bytes = new byte[1024];
			file.getParentFile().mkdirs();
			OutputStream outpuStream = new FileOutputStream(file);
			while ((read = uploadedInputStream.read(bytes)) != -1) {
				outpuStream.write(bytes, 0, read);
			}
			outpuStream.flush();
			outpuStream.close();
		} catch (IOException e) {

			e.printStackTrace();
		}
	}

	String getStorageDir() {
		SubnodeConfiguration nodeConfig = cfg.impl().configurationAt("storage");
		if(nodeConfig == null) return null;
		return nodeConfig.getString("[@basedir]");
	}

	// Parse Content-Disposition header to get the original file name
	private String parseFileName(MultivaluedMap<String, String> headers) {
		String[] contentDispositionHeader = headers.getFirst("Content-Disposition").split(";");
		System.out.println("Headers:\n" + headers);
		for (String name: contentDispositionHeader) {
			if ((name.trim().startsWith("filename"))) {
				String[] tmp = name.split("=");
				String fileName = tmp[1].trim().replaceAll("\"", "");
				return fileName;
			}
		}
		return "randomName";
	}
}


