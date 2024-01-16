package kp.utils;

import static kp.Constants.BOOTSTRAP_SERVER;

import java.lang.invoke.MethodHandles;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;

import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.TopologyDescription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import kp.Constants;

/**
 * The utilities.
 *
 */
public class Utils {

	private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass().getName());

	/**
	 * The hidden constructor.
	 * 
	 */
	private Utils() {
		throw new IllegalStateException("Utility class");
	}

	/**
	 * Initializes the {@link Properties}.
	 * 
	 * @param applicationId the application id
	 * @return the {@link Properties}
	 */
	public static Properties initializePropertiesForStream(String applicationId) {

		final Properties properties = new Properties();
		properties.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVER);
		try (Serde<String> serde = Serdes.String()) {
			properties.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, serde.getClass());
			properties.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, serde.getClass());
		}
		properties.put(StreamsConfig.APPLICATION_ID_CONFIG, applicationId);
		return properties;
	}

	/**
	 * Describes the topology.
	 * 
	 * @param topologyDescription the {@link TopologyDescription}
	 */
	public static void describeTopology(TopologyDescription topologyDescription) {

		final StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append(Constants.LINE_SEP).append(topologyDescription);
		stringBuilder.append(Constants.THIN_LINE).append(Constants.LINE_SEP);
		topologyDescription.subtopologies().forEach(//
				subtopology -> subtopology.nodes().forEach(//
						node -> stringBuilder.append(String.format("sub-topology node name[%s]%n", node.name()))));
		stringBuilder.append(Constants.THIN_LINE);
		final String message = stringBuilder.toString();
		logger.info(message);
	}

	/**
	 * Blocks the execution because the {@link CountDownLatch} is not counted down.
	 * 
	 */
	public static void block() {

		final CountDownLatch countDownLatch = new CountDownLatch(1);
		try {
			countDownLatch.await();
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			logger.error(e.getMessage());
		}
	}

}
