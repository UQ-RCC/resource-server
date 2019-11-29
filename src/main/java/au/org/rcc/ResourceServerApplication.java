package au.org.rcc;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.Security;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.context.annotation.Bean;

import au.org.rcc.miscs.ResourceServerSettings;
import au.org.rcc.miscs.SecuritySettings;

import org.springframework.boot.web.servlet.ServletComponentScan;

@ServletComponentScan
@SpringBootApplication
@EnableAutoConfiguration(exclude = {DataSourceTransactionManagerAutoConfiguration.class, DataSourceAutoConfiguration.class})
@EnableConfigurationProperties
public class ResourceServerApplication {
	
	
    public static void main(String[] args){
    	Options options = new Options();
    	// json file
    	Option tempDir = new Option("t", "tempdir", true, "Temporary dir");
    	tempDir.setRequired(false);
        options.addOption(tempDir);
    	// json file
    	Option jsonFile = new Option("f", "jsonfile", true, "config json file");
    	jsonFile.setRequired(true);
        options.addOption(jsonFile);
    	// port
    	Option serverPort = new Option("p", "port", true, "Server port");
    	serverPort.setRequired(false);
        options.addOption(serverPort);
        // port
    	Option serverProtocol = new Option("P", "protocol", true, "Server protocol");
    	serverProtocol.setRequired(false);
        options.addOption(serverProtocol);
        // remote host
    	Option remoteHost = new Option("h", "remotehost", true, "Remote Host");
    	remoteHost.setRequired(true);
        options.addOption(remoteHost);
        // binding host
    	Option bindingHost = new Option("H", "bindinghost", true, "Binding Host");
    	bindingHost.setRequired(false);
        options.addOption(bindingHost);
        // security config file
    	Option securityConfig = new Option("s", "securityconf", true, "Security Config");
    	securityConfig.setRequired(true);
        options.addOption(securityConfig);
        // root context
        Option rootContextConfig = new Option("r", "rootcontext", true, "Root Context");
        rootContextConfig.setRequired(false);
        options.addOption(rootContextConfig);
        
        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd;
        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            formatter.printHelp("utility-name", options);
            System.exit(1);
            return;
        }
        String jsonFileValue = cmd.getOptionValue("jsonfile", "configuration.json");
        int port = Integer.parseInt(cmd.getOptionValue("port", "9001"));
        String protocol = cmd.getOptionValue("protocol", "AJP/1.3");
        String remoteHostValue = cmd.getOptionValue("remotehost", "localhost");
        String bindingHostValue = cmd.getOptionValue("bindinghost", "localhost");
        String securityConfigFile = cmd.getOptionValue("securityconf", "ssh_authz_server.properties");
    	String tempDirStr = cmd.getOptionValue("tempdir", "/tmp/");
    	String rootContext = cmd.getOptionValue("rootcontext", "");
        ResourceServerSettings rsSettings = ResourceServerSettings.getInstance();
    	SecuritySettings securitySettings = SecuritySettings.getInstance();
    	try {
			rsSettings.setJsonConfigFile(jsonFileValue);
			securitySettings.readConfig(securityConfigFile);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
			return;
		}
    	rsSettings.setResourceServerPort(port);
    	rsSettings.setRemoteHost(remoteHostValue);
    	rsSettings.setResourceServerHost(bindingHostValue);
    	rsSettings.setResourceServerProtocol(protocol);
    	rsSettings.setTempDir(tempDirStr);
    	rsSettings.setRootContext(rootContext);

		Security.addProvider(new BouncyCastleProvider());
        SpringApplication.run(ResourceServerApplication.class, args);
    }
    

	/**
	 * Sets the Tomcat server port / protocol according to the configuration file
	 * @return a configured container factory
	 */
	@Bean
	public TomcatServletWebServerFactory tomcat() throws UnknownHostException {
		ResourceServerSettings settings = ResourceServerSettings.getInstance();

		TomcatServletWebServerFactory factory = new TomcatServletWebServerFactory(settings.getRootContext(), settings.getResourceServerPort());
		factory.setAddress(InetAddress.getByName(settings.getResourceServerHost()));
		factory.setProtocol(settings.getResourceServerProtocol());
		return factory;
	}
}
