/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Licensed under the Zeebe Community License 1.0. You may not use this file
 * except in compliance with the Zeebe Community License 1.0.
 */
package io.zeebe.util.logging.stackdriver;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.impl.ThrowableProxy;
import org.apache.logging.log4j.util.ReadOnlyStringMap;

public final class StackdriverLogEntryBuilder {

  private static final String ERROR_REPORT_LOCATION_CONTEXT_KEY = "reportLocation";
  private final ServiceContext service;
  private final Map<String, Object> context;

  private SourceLocation sourceLocation;
  private Severity severity;
  private String time;
  private String message;
  private StackTraceElement traceElement;
  private String type;
  private String exception;

  @Deprecated(since = "0.24.0", forRemoval = true)
  private String logger;

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
    // returns a ISO-8061; RFC3339 is a variant of it, and thus compatible
    this.time = DateTimeFormatter.ISO_INSTANT.format(time);
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
    final var errorTraceElements = error.getStackTrace();
    if (errorTraceElements != null && errorTraceElements.length > 0) {
      context.putIfAbsent(
          ERROR_REPORT_LOCATION_CONTEXT_KEY, mapStackTraceToSourceLocation(errorTraceElements[0]));
    }

    return withType(StackdriverLogEntry.ERROR_REPORT_TYPE)
        .withException(error.getExtendedStackTraceAsString());
  }

  public StackdriverLogEntryBuilder withType(final String type) {
    this.type = type;
    return this;
  }

  public StackdriverLogEntryBuilder withException(final String exception) {
    this.exception = exception;
    return this;
  }

  @Deprecated(since = "0.24.0", forRemoval = true)
  public StackdriverLogEntryBuilder withLogger(final String logger) {
    this.logger = logger;
    return this;
  }

  public StackdriverLogEntry build() {
    final StackdriverLogEntry stackdriverLogEntry = new StackdriverLogEntry();

    if (traceElement != null) {
      sourceLocation = mapStackTraceToSourceLocation(traceElement);

      if (severity == Severity.ERROR) {
        context.putIfAbsent(ERROR_REPORT_LOCATION_CONTEXT_KEY, sourceLocation);
      }
    }

    stackdriverLogEntry.setSeverity(severity.name());
    stackdriverLogEntry.setSourceLocation(sourceLocation);
    stackdriverLogEntry.setTime(time);
    stackdriverLogEntry.setMessage(Objects.requireNonNull(message));
    stackdriverLogEntry.setService(service);
    stackdriverLogEntry.setContext(context);
    stackdriverLogEntry.setType(type);
    stackdriverLogEntry.setException(exception);
    stackdriverLogEntry.setLogger(logger);

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
