/**
 * 
 */
package org.apache.hadoop.hive.ql.io.orc.extension.output;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hive.ql.io.orc.OrcMROutputFormat;
import org.apache.hadoop.hive.ql.io.orc.OrcMRWritable;
import org.apache.hadoop.hive.serde.serdeConstants;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

/**
 * 
 * @author yurun
 *
 */
public class OrcMROutputFormatDriver extends Configured implements Tool {

	public static class OrcMROutputFormatMapper extends Mapper<LongWritable, Text, Text, NullWritable> {

		@Override
		protected void map(LongWritable key, Text value, Mapper<LongWritable, Text, Text, NullWritable>.Context context)
				throws IOException, InterruptedException {
			context.write(value, NullWritable.get());
		}

	}

	public static class OrcMROutputFormatReducer extends Reducer<Text, NullWritable, NullWritable, OrcMRWritable> {

		@Override
		protected void reduce(Text key, Iterable<NullWritable> values,
				Reducer<Text, NullWritable, NullWritable, OrcMRWritable>.Context context)
				throws IOException, InterruptedException {
			String line = key.toString();

			String[] words = line.split(" ");

			OrcMRWritable mrOrcWritable = new OrcMRWritable();

			for (String word : words) {
				mrOrcWritable.add(new Text(word));
			}

			context.write(NullWritable.get(), mrOrcWritable);
		}

	}

	@Override
	public int run(String[] args) throws Exception {
		Configuration conf = getConf();

		conf.set(serdeConstants.LIST_COLUMNS, "first,second,third");
		conf.set(serdeConstants.LIST_COLUMN_TYPES, "string:string:string");

		Job job = new Job(conf);

		job.setJarByClass(OrcMROutputFormatDriver.class);

		FileInputFormat.addInputPath(job, new Path(args[0]));
		FileOutputFormat.setOutputPath(job, new Path(args[1]));

		job.setInputFormatClass(TextInputFormat.class);
		job.setOutputFormatClass(OrcMROutputFormat.class);

		job.setMapperClass(OrcMROutputFormatMapper.class);

		job.setMapOutputKeyClass(Text.class);
		job.setMapOutputValueClass(NullWritable.class);

		job.setReducerClass(OrcMROutputFormatReducer.class);

		job.setOutputKeyClass(NullWritable.class);
		job.setOutputValueClass(OrcMRWritable.class);

		return job.waitForCompletion(true) ? 0 : -1;
	}

	public static void main(String[] args) throws Exception {
		ToolRunner.run(new OrcMROutputFormatDriver(), args);
	}

}
