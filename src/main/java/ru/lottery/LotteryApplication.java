package ru.lottery;

import java.util.EnumSet;

import jakarta.servlet.DispatcherType;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

public class LotteryApplication {
  public static void main(String[] args) throws Exception {
    ApplicationFactory factory = new ApplicationFactory();

    int port = Integer.parseInt(System.getenv().getOrDefault("PORT", "8080"));
    Server server = new Server(port);

    ServletContextHandler context = new ServletContextHandler();
    context.setContextPath("/");
    context.addServlet(new ServletHolder(factory.getAuthServlet()), "/api/v1/auth/*");
    context.addServlet(new ServletHolder(factory.getUserMeServlet()), "/api/v1/users/me");
    context.addServlet(
        new ServletHolder(factory.getUserTicketsServlet()), "/api/v1/users/me/tickets");
    context.addServlet(new ServletHolder(factory.getHealthServlet()), "/health");
    context.addFilter(
        new FilterHolder(factory.getAuthFilter()),
        "/api/v1/users/*",
        EnumSet.of(DispatcherType.REQUEST));
    server.setHandler(context);
    server.start();

    server.join();
  }
}
