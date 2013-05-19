package bufferbench;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.junit.Test;
import redis.RedisProtocol;
import redis.netty.RedisDecoder;
import redis.netty4.RedisReplyDecoder;
import redis.netty4.Reply;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class BufferTest {

  @Test
  public void bytes() throws IOException {
    byte[] multiBulkReply = getBytes();
    long start = System.currentTimeMillis();
    for (int i = 0; i < 10; i++) {
      ByteArrayInputStream is = new ByteArrayInputStream(multiBulkReply);
      for (int j = 0; j < 100000; j++) {
        RedisProtocol.receive(is);
        is.reset();
      }
      long end = System.currentTimeMillis();
      long diff = end - start;
      System.out.println(diff + " " + ((double)diff)/100000);
      start = end;
    }
  }

  @Test
  public void channelbuffer() throws IOException {
    byte[] multiBulkReply = getBytes();
    long start = System.currentTimeMillis();
    RedisDecoder redisDecoder = new RedisDecoder();
    ChannelBuffer cb = ChannelBuffers.wrappedBuffer(multiBulkReply);
    for (int i = 0; i < 10; i++) {
      for (int j = 0; j < 100000; j++) {
        redis.netty.Reply receive = redisDecoder.receive(cb);
        cb.resetReaderIndex();
      }
      long end = System.currentTimeMillis();
      long diff = end - start;
      System.out.println(diff + " " + ((double)diff)/100000);
      start = end;
    }
  }

  @Test
  public void bytebuf() throws IOException {
    byte[] multiBulkReply = getBytes();
    long start = System.currentTimeMillis();
    RedisReplyDecoder redisDecoder = new RedisReplyDecoder();
    ByteBuf cb = Unpooled.wrappedBuffer(multiBulkReply);
    for (int i = 0; i < 10; i++) {
      for (int j = 0; j < 100000; j++) {
        Reply receive = redisDecoder.receive(cb);
        cb.resetReaderIndex();
      }
      long end = System.currentTimeMillis();
      long diff = end - start;
      System.out.println(diff + " " + ((double)diff)/100000);
      start = end;
    }
  }

  private byte[] getBytes() throws IOException {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    baos.write(redis.reply.MultiBulkReply.MARKER);
    baos.write("100\r\n".getBytes());
    for (int i = 0; i < 100; i++) {
      baos.write(redis.reply.BulkReply.MARKER);
      baos.write("6\r\n".getBytes());
      baos.write("foobar\r\n".getBytes());
    }
    return baos.toByteArray();
  }

}
