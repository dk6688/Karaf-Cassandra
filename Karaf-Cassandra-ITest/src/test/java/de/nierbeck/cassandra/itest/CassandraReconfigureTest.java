/*
 *    Copyright 2015 Achim Nierbeck
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package de.nierbeck.cassandra.itest;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.net.URI;
import java.util.Dictionary;
import java.util.Hashtable;

import javax.inject.Inject;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;

import de.nierbeck.cassandra.embedded.CassandraService;

@RunWith(PaxExam.class)
@ExamReactorStrategy(PerClass.class)
@Ignore("Ignore right now due to timing issues")
public class CassandraReconfigureTest extends TestBase{

	@Inject
	private ConfigurationAdmin cm;
	
	@Test
	public void shouldHaveBundleContext() {
		assertThat(bc, is(notNullValue()));
	}

	@Test
	public void reconfigureEmbeddedCassandra() throws Exception {
		logger.info("Re-Configuring Embedded Cassandra");
		
		File yamlFile = new File("etc/cassandra.yaml");
        CassandraService service = getOsgiService(CassandraService.class, null, 120000);
        
		//assertThat(yamlFile.exists(), is(true));
		
		URI yamlUri = yamlFile.toURI();
		
		logger.info("using following cassandra.yaml file: {}", yamlUri);
		
		Configuration configuration = cm.getConfiguration("de.nierbeck.cassandra.embedded");
		Dictionary<String,Object> properties = configuration.getProperties();
		if (properties == null) {
			properties = new Hashtable<>();
		}
		properties.put("cassandra.yaml", yamlUri.toString());
		properties.put("jmx_port", "7299");
        properties.put("native_transport_port", "9242");
		
        logger.info("updating configuration with new properties.");
		configuration.setBundleLocation(null);
		configuration.update(properties);
		
		Thread.sleep(6000L);
		
		logger.info("verify restart successful");
		service = getOsgiService(CassandraService.class, null, 120000);
		assertThat(service.isRunning(), is(true));
		
		logger.info("checking command");
		assertThat(executeCommand("cassandra-admin:isRunning"),
				containsString("Embedded Cassandra is available"));
		
		logger.info("shutting down cassandra");
		cassandraService.stop();
	}


}
