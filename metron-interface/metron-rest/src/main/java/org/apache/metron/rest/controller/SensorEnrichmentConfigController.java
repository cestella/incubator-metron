/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.metron.rest.controller;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.apache.metron.common.configuration.enrichment.SensorEnrichmentConfig;
import org.apache.metron.rest.service.SensorEnrichmentConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/sensorEnrichmentConfig")
public class SensorEnrichmentConfigController {

  @Autowired
  private SensorEnrichmentConfigService sensorEnrichmentConfigService;

  @ApiOperation(value = "Updates or creates a SensorEnrichmentConfig in Zookeeper")
  @ApiResponse(message = "Returns saved SensorEnrichmentConfig", code = 200)
  @RequestMapping(value = "/{name}", method = RequestMethod.POST)
  ResponseEntity<SensorEnrichmentConfig> save(@ApiParam(name="name", value="SensorEnrichmentConfig name", required=true)@PathVariable String name,
                                              @ApiParam(name="sensorEnrichmentConfig", value="SensorEnrichmentConfig", required=true)@RequestBody SensorEnrichmentConfig sensorEnrichmentConfig) throws Exception {
    return new ResponseEntity<>(sensorEnrichmentConfigService.save(name, sensorEnrichmentConfig), HttpStatus.CREATED);
  }

  @ApiOperation(value = "Retrieves a SensorEnrichmentConfig from Zookeeper")
  @ApiResponses(value = { @ApiResponse(message = "Returns SensorEnrichmentConfig", code = 200),
          @ApiResponse(message = "SensorEnrichmentConfig is missing", code = 404) })
  @RequestMapping(value = "/{name}", method = RequestMethod.GET)
  ResponseEntity<SensorEnrichmentConfig> findOne(@ApiParam(name="name", value="SensorEnrichmentConfig name", required=true)@PathVariable String name) throws Exception {
    SensorEnrichmentConfig sensorEnrichmentConfig = sensorEnrichmentConfigService.findOne(name);
    if (sensorEnrichmentConfig != null) {
      return new ResponseEntity<>(sensorEnrichmentConfig, HttpStatus.OK);
    }

    return new ResponseEntity<>(HttpStatus.NOT_FOUND);
  }

  @ApiOperation(value = "Retrieves all SensorEnrichmentConfigs from Zookeeper")
  @ApiResponse(message = "Returns all SensorEnrichmentConfigs", code = 200)
  @RequestMapping(method = RequestMethod.GET)
  ResponseEntity<List<SensorEnrichmentConfig>> getAll() throws Exception {
    return new ResponseEntity<>(sensorEnrichmentConfigService.getAll(), HttpStatus.OK);
  }

  @ApiOperation(value = "Deletes a SensorEnrichmentConfig from Zookeeper")
  @ApiResponses(value = { @ApiResponse(message = "SensorEnrichmentConfig was deleted", code = 200),
          @ApiResponse(message = "SensorEnrichmentConfig is missing", code = 404) })
  @RequestMapping(value = "/{name}", method = RequestMethod.DELETE)
  ResponseEntity<Void> delete(@ApiParam(name="name", value="SensorEnrichmentConfig name", required=true)@PathVariable String name) throws Exception {
    if (sensorEnrichmentConfigService.delete(name)) {
      return new ResponseEntity<>(HttpStatus.OK);
    } else {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
  }

  @ApiOperation(value = "Lists the available enrichments")
  @ApiResponse(message = "Returns a list of available enrichments", code = 200)
  @RequestMapping(value = "/list/available", method = RequestMethod.GET)
  ResponseEntity<List<String>> getAvailable() throws Exception {
    return new ResponseEntity<>(sensorEnrichmentConfigService.getAvailableEnrichments(), HttpStatus.OK);
  }
}
