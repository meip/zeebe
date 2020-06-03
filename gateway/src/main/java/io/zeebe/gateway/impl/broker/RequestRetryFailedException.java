/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Licensed under the Zeebe Community License 1.0. You may not use this file
 * except in compliance with the Zeebe Community License 1.0.
 */
package io.zeebe.gateway.impl.broker;

import io.zeebe.gateway.cmd.ClientException;

class RequestRetryFailedException extends ClientException {

  RequestRetryFailedException() {
    super(
        "Expected to execute the command on one of the partitions, but all failed; no partition left to retry.");
  }
}