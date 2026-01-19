package com.NamanJain.TubeFetch.controller;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.NamanJain.TubeFetch.service.DownloadService;

import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*", exposedHeaders = "Content-Disposition")
public class DownloadController {

	@Autowired
	private final DownloadService downloadService;

	public DownloadController(DownloadService downloadService) {
		this.downloadService = downloadService;
	}

	@GetMapping("/download")
	public void download(@RequestParam String url, @RequestParam String formatId, @RequestParam String extension,
			HttpServletResponse response) throws Exception {

		// Fetch video info
		Map<String, Object> videoInfo = downloadService.getVideoInfo(url);
		String title = (String) videoInfo.get("title");

		String filename = downloadService.buildFilename(title, extension);

		Process process = downloadService.buildDownloadProcess(url, formatId);

		response.setContentType("application/octet-stream");
		response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");

		try (InputStream is = process.getInputStream(); OutputStream os = response.getOutputStream()) {
			byte[] buffer = new byte[8192];
			int bytesRead;
			while ((bytesRead = is.read(buffer)) != -1) {
				os.write(buffer, 0, bytesRead);
				os.flush();
			}

			int exitCode = process.waitFor();
			if (exitCode != 0) {
				InputStream errorStream = process.getErrorStream();
				String error = new String(errorStream.readAllBytes());
				throw new RuntimeException("yt-dlp failed with exit code " + exitCode + ": " + error);
			}
		}
	}
	
    @PostMapping("info")
    public Map<String, Object> getInfo(@RequestBody Map<String, String> body)
            throws IOException, InterruptedException {
        String url = body.get("url");
        return downloadService.getVideoInfo(url);
    }

    @GetMapping("check")
    public ResponseEntity<String> check(@RequestParam String url) {
        if (url == null || !url.startsWith("http")) {
            return ResponseEntity.badRequest().body("Invalid URL");
        }
        return ResponseEntity.ok("OK");
    }

}
