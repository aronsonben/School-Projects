package cmsc433.p5;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.WritableComparable;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;

/**
 * Map reduce which takes in a CSV file with tweets as input and output
 * key/value pairs.</br>
 * </br>
 * The key for the map reduce depends on the specified {@link TrendingParameter}
 * , <code>trendingOn</code> passed to
 * {@link #score(Job, String, String, TrendingParameter)}).
 */
public class TweetPopularityMR {

	// For your convenience...
	public static final int          TWEET_SCORE   = 1;
	public static final int          RETWEET_SCORE = 2;
	public static final int          MENTION_SCORE = 1;
	public static final int			 PAIR_SCORE = 1;

	// Is either USER, TWEET, HASHTAG, or HASHTAG_PAIR. Set for you before call to map()
	private static TrendingParameter trendingOn;

	public static class TweetMapper extends Mapper<LongWritable, Text, WritableComparable, IntWritable> {
		
		// In intermediate table created by map, assign each key (word) one. -- taken from Word Count example
		private final static IntWritable one = new IntWritable(1);
		
		// Used to hold keys for intermediate table
		private Text word = new Text();
		
		@Override
		public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
			// Converts the CSV line into a tweet object
			Tweet tweet = Tweet.createTweet(value.toString());
			//System.out.println(tweet.toString());
			
			
			if(trendingOn == TrendingParameter.USER) {
				String username = tweet.getUserScreenName();
				
				// Add a count for each user mentioned - account for "times mentioned"
				List<String> mentioned = tweet.getMentionedUsers();
				for(int i=0; i < mentioned.size(); i++) {			// can a user mentioned themselves?
					String userMentioned = mentioned.get(i);
					word.set(userMentioned);
					context.write(word, one);
				}
				
				// Add count of TWO for user who got retweeted (b/c 2*(# times retweeted))
				if(tweet.wasRetweetOfUser()) {
					String retweetedUser = tweet.getRetweetedUser();
					word.set(retweetedUser);
					context.write(word, new IntWritable(RETWEET_SCORE));
				}
				
				// Account for "tweet by user"
				word.set(username);
				context.write(word, one);
				
			}
			else if(trendingOn == TrendingParameter.TWEET) {
				LongWritable writer = new LongWritable();
				Long tweetID = tweet.getId();
				
				// Add count of TWO for tweet that got retweeted (b/c 2*(# times retweeted)) 
                if(tweet.wasRetweetOfTweet()) { 
                    Long originalTweetID = tweet.getRetweetedTweet();
                    writer.set(originalTweetID);
                    context.write(writer, new IntWritable(RETWEET_SCORE)); 
                } 
                // +1 count for tweet existing 
                writer.set(tweetID);
                context.write(writer, one);
				
			}
			else if(trendingOn == TrendingParameter.HASHTAG) {
				List<String> hashtags = tweet.getHashtags();
				for(int i = 0; i < hashtags.size(); i++) {
					word.set(hashtags.get(i));
					context.write(word, one);
				}
			}
			else {	// TrendingParameter.HASHTAG_PAIR
				List<String> hashtags = tweet.getHashtags();
				for(int i=0; i < hashtags.size(); i++) {
					for(int j=i+1; j < hashtags.size(); j++) {
						String hash1 = hashtags.get(i).toString();
						String hash2 = hashtags.get(j).toString();
						String pair;
						if(hash1.compareTo(hash2) <= 0) {
							pair = "(" + hash1 + "," + hash2 + ")";
						} else {
							pair = "(" + hash2 + "," + hash1 + ")";
						}
						
						word.set(pair);
						context.write(word, one);
					}
				}
			}
		}
	}
	/* Tweet 
	 * 				
	// Always +1 count for current tweet existing 
	//word.set(tweetID);
	//context.write(word, new IntWritable(TWEET_SCORE));
	context.write(new Text(tweetID), new IntWritable(TWEET_SCORE));
	
	// Add count of TWO for tweet that got retweeted (b/c 2*(# times retweeted))
	if(tweet.wasRetweetOfTweet()) {
		String originalTweetID = tweet.getRetweetedTweet().toString();
		//Text word2 = new Text();
		//word2.set(originalTweetID);
		context.write(new Text(originalTweetID), new IntWritable(RETWEET_SCORE));
	}

	 */

	public static class PopularityReducer extends Reducer<WritableComparable, IntWritable, WritableComparable, IntWritable> {

		@Override
		public void reduce(WritableComparable key, Iterable<IntWritable> values, Context context) throws IOException, InterruptedException {
			int sum = 0;
			for (IntWritable val : values) {
				sum += val.get();
			}
			//System.out.println("Sum: " + sum);
			context.write(key, new IntWritable(sum));
		}
		
	}
	/*
	 * if(key.equals(new Text("362828133306597376"))) {
					System.out.println("Added " + val.get() + " to 362828133306597376. Current score is: " + score);
				}
	 */

	/**
	 * Method which performs a map reduce on a specified input CSV file and
	 * outputs the scored tweets, users, or hashtags.</br>
	 * </br>
	 * 
	 * @param job
	 * @param input
	 *          The CSV file containing tweets
	 * @param output
	 *          The output file with the scores
	 * @param trendingOn
	 *          The parameter on which to score
	 * @return true if the map reduce was successful, false otherwise.
	 * @throws Exception
	 */
	public static boolean score(Job job, String input, String output, TrendingParameter trendingOn) throws Exception {

		TweetPopularityMR.trendingOn = trendingOn;
		
		job.setJarByClass(TweetPopularityMR.class);
		
		// TODO: Set up map-reduce...
		/*if(trendingOn == TrendingParameter.TWEET) {
			job.setSortComparatorClass(LongWritable.Comparator.class);
		}*/
		
		// Set key, output classes for the job (same as output classes for Reducer)
		if(trendingOn == TrendingParameter.TWEET) {
			job.setOutputKeyClass(LongWritable.class);
		} else {
			job.setOutputKeyClass(Text.class);
		}
		job.setOutputValueClass(IntWritable.class);		// edit later

		//  Set Mapper and Reducer classes for the job. (Combiner often not needed.)
		job.setMapperClass(TweetMapper.class);
		job.setReducerClass(PopularityReducer.class); 
		
		// Sets format of input files. "TextInputFormat" views files as a sequence of lines
		job.setInputFormatClass(TextInputFormat.class);

		// Sets format of output files: here, lines of text.
		job.setOutputFormatClass(TextOutputFormat.class);
		
		// End

		FileInputFormat.addInputPath(job, new Path(input));
		FileOutputFormat.setOutputPath(job, new Path(output));

		return job.waitForCompletion(true);
	}
}
