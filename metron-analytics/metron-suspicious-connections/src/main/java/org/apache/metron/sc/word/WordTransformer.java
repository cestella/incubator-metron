package org.apache.metron.sc.word;

import org.apache.commons.lang3.StringUtils;
import org.apache.metron.common.dsl.Context;
import org.apache.metron.common.dsl.FunctionResolverSingleton;
import org.apache.metron.common.dsl.MapVariableResolver;
import org.apache.metron.common.stellar.StellarProcessor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class WordTransformer {
  private Map<String, Object> state;
  private List<String> wordTransformers;
  public WordTransformer(Map<String, Object> state, List<String> wordTransformers ) {
    this.state = state;
    this.wordTransformers = wordTransformers;
  }

  public List<String> transform(Map<String, Object> message, Context context) {
    MapVariableResolver resolver = new MapVariableResolver(message, state);
    StellarProcessor processor = new StellarProcessor();
    List<String> ret = new ArrayList<>();
    for(String stellarTransforms : wordTransformers) {
      Object o = processor.parse(stellarTransforms, resolver, FunctionResolverSingleton.getInstance(), context);
      if(o == null) {
        continue;
      }
      if(o instanceof List) {
        for(Object listO : (List<Object>)o) {
          if(listO != null && !StringUtils.isEmpty(listO.toString()))
          ret.add(listO.toString());
        }
      }
      else if(o instanceof Map) {
        for(Map.Entry<Object, Object> kv : ((Map<Object, Object>)o).entrySet()) {
          if(kv.getValue() != null && !StringUtils.isEmpty(kv.getValue() + "")) {
            ret.add(kv.getKey() + "_" + kv.getValue());
          }
        }
      }
      else {
        if(o != null && !StringUtils.isEmpty(o.toString())) {
          ret.add(o.toString());
        }
      }
    }
    return ret;
  }

}
