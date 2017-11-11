/*
 * Copyright 2012-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fr.pinguet62.springspecification.core.builder.database.autoconfigure.jdbc;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.boot.autoconfigure.DatabaseInitializationMode;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.config.ResourceNotFoundException;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.jdbc.config.SortedResourcesFactoryBean;
import org.springframework.jdbc.datasource.init.DatabasePopulatorUtils;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Initialize a {@link DataSource} based on a matching {@link DataSourceProperties}
 * config.
 *
 * @author Dave Syer
 * @author Phillip Webb
 * @author Eddú Meléndez
 * @author Stephane Nicoll
 * @author Kazuki Shimizu
 */
class SpringSpecificationDataSourceInitializer {

	private static final Log logger = LogFactory.getLog(SpringSpecificationDataSourceInitializer.class);

	private final DataSource dataSource;

	private final DataSourceProperties properties;

	private final ResourceLoader resourceLoader;

	/**
	 * Create a new instance with the {@link DataSource} to initialize and its matching
	 * {@link DataSourceProperties configuration}.
	 * @param dataSource the datasource to initialize
	 * @param properties the matching configuration
	 * @param resourceLoader the resource loader to use (can be null)
	 */
	SpringSpecificationDataSourceInitializer(DataSource dataSource, DataSourceProperties properties,
			ResourceLoader resourceLoader) {
		this.dataSource = dataSource;
		this.properties = properties;
		this.resourceLoader = (resourceLoader != null ? resourceLoader
				: new DefaultResourceLoader());
	}

	/**
	 * Create a new instance with the {@link DataSource} to initialize and its matching
	 * {@link DataSourceProperties configuration}.
	 * @param dataSource the datasource to initialize
	 * @param properties the matching configuration
	 */
	SpringSpecificationDataSourceInitializer(DataSource dataSource, DataSourceProperties properties) {
		this(dataSource, properties, null);
	}

	public DataSource getDataSource() {
		return this.dataSource;
	}

	/**
	 * Create the schema if necessary.
	 * @return {@code true} if the schema was created
	 * @see DataSourceProperties#getSchema()
	 */
	public boolean createSchema() {
		List<Resource> scripts = getScripts("spring.datasource.schema",
				this.properties.getSchema(), "schema");
		if (!scripts.isEmpty()) {
			if (!isEnabled()) {
				logger.debug("Initialization disabled (not running DDL scripts)");
				return false;
			}
			String username = this.properties.getSchemaUsername();
			String password = this.properties.getSchemaPassword();
			runScripts(scripts, username, password);
		}
		return !scripts.isEmpty();
	}

	/**
	 * Initialize the schema if necessary.
	 * @see DataSourceProperties#getData()
	 */
	public void initSchema() {
		List<Resource> scripts = getScripts("spring-specification.datasource.data",
				this.properties.getData(), "data");
		if (!scripts.isEmpty()) {
			if (!isEnabled()) {
				logger.debug("Initialization disabled (not running data scripts)");
				return;
			}
			String username = this.properties.getDataUsername();
			String password = this.properties.getDataPassword();
			runScripts(scripts, username, password);
		}
	}

	private boolean isEnabled() {
		DatabaseInitializationMode mode = DatabaseInitializationMode.EMBEDDED; // TODO this.properties.getInitializationMode();
		if (mode == DatabaseInitializationMode.NEVER) {
			return false;
		}
		if (mode == DatabaseInitializationMode.EMBEDDED
				&& !isEmbedded()) {
			return false;
		}
		return true;
	}

	private boolean isEmbedded() {
		try {
			return EmbeddedDatabaseConnection.isEmbedded(this.dataSource);
		}
		catch (Exception ex) {
			logger.debug("Could not determine if datasource is embedded", ex);
			return false;
		}
	}

	private List<Resource> getScripts(String propertyName, List<String> resources,
			String fallback) {
		if (resources != null) {
			return getResources(propertyName, resources, true);
		}
		String platform = this.properties.getPlatform();
		List<String> fallbackResources = new ArrayList<>();
		fallbackResources.add("classpath*:spring-specification/" + fallback + "-" + platform + ".sql");
		fallbackResources.add("classpath*:spring-specification/" + fallback + ".sql");
		return getResources(propertyName, fallbackResources, false);
	}

	private List<Resource> getResources(String propertyName, List<String> locations,
			boolean validate) {
		List<Resource> resources = new ArrayList<>();
		for (String location : locations) {
			for (Resource resource : doGetResources(location)) {
				if (resource.exists()) {
					resources.add(resource);
				}
				else if (validate) {
					throw new ResourceNotFoundException(propertyName, resource);
				}
			}
		}
		return resources;
	}

	private Resource[] doGetResources(String location) {
		try {
			SortedResourcesFactoryBean factory = new SortedResourcesFactoryBean(
					this.resourceLoader, Collections.singletonList(location));
			factory.afterPropertiesSet();
			return factory.getObject();
		}
		catch (Exception ex) {
			throw new IllegalStateException("Unable to load resources from " + location,
					ex);
		}
	}

	private void runScripts(List<Resource> resources, String username, String password) {
		if (resources.isEmpty()) {
			return;
		}
		ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
		populator.setContinueOnError(this.properties.isContinueOnError());
		populator.setSeparator(this.properties.getSeparator());
		if (this.properties.getSqlScriptEncoding() != null) {
			populator.setSqlScriptEncoding(this.properties.getSqlScriptEncoding().name());
		}
		for (Resource resource : resources) {
			populator.addScript(resource);
		}
		DataSource dataSource = this.dataSource;
		if (StringUtils.hasText(username) && StringUtils.hasText(password)) {
			dataSource = DataSourceBuilder.create(this.properties.getClassLoader())
					.driverClassName(this.properties.determineDriverClassName())
					.url(this.properties.determineUrl()).username(username)
					.password(password).build();
		}
		DatabasePopulatorUtils.execute(populator, dataSource);
	}

}