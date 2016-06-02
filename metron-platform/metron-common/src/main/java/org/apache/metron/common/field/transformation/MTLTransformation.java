package org.apache.metron.common.field.transformation;

import org.apache.metron.common.dsl.MapVariableResolver;
import org.apache.metron.common.dsl.VariableResolver;
import org.apache.metron.common.transformation.TransformationProcessor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MTLTransformation implements FieldTransformation {
  @Override
  public Map<String, Object> map( Map<String, Object> input
                                , List<String> outputField
                                , Map<String, Object> fieldMappingConfig
                                , Map<String, Object> sensorConfig
                                )
  {
    Map<String, Object> ret = new HashMap<>();
    VariableResolver resolver = new MapVariableResolver(input);
    TransformationProcessor processor = new TransformationProcessor();
    for(String oField : outputField) {
      Object transformObj = fieldMappingConfig.get(oField);
      if(transformObj != null) {
        Object o = processor.parse(transformObj.toString(), resolver);
        if (o != null) {
          ret.put(oField, o);
        }
      }
    }
    return ret;
  }
}
