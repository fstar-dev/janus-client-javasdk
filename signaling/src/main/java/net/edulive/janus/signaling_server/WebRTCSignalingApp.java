package main.java.net.edulive.janus.signaling_server;

import io.javalin.Javalin;
import io.javalin.config.JettyConfig;
import io.javalin.http.staticfiles.Location;
import io.javalin.websocket.WsContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import io.javalin.community.ssl.SslPlugin;

public class WebRTCSignalingApp {
	private static final Map<WsContext, String> userUsernameMap = new ConcurrentHashMap<>();

	private static final Logger logger = LoggerFactory.getLogger(WebRTCSignalingApp.class);

	public static void main(String[] args) {

		// SslPlugin sslPlugin = new SslPlugin(conf -> {
		// 	conf.pemFromPath("c:\\fullchain1.pem", "c:\\privkey1.pem");
		// });
//    	Javalin app = Javalin.create(config ->
////    		config.plugins.
//
//);
		Javalin app = Javalin.create(javalinConfig -> {
//    	    javalinConfig.registerPlugin(sslPlugin);
			javalinConfig.staticFiles.add("/main/resources/public");
		});
//        // SSL Context Factory for HTTPS configuration
//        SslContextFactory.Server sslContextFactory = new SslContextFactory.Server();
//        sslContextFactory.setKeyStorePath("C:\\Program Files\\Java\\jdk-21.0.2\\lib\\security\\keystore.jks");
//        sslContextFactory.setKeyStorePassword("123456");
//        sslContextFactory.setEndpointIdentificationAlgorithm("HTTPS");
//
//        // HTTPS Configuration
//        HttpConfiguration httpsConfig = new HttpConfiguration();
//        httpsConfig.addCustomizer(new SecureRequestCustomizer());

		// Create an HTTPS connector
//        ServerConnector sslConnector = new ServerConnector( app.jettyServer().server(),
//                new SslConnectionFactory(sslContextFactory, HttpVersion.HTTP_1_1.asString()),
//                new HttpConnectionFactory(httpsConfig));
//        sslConnector.setHost("0.0.0.0");
//        sslConnector.setPort(2443);
//        app.jettyServer().server().addConnector(sslConnector);

		app.get("/public", ctx -> ctx.redirect("/index.html"));
		// Add the connector to the server
		app.start(7070);

		MessageHandler messageHandler = new MessageHandler(
				System.getProperty("janus_address", "ws://49.13.235.28:8188"));

		app.ws("/api", ws -> {
			ws.onConnect(ctx -> {
				String username = randomString();
				userUsernameMap.put(ctx, username);
				messageHandler.createSession(username, ctx);
				ctx.send("{\"type\":\"status\",\"status\":\"connected\"}");
				logger.info("{} joined", username);
			});
			ws.onClose(ctx -> {
				String username = userUsernameMap.get(ctx);
				userUsernameMap.remove(ctx);
				messageHandler.destroySession(username);
				logger.info("{} left ", username);
			});
			ws.onMessage(ctx -> {
				String username = userUsernameMap.get(ctx);
				logger.info("{} send {}", username, ctx.message());
				messageHandler.handleMessage(userUsernameMap.get(ctx), ctx);
			});
		});
	}

	private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
	private static final Random random = new Random();

	public static String randomString() {
		int length = 20; // the desired length of the random string
		StringBuilder sb = new StringBuilder(length);
		for (int i = 0; i < length; i++) {
			int index = random.nextInt(CHARACTERS.length());
			sb.append(CHARACTERS.charAt(index));
		}
		return sb.toString();
	}
}
