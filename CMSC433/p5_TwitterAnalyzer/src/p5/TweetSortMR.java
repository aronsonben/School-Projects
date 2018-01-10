package cmsc433.p5;

import java.io.IOException;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.IntWritable.Comparator;
import org.apache.hadoop.mapred.KeyValueTextInputFormat;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;

/**
 * Map reduce which sorts the output of {@link TweetPopularityMR}.
 * The input will either be in the form of: </br>
 * 
 * <code></br>
 * &nbsp;(screen_name,  score)</br>
 * &nbsp;(hashtag, score)</br>
 * &nbsp;(tweet_id, score)</br></br>
 * </code>
 * 
 * The output will be in the same form, but with results sorted on the score.
 * 
 */
public class TweetSortMR {

	/**
	 * Minimum <code>int</code> value for a pair to be included in the output.
	 * Pairs with an <code>int</code> less than this value are omitted.
	 */
	private static int CUTOFF;

	public static class SwapMapper extends Mapper<Object, Text, IntWritable, Text> {

		String      id;
		int         score;
		
		private Text word = new Text();

		@Override
		public void map(Object key, Text value, Context context) throws IOException, InterruptedException {
			String[] columns = value.toString().split("\t");
			id = columns[0];
			score = Integer.valueOf(columns[1]);
			
			
			// TODO: Your code goes here
			//System.out.println(id + ": " + score);
			int negatedScore = score*(-1);
			IntWritable scoreWritable = new IntWritable(negatedScore); 
			word.set(id);
			context.write(scoreWritable, word);

		}
	}

	public static class SwapReducer extends Reducer<IntWritable, Text, Text, IntWritable> {
		
		private static IntWritable cutoffWritable = new IntWritable(CUTOFF);
		
		@Override
		public void reduce(IntWritable key, Iterable<Text> values, Context context) throws IOException, InterruptedException {

			// TODO: Negate keys back to being the values
			int score = key.get()*(-1);
			IntWritable scoreWritable = new IntWritable(score);
			
			for(Text val : values) {
				if(scoreWritable.compareTo(cutoffWritable) >= 0) {
					context.write(val, scoreWritable);
				}
			}

		}
	}

	/**
	 * This method performs value-based sorting on the given input by configuring
	 * the job as appropriate and using Hadoop.
	 * 
	 * @param job
	 *          Job created for this function
	 * @param input
	 *          String representing location of input directory
	 * @param output
	 *          String representing location of output directory
	 * @return True if successful, false otherwise
	 * @throws Exception
	 */
	public static boolean sort(Job job, String input, String output, int cutoff) throws Exception {

		CUTOFF = cutoff;
		
		job.setJarByClass(TweetSortMR.class);

		// TODO: Set up map-reduce...
		
		// Set key, output classes for the job (same as output classes for Reducer)
		job.setOutputKeyClass(IntWritable.class);
		job.setOutputValueClass(Text.class);		

		//  Set Mapper and Reducer classes for the job. (Combiner often not needed.)
		job.setMapperClass(SwapMapper.class);
		job.setReducerClass(SwapReducer.class); 
		
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
