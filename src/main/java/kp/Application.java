package kp;

import java.lang.invoke.MethodHandles;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import kp.consumers.KpConsumer;
import kp.processors.KpComparer;
import kp.processors.KpCounter;
import kp.processors.KpTransformer;
import kp.producers.KpProducer;

/**
 * The main class for the Kafka broker on Docker research.
 * <p>
 * Used libraries:
 * <ol>
 * <li>KafkaProducer
 * <li>KafkaStreams
 * <li>KafkaConsumer
 * </ol>
 */
public class Application {

	private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass().getName());
	private static final int WAIT_FOR_KAFKA_SECONDS = 15;

	/**
	 * The hidden constructor.
	 */
	private Application() {
		throw new IllegalStateException("Utility class");
	}

	/**
	 * The entry point for the application.
	 * 
	 * @param args the command-line arguments
	 */
	public static void main(String[] args) {

		waitForFullyOperationalKafkaService();

		final String selection = Optional.ofNullable(args)//
				.filter(arg -> arg.length > 0).map(arg -> arg[0]).orElse("");
		switch (selection) {
		case "consumer1":
			final KpConsumer kpConsumer1 = new KpConsumer(Constants.TOPIC_CONS_1);
			kpConsumer1.consumeRecords();
			break;
		case "consumer2":
			final KpConsumer kpConsumer2 = new KpConsumer(Constants.TOPIC_CONS_2);
			kpConsumer2.consumeRecords();
			break;
		case "transformer":
			final KpTransformer kpTransformer = new KpTransformer();
			kpTransformer.startStreams();
			break;
		case "counter":
			final KpCounter kpCounter = new KpCounter();
			kpCounter.startStreams();
			break;
		case "comparer":
			final KpComparer kpComparer = new KpComparer();
			kpComparer.startStreams();
			break;
		case "producer":
			final KpProducer kpProducer = new KpProducer();
			kpProducer.produceRecords();
			break;
		default:
			logger.info("unknown application function");
			break;
		}
	}

	/**
	 * Waits for the Kafka service to be fully operational.
	 * 
	 */
	private static void waitForFullyOperationalKafkaService() {

		try {
			TimeUnit.SECONDS.sleep(WAIT_FOR_KAFKA_SECONDS);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			logger.error(String.format("exception[%s]", e.getMessage()));
		}
	}

}
