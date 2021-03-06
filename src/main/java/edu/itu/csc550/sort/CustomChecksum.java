
package edu.itu.csc550.sort;

import java.io.IOException;
import java.util.zip.Checksum;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.PureJavaCrc32;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

public class CustomChecksum extends Configured implements Tool {
  static class ChecksumMapper 
      extends Mapper<Text, Text, NullWritable, CustomUnsigned> {
    private CustomUnsigned checksum = new CustomUnsigned();
    private CustomUnsigned sum = new CustomUnsigned();
    private Checksum crc32 = new PureJavaCrc32();

    public void map(Text key, Text value, 
                    Context context) throws IOException {
      crc32.reset();
      crc32.update(key.getBytes(), 0, key.getLength());
      crc32.update(value.getBytes(), 0, value.getLength());
      checksum.set(crc32.getValue());
      sum.add(checksum);
    }

    public void cleanup(Context context) 
        throws IOException, InterruptedException {
      context.write(NullWritable.get(), sum);
    }
  }

  static class ChecksumReducer 
      extends Reducer<NullWritable, CustomUnsigned, NullWritable, CustomUnsigned> {

    public void reduce(NullWritable key, Iterable<CustomUnsigned> values,
        Context context) throws IOException, InterruptedException  {
      CustomUnsigned sum = new CustomUnsigned();
      for (CustomUnsigned val : values) {
        sum.add(val);
      }
      context.write(key, sum);
    }
  }

  private static void usage() throws IOException {
    System.err.println("sum <out-dir> <report-dir>");
  }

  public int run(String[] args) throws Exception {
    Job job = Job.getInstance(getConf());
    if (args.length != 2) {
      usage();
      return 2;
    }
    SortInputFormat.setInputPaths(job, new Path(args[0]));
    FileOutputFormat.setOutputPath(job, new Path(args[1]));
    job.setJobName("Sum");
    job.setJarByClass(CustomChecksum.class);
    job.setMapperClass(ChecksumMapper.class);
    job.setReducerClass(ChecksumReducer.class);
    job.setOutputKeyClass(NullWritable.class);
    job.setOutputValueClass(CustomUnsigned.class);
    // force a single reducer
    job.setNumReduceTasks(1);
    job.setInputFormatClass(SortInputFormat.class);
    return job.waitForCompletion(true) ? 0 : 1;
  }

  /**
   * @param args
   */
  public static void main(String[] args) throws Exception {
    int res = ToolRunner.run(new Configuration(), new CustomChecksum(), args);
    System.exit(res);
  }

}
