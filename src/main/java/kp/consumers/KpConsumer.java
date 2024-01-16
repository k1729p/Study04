package kp.consumers;

import static kp.Constants.BOOTSTRAP_SERVER;
import static kp.Constants.LINE_SEP;
import static kp.Constants.THICK_LINE;
import static kp.Constants.TOPIC_CONS_1;

import java.lang.invoke.MethodHandles;
import java.time.Duration;
import java.util.Collections;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The KP consumer.
 * <p>
 * Consumes the {@link ConsumerRecord}s with the {@link KafkaConsumer}.
 */
public class KpConsumer {

	private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass().getName());

	final Properties properties;
	final String topic;

	/**
	 * The constructor.
	 */
	public KpConsumer() {
		this(TOPIC_CONS_1);
	}

	/**
	 * The constructor.
	 * 
	 * @param topic the topic
	 */
	public KpConsumer(String topic) {
		super();
		this.properties = initProperties();
		this.topic = topic;
	}

	/**
	 * Consumes the {@link ConsumerRecord}s.
	 * 
	 */
	public void consumeRecords() {

		try (final KafkaConsumer<String, String> consumer = new KafkaConsumer<>(properties)) {
			consumer.subscribe(Collections.singletonList(topic));
			final String message = String.format("consumer started, topic[%s]", topic);
			logger.info(message);

			boolean executeFlag = true;
			while (executeFlag) {
				final ConsumerRecords<String, String> consumerRecords = consumer.poll(Duration.ofMillis(100));
				if (consumerRecords.isEmpty()) {
					continue;
				}
				showRecords(consumerRecords);
				executeFlag = sleepMilliseconds();
			}
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
		props.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
		props.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());

		props.setProperty(CommonClientConfigs.GROUP_ID_CONFIG, "kp-consumer-group");
		props.setProperty(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true");
		props.setProperty(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "1000");
		return props;
	}

	/**
	 * Shows the {@link ConsumerRecords} content.
	 * 
	 * @param consumerRecords the {@link ConsumerRecords}
	 */
	private void showRecords(ConsumerRecords<String, String> consumerRecords) {

		final StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append(LINE_SEP).append(THICK_LINE).append(LINE_SEP);
		for (ConsumerRecord<String, String> consumerRecord : consumerRecords) {
			stringBuilder.append(String.format("ConsumerRecord: offset[%d], key[%s], value[%s]%n",
					consumerRecord.offset(), consumerRecord.key(), consumerRecord.value()));
		}
		stringBuilder.append(THICK_LINE);
		final String message = stringBuilder.toString();
		logger.info(message);
	}

	/**
	 * Pauses for given milliseconds.
	 * <p>
	 * Only for the definition of exit from endless loop, which is not used here.
	 * 
	 * @return the flag
	 */
	private boolean sleepMilliseconds() {

		try {
			TimeUnit.MILLISECONDS.sleep(1);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			logger.error(String.format("exception[%s]", e.getMessage()));
			return false;
		}
		return true;
	}

}
