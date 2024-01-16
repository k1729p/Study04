package kp.processors;

import static kp.Constants.KEY_LIST_1;
import static kp.Constants.KEY_LIST_2;
import static kp.Constants.TOPIC_CONS_1;
import static kp.Constants.TOPIC_CONS_2;
import static kp.Constants.TOPIC_PROD_1;
import static kp.Constants.TOPIC_PROD_2;

import java.lang.invoke.MethodHandles;
import java.util.Properties;

import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.KStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import kp.utils.Utils;

/**
 * Compares the records in producer topics with the records in consumer topics:
 * <ul>
 * <li>'<b>prod-1</b>' versus '<b>cons-1</b>'
 * <li>'<b>prod-2</b>' versus '<b>cons-2</b>'
 * </ul>
 * <p>
 * Uses the {@link StreamsBuilder} to define the actual processing topology.
 */
public class KpComparer {

	private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass().getName());
	private final Properties properties;
	private final Topology topology;

	/**
	 * The constructor.
	 */
	public KpComparer() {
		super();
		this.properties = Utils.initializePropertiesForStream("kp-comparer");
		this.topology = initializeTopology();
		Utils.describeTopology(topology.describe());
	}

	/**
	 * Starts the {@link KafkaStreams}.
	 * 
	 */
	public void startStreams() {

		try (final KafkaStreams kafkaStreams = new KafkaStreams(topology, properties)) {
			kafkaStreams.start();
			logger.info("comparer started");
			Utils.block();
		}
	}

	/**
	 * Initializes the {@link Topology}.
	 * 
	 * @return the {@link Topology}
	 */
	private Topology initializeTopology() {

		final StreamsBuilder streamsBuilder = new StreamsBuilder();
		final KStream<String, String> joinedStream1 = streamsBuilder.<String, String>stream(TOPIC_PROD_1).leftJoin(
				streamsBuilder.globalTable(TOPIC_CONS_1), //
				(inputKey, inputValue) -> inputKey, //
				(inputValue, globalValue) -> String.format("topic/value[%s]/[%s], topic/value[%s]/[%s]", //
						TOPIC_PROD_1, inputValue, TOPIC_CONS_1, globalValue));
		joinedStream1.foreach((key, value) -> {
			if (KEY_LIST_1.getFirst().equals(key)) {
				final String message = String.format("key[%s], %s", key, value);
				logger.info(message);
			}
		});
		final KStream<String, String> joinedStream2 = streamsBuilder.<String, String>stream(TOPIC_PROD_2).leftJoin(
				streamsBuilder.globalTable(TOPIC_CONS_2), //
				(inputKey, srcValue) -> inputKey, //
				(inputValue, globalValue) -> String.format("topic/value[%s]/[%s], topic/value[%s]/[%s]", //
						TOPIC_PROD_2, inputValue, TOPIC_CONS_2, globalValue));
		joinedStream2.foreach((key, value) -> {
			if (KEY_LIST_2.getLast().equals(key)) {
				final String message = String.format("key[%s], %s", key, value);
				logger.info(message);
			}
		});
		return streamsBuilder.build();
	}

}
