package org.apache.metron.common.dsl.functions;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.apache.commons.lang3.tuple.Pair;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;

public class DateFunctions {
  private static LoadingCache<Pair<String, String>, SimpleDateFormat> formatCache
          = CacheBuilder.newBuilder()
                        .build(new CacheLoader<Pair<String, String>, SimpleDateFormat>() {
                          @Override
                          public SimpleDateFormat load(Pair<String, String> key) throws Exception {
                            SimpleDateFormat sdf = new SimpleDateFormat(key.getLeft());
                            if(key.getRight() != null) {
                              sdf.setTimeZone(TimeZone.getTimeZone(key.getRight()));
                            }
                            return sdf;
                          }
                        });

  public static class ToTimestamp implements Function<List<Object>, Object> {
    @Override
    public Object apply(List<Object> objects) {
      Object dateObj = objects.get(0);
      Object formatObj = objects.get(1);
      Object tzObj = null;
      if(objects.size() >= 3) {
        tzObj = objects.get(2);
      }
      if(dateObj != null && formatObj != null) {
        try {
          SimpleDateFormat sdf = formatCache.get(Pair.of(formatObj.toString(), tzObj != null?tzObj.toString():null ));
          return sdf.parse(dateObj.toString()).getTime();
        } catch (ExecutionException e) {
          return null;
        } catch (ParseException e) {
          return null;
        }
      }
      return null;
    }
  }
}
