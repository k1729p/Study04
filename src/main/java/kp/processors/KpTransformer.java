package kp.processors;

import static kp.Constants.TOPIC_CONS_1;
import static kp.Constants.TOPIC_CONS_2;
import static kp.Constants.TOPIC_PROD_1;
import static kp.Constants.TOPIC_PROD_2;
import static kp.Constants.VAL_LIST_C1;
import static kp.Constants.VAL_LIST_C2;

import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.Properties;

import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.Branched;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Predicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import kp.utils.Utils;

/**
 * The KP transformer.<br>
 * Directs the records from the producer topics to the consumer topics branches.
 * <p>
 * Uses the {@link StreamsBuilder} to define the actual processing topology.<br>
 * The {@link KafkaStreams} DSL (Domain Specific Language) is built on top of
 * the Streams Processor API.
 */
public class KpTransformer {

	private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass().getName());
	private final Properties properties;
	private final Topology topology;

	/**
	 * The constructor.
	 */
	public KpTransformer() {
		super();
		this.properties = Utils.initializePropertiesForStream("kp-transformer");
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
			logger.info("transformer started");
			Utils.block();
		}
	}

	/**
	 * Gets the {@link Topology}. Used for tests.
	 * 
	 * @return the {@link Topology}
	 */
	public Topology getTopology() {
		return topology;
	}

	/**
	 * Initializes the {@link Topology}.
	 * 
	 * @return the {@link Topology}
	 */
	private Topology initializeTopology() {

		final StreamsBuilder streamsBuilder = new StreamsBuilder();
		final KStream<String, String> inputStream = streamsBuilder.stream(List.of(TOPIC_PROD_1, TOPIC_PROD_2));

		final Predicate<String, String> predicate1 = (key, value) -> VAL_LIST_C1
				.contains(value.substring(0, 1).toLowerCase());
		final Predicate<String, String> predicate2 = (key, value) -> VAL_LIST_C2
				.contains(value.substring(0, 1).toLowerCase());
		// It drops all not matching records because there is no default branch.
		inputStream.split()//
				.branch(predicate1, Branched.withConsumer(stream -> stream.to(TOPIC_CONS_1)))//
				.branch(predicate2, Branched.withConsumer(stream -> stream.to(TOPIC_CONS_2)));
		return streamsBuilder.build();
	}

}