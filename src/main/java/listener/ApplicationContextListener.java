package listener;

import dao.DBConnectionFactory;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

@WebListener
public class ApplicationContextListener implements ServletContextListener {
    public ApplicationContextListener() {
    }

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        DBConnectionFactory.init();
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        DBConnectionFactory.closePool();
    }
}
