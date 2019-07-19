
package edu.itu.csc550.sort;

import java.io.IOException;

import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.InvalidJobConfException;
import org.apache.hadoop.mapreduce.JobContext;
import org.apache.hadoop.mapreduce.OutputCommitter;
import org.apache.hadoop.mapreduce.RecordWriter;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.lib.output.FileOutputCommitter;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.security.TokenCache;

/**
 * An output format that writes the key and value appended together.
 */
public class SortOutputFormat extends FileOutputFormat<Text,Text> {
  static final String FINAL_SYNC_ATTRIBUTE = "mapreduce.sort.final.sync";
  private OutputCommitter committer = null;

  /**
   * Set the requirement for a final sync before the stream is closed.
   */
  static void setFinalSync(JobContext job, boolean newValue) {
    job.getConfiguration().setBoolean(FINAL_SYNC_ATTRIBUTE, newValue);
  }

  /**
   * Does the user want a final sync at close?
   */
  public static boolean getFinalSync(JobContext job) {
    return job.getConfiguration().getBoolean(FINAL_SYNC_ATTRIBUTE, false);
  }

  static class TeraRecordWriter extends RecordWriter<Text,Text> {
    private boolean finalSync = false;
    private FSDataOutputStream out;

    public TeraRecordWriter(FSDataOutputStream out,
                            JobContext job) {
      finalSync = getFinalSync(job);
      this.out = out;
    }

    public synchronized void write(Text key, 
                                   Text value) throws IOException {
      out.write(key.getBytes(), 0, key.getLength());
      out.write(value.getBytes(), 0, value.getLength());
    }
    
    public void close(TaskAttemptContext context) throws IOException {
      if (finalSync) {
        out.sync();
      }
      out.close();
    }
  }

  @Override
  public void checkOutputSpecs(JobContext job
                              ) throws InvalidJobConfException, IOException {
    // Ensure that the output directory is set
    Path outDir = getOutputPath(job);
    if (outDir == null) {
      throw new InvalidJobConfException("Output directory not set in JobConf.");
    }

    // get delegation token for outDir's file system
    TokenCache.obtainTokensForNamenodes(job.getCredentials(),
        new Path[] { outDir }, job.getConfiguration());
  }

  public RecordWriter<Text,Text> getRecordWriter(TaskAttemptContext job
                                                 ) throws IOException {
    Path file = getDefaultWorkFile(job, "");
    FileSystem fs = file.getFileSystem(job.getConfiguration());
     FSDataOutputStream fileOut = fs.create(file);
    return new TeraRecordWriter(fileOut, job);
  }
  
  public OutputCommitter getOutputCommitter(TaskAttemptContext context) 
      throws IOException {
    if (committer == null) {
      Path output = getOutputPath(context);
      committer = new FileOutputCommitter(output, context);
    }
    return committer;
  }

}
