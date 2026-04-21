package ru.lottery;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

public class LotteryApplication {
  public static void main(String[] args) throws Exception {
    ApplicationFactory factory = new ApplicationFactory();

    int port = Integer.parseInt(System.getenv().getOrDefault("PORT", "8080"));
    Server server = new Server(port);

    ServletContextHandler context = new ServletContextHandler();
    context.setContextPath("/");
    context.addServlet(new ServletHolder(factory.getHealthServlet()), "/health");
    server.setHandler(context);
    server.start();

    server.join();
  }
}
