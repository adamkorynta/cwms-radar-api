package fixtures;

import java.io.File;
import java.util.logging.Logger;

import org.apache.catalina.Engine;
import org.apache.catalina.Host;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.startup.ExpandWar;
import org.apache.catalina.startup.HostConfig;
import org.apache.catalina.startup.Tomcat;



/**
 * Tomcat server for ./gradle run and
 * Integration tests
 *
 * @Since 2021-11-05
 */
public class TomcatServer {
    private static final Logger logger = Logger.getLogger(TomcatServer.class.getName());
    private Tomcat tomcatInstance = null;
    /**
     * Setups the baseline for tomcat to run.
     * @param baseDir set to the CATALINA_BASE directory the build has setup
     * @param radarWar points to the actual WAR file to load
     * @param port Network port to listen on
     * @param contextName url prefix to use, can be "/","/cwms-data","/spk-data"
     *                    etc
     * @throws Exception any error that gets thrown
     */
    public TomcatServer(final String baseDir,
                        final String radarWar,
                        final int port,
                        final String contextName
    ) throws Exception {

        tomcatInstance = new Tomcat();
        tomcatInstance.setBaseDir(baseDir);
        Host host = tomcatInstance.getHost();

        host.setAppBase("webapps");
        new File(tomcatInstance.getServer().getCatalinaBase(),"temp").mkdirs();
        new File(tomcatInstance.getServer().getCatalinaBase(),"webapps").mkdirs();
        tomcatInstance.setPort(port);
        Connector connector = tomcatInstance.getConnector();
        connector.setSecure(true);
        connector.setScheme("https");
        tomcatInstance.setSilent(false);
        tomcatInstance.enableNaming();
        Engine engine = tomcatInstance.getEngine();
        logger.info("Got engine " + engine.getDefaultHost());

        host.addLifecycleListener(new HostConfig());
        
        tomcatInstance.addContext("", null);

        File radar = new File(radarWar);
        try {
            File existingRadar = new File(tomcatInstance.getHost().getAppBaseFile().getAbsolutePath(),contextName);
            ExpandWar.delete(existingRadar);
            ExpandWar.delete(new File(existingRadar.getAbsolutePath()+".war"));
            ExpandWar.copy(radar, new File(tomcatInstance.getHost().getAppBaseFile(),"cwms-data.war"));
        } catch( Exception ex) {
            throw new Exception("Unable to setup war",ex);
        }

    }

    public int getPort() {
        return tomcatInstance.getConnector().getLocalPort();
    }

    /**
     * Starts the instance of tomcat and returns when it's ready.
     * @throws LifecycleException any error in the startup sequence
     */
    public void start() throws LifecycleException {
        tomcatInstance.start();
        System.out.println("Tomcat listening at http://localhost:" + tomcatInstance.getConnector().getPort());
    }

    /**
     * Used for the ./gradlew run command.
     * Unit tests only need to call start and move on.
     */
    public void await() {
        tomcatInstance.getServer().await();
    }

    /**
     * Stops the instance of tomcat, including destroying the JNDI context.
     * @throws LifecycleException any error in the stop sequence
     */
    public void stop() throws LifecycleException {
        tomcatInstance.stop();
        //tomcatJndi.tearDown();
    }

    /**
     * arg[0] - the CATALINA_BASE directory you've setup
     * arg[1] - full path to the war file generated by this build script
     * arg[2] - name to use for this instance. See constructor for guidance
     * @param args standard argument list
     */
    public static void main(String []args) {
        String baseDir = args[0];
        String radarWar = args[1];
        String contextName = args[2];
        int port = Integer.parseInt(System.getProperty("RADAR_LISTEN_PORT","0").trim());

        try {
            TomcatServer tomcat = new TomcatServer(baseDir, radarWar, port, contextName);
            tomcat.start();
            tomcat.await();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }

    }
}
