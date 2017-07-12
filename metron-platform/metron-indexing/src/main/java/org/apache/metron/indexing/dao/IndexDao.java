package org.apache.metron.indexing.dao;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.metron.common.configuration.writer.WriterConfiguration;
import org.apache.metron.common.utils.JSONUtils;
import org.apache.metron.indexing.mutation.Mutation;
import org.apache.metron.indexing.mutation.MutationException;

import java.io.IOException;
import java.util.Optional;

public abstract class IndexDao {
  protected IndexUpdateCallback callback;

  public IndexDao() {
    callback = (x, y) -> {};
  }

  public IndexDao(IndexUpdateCallback callback) {
    this.callback = callback;
  }

  public abstract Document getLatest(String uuid, String sensorType) throws IOException;
  public abstract void update(Document update, WriterConfiguration configurations) throws IOException;

  public void update(final Document original, Mutation mutation, Optional<Long> timestamp, WriterConfiguration configurations) throws IOException, MutationException
  {
    String mutated = null;
    try {
      mutated =
      mutation.apply(() -> {
        try {
          return JSONUtils.INSTANCE.load(original.document, JsonNode.class);
        } catch (IOException e) {
          throw new IllegalStateException(e.getMessage(), e);
        }
      });
    }
    catch(Exception ex) {
      throw new MutationException(ex.getMessage(), ex);
    }
    Document updated = new Document(mutated, original.getUuid(), original.getSensorType(), timestamp.orElse(null));
    update(updated, configurations);
  }

  public void update(String uuid, String sensorType, Mutation mutation, Optional<Long> timestamp, WriterConfiguration configurations) throws IOException, MutationException
  {
    Document latest = getLatest(uuid, sensorType);
    if(latest == null) {
      throw new IllegalStateException("Unable to retrieve message with UUID: " + uuid + " please use the update() method that specifies the document.");
    }
    update(latest, mutation, timestamp, configurations);
  }

}
