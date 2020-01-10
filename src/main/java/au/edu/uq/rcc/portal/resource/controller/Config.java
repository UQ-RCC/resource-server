package au.edu.uq.rcc.portal.resource.controller;

import au.org.massive.strudel_web.job_control.ConfigurationRegistry;
import au.org.massive.strudel_web.job_control.InvalidJsonConfigurationException;
import au.org.massive.strudel_web.job_control.StrudelDesktopConfigurationAdapter;
import au.edu.uq.rcc.portal.resource.ssh.CertAuthManager;
import au.org.massive.strudel_web.vnc.GuacamoleSessionManager;
import au.edu.uq.rcc.portal.resource.miscs.ResourceServerSettings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

@Configuration
public class Config {
	@Bean(name = "nimrodDataSource")
	@ConfigurationProperties(prefix = "nimrod.datasource")
	public DataSource nimrodDataSource() {
		return DataSourceBuilder.create().build();
	}

	@Bean(name = "nimrodDb")
	@Autowired
	public JdbcTemplate nimrodJdbcTemplate(@Qualifier("nimrodDataSource") DataSource ds) {
		return new JdbcTemplate(ds);
	}

	@Bean
	public GuacamoleSessionManager guacamoleSessionManager(ResourceServerSettings settings, CertAuthManager certAuthManager) {
		return new GuacamoleSessionManager(settings.getTmpDir(), certAuthManager);
	}

	@Bean
	public ConfigurationRegistry configurationRegistry(@Value("${resource-server.jsonfile}") String jsonFile) throws InvalidJsonConfigurationException, IOException {
		String json = new String(Files.readAllBytes(Paths.get(jsonFile)), StandardCharsets.UTF_8);
		ConfigurationRegistry registry = new ConfigurationRegistry();
		StrudelDesktopConfigurationAdapter strudelConfig = new StrudelDesktopConfigurationAdapter("", json);
		for (String configId : strudelConfig.keySet()) {
			registry.addSystemConfiguration(configId, strudelConfig.get(configId));
		}
		return registry;
	}
}