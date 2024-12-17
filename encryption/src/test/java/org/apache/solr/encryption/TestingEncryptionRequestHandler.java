/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.solr.encryption;

import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.response.SolrQueryResponse;

import java.io.IOException;
import java.util.Map;

import static org.apache.solr.common.params.CommonParams.DISTRIB;

/**
 * {@link EncryptionRequestHandler} for tests. Builds a mock key cookie.
 */
public class TestingEncryptionRequestHandler extends EncryptionRequestHandler {

  public static final Map<String, String> MOCK_COOKIE_PARAMS = Map.of("testParam", "testValue");

  public static volatile String mockedDistributedResponseStatus;
  public static volatile State mockedDistributedResponseState;
  public static volatile Boolean isDistributionTimeout;

  public static void clearMockedValues() {
    mockedDistributedResponseStatus = null;
    mockedDistributedResponseState = null;
    isDistributionTimeout = null;
  }

  @Override
  public void handleRequestBody(SolrQueryRequest req, SolrQueryResponse rsp) throws Exception {
    if (!req.getParams().getBool(DISTRIB, false)) {
      if (mockedDistributedResponseStatus != null || mockedDistributedResponseState != null) {
        if (mockedDistributedResponseStatus != null) {
          rsp.add(STATUS, mockedDistributedResponseStatus);
        }
        if (mockedDistributedResponseState != null) {
          rsp.add(ENCRYPTION_STATE, mockedDistributedResponseState.value);
        }
        return;
      }
    }
    super.handleRequestBody(req, rsp);
  }

  @Override
  boolean isTimeout(long maxTimeNs) {
    return isDistributionTimeout == null ? super.isTimeout(maxTimeNs) : isDistributionTimeout;
  }

  @Override
  protected Map<String, String> buildKeyCookie(String keyId,
                                               SolrQueryRequest req,
                                               SolrQueryResponse rsp)
    throws IOException {
    KeySupplier keySupplier = EncryptionDirectoryFactory.getFactory(req.getCore()).getKeySupplier();
    return keySupplier.getKeyCookie(keyId, MOCK_COOKIE_PARAMS);
  }
}
