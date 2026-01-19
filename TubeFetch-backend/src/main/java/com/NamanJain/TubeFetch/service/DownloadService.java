package com.NamanJain.TubeFetch.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import tools.jackson.databind.ObjectMapper;

@Service
public class DownloadService {

	@Value("${yt-dlp.path}")
	private String ytDlpPath;

	public String buildFilename(String title, String extension) {
		String safeTitle = title.replaceAll("[\\\\/:*?\"<>|]", "_");
		return safeTitle + "." + extension;
	}

	@Cacheable(value = "videoInfo")
	public Map<String, Object> getVideoInfo(String url) throws IOException, InterruptedException {

		Process process = new ProcessBuilder(ytDlpPath, "--dump-json", url).start();
		InputStream is = process.getInputStream();
		String json = new String(is.readAllBytes());

		int exitCode = process.waitFor();
		if (exitCode != 0) {
			InputStream errorStream = process.getErrorStream();
			String error = new String(errorStream.readAllBytes());
			throw new RuntimeException("yt-dlp failed: " + error);
		}

		ObjectMapper mapper = new ObjectMapper();
		Map<String, Object> fullInfo = mapper.readValue(json, Map.class);

		Map<String, Object> info = new HashMap<>();
		info.put("title", fullInfo.get("title"));
		info.put("thumbnail", fullInfo.get("thumbnail"));
		info.put("formats", fullInfo.get("formats"));
		return info;
	}

	public Process buildDownloadProcess(String url, String formatId) throws IOException {
		return new ProcessBuilder(ytDlpPath, "-f", formatId, "-o", "-", url)
				.redirectErrorStream(true).start();
	}
}
