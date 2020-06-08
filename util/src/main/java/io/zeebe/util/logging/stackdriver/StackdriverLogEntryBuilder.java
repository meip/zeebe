/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Licensed under the Zeebe Community License 1.0. You may not use this file
 * except in compliance with the Zeebe Community License 1.0.
 */
package io.zeebe.util.logging.stackdriver;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.impl.ThrowableProxy;
import org.apache.logging.log4j.core.time.Instant;
import org.apache.logging.log4j.util.ReadOnlyStringMap;

public final class StackdriverLogEntryBuilder {

  public static final String ERROR_REPORT_LOCATION_CONTEXT_KEY = "reportLocation";
  private final ServiceContext service;
  private final Map<String, Object> context;

  private SourceLocation sourceLocation;
  private Severity severity;
  private String message;
  private StackTraceElement traceElement;
  private String type;
  private String exception;
  private Instant time;

  @Deprecated(since = "0.24.0", forRemoval = true)
  private String logger;

  @Deprecated(since = "0.24.0", forRemoval = true)
  private String thread;

  StackdriverLogEntryBuilder() {
    this.service = new ServiceContext();
    this.context = new HashMap<>();
  }

  public StackdriverLogEntryBuilder withLevel(final Level level) {
    switch (level.getStandardLevel()) {
      case FATAL:
        severity = Severity.CRITICAL;
        break;
      case ERROR:
        severity = Severity.ERROR;
        break;
      case WARN:
        severity = Severity.WARNING;
        break;
      case INFO:
        severity = Severity.INFO;
        break;
      case DEBUG:
      case TRACE:
        severity = Severity.DEBUG;
        break;
      case OFF:
      case ALL:
      default:
        this.severity = Severity.DEFAULT;
        break;
    }

    return this;
  }

  public StackdriverLogEntryBuilder withSource(final StackTraceElement traceElement) {
    this.traceElement = traceElement;
    return this;
  }

  public StackdriverLogEntryBuilder withTime(final Instant time) {
    this.time = time;
    return this;
  }

  public StackdriverLogEntryBuilder withMessage(final String message) {
    this.message = message;
    return this;
  }

  public StackdriverLogEntryBuilder withServiceName(final String serviceName) {
    this.service.setService(serviceName);
    return this;
  }

  public StackdriverLogEntryBuilder withServiceVersion(final String serviceVersion) {
    this.service.setVersion(serviceVersion);
    return this;
  }

  public StackdriverLogEntryBuilder withContextEntry(final String key, final Object value) {
    this.context.put(key, value);
    return this;
  }

  public StackdriverLogEntryBuilder withDiagnosticContext(final ReadOnlyStringMap context) {
    context.forEach(this::withContextEntry);
    return this;
  }

  public StackdriverLogEntryBuilder withException(final ThrowableProxy error) {
    return withException(error.getExtendedStackTraceAsString());
  }

  public StackdriverLogEntryBuilder withType(final String type) {
    this.type = type;
    return this;
  }

  public StackdriverLogEntryBuilder withException(final String exception) {
    this.exception = exception;
    return this;
  }

  public StackdriverLogEntryBuilder withLogger(final String logger) {
    this.logger = logger;
    return withContextEntry("loggerName", logger);
  }

  public StackdriverLogEntryBuilder withThreadName(final String threadName) {
    this.thread = threadName;
    return withContextEntry("threadName", threadName);
  }

  public StackdriverLogEntryBuilder withThreadId(final long threadId) {
    return withContextEntry("threadId", threadId);
  }

  public StackdriverLogEntryBuilder withThreadPriority(final int threadPriority) {
    return withContextEntry("threadPriority", threadPriority);
  }

  public StackdriverLogEntry build() {
    final StackdriverLogEntry stackdriverLogEntry = new StackdriverLogEntry();

    if (traceElement != null) {
      sourceLocation = mapStackTraceToSourceLocation(traceElement);

      if (severity == Severity.ERROR && exception == null) {
        context.putIfAbsent(ERROR_REPORT_LOCATION_CONTEXT_KEY, sourceLocation);
      }
    }

    if (severity == Severity.ERROR && type == null) {
      type = StackdriverLogEntry.ERROR_REPORT_TYPE;
    }

    if (time != null) {
      stackdriverLogEntry.setTimestampSeconds(time.getEpochSecond());
      stackdriverLogEntry.setTimestampNanos(time.getNanoOfSecond());
    }

    stackdriverLogEntry.setSeverity(severity.name());
    stackdriverLogEntry.setSourceLocation(sourceLocation);
    stackdriverLogEntry.setMessage(Objects.requireNonNull(message));
    stackdriverLogEntry.setService(service);
    stackdriverLogEntry.setContext(context);
    stackdriverLogEntry.setType(type);
    stackdriverLogEntry.setException(exception);
    stackdriverLogEntry.setLogger(logger);
    stackdriverLogEntry.setThread(thread);

    return stackdriverLogEntry;
  }

  private SourceLocation mapStackTraceToSourceLocation(final StackTraceElement stackTrace) {
    final var location = new SourceLocation();
    location.setFile(stackTrace.getFileName());
    location.setMethodName(stackTrace.getMethodName());
    location.setLine(stackTrace.getLineNumber());

    return location;
  }
}
