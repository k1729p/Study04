package kp.processors;

import static kp.Constants.KEY_LIST_1;
import static kp.Constants.TOPIC_CONS_1;
import static kp.Constants.TOPIC_CONS_2;
import static kp.Constants.TOPIC_PROD_1;
import static kp.Constants.VAL_LIST_C1;
import static kp.Constants.VAL_LIST_C2;
import static kp.Constants.VAL_LIST_P1;

import java.util.Map;
import java.util.stream.IntStream;

import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.TestInputTopic;
import org.apache.kafka.streams.TestOutputTopic;
import org.apache.kafka.streams.TopologyTestDriver;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import kp.utils.Utils;

/**
 * Testing the {@link KpTransformer}
 *
 */
public class KpTransformerTests {

	private static TopologyTestDriver testDriver;

	/**
	 * The constructor.
	 * 
	 */
	public KpTransformerTests() {
		super();
	}

	/**
	 * Set up before each test.
	 * 
	 */
	@BeforeAll
	static void setup() {
		testDriver = new TopologyTestDriver(new KpTransformer().getTopology(),
				Utils.initializePropertiesForStream("kp-test"));
	}

	/**
	 * Tear down after each test.
	 * 
	 */
	@AfterAll
	static void tearDown() {
		testDriver.close();
	}

	/**
	 * Should direct records to the consumer one.
	 * 
	 */
	@Test
	void shouldSplitToBranchOne() {

		try (Serde<String> serde = Serdes.String()) {
			// GIVEN
			final TestInputTopic<String, String> inputTopic = testDriver.createInputTopic(TOPIC_PROD_1,
					serde.serializer(), serde.serializer());
			final TestOutputTopic<String, String> outputTopic = testDriver.createOutputTopic(TOPIC_CONS_1,
					serde.deserializer(), serde.deserializer());
			// WHEN
			IntStream.range(0, KEY_LIST_1.size()).boxed()//
					.forEach(i -> inputTopic.pipeInput(KEY_LIST_1.get(i), VAL_LIST_P1.get(i)));
			final Map<String, String> actualMap = outputTopic.readKeyValuesToMap();
			// THEN
			Assertions.assertTrue(outputTopic.isEmpty());
			Assertions.assertEquals(actualMap.get(KEY_LIST_1.get(0)), VAL_LIST_C1.get(0));
			Assertions.assertNull(actualMap.get(KEY_LIST_1.get(1)));
			Assertions.assertEquals(actualMap.get(KEY_LIST_1.get(2)), VAL_LIST_C1.get(1));
			Assertions.assertNull(actualMap.get(KEY_LIST_1.get(3)));
		}
	}

	/**
	 * Should direct records to the consumer two.
	 * 
	 */
	@Test
	void shouldSplitToBranchTwo() {

		try (Serde<String> serde = Serdes.String()) {
			// GIVEN
			final TestInputTopic<String, String> inputTopic = testDriver.createInputTopic(TOPIC_PROD_1,
					serde.serializer(), serde.serializer());
			final TestOutputTopic<String, String> outputTopic = testDriver.createOutputTopic(TOPIC_CONS_2,
					serde.deserializer(), serde.deserializer());
			// WHEN
			IntStream.range(0, KEY_LIST_1.size()).boxed()//
					.forEach(i -> inputTopic.pipeInput(KEY_LIST_1.get(i), VAL_LIST_P1.get(i)));
			final Map<String, String> actualMap = outputTopic.readKeyValuesToMap();
			// THEN
			Assertions.assertTrue(outputTopic.isEmpty());
			Assertions.assertNull(actualMap.get(KEY_LIST_1.get(0)));
			Assertions.assertEquals(actualMap.get(KEY_LIST_1.get(1)), VAL_LIST_C2.get(0));
			Assertions.assertNull(actualMap.get(KEY_LIST_1.get(2)));
			Assertions.assertEquals(actualMap.get(KEY_LIST_1.get(3)), VAL_LIST_C2.get(1));
		}
	}

}
