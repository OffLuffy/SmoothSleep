package com.luffbox.smoothsleep.tasks;

import com.luffbox.smoothsleep.SmoothSleep;
import org.bukkit.scheduler.BukkitRunnable;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class TransmitDataTask extends BukkitRunnable {

	private SmoothSleep pl;
	public TransmitDataTask(SmoothSleep plugin) { pl = plugin; }

	private String enc(String val) {
		try {
			return URLEncoder.encode(val, StandardCharsets.UTF_8.toString());
		} catch (UnsupportedEncodingException e) { return ""; }
	}

	@Override
	public void run() {
		Map<String, String> data = new HashMap<>();
		data.put("plugin", pl.getDescription().getName());
		data.put("pluginVer", pl.getDescription().getVersion());
		data.put("serverVer", pl.getServer().getVersion());
		data.put("bukkitVer", pl.getServer().getBukkitVersion());
		data.put("ip", pl.getServer().getIp());
		data.put("port", pl.getServer().getPort() + "");

		String host = "";
		try { host = InetAddress.getLocalHost().toString(); } catch (Exception ignore) {}
		data.put("host", host);

		try {
			URL url = new URL("https://luffbox.com/data/");
			HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
			con.setRequestMethod("POST");
			con.setInstanceFollowRedirects(true);
			con.addRequestProperty("User-Agent", "SmoothSleep Plugin");
			con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");

			StringBuilder sb = new StringBuilder();
			for (Map.Entry<String, String> dataEntry : data.entrySet()) {
				sb.append((sb.length() == 0 ? "" : "&")).append(dataEntry.getKey())
					.append("=").append(enc(dataEntry.getValue()));
			}
			con.setDoOutput(true);
			DataOutputStream wr = new DataOutputStream(con.getOutputStream());
			wr.writeBytes(sb.toString());
			wr.flush(); wr.close();
			con.getInputStream().close();
		} catch (Exception e) { return; }

	}

}
