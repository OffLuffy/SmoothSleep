package com.luffbox.smoothsleep.tasks;

import com.luffbox.smoothsleep.SmoothSleep;
import org.bukkit.scheduler.BukkitRunnable;

import javax.net.ssl.HttpsURLConnection;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class TransmitDataTask extends BukkitRunnable {

	private SmoothSleep pl;
	public TransmitDataTask(SmoothSleep plugin) { pl = plugin; }

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
			con.setInstanceFollowRedirects(true);
			con.addRequestProperty("User-Agent", "SmoothSleep Plugin");
			con.setDoOutput(true);

			PrintStream ps = new PrintStream(con.getOutputStream());
			boolean first = true;
			for (Map.Entry<String, String> dataEntry : data.entrySet()) {
				ps.print( (first ? "" : "&") + dataEntry.getKey() + "=" + dataEntry.getValue());
				first = false;
			}
			ps.close();
		} catch (Exception e) { return; }

	}

}
