package com.cloudera.kafkaexamples;

import org.apache.commons.cli.*;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.util.Arrays;
import java.util.Properties;
import java.util.UUID;


public class SimpleConsumer {
  public static void main(String[] args) {

    // create Options object
    Options options = new Options();

    Option option = Option.builder()
            .longOpt( "from-beginning" )
            .desc("Read topic from beginning")
            .hasArg(false)
            .argName("from-beginning")
            .build();

    options.addOption(option);

    CommandLineParser parser = new DefaultParser();
    CommandLine cmd = null;
    try {
      cmd = parser.parse( options, args);
    } catch (ParseException e) {
      System.err.println( "Parsing failed.  Reason: " + e.getMessage() );
      System.exit(1);
    }

    // Set up client Java properties
    Properties props = new Properties();
    props.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,
        "quickstart.cloudera:9092");
    // Just a user-defined string to identify the consumer group
    props.put(ConsumerConfig.GROUP_ID_CONFIG, UUID.randomUUID().toString());
    // Enable auto offset commit
    props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true");
    props.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "1000");
    props.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
        StringDeserializer.class.getName());
    props.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
        StringDeserializer.class.getName());


    if (null != cmd && cmd.hasOption("from-beginning")) {
      // This is confusing versus using seekToBeginning(partitions).  The problem is we have not
      // been assigned any partitions yet, so seekToBeginning doesn't work without
      // calling poll(0)
      // See https://grokbase.com/t/kafka/users/16384874pk/seektobeginning-doesnt-work-without-auto-offset-reset
      props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
      System.out.println("Setting to earliest");
    }

    // List of topics to subscribe to
    KafkaConsumer<String, String> consumer = new KafkaConsumer<String, String>(props);
    consumer.subscribe(Arrays.asList("ufo_sightings"));
    while (true) {
      try {

        ConsumerRecords<String, String> records = consumer.poll(100);
        for (ConsumerRecord<String, String> record : records) {
          System.out.printf("Partition = %d\n", record.partition());
          System.out.printf("Offset = %d\n", record.offset());
          System.out.printf("Key    = %s\n", record.key());
          System.out.printf("Value  = %s\n", record.value());
        }
      } catch (Exception e) {
        e.printStackTrace();
      } 
    }
  }
}
        
