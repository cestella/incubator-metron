package org.apache.metron.pattern;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import net.byteseek.compiler.CompileException;
import net.byteseek.compiler.matcher.MatcherCompilerUtils;
import net.byteseek.compiler.matcher.SequenceMatcherCompiler;
import net.byteseek.matcher.sequence.SequenceMatcher;
import net.byteseek.searcher.Searcher;
import net.byteseek.searcher.sequence.horspool.BoyerMooreHorspoolSearcher;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public enum ByteArrayMatchingUtil {
  INSTANCE;
  private LoadingCache<String, Searcher<SequenceMatcher>> sequenceMatchers = CacheBuilder.newBuilder()
          .maximumSize(1000)
          .expireAfterWrite(10, TimeUnit.MINUTES)
          .build(
                  new CacheLoader<String, Searcher<SequenceMatcher>>() {
                    public Searcher<SequenceMatcher> load(String pattern) throws Exception {
                      return new BoyerMooreHorspoolSearcher(compile(pattern));
                    }
                  });
  private SequenceMatcherCompiler compiler = new SequenceMatcherCompiler();

  private SequenceMatcher compile(String pattern) throws CompileException {

    return compiler.compile(pattern);
  }

  public boolean match(String pattern, byte[] data) throws ExecutionException {

    Searcher<SequenceMatcher> searcher = sequenceMatchers.get(pattern);
    return !searcher.searchForwards(data).isEmpty();
  }
}
