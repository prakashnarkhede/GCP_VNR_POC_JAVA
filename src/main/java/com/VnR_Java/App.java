package com.VnR_Java;

import java.io.FileInputStream;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

import com.google.api.gax.paging.Page;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.ServiceOptions;
import com.google.cloud.pubsub.v1.AckReplyConsumer;
import com.google.cloud.pubsub.v1.MessageReceiver;
import com.google.cloud.pubsub.v1.Subscriber;
import com.google.cloud.storage.Bucket;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.google.common.collect.Lists;
import com.google.pubsub.v1.ProjectSubscriptionName;
import com.google.pubsub.v1.PubsubMessage;

public class App 
{
	private static final BlockingQueue<PubsubMessage> messages = new LinkedBlockingDeque<>();

	static class MessageReceiverExample implements MessageReceiver {

		public void receiveMessage(PubsubMessage message, AckReplyConsumer consumer) {
			messages.offer(message);
			consumer.ack();
		}
	}

	private static String projectId = ServiceOptions.getDefaultProjectId();
	private String topicId = formatForTest("bucketEventTriggerTopic");
	private static String subscriptionId = formatForTest("subscriber1");
	private int messageCount = 5;


	private static String formatForTest(String name) {
		return name + "-" + java.util.UUID.randomUUID().toString();
	}

	public static void main( String[] args ) throws Exception
	{
		ProcessingFile processFile = new ProcessingFile();

		GoogleCredentials credentials = GoogleCredentials.fromStream(new FileInputStream("C:\\Users\\praka\\AppData\\Roaming\\gcloud\\legacy_credentials\\aec.prakash@gmail.com\\adc.json"))
				.createScoped(Lists.newArrayList("https://www.googleapis.com/auth/cloud-platform"));
		Storage storage = StorageOptions.newBuilder().setCredentials(credentials).build().getService();

		//List all present buckets in the project
		Page<Bucket> buckets = storage.list();
		for (Bucket bucket : buckets.iterateAll()) {
			System.out.println("Bucket name from project::: "+bucket.toString());

		}
		
		
		//recieve messages from pubsub api
		ProjectSubscriptionName subscriptionName = ProjectSubscriptionName.of(
				projectId, "subscriber1");
		Subscriber subscriber = null;
		try {
			// create a subscriber bound to the asynchronous message receiver	
			subscriber =
					Subscriber.newBuilder(subscriptionName, new MessageReceiverExample()).build();
			subscriber.startAsync().awaitRunning();
			// Continue to listen to messages
			while (true) {
				PubsubMessage message = messages.take();
				System.out.println("Message Id: " + message.getMessageId());
				System.out.println("Data: " + message.getData().toStringUtf8());
				
				//extract the bucket name, consumer folder name and file name from Json
				String[] BucketConsumerFileName =  processFile.getBucketConsumerAndFileName(message.getData().toStringUtf8());
				processFile.readAndProcessFile(BucketConsumerFileName);
			}
		} finally {
			if (subscriber != null) {
				subscriber.stopAsync();
			}
		}
	}
}
