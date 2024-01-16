package kp.producers;

import static kp.Constants.BOOTSTRAP_SERVER;
import static kp.Constants.KEY_LIST_1;
import static kp.Constants.KEY_LIST_2;
import static kp.Constants.LINE_SEP;
import static kp.Constants.THICK_LINE;
import static kp.Constants.TOPIC_PROD_1;
import static kp.Constants.TOPIC_PROD_2;
import static kp.Constants.VAL_LIST_P1;
import static kp.Constants.VAL_LIST_P2;

import java.lang.invoke.MethodHandles;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.stream.IntStream;

import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The KP producer.
 * <p>
 * Sends the {@link ProducerRecord}s with the {@link KafkaProducer}.
 */
public class KpProducer {

	private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass().getName());
	private final Properties properties;
	private static final AtomicInteger atomic = new AtomicInteger();
	private static final int PRODUCER_PAUSE_SECONDS = 15;

	/**
	 * The constructor.
	 */
	public KpProducer() {
		super();
		this.properties = initProperties();
	}

	/**
	 * Produces the {@link ProducerRecord}s.
	 * 
	 */
	public void produceRecords() {

		boolean executeFlag = true;
		while (executeFlag) {
			produceRecordsSet();
			executeFlag = pauseProducing();
		}
	}

	/**
	 * Initializes the {@link Properties}.
	 * 
	 * @return the {@link Properties}
	 */
	private Properties initProperties() {

		final Properties props = new Properties();
		props.put(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVER);
		props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
		props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

		props.put(ProducerConfig.ACKS_CONFIG, "all");
		return props;
	}

	/**
	 * Produces the set of {@link ProducerRecord}s.
	 * 
	 */
	private void produceRecordsSet() {

		final int counter = atomic.incrementAndGet();
		final StringBuilder stringBuilder = new StringBuilder();
		final BiConsumer<Producer<String, String>, ProducerRecord<String, String>> sendAction = (producer, rec) -> {
			producer.send(rec);
			stringBuilder.append(String.format(//
					"ProducerRecord: key[%s], value[%s]%n", rec.key(), rec.value()));
		};
		stringBuilder.append(LINE_SEP).append(THICK_LINE).append(LINE_SEP);
		try (final Producer<String, String> producer = new KafkaProducer<>(properties)) {
			IntStream.range(0, KEY_LIST_1.size()).boxed()//
					.map(i -> new ProducerRecord<>(TOPIC_PROD_1, KEY_LIST_1.get(i),
							String.format("%s-%02d", VAL_LIST_P1.get(i), counter)))
					.forEach(rec -> sendAction.accept(producer, rec));
		}
		stringBuilder.append(THICK_LINE).append(LINE_SEP);

		stringBuilder.append(THICK_LINE).append(LINE_SEP);
		try (final Producer<String, String> producer = new KafkaProducer<>(properties)) {
			IntStream.range(0, KEY_LIST_2.size()).boxed()//
					.map(i -> new ProducerRecord<>(TOPIC_PROD_2, KEY_LIST_2.get(i),
							String.format("%s-%02d", VAL_LIST_P2.get(i), counter)))
					.forEach(rec -> sendAction.accept(producer, rec));
		}
		stringBuilder.append(THICK_LINE);
		final String message = stringBuilder.toString();
		logger.info(message);
	}

	/**
	 * Pauses the record producing
	 * 
	 * @return the flag
	 */
	private boolean pauseProducing() {

		try {
			TimeUnit.SECONDS.sleep(PRODUCER_PAUSE_SECONDS);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			logger.error(String.format("exception[%s]", e.getMessage()));
			return false;
		}
		return true;
	}

}
