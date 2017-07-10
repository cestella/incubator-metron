package org.apache.metron.writer.dao;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.metron.common.utils.JSONUtils;
import org.apache.metron.writer.mutation.Mutation;
import org.apache.metron.writer.mutation.MutationException;

import java.io.IOException;
import java.util.Optional;

public abstract class IndexDao {

  public abstract Document getLatest(String uuid) throws IOException;
  public abstract void update(Document update);

  public void update(final Document original, Mutation mutation, Optional<Long> timestamp) throws IOException, MutationException
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
    Document updated = new Document();
    updated.setUuid(original.getUuid());
    updated.setTimestamp(timestamp.orElse(System.currentTimeMillis()));
    updated.setDocument(mutated);
    update(updated);
  }

  public void update(String uuid, Mutation mutation, Optional<Long> timestamp) throws IOException, MutationException
  {
    Document latest = getLatest(uuid);
    update(latest, mutation, timestamp);
  }
}
